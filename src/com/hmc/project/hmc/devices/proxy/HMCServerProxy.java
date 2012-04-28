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
    public void interconnectionRequest(String requesterName) {
        // TODO Auto-generated method stub
    }

    @Override
    public HMCDevicesList getListOfLocalHMCDevices() {
        String returnedString;

        returnedString = sendCommandSync(CMD_GET_LIST_OF_LOCAL_HMC_DEVICES, "void-param");

        return HMCDevicesList.fromXMLString(returnedString);
	}

	@Override
	public void getListOfNewHMCDevices(String hashOfMyListOfDevices) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeHMCDevice() {
		// TODO Auto-generated method stub
		
	}

}
