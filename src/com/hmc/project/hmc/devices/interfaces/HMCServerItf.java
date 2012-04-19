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
 * HMCServer class Models the HMCServer device operations
 */
public interface HMCServerItf {

    static final int HMC_SERVER_INITIAL_COMMAND = HMCDeviceItf.HMC_DEVICE_LAST_COMMAND;

    static final int CMD_ADD_ME_TO_HMC = HMC_SERVER_INITIAL_COMMAND + 1;

    static final int HMC_DEVICE_LAST_COMMAND = CMD_ADD_ME_TO_HMC;

    /**
     * Used by a device that wants to be added to HMC by this HCMServer. 
     */
    void addMeToHMC();

  /**
   * Request received from a local admin device in order to interconnect to an external HMC. 
   * The external HMC system is identified using the XMPP address of its HMCServer. 
   * Inside this function the OTR session is negotiated and once we got the remote 
   * public key (of the external HMCServer) the admin's device will be contacted to
   *  verify this remote public key (its fingerprint) against the one displayed on the
   *  admin's device of the external HMC. See the sequece diagram for more details.
   */
  void interconnectTo(String externalHMCServerAddress) ;

  /**
   * Interconnects with an external HMC. The request is sent by an external HMCServer
   *  that we are going to interconnect to. Once the admin approves and verifies the
   *  fingerprint of the remote HMCServer, we can send a request to get the list of the 
   * external devices.
   */
  void interconnectionRequest(String requesterName) ;

  /**
   * retrieves the list of HMC devices XMPP JIDs and their public keys from HMC
   */
  void getListOfHMCDevices() ;

  /**
   * Get the new devices that were added to HMC while local device was offline. 
   * The parameter contains the hash of all devices that we have in the list now.
   *  If the hash is different of the hash that HMCServer has, then HMCServer replies 
   * with the complete list of devices (containing the new ones or missing the removed ones).
   */
  void getListOfNewHMCDevices(String hashOfMyListOfDevices) ;

  void removeHMCDevice() ;

}
