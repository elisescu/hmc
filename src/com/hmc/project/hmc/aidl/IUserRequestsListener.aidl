/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.aidl;

import com.hmc.project.hmc.aidl.IDeviceDescriptor;
interface IUserRequestsListener {

    boolean confirmDeviceAddition(IDeviceDescriptor newDevice);
    boolean confirmHMCInterconnection(IDeviceDescriptor remoteHMCServer, String remoteHMCName);
    boolean verifyFingerprint(String localFingerprint, String remoteFingerprint, String deviceName);

}