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

import com.hmc.project.hmc.devices.interfaces.HMCMediaClientDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaServiceDeviceItf;
import com.hmc.project.hmc.security.SecureChat;


// TODO: Auto-generated Javadoc
/**
 * The Class HMCMediaClientDeviceProxy.
 */
public class HMCMediaServiceDeviceProxy extends HMCMediaDeviceProxy implements
                        HMCMediaServiceDeviceItf {

    /**
     * Instantiates a new hMC media client device proxy.
     *
     * @param chatManager the chat manager
     * @param localFUllJID the local f ull jid
     * @param remoteFullJid the remote full jid
     */
    public HMCMediaServiceDeviceProxy(ChatManager chatManager, String localFUllJID,
                            String remoteFullJid) {
        super(chatManager, localFUllJID, remoteFullJid);
    }

    /**
     * Instantiates a new hMC media client device proxy.
     *
     * @param secureChat the secure chat
     */
    public HMCMediaServiceDeviceProxy(SecureChat secureChat) {
        super(secureChat);
    }

}
