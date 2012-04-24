/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IAddDeviceListener;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;
import com.hmc.project.hmc.utils.HMCUserNotifications;

public class HMCServerImplementation implements HMCServerItf, HMCDeviceImplementationItf {

    private static final String TAG = "HMCServerImplementation";
    private IUserRequestsListener mUserRequestsListener;
    private DeviceDescriptor mDeviceDescriptor;
    private HMCManager mHMCManager;
    private IUserRequestsListener mUserRequests;

    public HMCServerImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        mHMCManager = hmcManager;
        mDeviceDescriptor = thisDeviceDesc;
    }

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

    public void addNewDevice(String fullJID) {
        Log.d(TAG, "Have to add new device: !!" + fullJID);
        boolean userConfirmation = false;
        DeviceDescriptor remoteDevDesc = null;
        HMCAnonymousDeviceProxy newDevProxy = mHMCManager.createAnonymousProxy(fullJID);
        newDevProxy.setLocalImplementation(this);

        // send a hello message to remote anonymous device to negotiate OTR and
        // get information about device which will be approved by the user
        remoteDevDesc = newDevProxy.hello(mDeviceDescriptor);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // now the user should confirm or deny adding the device with
        // information present in remoteDevDesc data
        Log.d(TAG, "Got remote dev desc: " + remoteDevDesc.toString()
                + "\n And now ask the user for confirmatpion");
        try {
            userConfirmation = mUserRequests.confirmDeviceAddition(remoteDevDesc);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't retrieve the user confirmation");
            e.printStackTrace();
        }
        Log.d(TAG, "The user replied with :" + userConfirmation);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    public void addUserRequestsListener(IUserRequestsListener userRequests) {
        mUserRequests = userRequests;
    }

}
