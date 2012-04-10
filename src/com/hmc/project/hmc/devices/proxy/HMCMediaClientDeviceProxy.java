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
import com.hmc.project.hmc.security.SecureChat;


// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaClientDeviceProxy.
 */
public class HMCMediaClientDeviceProxy extends HMCMediaDeviceProxy implements HMCMediaClientDeviceItf {

    /**
     * Instantiates a new hMC media client device proxy.
     *
     * @param chatManager the chat manager
     * @param localFUllJID the local f ull jid
     * @param remoteFullJid the remote full jid
     */
    public HMCMediaClientDeviceProxy(ChatManager chatManager, String localFUllJID,
                            String remoteFullJid) {
        super(chatManager, localFUllJID, remoteFullJid);
    }

    /**
     * Instantiates a new hMC media client device proxy.
     *
     * @param secureChat the secure chat
     */
    public HMCMediaClientDeviceProxy(SecureChat secureChat) {
        super(secureChat);
    }

	/* (non-Javadoc)
	 * @see com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf#verifyFingerprints(java.lang.String, java.lang.String)
	 */
	@Override
	public void verifyFingerprints(String localFingerprint,
			String remoteFingerprint) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf#confirmAndVerifyFingerprints(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void confirmAndVerifyFingerprints(String localFingerprint,
			String remoteFingerprint, String newDeviceName) {
		// TODO Auto-generated method stub

	}

}
