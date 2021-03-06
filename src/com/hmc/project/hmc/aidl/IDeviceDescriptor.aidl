/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.aidl;

interface IDeviceDescriptor {
    String getDeviceName();
    String getUserName();
    int getDeviceType();
    String getFullJID();
    String getFingerprint();
}