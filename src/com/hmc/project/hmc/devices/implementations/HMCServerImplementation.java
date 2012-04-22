/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;

public class HMCServerImplementation implements HMCServerItf, HMCDeviceImplementationItf {

    private static final String TAG = "HMCServerImplementation";
    private IUserRequestsListener mUserRequestsListener;

    @Override
    public void interconnectTo(String externalHMCServerAddress) {
        // TODO Auto-generated method stub

    }

    @Override
    public void interconnectionRequest(String requesterName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getListOfLocalHMCDevices() {
        // TODO Auto-generated method stub

    }

    @Override
    public void getListOfNewHMCDevices(String hashOfMyListOfDevices) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHMCDevice() {
        // TODO Auto-generated method stub

    }

    @Override
    public String localExecute(int opCode, String params) {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerUserRequestsListener(IUserRequestsListener usrReqListener) {
        mUserRequestsListener = usrReqListener;
    }

    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener) {
        if (mUserRequestsListener == userReqListener) {
            mUserRequestsListener = null;
        } else {
            Log.e(TAG, "Unknown listerner for de-registration");
        }
    }

}
