/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCDevicesListener;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCMediaClientHndl;
import com.hmc.project.hmc.aidl.IHMCMediaServiceHndl;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.devices.handlers.HMCMediaClientHandler;
import com.hmc.project.hmc.devices.handlers.HMCServerHandler;
import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.implementations.HMCDeviceImplementationItf;
import com.hmc.project.hmc.devices.implementations.HMCMediaClientDeviceImplementation;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCServerProxy;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.HMCSecurityPolicy;

public class HMCManager extends IHMCManager.Stub implements ChatManagerListener,
                        HMCFingerprintsVerifier {
    
    private static final String TAG = "HMCManager";

    private static final int STATE_NOT_INITIALIZED = 0;
    private static final int STATE_INITIALIZED = 1;

    HashMap<String, HMCDeviceProxy> mLocalDevices;
    HashMap<String, HMCDeviceProxy> mAnonymysDevices;

    HMCServerProxy mLocalServer;
    HashMap<String, HashMap<String, HMCDeviceProxy>> mExternalHMCs;
    Connection mXMPPConnection;
    private Roster mXMPPRoster;
    private ChatManager mXMPPChatManager;
    private int mState;
    private RosterListener mRosterListener = new HMCRosterListener();
    private HMCDeviceImplementationItf mLocalImplementation;
    private Object mLocalImplHandler;
    private DeviceDescriptor mThisDeviceDescriptor;
    private String mHMCName;
    private IHMCDevicesListener mHMCDevicesListener = null;

    public HMCManager(Connection xmppConnection) {
        mExternalHMCs = new HashMap<String, HashMap<String, HMCDeviceProxy>>();
        mLocalDevices = new HashMap<String, HMCDeviceProxy>();
        mAnonymysDevices = new HashMap<String, HMCDeviceProxy>();
        mXMPPConnection = xmppConnection;
        mXMPPChatManager = mXMPPConnection.getChatManager();
        mXMPPChatManager.addChatListener(this);
        mXMPPRoster = mXMPPConnection.getRoster();

        Log.d(TAG, "Constructed the HMCManager for " + mXMPPConnection.getUser());
        // set the subscription mode to manually
        mXMPPRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        mXMPPRoster.addRosterListener(mRosterListener);
        mState = STATE_NOT_INITIALIZED;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        if (!createdLocally) {
            HMCDeviceProxy devProxy = new HMCDeviceProxy(chat, mXMPPConnection.getUser(), this);
            mAnonymysDevices.put(chat.getParticipant(), devProxy);
        }
    }

    @Override
    public boolean verifyFingerprints(String localFingerprint, String remoteFingerprint,
            String remoteJID) {
        boolean lVerified = false;

        // TODO: make sure this makes sense... it feels like is not really right
        // the way I decide how to verify the fingerprints based o the policy of
        // the network
        switch (HMCSecurityPolicy.getInstance().getHMCSecurityPolicy()) {
            case HMCSecurityPolicy.TRUST_EVERYBODY_ALWAYS:
                lVerified = true;
                break;
            case HMCSecurityPolicy.TRUST_HMC_ALWAYS:
            case HMCSecurityPolicy.TRUST_HMC_VALID:
            case HMCSecurityPolicy.TRUST_HMC_SERVER:
                lVerified = false;
                break;
            default:
                lVerified = false;
        }
        return lVerified;
    }

    @Override
    public void init(String deviceName, String userName) throws RemoteException {
        if (mState == STATE_NOT_INITIALIZED) {
            Collection<RosterEntry> entries = mXMPPRoster.getEntries();
            Log.d(TAG, "We have " + entries.size() + "devices we can connect with");

            // TODO: change the way I initialize the list of devices. Get the
            // list of devices from HMCServer
            for (RosterEntry entry : entries) {
                Log.d(TAG, "Device name " + entry.getName() + ", bareJID:" + entry.getUser());
            }
            mThisDeviceDescriptor = new DeviceDescriptor();
            mThisDeviceDescriptor.setDeviceName(deviceName);
            mThisDeviceDescriptor.setUserName(userName);
            mThisDeviceDescriptor.setFullJID(mXMPPConnection.getUser());
            // TODO: add a way to set the name nice only if the device is
            // HMCServer
            mHMCName = "Popescus HMC";
            // TODO: get my fingerprint from somewhere..or generate it now and
            // store it
            mThisDeviceDescriptor.setFingerprint("no-fingerprint-yet");

            mState = STATE_INITIALIZED;
        } else {
            Log.w(TAG, "Already initialized");
        }
        Log.d(TAG, "Have " + mLocalDevices.size() + " devices connected");
    }

    @Override
    public IHMCServerHndl implHMCServer() throws RemoteException {
        if (mLocalImplHandler == null) {
            if (mLocalImplementation == null) {
                mThisDeviceDescriptor.setDeviceType(HMCDeviceItf.TYPE.HMC_SERVER);
                mLocalImplementation = new HMCServerImplementation(this, mThisDeviceDescriptor);
            }
            mLocalImplHandler = new HMCServerHandler((HMCServerImplementation) mLocalImplementation);
        }
        return (IHMCServerHndl) mLocalImplHandler;
    }

    @Override
    public IHMCMediaClientHndl implHMCMediaClient() throws RemoteException {
        if (mLocalImplHandler == null) {
            if (mLocalImplementation == null) {
                mThisDeviceDescriptor.setDeviceType(HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE);
                mLocalImplementation = new HMCMediaClientDeviceImplementation(this);
            }
            mLocalImplHandler = new HMCMediaClientHandler(
                                    (HMCMediaClientDeviceImplementation) mLocalImplementation);
        }
        return (IHMCMediaClientHndl) mLocalImplHandler;
    }

    @Override
    public IHMCMediaServiceHndl implHMCMediaService() throws RemoteException {
        // TODO implement this. leaving it null for now
        return null;
    }

    class HMCRosterListener implements RosterListener {
        @Override
        public void entriesAdded(Collection<String> arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void entriesDeleted(Collection<String> arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void entriesUpdated(Collection<String> arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void presenceChanged(Presence pres) {
            // let the corresponding device proxy know that the remote device is
            // offline or back online

            // TODO: change this to send the presence to specific device, using
            // the resource as well (i.e. parsing the bare JID)
            HMCDeviceProxy dev = mLocalDevices.get(pres.getFrom());
            if (dev != null) {
                dev.presenceChanged(pres);
            } else {
                Log.e(TAG, "Received presence information from unknown device: " + pres.getFrom());
            }
        }

    }

    public HMCAnonymousDeviceProxy createAnonymousProxy(String fullJID) {
        HMCAnonymousDeviceProxy retVal = null;
        retVal = new HMCAnonymousDeviceProxy(mXMPPChatManager, mXMPPConnection.getUser(), fullJID,
                this);
        return retVal;
    }

    public String getHMCName() {
        return mHMCName;
    }

    public void setHMCName(String name) {
        mHMCName = name;
    }

    public void promoteAnonymousProxy(HMCAnonymousDeviceProxy newDevProxy) {
        // create a specific proxy for the newly added device and add it to the
        // devices list
        HMCDeviceProxy knownDevice = null;
        knownDevice = newDevProxy.promoteToSpecificProxy();
        if (knownDevice != null ) {
            mLocalDevices.put(newDevProxy.getDeviceDescriptor().getFullJID(), knownDevice);
        } else {
            Log.e(TAG, "Couldn't promote the anonimous device");
        }
        Log.d(TAG, knownDevice.getDeviceDescriptor().getDeviceName()
                                + " was added to our list of devices");
        // TODO: replace operation strings with constants defined somewhere
        onLocalDevicesListChanged("added", newDevProxy);
    }

    private void onLocalDevicesListChanged(String string, HMCAnonymousDeviceProxy newDevProxy) {
        if (mHMCDevicesListener != null) {
            try {
                mHMCDevicesListener.onDevicesListChanged(string, newDevProxy.getDeviceDescriptor());
            } catch (RemoteException e) {
                Log.e(TAG, "Error when notifying listener about devices list modification: "
                                        + string);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerDevicesListener(IHMCDevicesListener listener) throws RemoteException {
        mHMCDevicesListener = listener;
    }

    @Override
    public void unregisterDevicesListener(IHMCDevicesListener listener) throws RemoteException {
        // TODO: if we support multiple listeners, fix this
        mHMCDevicesListener = null;
    }


    @Override
    public List<IDeviceDescriptor> getListOfLocalDevices() throws RemoteException {
        ArrayList<IDeviceDescriptor> retVal = new ArrayList<IDeviceDescriptor>();

        Iterator<HMCDeviceProxy> iter = mLocalDevices.values().iterator();
        while (iter.hasNext()) {
            retVal.add(iter.next().getDeviceDescriptor());
        }

        return (List<IDeviceDescriptor>) retVal;
    }
}
