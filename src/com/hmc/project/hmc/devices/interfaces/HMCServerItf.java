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

/**
 * HMCServer class Models the HMCServer device operations
 */
public interface HMCServerItf {

    static final int HMC_SERVER_INITIAL_COMMAND = HMCDeviceItf.HMC_DEVICE_LAST_COMMAND;

    static final int CMD_GET_LIST_OF_LOCAL_HMC_DEVICES = HMC_SERVER_INITIAL_COMMAND + 1;

    static final int CMD_INTERCONNECTION_REQUEST = HMC_SERVER_INITIAL_COMMAND + 2;

    static final int CMD_EXCHANGE_HMC_INFO = HMC_SERVER_INITIAL_COMMAND + 3;

    static final int HMC_SERVER_LAST_COMMAND = CMD_INTERCONNECTION_REQUEST;


    /**
     * retrieves the list of HMC devices XMPP JIDs and their public keys from
     * HMC
     */
    HMCDevicesList getListOfLocalHMCDevices();

  /**
   * Get the new devices that were added to HMC while local device was offline. 
   * The parameter contains the hash of all devices that we have in the list now.
   *  If the hash is different of the hash that HMCServer has, then HMCServer replies 
   * with the complete list of devices (containing the new ones or missing the removed ones).
   */
  void getListOfNewHMCDevices(String hashOfMyListOfDevices) ;

  void removeHMCDevice() ;

}
