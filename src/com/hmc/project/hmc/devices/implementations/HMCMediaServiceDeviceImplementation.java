package com.hmc.project.hmc.devices.implementations;

import android.util.Log;

import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaServiceDeviceItf;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaClientDeviceImplementation.
 */
public class HMCMediaServiceDeviceImplementation extends HMCMediaDeviceImplementation implements
                        HMCMediaServiceDeviceItf {

    /** The Constant TAG. */
    private static final String TAG = "HMCMediaServiceDeviceImplementation";

    /**
     * Instantiates a new hMC media client device implementation.
     *
     * @param hmcManager the hmc manager
     * @param thisDeviceDesc the this device desc
     */
    public HMCMediaServiceDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
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
}
