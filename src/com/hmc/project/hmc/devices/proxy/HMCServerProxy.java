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

import com.hmc.project.hmc.devices.implementations.HMCDeviceImplementationItf;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;

public class HMCServerProxy extends HMCDeviceProxy implements HMCServerItf {
    private static final String TAG = "HMCServerProxy";
    private String mLocalFullJid;
    private String mRemoteFullJid;

    public HMCServerProxy(ChatManager chatManager, String fullJid, HMCFingerprintsVerifier ver) {
        super(chatManager, fullJid, ver);
    }

    public HMCServerProxy(Chat chat, HMCFingerprintsVerifier ver) {
        super(chat, ver);
    }

    @Override
    public void interconnectTo(String externalHMCServerAddress) {
        // TODO Auto-generated method stub
    }

    @Override
    public void interconnectionRequest(String requesterName) {
        // TODO Auto-generated method stub
    }

    @Override
    public void getListOfLocalHMCDevices() {
        String returnedString;

        Log.d(TAG, "Call addMeToHMC()");
        returnedString = sendCommandSync(CMD_GET_LIST_OF_LOCAL_HMC_DEVICES, "void-param");
        Log.d(TAG, "Returned value is: " + returnedString);
		
	}

	@Override
	public void getListOfNewHMCDevices(String hashOfMyListOfDevices) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeHMCDevice() {
		// TODO Auto-generated method stub
		
	}

    @Override
    protected String executeLocalSyncCommand(int opCode, String params) {
        if (mLocalImplementation != null) {
            return mLocalImplementation.localExecute(opCode, params);
        } else {
            return "not-having-local-implementation";
        }
    }

}
