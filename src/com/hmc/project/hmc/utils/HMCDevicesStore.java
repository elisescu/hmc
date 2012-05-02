/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;

import org.xmlpull.v1.XmlSerializer;

import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IHMCDevicesListener;
import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.implementations.HMCDevicesList;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;

public class HMCDevicesStore {
    private static final String TAG = "HMCDeviceStore";
    private static HMCDevicesStore INSTANCE = null;
    XmlSerializer mSerializer;
    StringWriter mWriter;
    private String mLocalDevsFilePath;
    private String mExtDevsFilePath;
    private int mNoDevice = 0;
    HashMap<String, HMCDeviceProxy> mListOfLocalDevices;
    HashMap<String, HMCDeviceProxy> mListOfExternalDevices;
    private HMCManager mHMCManager;
    private IHMCDevicesListener mDevicesListener;
    private String mExternalHMCName;

    public HMCDevicesStore(HMCManager mng, String filePath) {
        mListOfLocalDevices = new HashMap<String, HMCDeviceProxy>();
        mListOfExternalDevices = new HashMap<String, HMCDeviceProxy>();

        mHMCManager = mng;
        mLocalDevsFilePath = filePath;
        // TODO: build a proper HMC device store. For now we are in big hurry
        mExtDevsFilePath = mLocalDevsFilePath + "_ext";
    }

    public void registerDevicesListener(IHMCDevicesListener devListener) {
        mDevicesListener = devListener;
    }

    public IHMCDevicesListener getDevicesListener() {
        return mDevicesListener;
    }

    public void unregisterDevicesListener(IHMCDevicesListener devListener) {
        // TODO: if we support multiple listeners, fix this: add a vector with
        // listeners
        mDevicesListener = null;
    }

    public HashMap<String, HMCDeviceProxy> getListOfLocalDevices() {
        if (mListOfLocalDevices == null) {
            // read from file the list of devices
        }
        return mListOfLocalDevices;
    }

    public HashMap<String, HMCDeviceProxy> getListOfExternalDevices() {
        return mListOfExternalDevices;
    }

