/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.interfaces;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;

// TODO: Auto-generated Javadoc
/**
 * The Interface HMCMediaDeviceItf.
 */
public interface HMCMediaDeviceItf {

    /** The Constant HMC_MEDIA_DEVICE_INITIAL_COMMAND. */
    static final int HMC_MEDIA_DEVICE_INITIAL_COMMAND = HMCServerItf.HMC_SERVER_LAST_COMMAND;

    /** The Constant CMD_HELLO. */
    static final int CMD_HELLO = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 1;

    /** The Constant CMD_JOIN_HMC. */
    static final int CMD_JOIN_HMC = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 2;

    /** The Constant CMD_SEND_LIST_DEVICES. */
    static final int CMD_SEND_LIST_DEVICES = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 3;

    /** The Constant CMD_LOCAL_DEVICE_ADDED_NOTIFICATION. */
    static final int CMD_LOCAL_DEVICE_ADDED_NOTIFICATION = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 4;

    /** The Constant CMD_EXTERNAL_DEVICE_ADDED_NOTIFICATION. */
    static final int CMD_EXTERNAL_DEVICE_ADDED_NOTIFICATION = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 5;

    /** The Constant CMD_SET_EXTERNAL_DEVICES_LIST_NOTIFICATION. */
    static final int CMD_SET_EXTERNAL_DEVICES_LIST_NOTIFICATION = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 6;

    /**
     * Local device removed notification.
     */
    void localDeviceRemovedNotification();

    /**
     * Local device added notification.
     *
     * @param newDev the new dev
     */
    void localDeviceAddedNotification(DeviceDescriptor newDev);
}
