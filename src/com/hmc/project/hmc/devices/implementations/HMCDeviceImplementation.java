/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.DeviceAditionConfirmationListener;
import com.hmc.project.hmc.devices.proxy.AsyncCommandReplyListener;
import com.hmc.project.hmc.service.HMCManager;

public class HMCDeviceImplementation implements HMCDeviceItf {
    private static final String TAG = "HMCDeviceImplementation";
    protected DeviceDescriptor mDeviceDescriptor;
    protected HMCManager mHMCManager;
    protected IUserRequestsListener mUserRequestsListener;

    public HMCDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        mHMCManager = hmcManager;
        mDeviceDescriptor = thisDeviceDesc;
    }

    // this method has to be overriden by subclasses
    public String localExecute(int opCode, String params) {
        String retVal = null;
        switch (opCode) {
            case HMCDeviceItf.CMD_REMOTE_INCREMENT:
                retVal = _remoteIncrement(params);
                break;
            case HMCDeviceItf.CMD_TEST_ASYNC_COMMAND:
                retVal = testAsyncCommand(params, null);
                break;
            default:
                retVal = "invalid-operation";
                break;
        }
        return retVal;
    }

    public String _remoteIncrement(String params) {
        String retVal = null;
        try {
        int _param=Integer.parseInt(params);
        retVal=Integer.toString(remoteIncrement(_param));
        } catch (NumberFormatException e) {
            retVal = "bad-params";
        }
        return retVal;
    }

    @Override
    public int remoteIncrement(int val) {
        return (val + 1);
    }

    // this method should be implemented by the subclasses of device
    // implementation
    public void onNotificationReceived(int opCode, String params) {
        switch (opCode) {
            case HMCDeviceItf.CMD_TEST_NOTIFICATION:
                testNotification(params);
                break;
            default:
                Log.e(TAG, "Received unknown notification:" + opCode + " with params: " + params);
                break;
        }
    }

    @Override
    public void testNotification(String notifString) {
        Log.d(TAG, "Recieved notification: " + notifString);
    }

    public void registerUserRequestsListener(IUserRequestsListener usrReqListener) {
        mUserRequestsListener = usrReqListener;
    }

    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener) {
        if (mUserRequestsListener == userReqListener) {
            mUserRequestsListener = null;
        } else {
            Log.e(TAG, "Unknown listerner for de-registration (mUserRequestsListener)");
        }
    }

    public String testAsyncCommand(String param, AsyncCommandReplyListener listener) {
        String retVal = "relpy async test";
        Log.d(TAG, "Received test async command with param: " + param);

        // simulate long processing
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Returning the value for async test command: " + retVal);

        return retVal;
    }
}
