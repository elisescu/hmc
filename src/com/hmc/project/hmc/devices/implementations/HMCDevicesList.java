/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.devices.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
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

import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCDevicesList.
 */
public class HMCDevicesList {
    
    /** The Constant TAG_DEVLIST_ELEMENT. */
    private static final String TAG_DEVLIST_ELEMENT = "dl_el";
    
    /** The Constant TAG_DEVLIST_NAME. */
    private static final String TAG_DEVLIST_NAME = "dl_nm";
    
    /** The Constant TAG_DEVLIST_ISLOCAL. */
    private static final String TAG_DEVLIST_ISLOCAL = "dl_lc";
    
    /** The Constant TAG. */
    private static final String TAG = "HMCDevicesList";
    
    /** The m devices. */
    private HashMap<String, DeviceDescriptor> mDevices;
    
    /** The m name. */
    private String mName;
    
    /** The m is local. */
    private boolean mIsLocal;

    /**
     * Instantiates a new hMC devices list.
     *
     * @param name the name
     * @param localDevices the local devices
     */
    public HMCDevicesList(String name, boolean localDevices) {
        mName = name;
        mIsLocal = localDevices;
        mDevices = new HashMap<String, DeviceDescriptor>();
    }

    /**
     * Instantiates a new hMC devices list.
     *
     * @param name the name
     * @param localDevices the local devices
     * @param devices the devices
     */
    public HMCDevicesList(String name, boolean localDevices,
                            HashMap<String, DeviceDescriptor> devices) {
        mName = name;
        mIsLocal = localDevices;
        mDevices = devices;
    }

    /**
     * Adds the device.
     *
     * @param dev the dev
     */
    public void addDevice(DeviceDescriptor dev) {
        mDevices.put(dev.getFullJID(), dev);
    }

    /**
     * Gets the device.
     *
     * @param JID the jID
     * @return the device
     */
    public DeviceDescriptor getDevice(String JID) {
        return mDevices.get(JID);
    }

    /**
     * Gets the no devices.
     *
     * @return the no devices
     */
    public int getNoDevices() {
        return mDevices.size();
    }

    /**
     * Gets the iterator.
     *
     * @return the iterator
     */
    public Iterator<DeviceDescriptor> getIterator() {
        return mDevices.values().iterator();
    }

    /**
     * From xml string.
     *
     * @param input the input
     * @return the hMC devices list
     */
    public static HMCDevicesList fromXMLString(String input) {
        HMCDevicesList retList = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        if (input == null) {
            Log.e(TAG, "Trying to parse null input");
            return null;
        }

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(input)));
            doc.getDocumentElement().normalize();

            Element listEl = (Element) doc.getElementsByTagName(TAG_DEVLIST_ELEMENT).item(0);

            if (listEl == null) {
                Log.e(TAG, "Error: cannot parse XML list of devices: " + input);
                return null;
            }

            String name = listEl.getAttribute(TAG_DEVLIST_NAME);
            boolean isLocal = Boolean.parseBoolean(listEl.getAttribute(TAG_DEVLIST_ISLOCAL));

            retList = new HMCDevicesList(name, isLocal);

            NodeList nList = doc.getElementsByTagName(DeviceDescriptor.TAG_DEVICE_ELEMENT);
            Log.d(TAG, "Parse " + nList.getLength() + " devices");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    DeviceDescriptor dev = new DeviceDescriptor();
                    int devType = -1;
                    dev.setDeviceName(eElement.getAttribute(DeviceDescriptor.TAG_DEVICE_NAME));
                    dev.setUserName(eElement.getAttribute(DeviceDescriptor.TAG_USERNAME_NAME));

                    try {
                        devType = Integer.parseInt(eElement
                                                .getAttribute(DeviceDescriptor.TAG_DEVICE_TYPE));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    dev.setDeviceType(devType);
                    dev.setFullJID(eElement.getAttribute(DeviceDescriptor.TAG_FULLJID));
                    dev.setFingerprint(eElement.getAttribute(DeviceDescriptor.TAG_FINGERPRINT));

                    retList.addDevice(dev);
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
          return retList;
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
        Element retVal = null;
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
            doc = docBuilder.newDocument();

            retVal = doc.createElement(TAG_DEVLIST_ELEMENT);
            retVal.setAttribute(TAG_DEVLIST_NAME, mName);
            retVal.setAttribute(TAG_DEVLIST_ISLOCAL, mIsLocal + "");

            Iterator<DeviceDescriptor> itr = mDevices.values().iterator();
            while (itr.hasNext()) {
                DeviceDescriptor dev = (DeviceDescriptor) itr.next();

                Element devElement = doc.createElement(DeviceDescriptor.TAG_DEVICE_ELEMENT);
                devElement.setAttribute(DeviceDescriptor.TAG_DEVICE_NAME, dev.getDeviceName());
                devElement.setAttribute(DeviceDescriptor.TAG_USERNAME_NAME, dev.getUserName());
                devElement.setAttribute(DeviceDescriptor.TAG_DEVICE_TYPE,
                                        Integer.toString(dev.getDeviceType()));
                devElement.setAttribute(DeviceDescriptor.TAG_FULLJID, dev.getFullJID());
                devElement.setAttribute(DeviceDescriptor.TAG_FINGERPRINT, dev.getFingerprint());

                retVal.appendChild(devElement);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * Gets the hMC name.
     *
     * @return the hMC name
     */
    public String getHMCName() {
        return mName;
    }
}
