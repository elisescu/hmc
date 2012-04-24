/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.aidl;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.aidl.IAddDeviceListener;

interface IHMCServerHndl {

    void addNewDevice(String JID);
    
    void registerUserRequestsListener(IUserRequestsListener usrReqListener);
    
    void unregisterUserRequestsListener(IUserRequestsListener userReqListener);
    
    void addUserRequestsListener(IUserRequestsListener lsner);
}