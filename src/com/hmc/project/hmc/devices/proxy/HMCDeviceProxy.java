/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import net.java.otr4j.session.SessionID;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.packet.Presence;

import android.media.audiofx.Equalizer;
import android.util.Log;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.implementations.HMCDeviceImplementation;
import com.hmc.project.hmc.devices.implementations.HMCMediaDeviceImplementation;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;
import com.hmc.project.hmc.security.SecuredMessageListener;
import com.hmc.project.hmc.utils.UniqueId;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCDeviceProxy.
 */
public class HMCDeviceProxy implements HMCDeviceItf, SecuredMessageListener {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCDeviceProxy";

    // hex code of the message type. Must be smaller than 0xF
    /** The Constant CODE_SYNC_COMMAND. */
    protected static final int CODE_SYNC_COMMAND = 0x1;
    
    /** The Constant CODE_SYNC_REPLY. */
    protected static final int CODE_SYNC_REPLY = 0x2;
    
    /** The Constant CODE_NOTIFICATION. */
    protected static final int CODE_NOTIFICATION = 0x3;
    
    /** The Constant CODE_ASYNC_COMMAND. */
    protected static final int CODE_ASYNC_COMMAND = 0x4;
    
    /** The Constant CODE_ASYNC_REPLY. */
    protected static final int CODE_ASYNC_REPLY = 0x5;
    
    /** The Constant CODE_REMOTE_EXCEPTION. */
    protected static final int CODE_REMOTE_EXCEPTION = 0x6; // not used yet

    // max timeout for a command reply: 15 sec?
    /** The Constant REPLY_MAX_TIME_OUT. */
    private static final long REPLY_MAX_TIME_OUT = 4000;

    // RPC header format:
    // |...type_of_message...|...operation_code...|...operation_id...|...other_data(params, reply value)...|

    /** The m secure chat. */
    protected SecureChat mSecureChat;
    
    /** The m name. */
    private String mName = "no_name";
    
    /** The m local implementation. */
    protected HMCDeviceImplementation mLocalImplementation;
    
    /** The m device descriptor. */
    protected DeviceDescriptor mDeviceDescriptor = null;
    
    /** The m full jid. */
    protected String mFullJID;
    
    /** The m sync results. */
    private SyncResults mSyncResults;
    
    /** The m async results. */
    private ASyncResults mAsyncResults;

    /**
     * Instantiates a new hMC device proxy.
     *
     * @param chatManager the chat manager
     * @param localFullJID the local full jid
     * @param remoteFullJid the remote full jid
     * @param ver the ver
     */
    public HMCDeviceProxy(ChatManager chatManager, String localFullJID, String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        mSecureChat = new SecureChat(chatManager, localFullJID, remoteFullJid);

        mSecureChat.addMessageListener(this);
        mFullJID = remoteFullJid;
        mSyncResults = new SyncResults();
        mAsyncResults = new ASyncResults();
        mDeviceDescriptor = new DeviceDescriptor();
        mDeviceDescriptor.setFullJID(remoteFullJid);
	}

    /**
     * Instantiates a new hMC device proxy.
     *
     * @param chat the chat
     * @param localFullJID the local full jid
     * @param ver the ver
     */
    public HMCDeviceProxy(Chat chat, String localFullJID, HMCFingerprintsVerifier ver) {
        mSecureChat = new SecureChat(chat, localFullJID, ver);
        mSecureChat.addMessageListener(this);
        mFullJID = chat.getParticipant();
        mSyncResults = new SyncResults();
        mAsyncResults = new ASyncResults();
        mDeviceDescriptor = new DeviceDescriptor();
        mDeviceDescriptor.setFullJID(chat.getParticipant());
    }

    /**
     * Instantiates a new hMC device proxy.
     *
     * @param secureChat the secure chat
     */
    public HMCDeviceProxy(SecureChat secureChat) {
        mSecureChat = secureChat;
        mSecureChat.addMessageListener(this);
        mFullJID = secureChat.getParticipant();
        mSyncResults = new SyncResults();
        mAsyncResults = new ASyncResults();
        mDeviceDescriptor = new DeviceDescriptor();
        mDeviceDescriptor.setFullJID(secureChat.getParticipant());
    }

    /**
     * Sets the device descriptor.
     *
     * @param desc the new device descriptor
     */
    public void setDeviceDescriptor(DeviceDescriptor desc) {
        mDeviceDescriptor = desc;
    }

