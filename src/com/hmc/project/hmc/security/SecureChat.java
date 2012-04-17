/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

import net.java.otr4j.OtrException;
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

    public SecureChat(ChatManager manager, String jid, HMCFingerprintsVerifier ver,
                            SecuredMessageListener listenter) {

        mXMPPChat = manager.createChat(jid, this);
        mRemoteFullJID = jid;
        mLocalFullJID = "elisescu_1@jabber.org";
        mHMCFingerprintsVerifier = ver;
        mSecureMessageListener = listenter;
        mOTRStatus = SessionStatus.PLAINTEXT;
        Log.d(TAG, "Created a LOCAL secure chat with " + mRemoteFullJID);
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
    }

    public void startOtrSession() {
        if (mOtrSessionId == null) {
            mOtrSessionId = new SessionID(mLocalFullJID, mRemoteFullJID, "xmpp");
            HMCOTRManager.getInstance().addChat(mOtrSessionId, this);
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
        if (chat != mXMPPChat) {
            Log.e(TAG, "got a strange message from an unknown chat");
            return;
        }

        if (msg.getType() == Message.Type.chat && msg.getBody() != null) {
            if (mOTRStatus != SessionStatus.ENCRYPTED) {
                Log.d(TAG, "otr status: " + mOTRStatus + "received message:" + msg.getBody());
                startOtrSession();
            } else if (mOTRStatus == SessionStatus.ENCRYPTED) {
                String decryptedMsg = null;
                try {
                    decryptedMsg = HMCOTRManager.getInstance().getOtrEngine()
                                            .transformReceiving(mOtrSessionId, msg.getBody());
                } catch (OtrException e) {
                    e.printStackTrace();
                }
                mSecureMessageListener.processMessage(this, decryptedMsg);
            }
        }
    }

    public void sendMessage(String msg) {
        if (mOTRStatus != SessionStatus.ENCRYPTED) {
            startOtrSession();
            // TODO: wait until the OTR was finished and only then send the
            // messages
        }

        if (mOTRStatus == SessionStatus.ENCRYPTED) {
            String encryptedMessage = null;
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
        Log.d(TAG, "Sent encrypted message: " + msg);
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
            Log.e(TAG, "We need to authenticate now and verify the fingerprints");

            //String locFin = HMCOTRManager.getInstance().getOtrEngine().
                                    
            mHMCFingerprintsVerifier.verifyFingerprints("bla bla", "bla bla", mRemoteFullJID);
        }

    }

}
