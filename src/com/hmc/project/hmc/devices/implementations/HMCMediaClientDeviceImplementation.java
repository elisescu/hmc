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

    private String randomStr(int length) {
        String ret = "";
        for (int i = 0; i < length; i++)
            ret += "a";

        return ret;
    }
    /**
     * @param fullJID
     */
    public String runTests(String fullJID) {
        String retVal = "";
        long startTime, totalDuration, localDuration;
        int failedNo = 0;
        int testOTRNo = 50;
        int testShortRPCNo = 50;
        int testBigRPCNo = 10;
        HMCAnonymousDeviceProxy anonProxy = mHMCManager.createAnonymousProxy(fullJID);
        

        retVal += "Time for start and stop OTR session: ";
        // test multiple start-stop OTR
        totalDuration = 0;
        for (int i = 0; i < testOTRNo; i++) {
            startTime = System.currentTimeMillis();
            boolean succsess = anonProxy.test_StartAndStopOTR();
            if (!succsess) {
                failedNo++;
            } else {
                totalDuration += System.currentTimeMillis() - startTime;
            }
        }
        if (failedNo != testOTRNo)
            totalDuration /= testOTRNo - failedNo;
        retVal += totalDuration + " miliseconds. Ran: " + testOTRNo + ". Failed = " + failedNo;


        // test RPC command with short param
        retVal += "\n\nTime for call an RPC sync command: ";
        totalDuration = 0;
        failedNo = 0;
        String shortParam = "dummy";
        for (int i = 0; i < testShortRPCNo; i++) {
            startTime = System.currentTimeMillis();
            String ret = anonProxy.testSyncCommand(shortParam);
            boolean succes = shortParam.equals(ret);
            if (!succes) {
                failedNo++;
            } else {
                totalDuration += System.currentTimeMillis() - startTime;
            }
        }
        if (failedNo != testShortRPCNo)
            totalDuration /= testShortRPCNo - failedNo;
        retVal += totalDuration + " miliseconds. Ran: " + testShortRPCNo + ". Failed = " + failedNo;


        // test RPC command with short param
        String longParam = randomStr(1024);

        retVal += "\n\nTime for call an RPC sync command with long param: ";
        totalDuration = 0;
        failedNo = 0;
        if (testBigRPCNo > 10)
            testBigRPCNo = 10;

        for (int i = 1; i < testBigRPCNo; i++) {
            startTime = System.currentTimeMillis();
            String ret = anonProxy.testSyncCommand(longParam);
            localDuration = System.currentTimeMillis() - startTime;
            boolean succes = longParam.equals(ret);
            if (!succes) {
                failedNo++;
            } else {
                totalDuration += localDuration;
            }
            retVal += "\n size(param)= " + longParam.length() + ", duration: " + localDuration
                                    + " ms";
            longParam = randomStr(1024 * i);
        }
        if (failedNo != testBigRPCNo)
            totalDuration /= testBigRPCNo - failedNo;
        retVal += "\nAverage time: " + totalDuration + " miliseconds. Ran: " + testBigRPCNo
                                + ". Failed = " + failedNo;


        anonProxy.cleanOTRSession();
        mHMCManager.deleteAnonymousProxy(fullJID);

        return retVal;
    }
}
