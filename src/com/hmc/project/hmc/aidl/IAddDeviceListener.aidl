/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.aidl;

import com.hmc.project.hmc.aidl.IDeviceDescriptor;

interface IAddDeviceListener {
    void remoteDevDescReceived(IDeviceDescriptor remDev);
    void remoteDevAdded(boolean success);
}
