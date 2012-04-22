/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.aidl;

import com.hmc.project.hmc.aidl.IHMCDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.aidl.IHMCMediaClientHndl;
import com.hmc.project.hmc.aidl.IHMCMediaServiceHndl;

interface IHMCManager {
	IHMCDeviceDescriptor getDeviceDescriptor(String devJID);
	void init();
	IHMCServerHndl implHMCServer();
	IHMCMediaClientHndl implHMCMediaClient();
	IHMCMediaServiceHndl implHMCMediaService();
	
	int testRPC(String JID, int val);
}