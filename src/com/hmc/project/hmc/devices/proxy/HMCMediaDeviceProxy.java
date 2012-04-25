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

import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;

public class HMCMediaDeviceProxy extends HMCDeviceProxy implements HMCMediaDeviceItf {

    public HMCMediaDeviceProxy(ChatManager chatManager, String localFullJID, String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        super(chatManager, localFullJID, remoteFullJid, ver);
        // TODO Auto-generated constructor stub
    }

    public HMCMediaDeviceProxy(SecureChat secureChat) {
        super(secureChat);
    }

    @Override
	public void deviceRemovedNotification() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceAddedNotification() {
		// TODO Auto-generated method stub
		
	}
}
