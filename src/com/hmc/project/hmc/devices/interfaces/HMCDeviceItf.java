/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.interfaces;

/**
 * HMCDevice class models the generic HMC device (either HMCServer or the rest of MediaDevices).
 */
public interface HMCDeviceItf {
	public enum TYPE {
		HMC_SERVER, HMC_MEDIA_RENDER, HMC_MEDIA_PLAYER, HMC_MEDIA_SERVER
	}

	void testMethod();
}
