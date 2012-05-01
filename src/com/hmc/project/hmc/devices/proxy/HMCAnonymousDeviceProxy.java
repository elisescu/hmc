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
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
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
        retDevDesc = DeviceDescriptor.fromXMLString(devDescStr);

        if (retDevDesc != null) {
            Log.d(TAG, "Received remote descriptor:" + retDevDesc.toString());
        } else {
            Log.e(TAG, "returned null device descriptor from " + mFullJID);
        }
        return retDevDesc;
    }

    public void joinHMC(String hmcName, AsyncCommandReplyListener listener) {
        sendCommandAsync(HMCMediaDeviceItf.CMD_JOIN_HMC, hmcName, listener);
    }

    public void interconnectionRequest(String hmcName, AsyncCommandReplyListener listener) {
        sendCommandAsync(HMCServerItf.CMD_INTERCONNECTION_REQUEST, hmcName, listener);
    }
}
