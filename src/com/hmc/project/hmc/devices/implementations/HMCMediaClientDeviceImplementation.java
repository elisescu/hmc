package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.service.HMCManager;

public class HMCMediaClientDeviceImplementation extends HMCMediaDeviceImplementation implements
                        HMCMediaClientDeviceItf {

    private static final String TAG = "HMCMediaClientDeviceImplementation";
    private IUserRequestsListener mUserRequestListener;

    public HMCMediaClientDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);
    }

    @Override
    public String localExecute(int opCode, String params) {
        String retVal = null;
        switch (opCode) {
            default:
                retVal = super.localExecute(opCode, params);
                break;
        }
        return retVal;
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
