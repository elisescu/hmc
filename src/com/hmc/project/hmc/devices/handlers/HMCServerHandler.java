/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.devices.handlers;

import android.os.RemoteException;

import com.hmc.project.hmc.aidl.IAddDeviceListener;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCServerHandler.
 */
public class HMCServerHandler extends IHMCServerHndl.Stub {

    /** The m local implementation. */
    private HMCServerImplementation mLocalImplementation;

    /**
     * Instantiates a new hMC server handler.
     *
     * @param locImpl the loc impl
     */
    public HMCServerHandler(HMCServerImplementation locImpl) {
        mLocalImplementation = locImpl;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCServerHndl#addNewDevice(java.lang.String)
     */
    @Override
    public boolean addNewDevice(String fullJID) throws RemoteException {
        return mLocalImplementation.addNewDevice(fullJID);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCServerHndl#registerUserRequestsListener(com.hmc.project.hmc.aidl.IUserRequestsListener)
     */
    @Override
    public void registerUserRequestsListener(IUserRequestsListener usrReqListener)
                            throws RemoteException {
        mLocalImplementation.registerUserRequestsListener(usrReqListener);
        
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCServerHndl#unregisterUserRequestsListener(com.hmc.project.hmc.aidl.IUserRequestsListener)
     */
    @Override
    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener)
                            throws RemoteException {
        mLocalImplementation.unregisterUserRequestsListener(userReqListener);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCServerHndl#interconnectTo(java.lang.String)
     */
    @Override
    public boolean interconnectTo(String fullJID) throws RemoteException {
        return mLocalImplementation.interconnectTo(fullJID);
    }
}
