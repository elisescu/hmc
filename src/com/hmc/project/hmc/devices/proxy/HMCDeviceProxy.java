/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;

import android.os.Parcel;
import android.util.Log;

import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;
import com.hmc.project.hmc.security.SecuredMessageListener;

public class HMCDeviceProxy implements HMCDeviceItf, SecuredMessageListener {

    private static final String TAG = "HMCDeviceProxy";
	private String mName = "noname";
    private SecureChat mSecureChat;

    public HMCDeviceProxy(ChatManager chatManager, String jid, HMCFingerprintsVerifier ver) {
        mSecureChat = new SecureChat(chatManager, jid, ver, this);
	}

    public HMCDeviceProxy(Chat chat, HMCFingerprintsVerifier ver) {
        mSecureChat = new SecureChat(chat, ver, this);
    }

	public void sendCommand(int operationCode, Parcel params) {
	}

	@Override
	public void testMethod() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		return mName;
	}

    @Override
    public void processMessage(SecureChat chat, String msg) {
        // TODO Auto-generated method stub

        // here we got the descrypted message from the remote device that we
        // have the proxy for
        Log.d(TAG, "Message from " + chat.getParticipant() + ": " + msg);
        chat.sendMessage("Ia de aici inapoi:" + msg);
    }

}
