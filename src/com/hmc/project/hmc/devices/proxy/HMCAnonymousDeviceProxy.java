/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.proxy;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;

/**
 * @author elisescu
 *
 */
public class HMCAnonymousDeviceProxy extends HMCDeviceProxy {

    public HMCAnonymousDeviceProxy(ChatManager chatManager, String localFullJID,
            String remoteFullJid, HMCFingerprintsVerifier ver) {
        super(chatManager, localFullJID, remoteFullJid, ver);
        // TODO Auto-generated constructor stub
    }

    public HMCAnonymousDeviceProxy(Chat chat, String localFullJID, HMCFingerprintsVerifier ver) {
        super(chat, localFullJID, ver);
    }

    private static final String TAG = "HMCAnonymousDeviceProxy";

    public DeviceDescriptor hello(DeviceDescriptor myDev) {
        DeviceDescriptor retDevDesc = null;
        String devDescStr;
        devDescStr = sendCommandSync(HMCMediaDeviceItf.CMD_HELLO, myDev.toXMLString());
        // Log.d(TAG, "Sending device desc:" + myDev.toXMLString());

//        devDescStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//                                + "<dd_dvel dd_dvn=\"nume primit de la remote\" dd_usn=\"user device remote\" dd_dvt=\"2\" dd_flj=\""
//                                + mFullJID
//                                + "\" dd_fgp=\"fingerprint remote\" />";
        retDevDesc = DeviceDescriptor.fromXMLString(devDescStr);
        if (retDevDesc != null) {
        Log.d(TAG, "Received remote descriptor:" + retDevDesc.toString());
        } else {
            Log.e(TAG, "returned null device descriptor from " + mFullJID);
        }
        return retDevDesc;
    }

    public boolean joinHMC(String hmcName) {
        boolean accepted = false;
        String retVal;
        retVal = "true";

        // retVal = sendCommandSync(CMD_JOIN_HMC, hmcName);

        accepted = Boolean.parseBoolean(retVal);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return accepted;
    }
}
