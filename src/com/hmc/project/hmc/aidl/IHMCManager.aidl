/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.aidl;

import com.hmc.project.hmc.aidl.IHMCDeviceDescriptor;

interface IHMCManager {
	IHMCDeviceDescriptor getDeviceDescriptor(String devJID);
	void init(int localImplem);
	
	int testRPC(String JID, int val);
}