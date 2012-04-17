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

import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;

public class HMCServerProxy extends HMCDeviceProxy implements HMCServerItf {
    /**
     * @param chatManager
     * @param jid
     * @param ver
     */
    public HMCServerProxy(ChatManager chatManager, String jid, HMCFingerprintsVerifier ver) {
        super(chatManager, jid, ver);
        // TODO Auto-generated constructor stub
    }

    @Override
	public void interconnectTo(String externalHMCServerAddress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void interconnectionRequest(String requesterName) {
		// TODO Auto-generated method stub
		sendCommandSync(2, null);
	}

	@Override
	public void addMeToHMC() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getListOfHMCDevices() {
		// TODO Auto-generated method stub
		
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
