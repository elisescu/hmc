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
import com.hmc.project.hmc.security.HMCOTRManager;
import com.hmc.project.hmc.service.DeviceAditionConfirmationListener;
import com.hmc.project.hmc.service.HMCManager;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaDeviceImplementation.
 */
public class HMCMediaDeviceImplementation extends HMCDeviceImplementation implements
                        HMCMediaDeviceItf {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCMediaDeviceImplementation";
    
    /** The m pending dev desc. */
    private DeviceDescriptor mPendingDevDesc = null;
    
    /** The m device adition confirmation listener. */
    protected DeviceAditionConfirmationListener mDeviceAditionConfirmationListener;

    /**
     * Instantiates a new hMC media device implementation.
     *
     * @param hmcManager the hmc manager
     * @param thisDeviceDesc the this device desc
     */
    public HMCMediaDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.implementations.HMCDeviceImplementation#onNotificationReceived(int, java.lang.String, com.hmc.project.hmc.devices.proxy.HMCDeviceProxy)
     */
    @Override
    public void onNotificationReceived(int opCode, String params, HMCDeviceProxy fromDev) {
        if (authenticateRemoteDevice(opCode, fromDev.getDeviceDescriptor())) {
            switch (opCode) {
                case HMCMediaDeviceItf.CMD_SEND_LIST_DEVICES:
                    _sendListOfDevices(params);
                    break;
                case HMCMediaDeviceItf.CMD_LOCAL_DEVICE_ADDED_NOTIFICATION:
                    _localDeviceAddedNotification(params);
                    break;
                case HMCMediaDeviceItf.CMD_SET_EXTERNAL_DEVICES_LIST_NOTIFICATION:
                    _updateListOfExternalDevices(params);
                    break;
                default:
                    super.onNotificationReceived(opCode, params, fromDev);
            }
        }
    }

    /**
     * _update list of external devices.
     *
     * @param params the params
     */
    private void _updateListOfExternalDevices(String params) {
        mHMCManager.updateListOfExternalDevices(HMCDevicesList.fromXMLString(params), false);
    }



    /**
     * _local device added notification.
     *
     * @param params the params
     */
    private void _localDeviceAddedNotification(String params) {
        DeviceDescriptor newDev = DeviceDescriptor.fromXMLString(params);
        localDeviceAddedNotification(newDev);
    }

    /**
     * _send list of devices.
     *
     * @param params the params
     */
    private void _sendListOfDevices(String params) {
        HMCDevicesList devList = HMCDevicesList.fromXMLString(params);
        // update the HMCManager about the new list of devices
        mHMCManager.updateListOfLocalDevices(devList);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.implementations.HMCDeviceImplementation#localExecute(int, java.lang.String, com.hmc.project.hmc.devices.proxy.HMCDeviceProxy)
     */
    @Override
    public String onCommandReceived(int opCode, String params, HMCDeviceProxy fromDev) {
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
        return super.onCommandReceived(opCode, params, fromDev);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.implementations.HMCDeviceImplementation#authenticateRemoteDevice(int, com.hmc.project.hmc.devices.implementations.DeviceDescriptor)
     */
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

    /**
     * _join hmc.
     *
     * @param params the params
     * @param fromDev the from dev
     * @return the string
     */
    private String _joinHMC(String params, HMCDeviceProxy fromDev) {
        String retVal = null;

        retVal = joinHMC(params, fromDev) + "";

        return retVal;
    }

    /**
     * _hello.
     *
     * @param params the params
     * @param fromDev the from dev
     * @return the string
     */
    private String _hello(String params, HMCDeviceProxy fromDev) {
        String retVal = null;
        DeviceDescriptor recvDevDesc = DeviceDescriptor.fromXMLString(params);
        DeviceDescriptor ourDevDesc = hello(recvDevDesc, fromDev);

        if (ourDevDesc != null) {
            retVal = ourDevDesc.toXMLString();
        }

        return retVal;
    }

    /**
     * Hello.
     *
     * @param recvDevDesc the recv dev desc
     * @param fromDev the from dev
     * @return the device descriptor
     */
    private DeviceDescriptor hello(DeviceDescriptor recvDevDesc, HMCDeviceProxy fromDev) {
        if (recvDevDesc != null) {
            Log.d(TAG, "Received devive descriptor from remote in hello msg: "
                                                            + recvDevDesc.toString());
            mPendingDevDesc = recvDevDesc;
            fromDev.setDeviceDescriptor(recvDevDesc);
        } else {
            Log.e(TAG, "Didn't recieve descriptor from remote in hello: " + recvDevDesc);
        }
        mDeviceDescriptor.setFingerprint(HMCOTRManager.getInstance().getLocalFingerprint(
                mDeviceDescriptor.getFullJID()));
        return mDeviceDescriptor;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf#localDeviceRemovedNotification()
     */
    @Override
    public void localDeviceRemovedNotification() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf#localDeviceAddedNotification(com.hmc.project.hmc.devices.implementations.DeviceDescriptor)
     */
    @Override
    public void localDeviceAddedNotification(DeviceDescriptor newDev) {
        mHMCManager.localDeviceAddedNotification(newDev);
    }

    /**
     * Register device adition confirmation listener.
     *
     * @param deviceAdditionListener the device addition listener
     */
    public void registerDeviceAditionConfirmationListener(
            DeviceAditionConfirmationListener deviceAdditionListener) {
        mDeviceAditionConfirmationListener = deviceAdditionListener;
    }

    /**
     * Unregister device adition confirmation listener.
     *
     * @param deviceAdditionListener the device addition listener
     */
    public void unregisterDeviceAditionConfirmationListener(
            DeviceAditionConfirmationListener deviceAdditionListener) {
        if (mDeviceAditionConfirmationListener == deviceAdditionListener) {
            mDeviceAditionConfirmationListener = null;
        } else {
            Log.e(TAG, "Unknown listerner for de-registration (mDeviceAditionConfirmationListener)");
        }
    }

    /**
     * Join hmc.
     *
     * @param remoteHMCName the remote hmc name
     * @param fromDev the from dev
     * @return true, if successful
     */
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
