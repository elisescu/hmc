package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaClientDeviceImplementation.
 */
public class HMCMediaClientDeviceImplementation extends HMCMediaDeviceImplementation implements
                        HMCMediaClientDeviceItf {

    /** The Constant TAG. */
    private static final String TAG = "HMCMediaClientDeviceImplementation";

    /**
     * Instantiates a new hMC media client device implementation.
     *
     * @param hmcManager the hmc manager
     * @param thisDeviceDesc the this device desc
     */
    public HMCMediaClientDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.implementations.HMCMediaDeviceImplementation#localExecute(int, java.lang.String, com.hmc.project.hmc.devices.proxy.HMCDeviceProxy)
     */
    @Override
    public String onCommandReceived(int opCode, String params, HMCDeviceProxy fromDev) {
        String retVal = null;
        switch (opCode) {
            default:
                retVal = super.onCommandReceived(opCode, params, fromDev);
                break;
        }
        return retVal;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf#verifyFingerprints(java.lang.String, java.lang.String)
     */
    @Override
    public void verifyFingerprints(String localFingerprint, String remoteFingerprint) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf#confirmAndVerifyFingerprints(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void confirmAndVerifyFingerprints(String localFingerprint, String remoteFingerprint,
            String newDeviceName) {
        // TODO Auto-generated method stub

    }
}