    /**
     * Promote to specific proxy.
     *
     * @return the hMC device proxy
     */
    public HMCDeviceProxy promoteToSpecificProxy() {
        HMCDeviceProxy retVal = null;

        switch (mDeviceDescriptor.getDeviceType()) {
            case HMCDeviceItf.TYPE.HMC_SERVER:
                retVal = new HMCServerProxy(mSecureChat);
                retVal.setLocalImplementation(mLocalImplementation);
                retVal.setDeviceDescriptor(mDeviceDescriptor);
                Log.d(TAG, mDeviceDescriptor.getDeviceName() + " promoted as HMC Server");
                break;
            case HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE:
                retVal = new HMCMediaClientDeviceProxy(mSecureChat);
                retVal.setLocalImplementation(mLocalImplementation);
                retVal.setDeviceDescriptor(mDeviceDescriptor);
                Log.d(TAG, mDeviceDescriptor.getDeviceName() + " promoted as HMC Media Client");
                break;
            default:
                Log.e(TAG, "Promote to unknown device type proxy");
                break;
        }
        return retVal;
    }

    /**
     * Gets the device descriptor.
     *
     * @return the device descriptor
     */
    public DeviceDescriptor getDeviceDescriptor() {
        return mDeviceDescriptor;
    }

    /**
     * Send command async.
     *
     * @param opCode the op code
     * @param params the params
     * @param replyListener the reply listener
     */
    protected void sendCommandAsync(int opCode, String params,
                            AsyncCommandReplyListener replyListener) {
        String messageToBeSent = "";
        String commandId;
        String commandCode;
        CommandUniqueIdentifier commandUniqueIdentifier;

        // build the type of request
        messageToBeSent += Integer.toHexString(0x10 | CODE_ASYNC_COMMAND).substring(1)
                                .toUpperCase();

        // build the op-code to be sent
        if (opCode > 0xFFFF) {
            Log.e(TAG, "Used bad opcode in sendCommandSync");
            // TODO: improve the error mechanism.
            return;
        }
        commandCode = Integer.toHexString(0x10000 | opCode).substring(1).toUpperCase();
        messageToBeSent += commandCode;

        // build the command id.. even though this is not used in this case..
        commandId = Integer.toHexString(0x1000000 | UniqueId.getUniqueId()).substring(1)
                                .toUpperCase();
        messageToBeSent += commandId;

        // add the parameters as well
        messageToBeSent += params;

        // send the command to remote
        Log.d(TAG, "Message to be sent to remote: " + messageToBeSent);
        sendMessage(messageToBeSent);

        // optimize this. the number of pending operations can't be bigger than
        // 0xffffff so maybe don't need to put the opcode as well
        commandUniqueIdentifier = new CommandUniqueIdentifier(commandCode, commandId);
        Log.d(TAG, "(send)commandUniqueIdentifier = " + commandUniqueIdentifier);

        mAsyncResults.notifyListenerWhenResultCame(commandUniqueIdentifier, replyListener);
    }

    /**
     * Send notification.
     *
     * @param opCode the op code
     * @param params the params
     */
    protected void sendNotification(int opCode, String params) {
        String messageToBeSent = "";
        String commandId;
        String commandCode;

        // build the type of request
        messageToBeSent += Integer.toHexString(0x10 | CODE_NOTIFICATION).substring(1).toUpperCase();

        // build the op-code to be sent
        if (opCode > 0xFFFF) {
            Log.e(TAG, "Used bad opcode in sendCommandSync");
        }
        commandCode = Integer.toHexString(0x10000 | opCode).substring(1).toUpperCase();
        messageToBeSent += commandCode;

        // build the command id used to map the reply back to original command
        commandId = Integer.toHexString(0x1000000 | UniqueId.getUniqueId()).substring(1)
                                .toUpperCase();
        messageToBeSent += commandId;

        // add the parameters as well
        messageToBeSent += params;

        // send the command to remote
        Log.d(TAG, "Message to be sent to remote: " + messageToBeSent);
        sendMessage(messageToBeSent);
    }

