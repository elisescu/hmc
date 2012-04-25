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
 * 
 */
public interface HMCMediaDeviceItf {

    static final int HMC_MEDIA_DEVICE_INITIAL_COMMAND = HMCServerItf.HMC_SERVER_LAST_COMMAND;

    static final int CMD_HELLO = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 1;

    static final int CMD_JOIN_HMC = HMC_MEDIA_DEVICE_INITIAL_COMMAND + 2;

    void deviceRemovedNotification();

    void deviceAddedNotification();
}