    public boolean addNewExternalDevice(String hmcName, HMCDeviceProxy dev) {
        // TODO: fix this
        mExternalHMCName = hmcName;

        if (mListOfExternalDevices.containsKey(dev.getDeviceDescriptor().getFullJID())) {
            Log.e(TAG, "Device " + dev.getDeviceDescriptor().getFullJID()
                                    + " already exists in device store");
            return false;
        }
        mListOfExternalDevices.put(dev.getDeviceDescriptor().getFullJID(), dev);

        try {
            if (mDevicesListener != null) {
                mDevicesListener.onExternalDeviceAdded(hmcName, dev.getDeviceDescriptor());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    public boolean addNewLocalDevice(HMCDeviceProxy dev) {
        if (mListOfLocalDevices.containsKey(dev.getDeviceDescriptor().getFullJID())) {
            Log.e(TAG, "Device " + dev.getDeviceDescriptor().getFullJID()
                    + " already exists in device store");
            return false;
        }
        mListOfLocalDevices.put(dev.getDeviceDescriptor().getFullJID(), dev);

        try {
            if (mDevicesListener != null) {
                mDevicesListener.onDeviceAdded(dev.getDeviceDescriptor());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

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

        try {
            if (mDevicesListener != null) {
                mDevicesListener.onDeviceAdded(devDesc);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    public boolean addNewExternalDevice(String hmcName, DeviceDescriptor devDesc) {
        // TODO: fix this
        mExternalHMCName = hmcName;

        HMCDeviceProxy dev = mHMCManager.createNewDeviceProxy(devDesc);

        if (dev == null) {
            return false;
        }

        if (mListOfExternalDevices.containsKey(devDesc.getFullJID())) {
            Log.e(TAG, "Device " + devDesc.getFullJID() + " already exists in device store");
            return false;
        }

        mListOfExternalDevices.put(dev.getDeviceDescriptor().getFullJID(), dev);

        try {
            if (mDevicesListener != null) {
                mDevicesListener.onExternalDeviceAdded(hmcName, devDesc);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    private void flushCache() {
        // save the list of devices in our file
        HMCDevicesList devList = new HMCDevicesList(mHMCManager.getHMCName(), true,
                getListOfLocalDevicesDescriptors());

        try {
            writeStringToFile(mLocalDevsFilePath, devList.toXMLString());
        } catch (IOException e) {
            Log.e(TAG, "Cannot save the current devices list to file: " + mLocalDevsFilePath);
            e.printStackTrace();
        }

        devList = new HMCDevicesList(mExternalHMCName, false,
                                getListOfExternalDevicesDescriptors(mExternalHMCName));

        try {
            writeStringToFile(mExtDevsFilePath, devList.toXMLString());
        } catch (IOException e) {
            Log.e(TAG, "Cannot save the current devices list to file: " + mLocalDevsFilePath);
            e.printStackTrace();
        }

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

    private static String readStringFromFile(String path) throws IOException {
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

    // TODO: optimize the readings/writings from/to files
    private static void writeStringToFile(String path, String data) throws IOException {
        FileWriter fstream;
        fstream = new FileWriter(new File(path));

        BufferedWriter out = new BufferedWriter(fstream);
        out.write(data);
        out.close();
        fstream.close();
    }

    public HMCDeviceProxy getLocalDevice(String from) {
        HMCDeviceProxy retVal = mListOfLocalDevices.get(from);
        return retVal;
    }

    public void load() throws IOException {
        // load devices from file and create the proxies. Notify also the
        // listeners about the devices we have in the local and external lists
        HMCDevicesList devicesList;
        File devFile = new File(mLocalDevsFilePath);
        Iterator<DeviceDescriptor> devDescIter;

        // if it's the first time we call this method, then create the file to
        // store list of devices and add our device in this list
        if (!devFile.exists()) {
            HMCDevicesList devList = new HMCDevicesList(mHMCManager.getHMCName(), true);
            devList.addDevice(mHMCManager.getLocalDevDesc());
            writeStringToFile(mLocalDevsFilePath, devList.toXMLString());
            Log.d(TAG, "The initialization file doesn't exist .. so create a new onew with "
                    + devList.toXMLString());
        }

        devicesList = HMCDevicesList.fromXMLString(readStringFromFile(mLocalDevsFilePath));
        Log.d(TAG, "Parsed " + readStringFromFile(mLocalDevsFilePath) + " and the result is "
                                + devicesList);

        if (devicesList == null) {
            Log.e(TAG, "Error reading the devices file. Deleting the file..");
            devFile.delete();
            throw new IOException("Couldn't read or parse the devices input file");
        }

        devDescIter = devicesList.getIterator();

        while (devDescIter.hasNext()) {
            DeviceDescriptor devDesc = devDescIter.next();
            HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
            mListOfLocalDevices.put(devDesc.getFullJID(), devPrxy);

            try {
                if (mDevicesListener != null) {
                    mDevicesListener.onDeviceAdded(devDesc);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't notify the remote device listener");
                e.printStackTrace();
            }
        }

        // TODO: fix this !
        devFile = new File(mExtDevsFilePath);

        // if it's the first time we call this method, then create the file to
        // store list of devices and add our device in this list
        if (devFile.exists()) {
            devicesList = HMCDevicesList.fromXMLString(readStringFromFile(mExtDevsFilePath));
            Log.d(TAG, "Parsed " + readStringFromFile(mExtDevsFilePath) + " and the result is "
                                    + devicesList);



            if (devicesList == null) {
                Log.e(TAG, "Error reading the devices file. Deleting the file..");
                devFile.delete();
                throw new IOException("Couldn't read or parse the devices input file");
            }

            devDescIter = devicesList.getIterator();

            while (devDescIter.hasNext()) {
                DeviceDescriptor devDesc = devDescIter.next();
                HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
                mListOfExternalDevices.put(devDesc.getFullJID(), devPrxy);

                try {
                    if (mDevicesListener != null) {
                        mDevicesListener.onExternalDeviceAdded(mExternalHMCName, devDesc);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Couldn't notify the remote device listener");
                    e.printStackTrace();
                }
            }
        }
    }

    public void setLocalDevicesList(HMCDevicesList devList) {
        // a list of devices was received from the HMCServer after we joined its
        // HMC network
        Iterator<DeviceDescriptor> devDescIter = devList.getIterator();

        while (devDescIter.hasNext()) {
            DeviceDescriptor devDesc = devDescIter.next();
            HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
            mListOfLocalDevices.put(devDesc.getFullJID(), devPrxy);

            try {
                if (mDevicesListener != null) {
                    mDevicesListener.onDeviceAdded(devDesc);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't notify the remote device listener");
                e.printStackTrace();
            }
        }

        flushCache();
    }

    public void setExternalDevicesList(HMCDevicesList devList) {
        // a list of devices was received from the HMCServer after we joined its
        // HMC network
        Iterator<DeviceDescriptor> devDescIter = devList.getIterator();

        while (devDescIter.hasNext()) {
            DeviceDescriptor devDesc = devDescIter.next();
            HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
            mListOfExternalDevices.put(devDesc.getFullJID(), devPrxy);

            try {
                if (mDevicesListener != null) {
                    mDevicesListener.onExternalDeviceAdded(mExternalHMCName, devDesc);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't notify the remote device listener");
                e.printStackTrace();
            }
        }

        flushCache();
    }

    public HashMap<String, DeviceDescriptor> getListOfLocalDevicesDescriptors() {
        HashMap<String, DeviceDescriptor> retVal = new HashMap<String, DeviceDescriptor>();
        Iterator<HMCDeviceProxy> iter = getListOfLocalDevices().values().iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            retVal.put(devPrx.getDeviceDescriptor().getFullJID(), devPrx.getDeviceDescriptor());
        }
        return retVal;
    }

    public HashMap<String, DeviceDescriptor> getListOfExternalDevicesDescriptors(String hmcName) {
        HashMap<String, DeviceDescriptor> retVal = new HashMap<String, DeviceDescriptor>();
        Iterator<HMCDeviceProxy> iter = getListOfExternalDevices().values().iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            retVal.put(devPrx.getDeviceDescriptor().getFullJID(), devPrx.getDeviceDescriptor());
        }
        return retVal;
    }

}
