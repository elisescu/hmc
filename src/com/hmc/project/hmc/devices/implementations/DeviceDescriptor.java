/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hmc.project.hmc.aidl.IDeviceDescriptor;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

public class DeviceDescriptor extends IDeviceDescriptor.Stub implements Parcelable {
    private static final String TAG = null;
    // this order should be maintained in the XML representation
    public static final String TAG_DEVICE_NAME = "dd_dvn";
    public static final String TAG_USERNAME_NAME = "dd_usn";
    public static final String TAG_DEVICE_TYPE = "dd_dvt";
    public static final String TAG_FULLJID = "dd_flj";
    public static final String TAG_FINGERPRINT = "dd_fgp";
    public static final String TAG_DEVICE_ELEMENT = "dd_dvel";
    private String mDeviceName;
    private String mUserName = "";
    private int mDeviceType;
    private String mFullJID;
    private String mFingerprint;

    DocumentBuilderFactory mDocBuilderFactory;
    DocumentBuilder mDocBuilder;

    public DeviceDescriptor() {
    }
    
    public static DeviceDescriptor fromXMLString(String input) {
        DeviceDescriptor retDevDesc = new DeviceDescriptor();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(input)));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(DeviceDescriptor.TAG_DEVICE_ELEMENT);
            if (nList.getLength() > 1) {
                Log.e(TAG, "Received more than a single inside xml string device");
            }

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int devType = -1;
                    retDevDesc.setDeviceName(eElement.getAttribute(DeviceDescriptor.TAG_DEVICE_NAME));
                    retDevDesc.setUserName(eElement.getAttribute(DeviceDescriptor.TAG_USERNAME_NAME));

                    try {
                        devType = Integer.parseInt(eElement
                                .getAttribute(DeviceDescriptor.TAG_DEVICE_TYPE));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    retDevDesc.setDeviceType(devType);
                    retDevDesc.setFullJID(eElement.getAttribute(DeviceDescriptor.TAG_FULLJID));
                    retDevDesc.setFingerprint(eElement.getAttribute(DeviceDescriptor.TAG_FINGERPRINT));
                }
            }

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retDevDesc;
    }

    public String toXMLString() {
        String retVal = null;
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = factory.newTransformer();

            DOMSource domSource = new DOMSource(toXMLElement());
            OutputStream output = new StringOutputStream();
            StreamResult result = new StreamResult(output);

            transformer.transform(domSource, result);
            retVal = output.toString();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }

    public Element toXMLElement() {
        Element devElement = null;
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
            doc = docBuilder.newDocument();

            devElement = doc.createElement(DeviceDescriptor.TAG_DEVICE_ELEMENT);
            devElement.setAttribute(DeviceDescriptor.TAG_DEVICE_NAME, getDeviceName());
            devElement.setAttribute(DeviceDescriptor.TAG_USERNAME_NAME, getUserName());
            devElement.setAttribute(DeviceDescriptor.TAG_DEVICE_TYPE,
                    Integer.toString(getDeviceType()));
            devElement.setAttribute(DeviceDescriptor.TAG_FULLJID, getFullJID());
            devElement.setAttribute(DeviceDescriptor.TAG_FINGERPRINT, getFingerprint());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return devElement;
    }

    @Override
    public String toString() {
        String retStr = "";
        retStr = "\ndevice name: " + getDeviceName();
        if (getUserName() != "")
            retStr += "\nuser name: " + getUserName();
        retStr += "\ndevice type: " + getDeviceType() + "\nfingerprint: " + getFingerprint();
        return retStr;
    }

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

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        
    }
}
