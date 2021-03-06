/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/
package com.hmc.project.hmc.aidl;
import com.hmc.project.hmc.aidl.IDeviceDescriptor;

interface IHMCDevicesListener {
    void onDeviceAdded(IDeviceDescriptor devDesc);
    void onDeviceRemoved(IDeviceDescriptor devDesc);
    void onPresenceChanged(String presence, IDeviceDescriptor devDesc);
    void onExternalDeviceAdded(String externalName, IDeviceDescriptor devDesc);
}