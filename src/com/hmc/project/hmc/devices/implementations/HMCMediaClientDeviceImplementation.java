package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
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

    /**
     * @param fullJID
     */
    public String runTests(String fullJID) {
        String retVal = "";
        long startTime, duration;
        int failedNo = 0;
        
        retVal += "Time for start and stop OTR session: ";

        // test 10 start-stop OTR
        HMCAnonymousDeviceProxy anonProxy = mHMCManager.createAnonymousProxy(fullJID);
        duration = 0;
        for (int i = 0; i < 10; i++) {
            startTime = System.currentTimeMillis();
            boolean succses = anonProxy.test_StartAndStopOTR();
            if (!succses) {
                failedNo++;
            } else {
                duration += System.currentTimeMillis() - startTime;
            }
        }
        if (failedNo != 10)
            duration /= 10 - failedNo;
        retVal += duration + " miliseconds. Failed = " + failedNo;

        retVal += "\n\nTime for call an RPC sync command: ";
        // test 10 start-stop OTR
        duration = 0;
        failedNo = 0;
        for (int i = 0; i < 10; i++) {
            startTime = System.currentTimeMillis();
            String ret = anonProxy.testSyncCommand("dummy");
            boolean succes = ret.equals("sync-reply");

            if (!succes) {
                failedNo++;
            } else {
                duration += System.currentTimeMillis() - startTime;
            }
        }
        if (failedNo != 10)
            duration /= 10 - failedNo;

        retVal += duration + " miliseconds. Failed = " + failedNo;

        anonProxy.cleanOTRSession();

        mHMCManager.deleteAnonymousProxy(fullJID);

        return retVal;
    }
}
