/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.aidl;

import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.aidl.IHMCMediaClientHndl;
import com.hmc.project.hmc.aidl.IHMCMediaServiceHndl;

interface IHMCManager {
	void init(String deviceName, String userName);
	IHMCServerHndl implHMCServer();
	IHMCMediaClientHndl implHMCMediaClient();
	IHMCMediaServiceHndl implHMCMediaService();
}