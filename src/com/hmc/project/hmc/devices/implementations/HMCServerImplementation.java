/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.util.HashMap;
import android.os.RemoteException;
import android.util.Log;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.devices.proxy.AsyncCommandReplyListener;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;

public class HMCServerImplementation extends HMCDeviceImplementation implements HMCServerItf {

    private static final String TAG = "HMCServerImplementation";
    private IUserRequestsListener mUserRequestsListener;
    private IUserRequestsListener mUserRequests;

    public HMCServerImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);
    }

    public void interconnectTo(String externalHMCServerAddress) {
        // TODO Auto-generated method stub
    }

    @Override
    public void interconnectionRequest(String requesterName) {
        // TODO Auto-generated method stub

    }

    @Override
    public HMCDevicesList getListOfLocalHMCDevices() {
        HashMap<String, DeviceDescriptor> ourLocalDevices = mHMCManager
                                .getListOfLocalDevicesDescriptors();
        HMCDevicesList locDevsList = new HMCDevicesList(mHMCManager.getHMCName(), true,
                                ourLocalDevices);

        return locDevsList;
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
        String retVal = null;
        switch (opCode) {
            case HMCServerItf.CMD_GET_LIST_OF_LOCAL_HMC_DEVICES:
                retVal = _getListOfLocalHMCDevices();
                break;
            default:
                retVal = super.localExecute(opCode, params);
                break;
        }
        return retVal;
    }

    private String _getListOfLocalHMCDevices() {
        String retVal = null;
        HMCDevicesList list = getListOfLocalHMCDevices();
        retVal = list.toXMLString();
        return retVal;
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

    public boolean addNewDevice(String fullJID) {
        Log.d(TAG, "Have to add new device: !!" + fullJID);
        boolean addingSuccess = true;
        boolean userConfirmation = false;
        DeviceDescriptor remoteDevDesc = null;
        HMCAnonymousDeviceProxy newDevProxy = mHMCManager.createAnonymousProxy(fullJID);
        newDevProxy.setLocalImplementation(this);
        // send a hello message to remote anonymous device to negotiate OTR and
        // get information about device which will be approved by the user
        remoteDevDesc = newDevProxy.hello(mDeviceDescriptor);
        // check the descriptor received
        if (remoteDevDesc == null) {
            mHMCManager.deleteAnonymousProxy(fullJID);
            return false;
        }
        newDevProxy.setDeviceDescriptor(remoteDevDesc);

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

        if (!userConfirmation) {
            // the user didn't confirm the addition of the device
            mHMCManager.deleteAnonymousProxy(fullJID);
            return false;
        }

        addingSuccess = newDevProxy.joinHMC(mHMCManager.getHMCName());

        mHMCManager.promoteAnonymousProxy(newDevProxy);

        return addingSuccess;
    }

    public void addUserRequestsListener(IUserRequestsListener userRequests) {
        mUserRequests = userRequests;
    }

}
