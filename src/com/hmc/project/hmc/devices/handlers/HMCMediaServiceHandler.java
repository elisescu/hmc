/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.handlers;

import android.os.RemoteException;

import com.hmc.project.hmc.aidl.IHMCMediaServiceHndl;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.implementations.HMCMediaServiceDeviceImplementation;


// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaClientHandler.
 */
public class HMCMediaServiceHandler extends IHMCMediaServiceHndl.Stub {

    /** The m local implementation. */
    private HMCMediaServiceDeviceImplementation mLocalImplementation;

    /**
     * Instantiates a new hMC media client handler.
     *
     * @param localImplementation the local implementation
     */
    public HMCMediaServiceHandler(HMCMediaServiceDeviceImplementation localImplementation) {
        mLocalImplementation = localImplementation;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.hmc.project.hmc.aidl.IHMCMediaClientHndl#registerUserRequestsListener
     * (com.hmc.project.hmc.aidl.IUserRequestsListener)
     */
    @Override
    public void registerUserRequestsListener(IUserRequestsListener usrReqListener)
                            throws RemoteException {
        mLocalImplementation.registerUserRequestsListener(usrReqListener);
    }


    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCMediaClientHndl#unregisterUserRequestsListener(com.hmc.project.hmc.aidl.IUserRequestsListener)
     */
    @Override
    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener)
                            throws RemoteException {
        mLocalImplementation.unregisterUserRequestsListener(userReqListener);
    }
}
