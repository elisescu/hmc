/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

import net.java.otr4j.OtrException;
import net.java.otr4j.session.Session;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionStatus;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.util.Log;

/**
 * @author elisescu
 *
 */
public class SecureChat implements MessageListener {

    private static final String TAG = "SecureChat";
    private Chat mXMPPChat;
    HMCFingerprintsVerifier mHMCFingerprintsVerifier;
    SecuredMessageListener mSecureMessageListener;
    private String mRemoteFullJID;
    private SessionID mOtrSessionId = null;
    private String mLocalFullJID;
    private SessionStatus mOTRStatus = SessionStatus.PLAINTEXT;

    public SecureChat(ChatManager manager, String fullJid, HMCFingerprintsVerifier ver,
                            SecuredMessageListener listenter) {

        mXMPPChat = manager.createChat(fullJid, this);
        mRemoteFullJID = fullJid;
        mLocalFullJID = "elisescu_1@jabber.org";
        mHMCFingerprintsVerifier = ver;
        mSecureMessageListener = listenter;
        mOTRStatus = SessionStatus.PLAINTEXT;
        Log.d(TAG, "Created a LOCAL secure chat with " + mRemoteFullJID);
        mOtrSessionId = new SessionID(mLocalFullJID, mRemoteFullJID, "xmpp");
        HMCOTRManager.getInstance().addChat(mOtrSessionId, this);
    }

    public SecureChat(Chat chat, HMCFingerprintsVerifier ver, SecuredMessageListener listenter) {
        mXMPPChat = chat;
        chat.addMessageListener(this);
        mHMCFingerprintsVerifier = ver;
        mSecureMessageListener = listenter;
        // TODO: make sure to fix this and get the correct fullJIDs
        mRemoteFullJID = chat.getParticipant();
        mLocalFullJID = "elisescu_1@jabber.org";
        mOTRStatus = SessionStatus.PLAINTEXT;
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
            mOtrSessionId = null;
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

        if (decryptedMsg != null && mOTRStatus == SessionStatus.ENCRYPTED) {
            mSecureMessageListener.processMessage(this, decryptedMsg);
        } else {
            Log.d(TAG, "Received unecrypted or null message: " + msg.getBody());
        }

    }

    public void sendMessage(String msg) {
        String encryptedMessage = null;
        if (mOTRStatus != SessionStatus.ENCRYPTED) {
            startOtrSession();
            // wait now for the OTR negotiation to take place
            synchronized (mOtrSessionId) {
                try {
                    mOtrSessionId.wait(15000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (mOTRStatus != SessionStatus.ENCRYPTED) {
                    Log.e(TAG, "Could not start an ecrypted OTR session");
                }

            }
        }

        if (mOTRStatus == SessionStatus.ENCRYPTED)
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
        mOTRStatus = sessionStatus;
        Log.e(TAG, "Otr status changed to: " + mOTRStatus);
        if (mOTRStatus == SessionStatus.FINISHED) {
            Log.e(TAG, "For some reason, the OTR was stopped. Restarting it");

            startOtrSession();
        }

        if (mOTRStatus == SessionStatus.ENCRYPTED) {
            // let know that we negotiated the OTR session
            synchronized (mOtrSessionId) {
                mOtrSessionId.notify();
            }
            Log.e(TAG, "We need to authenticate now and verify the fingerprints");

            //String locFin = HMCOTRManager.getInstance().getOtrEngine().
                                    
            mHMCFingerprintsVerifier.verifyFingerprints("bla bla", "bla bla", mRemoteFullJID);
        }

    }
    
//    public enum SecureChatState {
//        PLAINTEXT,
//        ENCRYPTED,
//        AUTHENTICATED,
//        NEGOTIATING
//    }
//
//    SecureChatState toChatState(SessionStatus sessSt) {
//
//        if (sessSt == SessionStatus.PLAINTEXT)
//            return SecureChatState.PLAINTEXT;
//    }
}
