/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DeviceDescriptor {
    private static final String TAG = null;
    // this order should be maintained in the XML representation
    public static final String TAG_DEVICE_NAME = "dd_dvn";
    public static final String TAG_USERNAME_NAME = "dd_usn";
    public static final String TAG_DEVICE_TYPE = "dd_dvt";
    public static final String TAG_FULLJID = "dd_flj";
    public static final String TAG_FINGERPRINT = "dd_fgp";
    public static final String TAG_DEVICE_ELEMENT = "dd_dvel";
    private String mDeviceName;
    private String mUserName;
    private int mDeviceType;
    private String mFullJID;
    private String mFingerprint;

    DocumentBuilderFactory mDocBuilderFactory;
    DocumentBuilder mDocBuilder;

    public DeviceDescriptor() {
    }
    
//
//    void fromXMLElement(Element eElement) {
//        String devType;
//        mDeviceName = getTagValue(TAG_DEVICE_NAME, eElement);
//        mUserName = getTagValue(TAG_USERNAME_NAME, eElement);
//        devType = getTagValue(TAG_DEVICE_TYPE, eElement);
//        mFullJID = getTagValue(TAG_FULLJID, eElement);
//        mFingerprint = getTagValue(TAG_FINGERPRINT, eElement);
//
//        try {
//            mDeviceType = Integer.parseInt(devType);
//        } catch (NumberFormatException e) {
//            Log.e(TAG, "Couldn't get the type of device");
//            mDeviceType = -1;
//        }
//    }
//
//    private static String getTagValue(String sTag, Element eElement) {
//        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
//        Node nValue = (Node) nlList.item(0);
//        return nValue.getNodeValue();
//    }
//
//    public Element toXMLElement() {
//        Document doc = mDocBuilder.newDocument();
//
//        Element retVal = doc.createElement("Device");
//        retVal.setAttribute(TAG_DEVICE_NAME, mDeviceName);
//        retVal.setAttribute(TAG_USERNAME_NAME, mUserName);
//        retVal.setAttribute(TAG_DEVICE_TYPE, Integer.toString(mDeviceType));
//        retVal.setAttribute(TAG_FULLJID, mFullJID);
//        retVal.setAttribute(TAG_FINGERPRINT, mFingerprint);
//        return retVal;
//    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(int deviceType) {
        mDeviceType = deviceType;
    }

    public String getFullJID() {
        return mFullJID;
    }

    public void setFullJID(String fullJID) {
        mFullJID = fullJID;
    }

    public String getFingerprint() {
        return mFingerprint;
    }

    public void setFingerprint(String fingerprint) {
        mFingerprint = fingerprint;
    }
}
