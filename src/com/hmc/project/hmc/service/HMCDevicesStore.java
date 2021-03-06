/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.service;

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

// TODO: Auto-generated Javadoc
/**
 * The Class HMCDevicesStore.
 */
public class HMCDevicesStore {

    /** The Constant TAG. */
    private static final String TAG = "HMCDeviceStore";

    /** The INSTANCE. */
    private static HMCDevicesStore INSTANCE = null;

    /** The m serializer. */
    XmlSerializer mSerializer;

    /** The m writer. */
    StringWriter mWriter;

    /** The m local devs file path. */
    private String mLocalDevsFilePath;

    /** The m ext devs file path. */
    private String mExtDevsFilePath;

    /** The m no device. */
    private int mNoDevice = 0;

    /** The m list of local devices. */
    HashMap<String, HMCDeviceProxy> mListOfLocalDevices;

    /** The m list of external devices. */
    HashMap<String, HMCDeviceProxy> mListOfExternalDevices;

    /** The m hmc manager. */
    private HMCManager mHMCManager;

    /** The m devices listener. */
    private IHMCDevicesListener mDevicesListener;

    /** The m external hmc name. */
    private String mExternalHMCName;

    /**
     * Instantiates a new hMC devices store.
     *
     * @param mng the mng
     * @param filePath the file path
     */
    public HMCDevicesStore(HMCManager mng, String filePath) {
        mListOfLocalDevices = new HashMap<String, HMCDeviceProxy>();
        mListOfExternalDevices = new HashMap<String, HMCDeviceProxy>();

        mHMCManager = mng;
        mLocalDevsFilePath = filePath;
        // TODO: build a proper HMC device store. For now we are in big hurry
        mExtDevsFilePath = mLocalDevsFilePath + "_ext";
    }

    /**
     * Register devices listener.
     *
     * @param devListener the dev listener
     */
    public void registerDevicesListener(IHMCDevicesListener devListener) {
        mDevicesListener = devListener;
    }

    /**
     * Gets the devices listener.
     *
     * @return the devices listener
     */
    public IHMCDevicesListener getDevicesListener() {
        return mDevicesListener;
    }

    /**
     * Unregister devices listener.
     *
     * @param devListener the dev listener
     */
    public void unregisterDevicesListener(IHMCDevicesListener devListener) {
        // TODO: if we support multiple listeners, fix this: add a vector with
        // listeners
        mDevicesListener = null;
    }

    /**
     * Gets the list of local devices.
     *
     * @return the list of local devices
     */
    public HashMap<String, HMCDeviceProxy> getListOfLocalDevices() {
        if (mListOfLocalDevices == null) {
            // read from file the list of devices
        }
        return mListOfLocalDevices;
    }

    /**
     * Gets the list of external devices.
     *
     * @return the list of external devices
     */
    public HashMap<String, HMCDeviceProxy> getListOfExternalDevices() {
        return mListOfExternalDevices;
    }

    /**
     * Adds the new external device.
     *
     * @param hmcName the hmc name
     * @param dev the dev
     * @return true, if successful
     */
    public boolean addNewExternalDevice(String hmcName, HMCDeviceProxy dev) {
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
            } else {
                Log.w(TAG, "Have no device listener to notofy about new device");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    /**
     * Adds the new local device.
     *
     * @param dev the dev
     * @return true, if successful
     */
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
            } else {
                Log.w(TAG, "Have no device listener to notofy about new device");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    /**
     * Adds the new local device.
     *
     * @param devDesc the dev desc
     * @return true, if successful
     */
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
            } else {
                Log.w(TAG, "Have no device listener to notofy about new device");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    /**
     * Adds the new external device.
     *
     * @param hmcName the hmc name
     * @param devDesc the dev desc
     * @return true, if successful
     */
    public boolean addNewExternalDevice(String hmcName, DeviceDescriptor devDesc) {
        // TODO: fix this
        // mExternalHMCName = hmcName;

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
            } else {
                Log.w(TAG, "Have no device listener to notofy about new device");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't notify the remote device listener");
            e.printStackTrace();
        }

        flushCache();
        return true;
    }

    /**
     * Flush cache.
     */
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

