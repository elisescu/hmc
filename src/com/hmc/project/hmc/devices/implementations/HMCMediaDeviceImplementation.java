/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.util.Iterator;

import android.util.Log;

import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.service.DeviceAditionConfirmationListener;
import com.hmc.project.hmc.service.HMCManager;

public class HMCMediaDeviceImplementation extends HMCDeviceImplementation implements
                        HMCMediaDeviceItf {
    private static final String TAG = "HMCMediaDeviceImplementation";
    private DeviceDescriptor mPendingDevDesc = null;
    protected DeviceAditionConfirmationListener mDeviceAditionConfirmationListener;

    public HMCMediaDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);
    }

    @Override
    public void onNotificationReceived(int opCode, String params) {
        switch (opCode) {
            case HMCMediaDeviceItf.CMD_SEND_LIST_DEVICES:
                _sendListOfDevices(params);
                break;
            case HMCMediaDeviceItf.CMD_DEVICE_ADDED_NOTIFICATION:
                _localDeviceAddedNotification(params);
                break;
            default:
                break;
        }
    }

    private void _localDeviceAddedNotification(String params) {
        DeviceDescriptor newDev = DeviceDescriptor.fromXMLString(params);
        localDeviceAddedNotification(newDev);
    }

    private void _sendListOfDevices(String params) {
        HMCDevicesList devList = HMCDevicesList.fromXMLString(params);
        // update the HMCManager about the new list of devices
        mHMCManager.updateListOfLocalDevices(devList);
    }

    @Override
    public String localExecute(int opCode, String params) {
        String retVal = null;
        switch (opCode) {
            case HMCMediaDeviceItf.CMD_HELLO:
                retVal = _hello(params);
                break;
            case HMCMediaDeviceItf.CMD_JOIN_HMC:
                retVal = _joinHMC(params);
                break;
            default:
                retVal = super.localExecute(opCode, params);
                break;
        }
        return retVal;
    }

    private String _joinHMC(String params) {
        String retVal = null;

        retVal = joinHMC(params) + "";

        return retVal;
    }

    private String _hello(String params) {
        String retVal = null;
        DeviceDescriptor recvDevDesc = DeviceDescriptor.fromXMLString(params);
        DeviceDescriptor ourDevDesc = hello(recvDevDesc);

        if (ourDevDesc != null) {
            retVal = ourDevDesc.toXMLString();
        }

        return retVal;
    }

    private DeviceDescriptor hello(DeviceDescriptor recvDevDesc) {
        if (recvDevDesc != null) {
            Log.d(TAG, "Received devive descriptor from remote in hello msg: "
                                                            + recvDevDesc.toString());
            mPendingDevDesc = recvDevDesc;
        } else {
            Log.e(TAG, "Didn't recieve descriptor from remote in hello: " + recvDevDesc);
        }
        return mDeviceDescriptor;
    }

    @Override
    public void localDeviceRemovedNotification() {
        // TODO Auto-generated method stub

    }

    @Override
    public void localDeviceAddedNotification(DeviceDescriptor newDev) {
        mHMCManager.localDeviceAddedNotification(newDev);
    }

    public void registerDeviceAditionConfirmationListener(
            DeviceAditionConfirmationListener deviceAdditionListener) {
        mDeviceAditionConfirmationListener = deviceAdditionListener;
    }

    public void unregisterDeviceAditionConfirmationListener(
            DeviceAditionConfirmationListener deviceAdditionListener) {
        if (mDeviceAditionConfirmationListener == deviceAdditionListener) {
            mDeviceAditionConfirmationListener = null;
        } else {
            Log.e(TAG, "Unknown listerner for de-registration (mDeviceAditionConfirmationListener)");
        }
    }

    public boolean joinHMC(String remoteHMCName) {
        boolean retVal = false;

        Log.d(TAG, "Received call from " + remoteHMCName + "to join the HMC");
        Log.d(TAG, "Starting the user confirmation activity, using the listener:"
                                + mDeviceAditionConfirmationListener);
        if (mPendingDevDesc != null && mDeviceAditionConfirmationListener != null) {
            retVal = mDeviceAditionConfirmationListener.confirmDeviceAddition(mPendingDevDesc,
                                    remoteHMCName);
        } else {
            Log.e(TAG, "Cannot ask the user for confirmation");
            retVal = false;
        }

        Log.d(TAG, "Returned with user's response: " + retVal);
        return retVal;
    }

}
