/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

import net.java.otr4j.OtrException;
import net.java.otr4j.io.messages.MysteriousT;
import net.java.otr4j.session.Session;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionStatus;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;

import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class SecureChat.
 *
 * @author elisescu
 */
public class SecureChat implements MessageListener {

    /** The Constant TAG. */
    private static final String TAG = "SecureChat";
    
    /** The Constant MAX_OTR_TIMEOUT. */
    private static final long MAX_OTR_TIMEOUT = 4000;
    
    /** The m xmpp chat. */
    private Chat mXMPPChat;
    
    /** The m secure message listener. */
    SecuredMessageListener mSecureMessageListener;
    
    /** The m remote full jid. */
    private String mRemoteFullJID;
    
    /** The m otr session id. */
    private SessionID mOtrSessionId = null;
    
    /** The m local full jid. */
    private String mLocalFullJID;
    
    /** The m otr status. */
    private SecureChatState mOTRStatus = SecureChatState.PLAINTEXT;
    
    /** The m presence type. */
    private Presence.Type mPresenceType;
    
    /** The m this. */
    private SecureChat mThis;
    
    /** The m decrypted message. */
    private String mDecryptedMessage;

    /**
     * The Enum SecureChatState.
     */
    enum SecureChatState {
        
        /** The PLAINTEXT. */
        PLAINTEXT, 
 /** The ENCRYPTED. */
 ENCRYPTED, 
 /** The AUTHENTICATED. */
 AUTHENTICATED, 
 /** The NEGOTIATING. */
 NEGOTIATING
    }

    /**
     * Instantiates a new secure chat.
     *
     * @param manager the manager
     * @param localFullJID the local full jid
     * @param remoteFullJid the remote full jid
     */
    public SecureChat(ChatManager manager, String localFullJID, String remoteFullJid) {

        mXMPPChat = manager.createChat(remoteFullJid, this);
        mRemoteFullJID = remoteFullJid;
        mLocalFullJID = localFullJID;
        Log.d(TAG, "Created a LOCAL secure chat with " + mRemoteFullJID);
        mOtrSessionId = new SessionID(mLocalFullJID, mRemoteFullJID, "xmpp");
        mOTRStatus = toChatState(HMCOTRManager.getInstance().getOtrEngine()
                                .getSessionStatus(mOtrSessionId));
        HMCOTRManager.getInstance().addChat(mOtrSessionId, this);
    }

    /**
     * Clean otr session.
     */
    public void cleanOTRSession() {
        if (mOTRStatus == SecureChatState.ENCRYPTED || mOTRStatus == SecureChatState.AUTHENTICATED) {
            stopOtrSession();
        }
        // HMCOTRManager.getInstance().removeChat(mOtrSessionId, this);
    }

    /**
     * Instantiates a new secure chat.
     *
     * @param chat the chat
     * @param localFullJID the local full jid
     */
    public SecureChat(Chat chat, String localFullJID) {
        mXMPPChat = chat;
        chat.addMessageListener(this);
        mRemoteFullJID = chat.getParticipant();
        mLocalFullJID = localFullJID;
        mOTRStatus = SecureChatState.PLAINTEXT;
        Log.d(TAG, "Created a non-LOCAL secure chat with " + mRemoteFullJID);
        mOtrSessionId = new SessionID(mLocalFullJID, mRemoteFullJID, "xmpp");
        HMCOTRManager.getInstance().addChat(mOtrSessionId, this);
    }

