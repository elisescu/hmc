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
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaDeviceProxy;
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
    public void onNotificationReceived(int opCode, String params, HMCDeviceProxy fromDev) {
        if (authenticateRemoteDevice(opCode, fromDev.getDeviceDescriptor())) {
            switch (opCode) {
                case HMCMediaDeviceItf.CMD_SEND_LIST_DEVICES:
                    _sendListOfDevices(params);
                    break;
                case HMCMediaDeviceItf.CMD_DEVICE_ADDED_NOTIFICATION:
                    _localDeviceAddedNotification(params);
                    break;
                default:
                    super.onNotificationReceived(opCode, params, fromDev);
            }
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
    public String localExecute(int opCode, String params, HMCDeviceProxy fromDev) {
        Log.d(TAG, "Local execute: " + opCode + "  " + params + "from ");

        if (authenticateRemoteDevice(opCode, fromDev.getDeviceDescriptor())) {
            switch (opCode) {
                case HMCMediaDeviceItf.CMD_HELLO:
                    return _hello(params, fromDev);
                case HMCMediaDeviceItf.CMD_JOIN_HMC:
                    return _joinHMC(params, fromDev);
            }
        }
        // if there's no operation on this class, maybe there's one on the super
        // class (the super class is responsible with authenticate the remote
        // for its operations
        return super.localExecute(opCode, params, fromDev);
    }

    @Override
    protected boolean authenticateRemoteDevice(int opCode, DeviceDescriptor fromDevDesc) {
        boolean remoteAuthenticated = false;
        // authenticate the remote device requesting calling the local methods
        switch (opCode) {
            case HMCMediaDeviceItf.CMD_HELLO:
                // we can send/receive hello messages with unknown devices
                remoteAuthenticated = true;
                break;
            case HMCMediaDeviceItf.CMD_JOIN_HMC:
                // allow unknown devices to call these two methods (used for
                // new device)
                if (mPendingDevDesc.getFullJID().equals(fromDevDesc.getFullJID()) &&
                    mPendingDevDesc.getFingerprint().equals(fromDevDesc.getFingerprint())) {
                    Log.v(TAG, "Authenticated for join HMC true");
                    remoteAuthenticated = true;
                } else {
                    remoteAuthenticated = false;
                    Log.v(TAG, "Authenticated for join HMC false");
                }
                break;
            case HMCMediaDeviceItf.CMD_SEND_LIST_DEVICES:
            default:
                remoteAuthenticated = super.authenticateRemoteDevice(opCode, fromDevDesc);
        }
        Log.d(TAG, "Remote devices authenticated = " + remoteAuthenticated);
        return remoteAuthenticated;
    }

    private String _joinHMC(String params, HMCDeviceProxy fromDev) {
        String retVal = null;

        retVal = joinHMC(params, fromDev) + "";

        return retVal;
    }

    private String _hello(String params, HMCDeviceProxy fromDev) {
        String retVal = null;
        DeviceDescriptor recvDevDesc = DeviceDescriptor.fromXMLString(params);
        DeviceDescriptor ourDevDesc = hello(recvDevDesc, fromDev);

        if (ourDevDesc != null) {
            retVal = ourDevDesc.toXMLString();
        }

        return retVal;
    }

    private DeviceDescriptor hello(DeviceDescriptor recvDevDesc, HMCDeviceProxy fromDev) {
        if (recvDevDesc != null) {
            Log.d(TAG, "Received devive descriptor from remote in hello msg: "
                                                            + recvDevDesc.toString());
            mPendingDevDesc = recvDevDesc;
            fromDev.setDeviceDescriptor(recvDevDesc);
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

    public boolean joinHMC(String remoteHMCName, HMCDeviceProxy fromDev) {
        boolean retVal = false;

        Log.d(TAG, "Received call from " + remoteHMCName + "to join the HMC");
        Log.d(TAG, "Starting the user confirmation activity, using the listener:"
                                + mDeviceAditionConfirmationListener);
        if (mPendingDevDesc != null && mDeviceAditionConfirmationListener != null) {
            retVal = mDeviceAditionConfirmationListener.confirmDeviceAddition(mPendingDevDesc,
                                    remoteHMCName, mDeviceDescriptor.getFingerprint());
            // if the user accepted addition, then add the remote device to our
            // list of devices
            if (retVal) {
                try {
                    Log.d(TAG, "The user confirmed addition so add the device to the list");
                    mHMCManager.promoteAnonymousProxyToLocal((HMCAnonymousDeviceProxy) fromDev,
                                            false);
                    Log.d(TAG, "The device was promoted");
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // this shouldn't happen
                    Log.e(TAG, "FATAL: Received joinHMC from non-anonymous device");
                }
            }
        } else {
            Log.e(TAG, "Cannot ask the user for confirmation");
            retVal = false;
        }

        Log.d(TAG, "Returned with user's response: " + retVal);
        return retVal;
    }

}
