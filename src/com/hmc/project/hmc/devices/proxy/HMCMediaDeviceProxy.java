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
import com.hmc.project.hmc.security.SecureChat;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaDeviceProxy.
 */
public class HMCMediaDeviceProxy extends HMCDeviceProxy implements HMCMediaDeviceItf {

    /** The Constant TAG. */
    private static final String TAG = "HMCMediaDeviceProxy";

    /**
     * Instantiates a new hMC media device proxy.
     *
     * @param chatManager the chat manager
     * @param localFullJID the local full jid
     * @param remoteFullJid the remote full jid
     */
    public HMCMediaDeviceProxy(ChatManager chatManager, String localFullJID, String remoteFullJid) {
        super(chatManager, localFullJID, remoteFullJid);
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new hMC media device proxy.
     *
     * @param secureChat the secure chat
     */
    public HMCMediaDeviceProxy(SecureChat secureChat) {
        super(secureChat);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf#localDeviceRemovedNotification()
     */
    @Override
	public void localDeviceRemovedNotification() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf#localDeviceAddedNotification(com.hmc.project.hmc.devices.implementations.DeviceDescriptor)
	 */
	@Override
	public void localDeviceAddedNotification(DeviceDescriptor newDev) {
        sendNotification(CMD_LOCAL_DEVICE_ADDED_NOTIFICATION, newDev.toXMLString());
	}

    /**
     * Send list of devices.
     *
     * @param list the list
     */
    public void sendListOfDevices(HMCDevicesList list) {
        // Send the list of deviecs to the newly added client. Use the
        // notification RPC mechanism
        Log.d(TAG, "Send the list to remote: " + list.toXMLString());
        sendNotification(CMD_SEND_LIST_DEVICES, list.toXMLString());
    }

    /**
     * Sets the external devices list.
     *
     * @param devList the new external devices list
     */
    public void setExternalDevicesList(HMCDevicesList devList) {
        sendNotification(CMD_SET_EXTERNAL_DEVICES_LIST_NOTIFICATION, devList.toXMLString());
    }
}
