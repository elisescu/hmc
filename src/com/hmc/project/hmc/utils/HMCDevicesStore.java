/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;

import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;

public class HMCDevicesStore {
    private static final String TAG = "HMCDeviceStore";
    private static HMCDevicesStore INSTANCE = null;
    XmlSerializer mSerializer;
    StringWriter mWriter;
    private String mFilePath = "sdcard/HMCDeviceStore.dat";
    private int mNoDevice = 0;
    HashMap<String, HMCDeviceProxy> mListOfLocalDevices;
    private HMCManager mHMCManager;

    private HMCDevicesStore(HMCManager mng, String filePath) {
        mListOfLocalDevices = new HashMap<String, HMCDeviceProxy>();
        mHMCManager = mng;
        mFilePath = filePath;
    }

    public HMCDevicesStore() {
        // TODO Auto-generated constructor stub
    }

    public static HMCDevicesStore getInstance() {
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

    public HashMap<String, HMCDeviceProxy> getListOfLocalDevices() {
        if (mListOfLocalDevices == null) {
            // read from file the list of devices
        }
        return mListOfLocalDevices;
    }

    @Deprecated
    public boolean addNewLocalDevice(HMCDeviceProxy dev) {
        if (mListOfLocalDevices.containsKey(dev.getDeviceDescriptor().getFullJID())) {
            Log.e(TAG, "Device " + dev.getDeviceDescriptor().getFullJID()
                    + " already exists in device store");
            return false;
        }
        mListOfLocalDevices.put(dev.getDeviceDescriptor().getFullJID(), dev);

        flushCache();
        return true;
    }

    public boolean addNewLocalDevice(DeviceDescriptor devDesc) {
        HMCDeviceProxy dev = mHMCManager.createNewDeviceProxy(devDesc);

        if (dev == null) {
            return false;
        }

        if (mListOfLocalDevices.containsKey(devDesc.getFullJID())) {
            Log.e(TAG, "Device " + devDesc.getFullJID() + " already exists in device store");
            return false;
        }

        mListOfLocalDevices.put(dev.getDeviceDescriptor().getFullJID(), dev);
        // mHMCManager.

        flushCache();
        return true;
    }

    private void flushCache() {
        // save the list of devices in our file

    }

    public boolean removeLocalDevice(String fullJID) {
        boolean retVal = false;
        // String fullJID = dev.getDeviceDescriptor().getFullJID();
        HMCDeviceProxy dev = mListOfLocalDevices.get(fullJID);
        if (dev != null) {
            dev.cleanOTRSession();
            mListOfLocalDevices.remove(fullJID);
            retVal = true;
        }

        return retVal;
    }

    private static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

}
