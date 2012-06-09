/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.proxy;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.HMCDevicesList;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.SecureChat;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCServerProxy.
 */
public class HMCServerProxy extends HMCDeviceProxy implements HMCServerItf {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCServerProxy";

    /**
     * Instantiates a new hMC server proxy.
     *
     * @param chatManager the chat manager
     * @param localFullJID the local full jid
     * @param remoteFullJid the remote full jid
     * @param ver the ver
     */
    public HMCServerProxy(ChatManager chatManager, String localFullJID, String remoteFullJid,
                            HMCFingerprintsVerifier ver) {
        super(chatManager, localFullJID, remoteFullJid, ver);
    }

    /**
     * Instantiates a new hMC server proxy.
     *
     * @param chat the chat
     * @param localFUllJID the local f ull jid
     * @param ver the ver
     */
    public HMCServerProxy(Chat chat, String localFUllJID, HMCFingerprintsVerifier ver) {
        super(chat, localFUllJID, ver);
    }

    /**
     * Instantiates a new hMC server proxy.
     *
     * @param secureChat the secure chat
     */
    public HMCServerProxy(SecureChat secureChat) {
        super(secureChat);
    }

	/* (non-Javadoc)
	 * @see com.hmc.project.hmc.devices.interfaces.HMCServerItf#getListOfNewHMCDevices(java.lang.String)
	 */
	@Override
	public void getListOfNewHMCDevices(String hashOfMyListOfDevices) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.hmc.project.hmc.devices.interfaces.HMCServerItf#removeHMCDevice()
	 */
	@Override
	public void removeHMCDevice() {
		// TODO Auto-generated method stub
		
	}

    /**
     * Exchange lists of local devices.
     *
     * @param list the list
     * @return the hMC devices list
     */
    public HMCDevicesList exchangeListsOfLocalDevices(HMCDevicesList list) {
        // Send the list of deviecs to the newly added client. Use the
        // notification RPC mechanism
        String strList = list.toXMLString();
        Log.d(TAG, "Send the list to remote: " + strList);
        String retDevsList = sendCommandSync(CMD_EXCHANGE_LISTS_OF_LOCAL_DEVICES, strList);
        Log.d(TAG, "Received list from remote: " + retDevsList);
        return HMCDevicesList.fromXMLString(retDevsList);
    }

}
