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

/**
 * @author elisescu
 *
 */
public class SecureChat implements MessageListener {

    private static final String TAG = "SecureChat";
    private static final long MAX_OTR_TIMEOUT = 15000;
    private Chat mXMPPChat;
    HMCFingerprintsVerifier mHMCFingerprintsVerifier;
    SecuredMessageListener mSecureMessageListener;
    private String mRemoteFullJID;
    private SessionID mOtrSessionId = null;
    private String mLocalFullJID;
    private SecureChatState mOTRStatus = SecureChatState.PLAINTEXT;
    private Presence.Type mPresenceType;

    enum SecureChatState {
        PLAINTEXT, ENCRYPTED, AUTHENTICATED, NEGOTIATING
    }

    public SecureChat(ChatManager manager, String localFullJID, String remoteFullJid,
            HMCFingerprintsVerifier ver) {

        mXMPPChat = manager.createChat(remoteFullJid, this);
        mRemoteFullJID = remoteFullJid;
        mLocalFullJID = localFullJID;
        mHMCFingerprintsVerifier = ver;
        Log.d(TAG, "Created a LOCAL secure chat with " + mRemoteFullJID);
        mOtrSessionId = new SessionID(mLocalFullJID, mRemoteFullJID, "xmpp");
        mOTRStatus = toChatState(HMCOTRManager.getInstance().getOtrEngine()
                                .getSessionStatus(mOtrSessionId));
        HMCOTRManager.getInstance().addChat(mOtrSessionId, this);
    }

    public void cleanOTRSession() {
        if (mOTRStatus == SecureChatState.ENCRYPTED || mOTRStatus == SecureChatState.AUTHENTICATED) {
            stopOtrSession();
        }
        HMCOTRManager.getInstance().removeChat(mOtrSessionId, this);
    }

    public SecureChat(Chat chat, String localFullJID, HMCFingerprintsVerifier ver) {
        mXMPPChat = chat;
        chat.addMessageListener(this);
        mHMCFingerprintsVerifier = ver;
        // TODO: make sure to fix this and get the correct fullJIDs
        mRemoteFullJID = chat.getParticipant();
        mLocalFullJID = localFullJID;
        mOTRStatus = SecureChatState.PLAINTEXT;
        Log.d(TAG, "Created a non-LOCAL secure chat with " + mRemoteFullJID);
        mOtrSessionId = new SessionID(mLocalFullJID, mRemoteFullJID, "xmpp");
        HMCOTRManager.getInstance().addChat(mOtrSessionId, this);
    }

    public void startOtrSession() {
        if (mOtrSessionId == null) {
            Log.e(TAG, "The otr SessionID was not initialized ");
            return;
        }

        try {
            HMCOTRManager.getInstance().getOtrEngine().startSession(mOtrSessionId);
            } catch (OtrException e) {
            e.printStackTrace();
        }
    }

    public void stopOtrSession() {
        if (mOtrSessionId == null) {
            Log.e(TAG, "The otr SessionID was not initialized ");
            return;
        }

        try {
            HMCOTRManager.getInstance().getOtrEngine().endSession(mOtrSessionId);
        } catch (OtrException e) {
            e.printStackTrace();
        }
    }

    private Message buildSendingMessage(String body) {
        org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message();
        message.setTo(mRemoteFullJID);
        message.setBody(body);
        message.setThread(mXMPPChat.getThreadID());
        message.setType(org.jivesoftware.smack.packet.Message.Type.chat);
        return message;
    }

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
            mSecureMessageListener.processMessage(this, decryptedMsg);
        } else if (msg.getBody().startsWith("?OTR:")) {
            // TODO: check and fix this work around
            // refresh the OTR session:
            Log.d(TAG, "Received unknown OTR message. Should we refresh the session??");
        } else {
            Log.d(TAG, "Received unecrypted or null message: " + msg.getBody());
        }

    }

    public void sendMessage(String msg) {
        String encryptedMessage = null;
        if (mOTRStatus == SecureChatState.PLAINTEXT) {
            startOtrSession();
            // wait now for the OTR negotiation to take place
            synchronized (mOtrSessionId) {
                try {
                    mOtrSessionId.wait(MAX_OTR_TIMEOUT);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (mOTRStatus == SecureChatState.PLAINTEXT) {
                    Log.e(TAG, "Could not start an ecrypted OTR session");
                }

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

    public String getParticipant() {
        return mRemoteFullJID;
    }

    public void injectMessage(String msg) {
        Log.d(TAG, "Inject OTR message: " + msg);
        try {
            mXMPPChat.sendMessage(buildSendingMessage(msg));
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void otrStatusChanged(SessionStatus sessionStatus) {
        mOTRStatus = toChatState(sessionStatus);
        Log.e(TAG, "Otr status changed to: " + mOTRStatus);

        if (mOTRStatus == SecureChatState.ENCRYPTED) {
            // let know that we negotiated the OTR session
            synchronized (mOtrSessionId) {
                mOtrSessionId.notify();
            }
            Log.e(TAG, "We need to authenticate now and verify the fingerprints");
                                    
            mHMCFingerprintsVerifier.verifyFingerprints("bla bla", "bla bla", mRemoteFullJID);
        }
    }

    public void presenceChanged(Presence pres) {
        mPresenceType = pres.getType();

        Log.d(TAG, "Received presence from " + pres.getFrom() + " : " + pres.getType());
        if (pres.getType() == Presence.Type.unavailable && mOTRStatus == SecureChatState.ENCRYPTED) {
            Log.d(TAG, "Stopping the OTR session here ");
            stopOtrSession();
        }
    }
    
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

    public void addMessageListener(SecuredMessageListener msgListener) {
        mSecureMessageListener = msgListener;
    }

    public SessionID getSessionID() {
        return mOtrSessionId;
    }
}
