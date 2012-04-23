/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.proxy;

import org.jivesoftware.smack.Chat;

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;

/**
 * @author elisescu
 *
 */
public class HMCAnonymousDeviceProxy extends HMCDeviceProxy {

    private static final String TAG = "HMCAnonymousDeviceProxy";

    public HMCAnonymousDeviceProxy(Chat chat, String localFullJID, HMCFingerprintsVerifier ver) {
        super(chat, localFullJID, ver);
    }

    public DeviceDescriptor hello(DeviceDescriptor myDev) {
        DeviceDescriptor retDevDesc = null;

        return retDevDesc;
    }

    @Override
    protected String executeLocalSyncCommand(int opCode, String params) {
        if (mLocalImplementation != null) {
            return mLocalImplementation.localExecute(opCode, params);
        } else {
            Log.e(TAG, "Don't have local implementation to execute request!");
            return "not-having-local-implementation";
        }
    }

}
