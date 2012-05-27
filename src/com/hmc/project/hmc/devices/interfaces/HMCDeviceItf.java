/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.interfaces;

import com.hmc.project.hmc.devices.proxy.AsyncCommandReplyListener;

// TODO: Auto-generated Javadoc
/**
 * HMCDevice class models the generic HMC device (either HMCServer or the rest of MediaDevices).
 */
public interface HMCDeviceItf {
    
    /**
     * The Class TYPE.
     */
    public class TYPE {
        // HMC Server
        /** The Constant HMC_SERVER. */
        public static final int HMC_SERVER = 1;

        // HMC Client Device
        /** The Constant HMC_CLIENT_DEVICE. */
        public static final int HMC_CLIENT_DEVICE = 2;

        // HMC Service Device
        /** The Constant HMC_SERVICE_DEVICE. */
        public static final int HMC_SERVICE_DEVICE = 3;
	}

    /** The Constant HMC_DEVICE_INITIAL_COMMAND. */
    static final int HMC_DEVICE_INITIAL_COMMAND = 1;

    /** The Constant CMD_REMOTE_INCREMENT. */
    static final int CMD_REMOTE_INCREMENT = HMC_DEVICE_INITIAL_COMMAND + 1;
    
    /** The Constant CMD_TEST_NOTIFICATION. */
    static final int CMD_TEST_NOTIFICATION = HMC_DEVICE_INITIAL_COMMAND + 2;
    
    /** The Constant CMD_TEST_ASYNC_COMMAND. */
    static final int CMD_TEST_ASYNC_COMMAND = HMC_DEVICE_INITIAL_COMMAND + 3;

    /** The Constant CMD_TEST_SYNC_COMMAND. */
    static final int CMD_TEST_SYNC_COMMAND = HMC_DEVICE_INITIAL_COMMAND + 4;

    /** The Constant HMC_DEVICE_LAST_COMMAND. */
    static final int HMC_DEVICE_LAST_COMMAND = CMD_TEST_ASYNC_COMMAND;
	
    /**
     * Remote increment.
     *
     * @param val the val
     * @return the int
     */
    int remoteIncrement(int val);

    /**
     * Test notification.
     *
     * @param notifString the notif string
     */
    void testNotification(String notifString);

}
