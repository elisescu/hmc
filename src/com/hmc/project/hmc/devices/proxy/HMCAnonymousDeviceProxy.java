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
import com.hmc.project.hmc.devices.implementations.HMCDevicesList;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCAnonymousDeviceProxy.
 *
 * @author elisescu
 */
public class HMCAnonymousDeviceProxy extends HMCDeviceProxy {

    /**
     * Instantiates a new hMC anonymous device proxy.
     *
     * @param chatManager the chat manager
     * @param localFullJID the local full jid
     * @param remoteFullJid the remote full jid
     * @param ver the ver
     */
    public HMCAnonymousDeviceProxy(ChatManager chatManager, String localFullJID,
            String remoteFullJid, HMCFingerprintsVerifier ver) {
        super(chatManager, localFullJID, remoteFullJid, ver);
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new hMC anonymous device proxy.
     *
     * @param chat the chat
     * @param localFullJID the local full jid
     * @param ver the ver
     */
    public HMCAnonymousDeviceProxy(Chat chat, String localFullJID, HMCFingerprintsVerifier ver) {
        super(chat, localFullJID, ver);
    }

    /** The Constant TAG. */
    private static final String TAG = "HMCAnonymousDeviceProxy";

    /**
     * Hello.
     *
     * @param myDev the my dev
     * @return the device descriptor
     */
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

    /**
     * Join hmc.
     *
     * @param hmcName the hmc name
     * @param listener the listener
     */
    public void joinHMC(String hmcName, AsyncCommandReplyListener listener) {
        sendCommandAsync(HMCMediaDeviceItf.CMD_JOIN_HMC, hmcName, listener);
    }

    /**
     * Interconnection request.
     *
     * @param hmcName the hmc name
     * @param listener the listener
     */
    public void interconnectionRequest(String hmcName, AsyncCommandReplyListener listener) {
        sendCommandAsync(HMCServerItf.CMD_INTERCONNECTION_REQUEST, hmcName, listener);
    }

    /**
     * Exchange hmc info.
     *
     * @param localHMCInfo the local hmc info
     * @return the hMC devices list
     */
    public HMCDevicesList exchangeHMCInfo(HMCDevicesList localHMCInfo) {
        HMCDevicesList remoteInfo = null;

        String retStr = sendCommandSync(HMCServerItf.CMD_EXCHANGE_HMC_INFO,
                localHMCInfo.toXMLString());
        remoteInfo = HMCDevicesList.fromXMLString(retStr);
        return remoteInfo;
    }

    /**
     * needed for testing purposes: start and stop the OTR session on the secure
     * chat reference.
     */
    public void test_StartAndStopOTR() {
        mSecureChat.startOtrSession();
        mSecureChat.stopOtrSession();
    }
}
