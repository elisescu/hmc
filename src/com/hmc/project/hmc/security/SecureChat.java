/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
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
    private String mJID;

    public SecureChat(ChatManager manager, String jid, HMCFingerprintsVerifier ver,
                            SecuredMessageListener listenter) {

        mXMPPChat = manager.createChat(jid, this);
        mJID = jid;
        mHMCFingerprintsVerifier = ver;
        mSecureMessageListener = listenter;
    }

    @Override
    public void processMessage(Chat chat, Message msg) {
        if (chat != mXMPPChat) {
            Log.e(TAG, "got a strange message from an unknown chat");
        }
        mSecureMessageListener.processMessage(this, msg.getBody());
    }

    /**
     * @return
     */
    public String getFrom() {
        return mJID;
    }

}
