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

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.HMCDeviceImplementationItf;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;
import com.hmc.project.hmc.security.SecuredMessageListener;
import com.hmc.project.hmc.utils.UniqueId;

public class HMCDeviceProxy implements HMCDeviceItf, SecuredMessageListener {
    private static final String TAG = "HMCDeviceProxy";

    // hex code of the message type. Must be smaller than 0xF
    protected static final int CODE_SYNC_COMMAND = 0x1;
    protected static final int CODE_REPLY = 0x2;

    // max timeout for a command reply: 15 sec?
    private static final long REPLY_MAX_TIME_OUT = 15000;

    // RPC header format:
    // |...type_of_message...|...operation_code...|...operation_id...|...other_data(params, reply value)...|

    private SecureChat mSecureChat;
    private HashMap<String, Object> mRepliesLocks;
    private HashMap<String, String> mRepliesValues;
    private String mName = "no_name";
    protected HMCDeviceImplementationItf mLocalImplementation;

    public HMCDeviceProxy(ChatManager chatManager, String localFullJID, String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        mSecureChat = new SecureChat(chatManager, localFullJID, remoteFullJid, ver, this);

        mRepliesLocks = new HashMap<String, Object>();
        mRepliesValues = new HashMap<String, String>();
	}

    public HMCDeviceProxy(Chat chat, String localFullJID, HMCFingerprintsVerifier ver) {
        mSecureChat = new SecureChat(chat, localFullJID, ver, this);

        mRepliesLocks = new HashMap<String, Object>();
        mRepliesValues = new HashMap<String, String>();
    }

    protected String sendCommandSync(int opCode, String params) {
        String returnVal = "";
        String messageToBeSent = "";
        String commandId;
        String commandCode;
        String commandUniqueIdentifier;

        // TODO: improve the way of building the request string

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

        // build the command id used to map the reply back to original command
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
        commandUniqueIdentifier = commandCode + commandId;
        Log.d(TAG, "(send)commandUniqueIdentifier = " + commandUniqueIdentifier);
        Object lLock = new Object();
        // now wait for the remote reply.
        mRepliesLocks.put(commandUniqueIdentifier, lLock);

        try {
            synchronized (lLock) {
                Log.d(TAG, "Now waiting to be notified on:" + lLock.hashCode());
                lLock.wait(REPLY_MAX_TIME_OUT);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // now we got back the result, so return the result
        returnVal = mRepliesValues.get(commandUniqueIdentifier);

        Log.d(TAG, "have been notified and also got a result back: " + returnVal);
        if (returnVal != null) {
            mRepliesValues.remove(commandUniqueIdentifier);
            Log.d(TAG, "Returned value:" + returnVal);
        } else {
            Log.e(TAG, "Didnt'get get any reply from remote");
        }

        mRepliesLocks.remove(commandUniqueIdentifier);

        return returnVal;
	}

    private void sendMessage(String messageToBeSent) {
        mSecureChat.sendMessage(messageToBeSent);
    }

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

	public String getName() {
		return mName;
	}

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
        case CODE_REPLY: {
                onReplyReceived(opCode, opId, msg.substring(11));
            break;
        }
        default:
                Log.e(TAG, "Invalid message code: " + msgCode);
            break;
        }
    }

    // this method should not be needed to be overridden by subclasses
    private void onReplyReceived(int opCode, int opId, String reply) {
        Log.d(TAG, "Reply from remote: " + reply + " for opcode= " + opCode + " and opId= " + opId);
        String code;
        code = Integer.toHexString(0x10000 | opCode).substring(1).toUpperCase()
                + Integer.toHexString(0x1000000 | opId).substring(1).toUpperCase();
        mRepliesValues.put(code, reply);

        Log.d(TAG, "(reply)commandUniqueIdentifier= " + code);
        // notify about the reply we got here
        Object lLock = mRepliesLocks.get(code);

        if (lLock != null) {
            Log.d(TAG, "Notifying the waiting thread (" + lLock.hashCode() + ")");
            synchronized (lLock) {
                lLock.notify();
            }
        } else {
            Log.e(TAG, "Unknown reply: no operation waiting for this reply");
        }
    }

    // this method should not be needed to be overridden by subclasses
    private void onSyncCommand(int opCode, int opId, String params) {
        String reply = executeLocalSyncCommand(opCode, params);

        // pack back the reply and send it to remote entity
        String msgToBeSent = Integer.toHexString(0x10 | CODE_REPLY).substring(1)
                                .toUpperCase();
        msgToBeSent += Integer.toHexString(0x10000 | opCode).substring(1)
                                .toUpperCase();
        msgToBeSent += Integer.toHexString(0x1000000 | opId).substring(1)
                                .toUpperCase();
        msgToBeSent += reply;
        Log.d(TAG, "Reply message that is sent back is: " + msgToBeSent);
        sendMessage(msgToBeSent);
    }

    // this should be overridden by subclasses and call the specific method of
    // the implementation, based on opCode value
    protected String executeLocalSyncCommand(int opCode, String params) {
        String returnVal = "";
        switch (opCode) {
            case CMD_REMOTE_INCREMENT:
                returnVal = "141186";
                break;
            default:
                break;
        }
        return returnVal;
    }

    public void presenceChanged(Presence pres) {
        mSecureChat.presenceChanged(pres);
    }

    public void setLocalImplementation(HMCDeviceImplementationItf locImpl) {
        mLocalImplementation = locImpl;
    }

}
