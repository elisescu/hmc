/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;


import android.os.Parcel;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;

public class HMCDeviceProxy implements HMCDeviceItf {

	//	private Contact mXMPPContact;
	//	private Chat mXMPPChat;
	private String mName = "noname";

	public HMCDeviceProxy() {
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

}
