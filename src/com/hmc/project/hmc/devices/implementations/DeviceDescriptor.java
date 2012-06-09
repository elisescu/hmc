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

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceDescriptor.
 */
public class DeviceDescriptor extends IDeviceDescriptor.Stub implements Parcelable {
    
    /** The Constant TAG. */
    private static final String TAG = null;
    // this order should be maintained in the XML representation
    /** The Constant TAG_DEVICE_NAME. */
    public static final String TAG_DEVICE_NAME = "dd_dvn";
    
    /** The Constant TAG_USERNAME_NAME. */
    public static final String TAG_USERNAME_NAME = "dd_usn";
    
    /** The Constant TAG_DEVICE_TYPE. */
    public static final String TAG_DEVICE_TYPE = "dd_dvt";
    
    /** The Constant TAG_FULLJID. */
    public static final String TAG_FULLJID = "dd_flj";
    
    /** The Constant TAG_FINGERPRINT. */
    public static final String TAG_FINGERPRINT = "dd_fgp";
    
    /** The Constant TAG_DEVICE_ELEMENT. */
    public static final String TAG_DEVICE_ELEMENT = "dd_dvel";
    
    /** The m device name. */
    private String mDeviceName = "";
    
    /** The m user name. */
    private String mUserName = "";
    
    /** The m device type. */
    private int mDeviceType;
    
    /** The m full jid. */
    private String mFullJID;
    
    /** The m fingerprint. */
    private String mFingerprint;

    /** The m doc builder factory. */
    DocumentBuilderFactory mDocBuilderFactory;
    
    /** The m doc builder. */
    DocumentBuilder mDocBuilder;

    /**
     * Instantiates a new device descriptor.
     */
    public DeviceDescriptor() {
    }
    
    /**
     * From xml string.
     *
     * @param input the input
     * @return the device descriptor
     */
    public static DeviceDescriptor fromXMLString(String input) {

        if (input == null) {
            Log.e(TAG, "received null string to parse");
            return null;
        }
        
        DeviceDescriptor retDevDesc = new DeviceDescriptor();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(input)));
            Element rootElement = doc.getDocumentElement();

            // TODO: improve the way of handling errors
            if (rootElement == null)
                return null;

            rootElement.normalize();

            NodeList nList = doc.getElementsByTagName(DeviceDescriptor.TAG_DEVICE_ELEMENT);

            // TODO: improve the way of handling errors
            if (nList == null) {
                return null;
            }

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

    /**
     * To xml string.
     *
     * @return the string
     */
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

    /**
     * To xml element.
     *
     * @return the element
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String retStr = "";
        retStr = "\ndevice name: " + getDeviceName();
        retStr += "\nfullJID: " + getFullJID();
        if (getUserName() != "")
            retStr += "\nuser name: " + getUserName();
        retStr += "\ndevice type: " + getDeviceType() + "\nfingerprint: " + getFingerprint();
        return retStr;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IDeviceDescriptor#getDeviceName()
     */
    public String getDeviceName() {
        if (mDeviceName == null)
            return "";

        return mDeviceName;
    }

    /**
     * Sets the device name.
     *
     * @param deviceName the new device name
     */
    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IDeviceDescriptor#getUserName()
     */
    public String getUserName() {
        if (mUserName == null)
            return "";
        return mUserName;
    }

    /**
     * Sets the user name.
     *
     * @param userName the new user name
     */
    public void setUserName(String userName) {
        mUserName = userName;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IDeviceDescriptor#getDeviceType()
     */
    public int getDeviceType() {
        return mDeviceType;
    }

    /**
     * Sets the device type.
     *
     * @param deviceType the new device type
     */
    public void setDeviceType(int deviceType) {
        mDeviceType = deviceType;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IDeviceDescriptor#getFullJID()
     */
    public String getFullJID() {
        if (mFullJID == null)
            return "";
        return mFullJID;
    }

    /**
     * Sets the full jid.
     *
     * @param fullJID the new full jid
     */
    public void setFullJID(String fullJID) {
        mFullJID = fullJID;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IDeviceDescriptor#getFingerprint()
     */
    public String getFingerprint() {
        if (mFingerprint == null)
            return "";
        return mFingerprint;
    }

    /**
     * Sets the fingerprint.
     *
     * @param fingerprint the new fingerprint
     */
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
