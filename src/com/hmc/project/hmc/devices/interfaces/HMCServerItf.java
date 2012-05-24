/**
 * Copyright (c) 2012 Vasile Popescu
 * 
 * This file is part of HMC Software.
 * 
 * HMC Software is distributed under NDA so it cannot be distributed 
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.interfaces;

import com.hmc.project.hmc.devices.implementations.HMCDevicesList;

// TODO: Auto-generated Javadoc
/**
 * HMCServer class Models the HMCServer device operations.
 */
public interface HMCServerItf {

    /** The Constant HMC_SERVER_INITIAL_COMMAND. */
    static final int HMC_SERVER_INITIAL_COMMAND = HMCDeviceItf.HMC_DEVICE_LAST_COMMAND;

    /** The Constant CMD_GET_LIST_OF_LOCAL_HMC_DEVICES. */
    static final int CMD_GET_LIST_OF_LOCAL_HMC_DEVICES = HMC_SERVER_INITIAL_COMMAND + 1;

    /** The Constant CMD_INTERCONNECTION_REQUEST. */
    static final int CMD_INTERCONNECTION_REQUEST = HMC_SERVER_INITIAL_COMMAND + 2;

    /** The Constant CMD_EXCHANGE_HMC_INFO. */
    static final int CMD_EXCHANGE_HMC_INFO = HMC_SERVER_INITIAL_COMMAND + 3;

    /** The Constant CMD_EXCHANGE_LISTS_OF_LOCAL_DEVICES. */
    static final int CMD_EXCHANGE_LISTS_OF_LOCAL_DEVICES = HMC_SERVER_INITIAL_COMMAND + 4;

    /** The Constant HMC_SERVER_LAST_COMMAND. */
    static final int HMC_SERVER_LAST_COMMAND = CMD_EXCHANGE_LISTS_OF_LOCAL_DEVICES;

  /**
   * Get the new devices that were added to HMC while local device was offline.
   * The parameter contains the hash of all devices that we have in the list now.
   * If the hash is different of the hash that HMCServer has, then HMCServer replies
   * with the complete list of devices (containing the new ones or missing the removed ones).
   *
   * @param hashOfMyListOfDevices the hash of my list of devices
   * @return the list of new hmc devices
   */
  void getListOfNewHMCDevices(String hashOfMyListOfDevices) ;

  /**
   * Removes the hmc device.
   */
  void removeHMCDevice() ;

}
