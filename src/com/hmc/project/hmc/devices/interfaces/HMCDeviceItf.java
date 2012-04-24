/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.interfaces;

/**
 * HMCDevice class models the generic HMC device (either HMCServer or the rest of MediaDevices).
 */
public interface HMCDeviceItf {
    public class TYPE {
        // HMC Server
        public static final int HMC_SERVER = 1;

        // HMC Client Device
        public static final int HMC_CLIENT_DEVICE = 2;

        // HMC Service Device
        public static final int HMC_SERVICE_DEVICE = 3;
	}

    static final int HMC_DEVICE_INITIAL_COMMAND = 1;

    static final int CMD_HELLO = 2;
    static final int CMD_JOIN_HMC = 3;

    static final int CMD_REMOTE_INCREMENT = 4;

    static final int HMC_DEVICE_LAST_COMMAND = CMD_REMOTE_INCREMENT;
	
    int remoteIncrement(int val);
}