    /**
     * Send command sync.
     *
     * @param opCode the op code
     * @param params the params
     * @return the string
     */
    protected String sendCommandSync(int opCode, String params) {
        String returnVal = "";
        String messageToBeSent = "";
        String commandId;
        String commandCode;
        CommandUniqueIdentifier commandUniqueIdentifier;

        // build the type of request
        messageToBeSent += Integer.toHexString(0x10 | CODE_SYNC_COMMAND).substring(1).toUpperCase();

        // build the op-code to be sent
        if (opCode > 0xFFFF) {
            Log.e(TAG, "Used bad opcode in sendCommandSync");
            // TODO: improve the error mechanism.
            return "bad_opcode";
        }
        commandCode = Integer.toHexString(0x10000 | opCode).substring(1).toUpperCase();
        messageToBeSent += commandCode;

        // build the command id.. even though this is not used in this case..
        commandId = Integer.toHexString(0x1000000 | UniqueId.getUniqueId()).substring(1)
                                .toUpperCase();
        messageToBeSent += commandId;

        // add the parameters as well
        messageToBeSent += params;
        
        // send the command to remote
        Log.d(TAG, "Message to be sent to remote: " + messageToBeSent);
        sendMessage(messageToBeSent);

        // optimize this. the number of pending operations can't be bigger than
        // 0xffffff so maybe don't need to put the opcode as well
        commandUniqueIdentifier = new CommandUniqueIdentifier(commandCode, commandId);
        Log.d(TAG, "(send)commandUniqueIdentifier = " + commandUniqueIdentifier);

        returnVal = mSyncResults.waitForResult(commandUniqueIdentifier);

        return returnVal;
	}

