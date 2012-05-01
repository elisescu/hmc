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
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaDeviceProxy;
import com.hmc.project.hmc.service.HMCInterconnectionConfirmationListener;
import com.hmc.project.hmc.service.HMCManager;

public class HMCServerImplementation extends HMCDeviceImplementation implements HMCServerItf {

    private static final String TAG = "HMCServerImplementation";
    private HMCInterconnectionConfirmationListener mHMCInterconnectionConfirmationListener;
    private boolean addingSuccess = true;
    private boolean interconnectionSuccess = true;

    public HMCServerImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);
    }

    public boolean interconnectTo(String externalHMCServerAddress) {

        Log.d(TAG, "Going to intercoonecto to " + externalHMCServerAddress);
        boolean userConfirmation = false;
        DeviceDescriptor remoteHMCServerDesc = null;
        String remoteHMCName = "no-name :(";

        HMCAnonymousDeviceProxy newDevProxy = mHMCManager
                                .createAnonymousProxy(externalHMCServerAddress);
        newDevProxy.setLocalImplementation(this);
        // send a hello message to remote anonymous device to negotiate OTR and
        // get information about device which will be approved by the user
        remoteHMCServerDesc = newDevProxy.hello(mDeviceDescriptor);
        // check the descriptor received
        if (remoteHMCServerDesc == null) {
            mHMCManager.deleteAnonymousProxy(externalHMCServerAddress);
            return false;
        }
        newDevProxy.setDeviceDescriptor(remoteHMCServerDesc);

        Log.d(TAG, "Got remote dev desc: " + remoteHMCServerDesc.toString()
                                + "\n Now send the joining request");
        

        // sending the join request
        newDevProxy.interconnectionRequest(mHMCManager.getHMCName(),
                                new AsyncCommandReplyListener() {
            public void onReplyReceived(String reply) {
                interconnectionSuccess = Boolean.parseBoolean(reply);
                Log.d(TAG, "Got the reply from media deviec user:" + interconnectionSuccess);
            }
        });

        // now the user should confirm or deny adding the device with
        // information present in remoteDevDesc data
        try {
            userConfirmation = mUserRequestsListener.confirmHMCInterconnection(remoteHMCServerDesc,
                                    remoteHMCName);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't retrieve the user confirmation");
            e.printStackTrace();
        }
        Log.d(TAG, "The user replied with :" + userConfirmation);

        if (!userConfirmation) {
            // the user didn't confirm the addition of the device
            mHMCManager.deleteAnonymousProxy(externalHMCServerAddress);
            return false;
        }

        // if remote device accepted to join HMC, then send it the list of
        // devices
        if (addingSuccess) {
            HMCMediaDeviceProxy specificDevPrxy = (HMCMediaDeviceProxy) mHMCManager
                                    .promoteAnonymousProxy(newDevProxy);
            // now that we have the specific proxy, added also in our list of
            // devices
            specificDevPrxy.sendListOfDevices(getListOfLocalHMCDevices());
        }

        return addingSuccess;
    }

    public boolean interconnectionRequest(String requesterName) {
        boolean retVal = true;
        Log.d(TAG, "Received interconnection request. Replying with true");

        return retVal;
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
            case HMCServerItf.CMD_INTERCONNECTION_REQUEST:
                retVal = _interconnectionRequest(params);
                break;
            default:
                retVal = super.localExecute(opCode, params);
                break;
        }
        return retVal;
    }

    private String _interconnectionRequest(String params) {
        return interconnectionRequest(params) + "";
    }

    private String _getListOfLocalHMCDevices() {
        String retVal = null;
        HMCDevicesList list = getListOfLocalHMCDevices();
        retVal = list.toXMLString();
        return retVal;
    }

    public boolean addNewDevice(String fullJID) {
        Log.d(TAG, "Have to add new device: !!" + fullJID);
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

        Log.d(TAG, "Got remote dev desc: " + remoteDevDesc.toString()
                                + "\n Now send the joining request");
        // sending the join request
        newDevProxy.joinHMC(mHMCManager.getHMCName(), new AsyncCommandReplyListener() {
            public void onReplyReceived(String reply) {
                addingSuccess = Boolean.parseBoolean(reply);
                Log.d(TAG, "Got the reply from media deviec user:" + addingSuccess);
            }
        });

        // now the user should confirm or deny adding the device with
        // information present in remoteDevDesc data
        try {
            userConfirmation = mUserRequestsListener.confirmDeviceAddition(remoteDevDesc);
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

        // if remote device accepted to join HMC, then send it the list of
        // devices
        if (addingSuccess) {
            HMCMediaDeviceProxy specificDevPrxy = (HMCMediaDeviceProxy)mHMCManager
                                    .promoteAnonymousProxy(newDevProxy);
            // now that we have the specific proxy, added also in our list of
            // devices
            specificDevPrxy.sendListOfDevices(getListOfLocalHMCDevices());
        }

        return addingSuccess;
    }


    public void registerDeviceAditionConfirmationListener(
                            HMCInterconnectionConfirmationListener listener) {
        mHMCInterconnectionConfirmationListener = listener;

    }
}
