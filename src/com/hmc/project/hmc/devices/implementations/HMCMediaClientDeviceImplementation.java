package com.hmc.project.hmc.devices.implementations;

import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;

public class HMCMediaClientDeviceImplementation implements HMCMediaClientDeviceItf,
        HMCDeviceImplementationItf {

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

}
