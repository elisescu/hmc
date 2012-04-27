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
import com.hmc.project.hmc.aidl.IHMCDevicesListener;

interface IHMCManager {
	void init(String deviceName, String userName, int devType);
	IHMCServerHndl implHMCServer();
	IHMCMediaClientHndl implHMCMediaClient();
	IHMCMediaServiceHndl implHMCMediaService();
	void registerDevicesListener(IHMCDevicesListener listener);
	void unregisterDevicesListener(IHMCDevicesListener listener);
	Map getListOfLocalDevices();
	IDeviceDescriptor getLocalDevDescriptor();
	
}