    /**
     * Send message.
     *
     * @param messageToBeSent the message to be sent
     */
    private void sendMessage(String messageToBeSent) {
        mSecureChat.sendMessage(messageToBeSent);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCDeviceItf#remoteIncrement(int)
     */
    public int remoteIncrement(int val) {
        int returnVal;
        String returnedString;

        Log.d(TAG, "Call sendCommandSync with: " + val);
        returnedString = sendCommandSync(CMD_REMOTE_INCREMENT, Integer.toString(val));
        try {
            // TODO: replace the simple conversion of params to string with
            // encoding XML
            returnVal = Integer.parseInt(returnedString);
            Log.d(TAG, "The returned value is: " + returnVal);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Unknown reply for remoteIncrement: " + returnedString);
            return val = 0xBAADF00D;
        }
        return returnVal;
    }

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.security.SecuredMessageListener#processMessage(com.hmc.project.hmc.security.SecureChat, java.lang.String)
     */
    @Override
    public void processMessage(SecureChat chat, String msg) {
        int msgCode = -1;
        int opCode = -1;
        int opId = -1;

        // check that the received message has the format of RPC
        try {
            msgCode = Integer.parseInt(msg.substring(0, 1), 16);
            opCode = Integer.parseInt(msg.substring(1, 5), 16);
            opId = Integer.parseInt(msg.substring(5, 11), 16);
            // Log.d(TAG, "RPC message received from " + chat.getParticipant() +
            // ": " + msg);
            Log.d(TAG, "RPC info: msgCode = " + msgCode + " opCode= " + opCode + " opId= " + opId);
        } catch (NumberFormatException e1) {
            Log.e(TAG, " Errorneous message received:" + msg);
            return;
        } catch (IndexOutOfBoundsException e2) {
            Log.e(TAG, " Errorneous message received:" + msg);
            return;
        }

        // decode the type of message (reply, sync command, etc)
        switch (msgCode) {
            case CODE_SYNC_COMMAND: {
                onSyncCommand(opCode, opId, msg.substring(11));
                break;
            }
            case CODE_SYNC_REPLY: {
                onSyncReplyReceived(opCode, opId, msg.substring(11));
                break;
            }
            case CODE_NOTIFICATION: {
                onNotificationReceived(opCode, opId, msg.substring(11));
                break;
            }
            case CODE_ASYNC_COMMAND: {
                onAsyncCommand(opCode, opId, msg.substring(11));
                break;
            }
            case CODE_ASYNC_REPLY: {
                onAsyncReplyReceived(opCode, opId, msg.substring(11));
                break;
            }
        default:
                Log.e(TAG, "Invalid message code: " + msgCode);
            break;
        }
    }

    /**
     * On async reply received.
     *
     * @param opCode the op code
     * @param opId the op id
     * @param reply the reply
     */
    private void onAsyncReplyReceived(int opCode, int opId, String reply) {
        Log.d(TAG, "Async Reply from remote: " + reply + " for opcode= " + opCode + " and opId= "
                                + opId);
        CommandUniqueIdentifier code;
        code = new CommandUniqueIdentifier(Integer.toHexString(0x10000 | opCode).substring(1)
                                .toUpperCase(), Integer.toHexString(0x1000000 | opId).substring(1)
                                .toUpperCase());

        mAsyncResults.notifyListenerForReply(code, reply);
    }

    /**
     * On notification received.
     *
     * @param opCode the op code
     * @param opId the op id
     * @param params the params
     */
    private void onNotificationReceived(int opCode, int opId, String params) {
        if (mLocalImplementation != null) {
            mLocalImplementation.onNotificationReceived(opCode, params, this);
        } else {
            Log.e(TAG, "Don't have local implementation to execute request!");
        }
    }

    // this method should not be needed to be overridden by subclasses
    /**
     * On sync reply received.
     *
     * @param opCode the op code
     * @param opId the op id
     * @param reply the reply
     */
    private void onSyncReplyReceived(int opCode, int opId, String reply) {
        Log.d(TAG, "Reply from remote: " + reply + " for opcode= " + opCode + " and opId= " + opId);
        CommandUniqueIdentifier code;
        code = new CommandUniqueIdentifier(Integer.toHexString(0x10000 | opCode).substring(1)
                                .toUpperCase(), Integer.toHexString(0x1000000 | opId).substring(1)
                                .toUpperCase());

        mSyncResults.notifyReply(code, reply);
    }

    // this method should not be needed to be overridden by subclasses
    /**
     * On sync command.
     *
     * @param opCode the op code
     * @param opId the op id
     * @param params the params
     */
    private void onSyncCommand(int opCode, int opId, String params) {
        String reply = executeLocalCommand(opCode, params);

        // pack back the reply and send it to remote entity
        String msgToBeSent = Integer.toHexString(0x10 | CODE_SYNC_REPLY).substring(1)
                                .toUpperCase();
        msgToBeSent += Integer.toHexString(0x10000 | opCode).substring(1)
                                .toUpperCase();
        msgToBeSent += Integer.toHexString(0x1000000 | opId).substring(1)
                                .toUpperCase();
        msgToBeSent += reply;
        Log.d(TAG, "Reply message that is sent back is: " + msgToBeSent);
        sendMessage(msgToBeSent);
    }

    // this method should not be needed to be overridden by subclasses
    /**
     * On async command.
     *
     * @param opCode the op code
     * @param opId the op id
     * @param params the params
     */
    private void onAsyncCommand(int opCode, int opId, String params) {
        String reply = executeLocalCommand(opCode, params);

        // pack back the reply and send it to remote entity
        String msgToBeSent = Integer.toHexString(0x10 | CODE_ASYNC_REPLY).substring(1)
                                .toUpperCase();
        msgToBeSent += Integer.toHexString(0x10000 | opCode).substring(1).toUpperCase();
        msgToBeSent += Integer.toHexString(0x1000000 | opId).substring(1).toUpperCase();
        msgToBeSent += reply;
        Log.d(TAG, "Reply message that is sent back is: " + msgToBeSent);
        sendMessage(msgToBeSent);
    }

    // this should NOT be overridden by subclasses. The implementation will take
    // care of executing the local method and return back the proper value
    /**
     * Execute local command.
     *
     * @param opCode the op code
     * @param params the params
     * @return the string
     */
    protected String executeLocalCommand(int opCode, String params) {
        if (mLocalImplementation != null) {
            return mLocalImplementation.onCommandReceived(opCode, params, this);
        } else {
            Log.e(TAG, "Don't have local implementation to execute request!");
            return "not-having-local-implementation";
        }
    }

    /**
     * Presence changed.
     *
     * @param pres the pres
     */
    public void presenceChanged(Presence pres) {
        mSecureChat.presenceChanged(pres);
    }

    /**
     * Sets the local implementation.
     *
     * @param locImpl the new local implementation
     */
    public void setLocalImplementation(HMCDeviceImplementation locImpl) {
        Log.d(TAG, "Setting up the implementation: " + locImpl);
        mLocalImplementation = locImpl;
    }

    /**
     * The Class CommandUniqueIdentifier.
     */
    private class CommandUniqueIdentifier {
        
        /** The m op code. */
        private String mOpCode;
        
        /** The m op id. */
        private String mOpId;

        /**
         * Instantiates a new command unique identifier.
         *
         * @param opCode the op code
         * @param opID the op id
         */
        public CommandUniqueIdentifier(String opCode, String opID) {
            mOpCode = opCode;
            mOpId = opID;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return mOpCode + mOpId;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;

            CommandUniqueIdentifier comid = (CommandUniqueIdentifier) obj;

            return this.toString().equals(comid.toString());
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    /**
     * Clean otr session.
     */
    public void cleanOTRSession() {
        mSecureChat.cleanOTRSession();
    }

    /**
     * The Class SyncResults.
     */
    private class SyncResults {
        
        /** The m replies locks. */
        private ConcurrentHashMap<CommandUniqueIdentifier, String> mRepliesLocks;
        
        /** The m replies values. */
        private ConcurrentHashMap<CommandUniqueIdentifier, String> mRepliesValues;

        /**
         * Instantiates a new sync results.
         */
        public SyncResults() {
            mRepliesLocks = new ConcurrentHashMap<CommandUniqueIdentifier, String>();
            mRepliesValues = new ConcurrentHashMap<CommandUniqueIdentifier, String>();
        }

        /**
         * Wait for result.
         *
         * @param commandUniqueIdentifier the command unique identifier
         * @return the string
         */
        public String waitForResult(CommandUniqueIdentifier commandUniqueIdentifier) {
            String returnVal = null;
            String lLock = System.currentTimeMillis() + "";
            boolean timedOut = false;

            // now wait for the remote reply.
            synchronized (mRepliesLocks) {
                mRepliesLocks.put(commandUniqueIdentifier, lLock);
                Log.d(TAG, "mRepliesLocks.size(): " + mRepliesLocks.size());
            }

            try {
                synchronized (lLock) {
                    Log.d(TAG, "Now waiting to be notified on:" + lLock);
                    long tBefore=System.currentTimeMillis();
                    lLock.wait(REPLY_MAX_TIME_OUT);
                    if ((System.currentTimeMillis() - tBefore) > REPLY_MAX_TIME_OUT) {
                        timedOut = true;
                        Log.d(TAG, "Timed out !");
                    }
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // now we got back the result, so return the result
            returnVal = mRepliesValues.get(commandUniqueIdentifier);

            if (returnVal != null) {
                mRepliesValues.remove(commandUniqueIdentifier);
                Log.d(TAG, "Returned value:" + returnVal);
            } else {
                Log.e(TAG, "Didnt'get get any reply from remote");
            }

            return returnVal;
        }

        /**
         * Notify reply.
         *
         * @param code the code
         * @param reply the reply
         */
        public void notifyReply(CommandUniqueIdentifier code, String reply) {
            mRepliesValues.put(code, reply);

            Log.d(TAG, "(reply)commandUniqueIdentifier= " + code + " (" + mRepliesLocks.size()
                    + ")");
            // notify about the reply we got here
            String lLock = null;
            synchronized (mRepliesLocks) {
                lLock = mRepliesLocks.get(code);
                Log.d(TAG, "We have waiting operations: " + mRepliesLocks.size());
            }

            if (lLock != null) {
                Log.d(TAG, "Notifying the waiting thread (" + lLock + ")");
                synchronized (lLock) {
                    lLock.notify();
                }
            } else {
                Log.e(TAG, "Unknown reply: no operation waiting for this reply");
            }

            synchronized (mRepliesLocks) {
                mRepliesLocks.remove(code);
            }
        }
    }

    /**
     * The Class ASyncResults.
     */
    private class ASyncResults {
        
        /** The m replies listeners. */
        private HashMap<CommandUniqueIdentifier, AsyncCommandReplyListener> mRepliesListeners;

        /**
         * Instantiates a new a sync results.
         */
        public ASyncResults() {
            mRepliesListeners = new HashMap<CommandUniqueIdentifier, AsyncCommandReplyListener>();
        }

        /**
         * Notify listener when result came.
         *
         * @param commandUniqueIdentifier the command unique identifier
         * @param listener the listener
         */
        public void notifyListenerWhenResultCame(CommandUniqueIdentifier commandUniqueIdentifier,
                                AsyncCommandReplyListener listener) {
            mRepliesListeners.put(commandUniqueIdentifier, listener);
        }

        /**
         * Notify listener for reply.
         *
         * @param code the code
         * @param reply the reply
         */
        public void notifyListenerForReply(CommandUniqueIdentifier code, String reply) {
            AsyncCommandReplyListener listener = mRepliesListeners.get(code);
            if (listener != null) {
                listener.onReplyReceived(reply);
                mRepliesListeners.remove(code);
            } else {
                Log.e(TAG, "Don't have a listener to wait for this reply !!");
            }
        }
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCDeviceItf#testNotification(java.lang.String)
     */
    @Override
    public void testNotification(String notifString) {
        sendNotification(CMD_TEST_NOTIFICATION, notifString);
    }

    /**
     * Test async command.
     *
     * @param param the param
     * @param listener the listener
     */
    public void testAsyncCommand(String param, AsyncCommandReplyListener listener) {
        sendCommandAsync(CMD_TEST_ASYNC_COMMAND, param, listener);
    }

    public String testSyncCommand(String param) {
        return sendCommandSync(CMD_TEST_SYNC_COMMAND, param);
    }

}
