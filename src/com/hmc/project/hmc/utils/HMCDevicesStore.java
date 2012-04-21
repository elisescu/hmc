/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.utils;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

class HMCDevicesStore {
    private static final String TAG = "HMCDeviceStore";
    private HMCDevicesStore INSTANCE = null;
    XmlSerializer mSerializer;
    StringWriter mWriter;
    private String mFilePath = "sdcard/HMCDeviceStore.dat";
    private int mNoDevice = 0;

    public HMCDevicesStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HMCDevicesStore();
        }
        return INSTANCE;
    }

    public int getNoDevices() {
        return mNoDevice;
    }

    public void setNoDevices(int ndev) {
        mNoDevice = ndev;
    }

}
