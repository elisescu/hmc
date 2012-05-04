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

/**
 * 
 */
public interface HMCMediaDeviceItf {

    static final int HMC_MEDIA_DEVICE_INITIAL_COMMAND = HMCServerItf.HMC_SERVER_LAST_COMMAND;

    static final int CMD_HELLO = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 1;

    static final int CMD_JOIN_HMC = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 2;

    static final int CMD_SEND_LIST_DEVICES = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 3;

    static final int CMD_LOCAL_DEVICE_ADDED_NOTIFICATION = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 4;

    static final int CMD_EXTERNAL_DEVICE_ADDED_NOTIFICATION = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 5;

    static final int CMD_SET_EXTERNAL_DEVICES_LIST_NOTIFICATION = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 6;

    void localDeviceRemovedNotification();

    void localDeviceAddedNotification(DeviceDescriptor newDev);
}
