/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;

import com.hmc.project.hmc.devices.interfaces.HMCServerItf;

public class HMCServerProxy extends HMCDeviceProxy implements HMCServerItf {
	@Override
	public void interconnectTo(String externalHMCServerAddress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void interconnectionRequest(String requesterName) {
		// TODO Auto-generated method stub
		sendCommand(2, null);
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
