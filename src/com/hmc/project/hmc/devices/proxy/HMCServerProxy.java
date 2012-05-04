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

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.HMCDevicesList;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;

public class HMCServerProxy extends HMCDeviceProxy implements HMCServerItf {
    private static final String TAG = "HMCServerProxy";

    public HMCServerProxy(ChatManager chatManager, String localFullJID, String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        super(chatManager, localFullJID, remoteFullJid, ver);
    }

    public HMCServerProxy(Chat chat, String localFUllJID, HMCFingerprintsVerifier ver) {
        super(chat, localFUllJID, ver);
    }

    public HMCServerProxy(SecureChat secureChat) {
        super(secureChat);
    }

	@Override
	public void getListOfNewHMCDevices(String hashOfMyListOfDevices) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeHMCDevice() {
		// TODO Auto-generated method stub
		
	}

    public void sendListOfDevices(HMCDevicesList list) {
        // Send the list of deviecs to the newly added client. Use the
        // notification RPC mechanism
        Log.d(TAG, "Send the list to remote: " + list.toXMLString());
        sendNotification(CMD_SEND_LIST_DEVICES, list.toXMLString());
    }

}
