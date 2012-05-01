/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;

import org.jivesoftware.smack.ChatManager;

import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;


public class HMCMediaClientDeviceProxy extends HMCMediaDeviceProxy implements HMCMediaClientDeviceItf {
    public HMCMediaClientDeviceProxy(ChatManager chatManager, String localFUllJID,
                            String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        super(chatManager, localFUllJID, remoteFullJid, ver);
    }

    public HMCMediaClientDeviceProxy(SecureChat secureChat) {
        super(secureChat);
    }

	@Override
	public void verifyFingerprints(String localFingerprint,
			String remoteFingerprint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void confirmAndVerifyFingerprints(String localFingerprint,
			String remoteFingerprint, String newDeviceName) {
		// TODO Auto-generated method stub
		
	}

}
