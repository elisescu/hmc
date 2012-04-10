/**
 * Copyright (c) 2012 Vasile Popescu
 *
 * This file is part of HMC Software.
 *
 * HMC Software is distributed under NDA so it cannot be distributed
 * and/or modified without prior written agreement of the author.
 */
package com.hmc.project.hmc.devices.interfaces;

// TODO: Auto-generated Javadoc
/**
 * HMCMediaClientDevice is identified not only through the XMPP account
 *  but also by the resource (using full JID). This is because when the user
 * logs in on multiple devices with the same account, then what differs
 * between these devices is their full JID.
 *
 */
public interface HMCMediaClientDeviceItf {

  /**
   * displays to the user remote and the local fingerprints so that the user verifies them
   * manualy with the ones displayed on the other device (usually HMCServer
   * when adding the new device and also when interconnecting two HMC systems).
   *
   * @param localFingerprint the local fingerprint
   * @param remoteFingerprint the remote fingerprint
   */
  void verifyFingerprints(String localFingerprint, String remoteFingerprint) ;

  /**
   * Confirm and verify fingerprints.
   *
   * @param localFingerprint the local fingerprint
   * @param remoteFingerprint the remote fingerprint
   * @param newDeviceName the new device name
   */
  void confirmAndVerifyFingerprints(String localFingerprint, String remoteFingerprint, String newDeviceName) ;

}
