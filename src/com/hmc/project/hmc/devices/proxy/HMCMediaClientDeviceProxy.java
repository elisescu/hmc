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


public class HMCMediaClientDeviceProxy extends HMCMediaDeviceProxy implements HMCMediaClientDeviceItf {

    /**
     * @param chatManager
     * @param jid
     * @param ver
     */
    public HMCMediaClientDeviceProxy(ChatManager chatManager, String jid,
                            HMCFingerprintsVerifier ver) {
        super(chatManager, jid, ver);
        // TODO Auto-generated constructor stub
    }

    @Override
	public void deviceRemovedNotification() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceAddedNotification() {
		// TODO Auto-generated method stub
		
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
