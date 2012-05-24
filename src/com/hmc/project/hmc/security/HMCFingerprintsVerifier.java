/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;

// TODO: Auto-generated Javadoc
/**
 * The Interface HMCFingerprintsVerifier.
 */
public interface HMCFingerprintsVerifier {

    // ask the implementer to verify the fingerprint of the remote device that
    // I'm communicating with the device can be either a device from local HMC
    // network or a device from an external HMC network or an unknown
    // fingerprint
    /**
     * Authenticate device.
     *
     * @param remoteDevice the remote device
     * @return true, if successful
     */
    public boolean authenticateDevice(DeviceDescriptor remoteDevice);
}
