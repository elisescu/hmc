/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;

import org.jivesoftware.smack.ChatManager;

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.implementations.HMCDevicesList;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;

public class HMCMediaDeviceProxy extends HMCDeviceProxy implements HMCMediaDeviceItf {

    private static final String TAG = "HMCMediaDeviceProxy";

    public HMCMediaDeviceProxy(ChatManager chatManager, String localFullJID, String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        super(chatManager, localFullJID, remoteFullJid, ver);
        // TODO Auto-generated constructor stub
    }

    public HMCMediaDeviceProxy(SecureChat secureChat) {
        super(secureChat);
    }

    @Override
	public void localDeviceRemovedNotification() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void localDeviceAddedNotification(DeviceDescriptor newDev) {
        sendNotification(CMD_LOCAL_DEVICE_ADDED_NOTIFICATION, newDev.toXMLString());
	}

    public void sendListOfDevices(HMCDevicesList list) {
        // Send the list of deviecs to the newly added client. Use the
        // notification RPC mechanism
        Log.d(TAG, "Send the list to remote: " + list.toXMLString());
        sendNotification(CMD_SEND_LIST_DEVICES, list.toXMLString());
    }

    /**
     * @param deviceDescriptor
     */
    public void externalDeviceAddedNotification(DeviceDescriptor deviceDescriptor, String hmcName) {
        // TODO: add also the hmcName to the parameters. For now we support only
        // one external HMC so it works for now
        sendNotification(CMD_EXTERNAL_DEVICE_ADDED_NOTIFICATION, deviceDescriptor.toXMLString());
    }

    /**
     * @param devList
     */
    public void setExternalDevicesList(HMCDevicesList devList) {
        sendNotification(CMD_SET_EXTERNAL_DEVICES_LIST_NOTIFICATION, devList.toXMLString());
    }
}
