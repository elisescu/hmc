/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;

import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;


public class HMCMediaClientDeviceProxy extends HMCMediaDeviceProxy implements HMCMediaClientDeviceItf {

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
