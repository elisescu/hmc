/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

/**
 * @author elisescu
 *
 */
public interface HMCDeviceImplementationItf {

    String localExecute(int opCode, String params);
}
