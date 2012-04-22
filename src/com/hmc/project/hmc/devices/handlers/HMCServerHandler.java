/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.devices.handlers;

import android.os.RemoteException;

import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;

public class HMCServerHandler extends IHMCServerHndl.Stub {

    private HMCServerImplementation mLocalImplementation;

    public HMCServerHandler(HMCServerImplementation locImpl) {
        mLocalImplementation = locImpl;
    }

    @Override
    public void addNewDevice(String JID) throws RemoteException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerUserRequestsListener(IUserRequestsListener usrReqListener)
                            throws RemoteException {
        mLocalImplementation.registerUserRequestsListener(usrReqListener);
        
    }

    @Override
    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener)
                            throws RemoteException {
        mLocalImplementation.unregisterUserRequestsListener(userReqListener);
    }

}