        // TODO: implement a proper fix for this temporary one
        // if the external devices list is empty, then don't try to transform it
        // to XML and to save it. Unlike the local devices list, the external
        // one can be empty
        Log.d(TAG, "size of external devicess: " + getListOfExternalDevices().size());
        if (getListOfExternalDevices().size() > 0) {
            devList = new HMCDevicesList(mExternalHMCName, false,
                                    getListOfExternalDevicesDescriptors(mExternalHMCName));

            try {
                writeStringToFile(mExtDevsFilePath, devList.toXMLString());
            } catch (IOException e) {
                Log.e(TAG, "Cannot save the current devices list to file: " + mLocalDevsFilePath);
                e.printStackTrace();
            }
        }

    }

    /**
     * Removes the local device.
     *
     * @param fullJID the full jid
     * @return true, if successful
     */
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

    /**
     * Read string from file.
     *
     * @param path the path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
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
    /**
     * Write string to file.
     *
     * @param path the path
     * @param data the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void writeStringToFile(String path, String data) throws IOException {
        FileWriter fstream;
        fstream = new FileWriter(new File(path));

        BufferedWriter out = new BufferedWriter(fstream);
        out.write(data);
        out.close();
        fstream.close();
    }

    /**
     * Gets the local device.
     *
     * @param from the from
     * @return the local device
     */
    public HMCDeviceProxy getLocalDevice(String from) {
        HMCDeviceProxy retVal = mListOfLocalDevices.get(from);
        return retVal;
    }

    public HMCDeviceProxy getExternalDevice(String from) {
        HMCDeviceProxy retVal = mListOfExternalDevices.get(from);
        return retVal;
    }

    /**
     * Load.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
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
            // if
            // (!devDesc.getFullJID().equals(mHMCManager.getLocalDevDesc().getFullJID()))
            {
                HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
                mListOfLocalDevices.put(devDesc.getFullJID(), devPrxy);

                try {
                    if (mDevicesListener != null) {
                        mDevicesListener.onDeviceAdded(devDesc);
                    } else {
                        Log.w(TAG, "Have no device listener to notofy about new device");
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Couldn't notify the remote device listener");
                    e.printStackTrace();
                }
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
                    } else {
                        Log.w(TAG, "Have no device listener to notofy about new device");
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Couldn't notify the remote device listener");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sets the local devices list.
     *
     * @param devList the new local devices list
     */
    public void setLocalDevicesList(HMCDevicesList devList) {
        // a list of devices was received from the HMCServer after we joined its
        // HMC network
        Iterator<DeviceDescriptor> devDescIter = devList.getIterator();

        while (devDescIter.hasNext()) {
            DeviceDescriptor devDesc = devDescIter.next();
            if (!mListOfLocalDevices.containsKey(devDesc.getFullJID())) {
                HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
                mListOfLocalDevices.put(devDesc.getFullJID(), devPrxy);
            } else {
                Log.w(TAG, "Device already existing in the local devices list");
            }

            try {
                if (mDevicesListener != null) {
                    mDevicesListener.onDeviceAdded(devDesc);
                } else {
                    Log.w(TAG, "Have no device listener to notofy about new device");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't notify the remote device listener");
                e.printStackTrace();
            }
        }

        flushCache();
    }

    /**
     * Sets the external devices list.
     *
     * @param devList the new external devices list
     */
    public void setExternalDevicesList(HMCDevicesList devList) {
        // a list of devices was received from the HMCServer after we joined its
        // HMC network
        if (devList == null) {
            Log.e(TAG, "Received null list of external devices !");
            return;
        }

        Iterator<DeviceDescriptor> devDescIter = devList.getIterator();

        while (devDescIter.hasNext()) {
            DeviceDescriptor devDesc = devDescIter.next();

            if (!mListOfExternalDevices.containsKey(devDesc.getFullJID())) {
                HMCDeviceProxy devPrxy = mHMCManager.createNewDeviceProxy(devDesc);
                mListOfExternalDevices.put(devDesc.getFullJID(), devPrxy);
            } else {
                Log.w(TAG, "Device already existing in the external devices list");
            }

            try {
                if (mDevicesListener != null) {
                    mDevicesListener.onExternalDeviceAdded(mExternalHMCName, devDesc);
                } else {
                    Log.w(TAG, "Have no device listener to notofy about new device");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't notify the remote device listener");
                e.printStackTrace();
            }
        }

        flushCache();
    }

    /**
     * Gets the list of local devices descriptors.
     *
     * @return the list of local devices descriptors
     */
    public HashMap<String, DeviceDescriptor> getListOfLocalDevicesDescriptors() {
        HashMap<String, DeviceDescriptor> retVal = new HashMap<String, DeviceDescriptor>();
        Iterator<HMCDeviceProxy> iter = getListOfLocalDevices().values().iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            retVal.put(devPrx.getDeviceDescriptor().getFullJID(), devPrx.getDeviceDescriptor());
        }
        return retVal;
    }

    /**
     * Gets the list of external devices descriptors.
     *
     * @param hmcName the hmc name
     * @return the list of external devices descriptors
     */
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