    /**
     * Start otr session.
     */
    public boolean startOtrSession() {
        if (mOtrSessionId == null) {
            Log.e(TAG, "The otr SessionID was not initialized ");
            return false;
        }

        try {
            HMCOTRManager.getInstance().getOtrEngine().startSession(mOtrSessionId);

            // wait now for the OTR negotiation to take place
            synchronized (mOtrSessionId) {
                mOtrSessionId.wait(MAX_OTR_TIMEOUT);
            }
            if (mOTRStatus == SecureChatState.PLAINTEXT) {
                Log.e(TAG, "Could not start an ecrypted OTR session");
                return false;
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        catch (OtrException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Stop otr session.
     */
    public boolean stopOtrSession() {
        if (mOtrSessionId == null) {
            Log.e(TAG, "The otr SessionID was not initialized ");
            return false;
        }

        try {
            HMCOTRManager.getInstance().getOtrEngine().endSession(mOtrSessionId);
        } catch (OtrException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Builds the sending message.
     *
     * @param body the body
     * @return the message
     */
    private Message buildSendingMessage(String body) {
        org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message();
        message.setTo(mRemoteFullJID);
        message.setBody(body);
        message.setThread(mXMPPChat.getThreadID());
        message.setType(org.jivesoftware.smack.packet.Message.Type.chat);
        return message;
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.smack.MessageListener#processMessage(org.jivesoftware.smack.Chat, org.jivesoftware.smack.packet.Message)
     */
    @Override
    public void processMessage(Chat chat, Message msg) {
        String decryptedMsg = null;
        if (chat != mXMPPChat) {
            Log.e(TAG, "got a strange message from an unknown chat");
            return;
        }

        if (mOtrSessionId != null) {
            try {
                decryptedMsg = HMCOTRManager.getInstance().getOtrEngine()
                        .transformReceiving(mOtrSessionId, msg.getBody());
            } catch (OtrException e) {
                Log.e(TAG, "Cannont initialize the OTR session");
                e.printStackTrace();
            }
        }

        if (decryptedMsg != null && mOTRStatus == SecureChatState.ENCRYPTED) {
            mThis = this;
            mDecryptedMessage = decryptedMsg;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mSecureMessageListener != null) {
                        Log.d(TAG, "Notify listener " + mSecureMessageListener.hashCode());
                        mSecureMessageListener.processMessage(mThis, mDecryptedMessage);
                    } else {
                        Log.e(TAG, "Null message listener for encrypted messages received");
                    }
                }
            }).start();
        } else if (msg.getBody().startsWith("?OTR:")) {
            // TODO: check and fix this work around
            // refresh the OTR session:
            Log.d(TAG, "Received unknown OTR message. Should we refresh the session??");
        } else {
            Log.d(TAG, "Received unecrypted or null message: " + msg.getBody());
        }

    }

    /**
     * Send message.
     *
     * @param msg the msg
     */
    public void sendMessage(String msg) {
        String encryptedMessage = null;
        if (mOTRStatus == SecureChatState.PLAINTEXT) {
            startOtrSession();
            if (mOTRStatus == SecureChatState.PLAINTEXT) {
                Log.e(TAG, "Could not start an ecrypted OTR session");
            }
        }

        if (mOTRStatus == SecureChatState.ENCRYPTED)
        {
            try {
                encryptedMessage = HMCOTRManager.getInstance().getOtrEngine()
                                        .transformSending(mOtrSessionId, msg);
            } catch (OtrException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                mXMPPChat.sendMessage(buildSendingMessage(encryptedMessage));
            } catch (XMPPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the participant.
     *
     * @return the participant
     */
    public String getParticipant() {
        return mRemoteFullJID;
    }

    /**
     * Inject message.
     *
     * @param msg the msg
     */
    public void injectMessage(String msg) {
        Log.d(TAG, "Inject OTR message: " + msg);
        try {
            mXMPPChat.sendMessage(buildSendingMessage(msg));
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Otr status changed.
     *
     * @param sessionStatus the session status
     */
    public void otrStatusChanged(SessionStatus sessionStatus) {
        mOTRStatus = toChatState(sessionStatus);
        Log.e(TAG, "Otr status changed to: " + mOTRStatus);

        if (mOTRStatus == SecureChatState.ENCRYPTED) {
            // let know that we negotiated the OTR session
            synchronized (mOtrSessionId) {
                mOtrSessionId.notify();
            }
            Log.e(TAG, "We need to authenticate now and verify the fingerprints");
        }
    }

    /**
     * Presence changed.
     *
     * @param pres the pres
     */
    public void presenceChanged(Presence pres) {
        mPresenceType = pres.getType();

        Log.d(TAG, "Received presence from " + pres.getFrom() + " : " + pres.getType());
        if (pres.getType() == Presence.Type.unavailable && mOTRStatus == SecureChatState.ENCRYPTED) {
            Log.d(TAG, "Stopping the OTR session here ");
            stopOtrSession();
        }
    }
    
    /**
     * To chat state.
     *
     * @param sessSt the sess st
     * @return the secure chat state
     */
    SecureChatState toChatState(SessionStatus sessSt) {
        SecureChatState retVal = SecureChatState.PLAINTEXT;
        switch (sessSt) {
            case PLAINTEXT:
            case FINISHED:
                retVal = SecureChatState.PLAINTEXT;
                break;
            case ENCRYPTED:
                retVal = SecureChatState.ENCRYPTED;
                break;
            default:
                break;
        }
        return retVal;
    }

    /**
     * Registers the message listener.
     * 
     * @param msgListener
     *            the msg listener
     */
    public void registerMessageListener(SecuredMessageListener msgListener) {
        mSecureMessageListener = msgListener;
    }

    /**
     * Unregisters the message listener.
     * 
     * @param msgListener
     *            the msg listener
     */
    public void unregisterMessageListener() {
        Log.d(TAG, "Removed secure messages listener (Device Proxy) " + mSecureMessageListener);
        mSecureMessageListener = null;
    }

    /**
     * Gets the session id.
     *
     * @return the session id
     */
    public SessionID getSessionID() {
        return mOtrSessionId;
    }
}
