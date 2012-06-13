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
import com.hmc.project.hmc.aidl.IAsyncRPCReplyListener;
import com.hmc.project.hmc.aidl.IMediaRenderer;
import com.hmc.project.hmc.aidl.IHMCRenderingListener;

interface IHMCManager {
	void init(String deviceName, String userName, int devType, String hmcName);
	IHMCServerHndl implHMCServer();
	IHMCMediaClientHndl implHMCMediaClient();
	IHMCMediaServiceHndl implHMCMediaService();
	void registerDevicesListener(IHMCDevicesListener listener);
	void unregisterDevicesListener(IHMCDevicesListener listener);
	Map getListOfLocalDevices();
	IDeviceDescriptor getLocalDevDescriptor();
	// TODO: find something more elegant than this mechanism
	void setUserReplyDeviceAddition(boolean val); 
	void setUserReplyHMCInterconnection(boolean val);
	Map getListOfExternalDevices();
	boolean initRemoteRender_TEMP(String fullJID, IAsyncRPCReplyListener listener);
	IMediaRenderer initRemoteRender(String fullJID);
	void setLocalMediaRender(IMediaRenderer rend);
	void unsetLocalMediaRender();
	
	void setLocalRenderingListener(IHMCRenderingListener rend);
}