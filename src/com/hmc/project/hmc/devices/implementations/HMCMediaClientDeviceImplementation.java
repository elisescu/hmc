package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;

public class HMCMediaClientDeviceImplementation implements HMCMediaClientDeviceItf,
        HMCDeviceImplementationItf {

    private static final String TAG = "HMCMediaClientDeviceImplementation";
    private IUserRequestsListener mUserRequestListener;

    @Override
    public String localExecute(int opCode, String params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verifyFingerprints(String localFingerprint, String remoteFingerprint) {
        // TODO Auto-generated method stub

    }

    @Override
    public void confirmAndVerifyFingerprints(String localFingerprint, String remoteFingerprint,
            String newDeviceName) {
        // TODO Auto-generated method stub

    }

    /**
     * @param usrReqListener
     */
    public void registerUserRequestsListener(IUserRequestsListener usrReqListener) {
        mUserRequestListener = usrReqListener;
    }

    /**
     * @param userReqListener
     */
    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener) {
        if (mUserRequestListener == userReqListener) {
            mUserRequestListener = null;
        } else {
            Log.e(TAG, "Unknown listerner for de-registration");
        }
    }

}
