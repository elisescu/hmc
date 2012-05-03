/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.java.otr4j.session.SessionID;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCDevicesListener;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCMediaClientHndl;
import com.hmc.project.hmc.aidl.IHMCMediaServiceHndl;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.handlers.HMCMediaClientHandler;
import com.hmc.project.hmc.devices.handlers.HMCServerHandler;
import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.devices.implementations.HMCDeviceImplementation;
import com.hmc.project.hmc.devices.implementations.HMCDevicesList;
import com.hmc.project.hmc.devices.implementations.HMCMediaDeviceImplementation;
import com.hmc.project.hmc.devices.implementations.HMCMediaClientDeviceImplementation;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaClientDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCServerProxy;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.HMCOTRManager;
import com.hmc.project.hmc.security.HMCSecurityPolicy;
import com.hmc.project.hmc.ui.mediaclient.ConfirmJoinHMC;
import com.hmc.project.hmc.utils.HMCDevicesStore;

public class HMCManager extends IHMCManager.Stub implements ChatManagerListener,
                        HMCFingerprintsVerifier {
    
    private static final String TAG = "HMCManager";

    private static final int STATE_NOT_INITIALIZED = 0;
    private static final int STATE_INITIALIZED = 1;

    private static final String DEVICES_STORE_PATH = "/sdcard/HMCDeviceStore.dat";

    private HashMap<String, HMCDeviceProxy> mAnonymousDevices;

    HMCServerProxy mLocalServer;
    HashMap<String, HashMap<String, HMCDeviceProxy>> mExternalHMCs;
    Connection mXMPPConnection;
    private Roster mXMPPRoster;
    private ChatManager mXMPPChatManager;
    private int mState;
    private RosterListener mRosterListener = new HMCRosterListener();
    private HMCDeviceImplementation mLocalImplementation;
    private Object mLocalImplHandler = null;
    private DeviceDescriptor mLocalDeviceDescriptor;
    private String mHMCName;
    private HMCService mHMCService;
    private DeviceAditionConfirmationListener mDeviceAdditionListener;
    private HMCInterconnectionConfirmationListener mInterconnectionHMCListener;
    private HMCDevicesStore mHMCDevicesStore;

    public HMCManager(Connection xmppConnection, HMCService service) {
        mExternalHMCs = new HashMap<String, HashMap<String, HMCDeviceProxy>>();
        mAnonymousDevices = new HashMap<String, HMCDeviceProxy>();
        mXMPPConnection = xmppConnection;
        mXMPPChatManager = mXMPPConnection.getChatManager();
        mXMPPChatManager.addChatListener(this);
        mXMPPRoster = mXMPPConnection.getRoster();
        mHMCService = service;
        mHMCDevicesStore = new HMCDevicesStore(this, DEVICES_STORE_PATH);

        mDeviceAdditionListener = new DeviceAditionConfirmationListener(mHMCService);
        mInterconnectionHMCListener = new HMCInterconnectionConfirmationListener(mHMCService);

        Log.d(TAG, "Constructed the HMCManager for " + mXMPPConnection.getUser());
        // set the subscription mode to manually
        mXMPPRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        mXMPPRoster.addRosterListener(mRosterListener);
        mState = STATE_NOT_INITIALIZED;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        if (!createdLocally) {
            HMCDeviceProxy devProxy = new HMCAnonymousDeviceProxy(chat, mXMPPConnection.getUser(),
                                    this);
            devProxy.setLocalImplementation(mLocalImplementation);
            mAnonymousDevices.put(chat.getParticipant(), devProxy);
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
    public void init(String deviceName, String userName, int devType, String hmcName)
            throws RemoteException {
        if (mState == STATE_NOT_INITIALIZED) {
            Collection<RosterEntry> entries = mXMPPRoster.getEntries();
            Log.d(TAG, "We have " + entries.size() + "devices we can connect with");

            // TODO: change the way I initialize the list of devices. Get the
            // list of devices from HMCServer
            for (RosterEntry entry : entries) {
                Log.d(TAG, "Device name " + entry.getName() + ", bareJID:" + entry.getUser());
            }
            mLocalDeviceDescriptor = new DeviceDescriptor();
            mLocalDeviceDescriptor.setDeviceName(deviceName);
            mLocalDeviceDescriptor.setUserName(userName);
            mLocalDeviceDescriptor.setFullJID(mXMPPConnection.getUser());
            // TODO: add a way to set the name nice only if the device is
            // HMCServer
            mHMCName = hmcName;
            mLocalDeviceDescriptor.setFingerprint(HMCOTRManager.getInstance().getLocalFingerprint(
                                    mXMPPConnection.getUser()));

            switch (devType) {
                case HMCDeviceItf.TYPE.HMC_SERVER: {
                    if (mLocalImplementation == null) {
                        mLocalDeviceDescriptor.setDeviceType(HMCDeviceItf.TYPE.HMC_SERVER);
                        mLocalImplementation = new HMCServerImplementation(this,
                                                mLocalDeviceDescriptor);
                        ((HMCServerImplementation) mLocalImplementation)
                                                .registerDeviceAditionConfirmationListener(mInterconnectionHMCListener);
                    }
                }
                break;
                case HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE: {
                    if (mLocalImplementation == null) {
                        mLocalDeviceDescriptor.setDeviceType(HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE);
                        mLocalImplementation = new HMCMediaClientDeviceImplementation(this,
                                                mLocalDeviceDescriptor);
                        ((HMCMediaDeviceImplementation) mLocalImplementation)
                                .registerDeviceAditionConfirmationListener(mDeviceAdditionListener);
                    }
                }
                break;
                default:
                    Log.e(TAG, "Don't have implementation for that specific device: " + devType);
                    break;
            }

            // ask the device store to load the devices
            try {
                mHMCDevicesStore.load();
            } catch (IOException e) {
                Log.e(TAG, "Problem with parsing the input devices file");
                e.printStackTrace();
            }

            mState = STATE_INITIALIZED;
        } else {
            Log.w(TAG, "Already initialized");
        }
    }

    @Override
    public IHMCServerHndl implHMCServer() throws RemoteException {
        if (mLocalImplHandler == null) {
            mLocalImplHandler = new HMCServerHandler((HMCServerImplementation) mLocalImplementation);
        }
        return (IHMCServerHndl) mLocalImplHandler;
    }

    @Override
    public IHMCMediaClientHndl implHMCMediaClient() throws RemoteException {
        if (mLocalImplHandler == null) {
            mLocalImplHandler = new HMCMediaClientHandler(
                                    (HMCMediaClientDeviceImplementation) mLocalImplementation);
        }
        Log.d(TAG, "Return handler [" + mLocalImplHandler + "] for implementation: "
                                + mLocalImplementation);
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
            HMCDeviceProxy dev = mHMCDevicesStore.getLocalDevice(pres.getFrom());
            if (dev != null) {
                try {
                    // let the UI know about the presence change of device, if
                    // the device is a local device and not an anonymous one
                    mHMCDevicesStore.getDevicesListener().onPresenceChanged(
                            pres.getType().toString(), dev.getDeviceDescriptor());
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // try to anonymous device proxies
                dev = mAnonymousDevices.get(pres.getFrom());
            }
            
            if (dev != null) {
                // let the device proxy that the remote changed its presence.
                // Useful to turn of the otr session if the remote went offline
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

        mAnonymousDevices.put(fullJID, retVal);

        return retVal;
    }

    public void deleteAnonymousProxy(String fullJID) {
        HMCAnonymousDeviceProxy dev = (HMCAnonymousDeviceProxy) mAnonymousDevices.get(fullJID);
        if (dev != null) {
            dev.cleanOTRSession();
            mAnonymousDevices.remove(fullJID);
        } else {
            Log.e(TAG, "Asked to remove unknown anonymous proxy:" + fullJID);
        }
    }

    public String getHMCName() {
        return mHMCName;
    }

    public void setHMCName(String name) {
        mHMCName = name;
    }

    public HMCDeviceProxy promoteAnonymousProxy(HMCAnonymousDeviceProxy newDevProxy) {
        // create a specific proxy for the newly added device and add it to the
        // devices list
        HMCDeviceProxy knownDevice = null;
        knownDevice = newDevProxy.promoteToSpecificProxy();
        if (knownDevice != null ) {

            // let know also the rest of devices about this addition
            // local devices
            Iterator<HMCDeviceProxy> localDevicesIter = mHMCDevicesStore.getListOfLocalDevices()
                                    .values().iterator();
            
            while (localDevicesIter.hasNext()) {
                HMCDeviceProxy localDev = localDevicesIter.next();
                
                // TODO: fix this bad approach
                if (localDev.getDeviceDescriptor().getDeviceType() != HMCDeviceItf.TYPE.HMC_SERVER) {
                    HMCMediaDeviceProxy mediaDev = (HMCMediaDeviceProxy) localDev;
                    mediaDev.localDeviceAddedNotification(knownDevice.getDeviceDescriptor());
                }
            }

            // add the new device in local store
            mHMCDevicesStore.addNewLocalDevice(knownDevice);
        } else {
            Log.e(TAG, "Couldn't promote the anonimous device");
        }
        Log.d(TAG, knownDevice.getDeviceDescriptor().getDeviceName() + "("
                                + knownDevice.getDeviceDescriptor().getFullJID()
                                + ") was promoted and added to our list of devices");
        return knownDevice;
    }

    public HMCDeviceProxy createNewDeviceProxy(DeviceDescriptor devDesc) {
        if (devDesc == null) {
            Log.e(TAG, "Cannot create new device proxy with null DeviceDescriptor");
            return null;
        }
        // create the device proxy
        HMCDeviceProxy devProxy = null;
        switch (devDesc.getDeviceType()) {
            case HMCDeviceItf.TYPE.HMC_SERVER:
                devProxy = new HMCServerProxy(mXMPPChatManager,
                        mLocalDeviceDescriptor.getFullJID(), devDesc.getFullJID(), this);
                break;
            case HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE:
                devProxy = new HMCMediaClientDeviceProxy(mXMPPChatManager,
                        mLocalDeviceDescriptor.getFullJID(), devDesc.getFullJID(), this);
                break;
            default:
                devProxy = null;
                break;
        }

        if (devProxy == null) {
            Log.e(TAG, "Unknown type of device");
            return null;
        }

        devProxy.setLocalImplementation(mLocalImplementation);
        devProxy.setDeviceDescriptor(devDesc);

        return devProxy;
    }
    
    public void localDeviceAddedNotification(DeviceDescriptor devDesc) {
        mHMCDevicesStore.addNewLocalDevice(devDesc);
    }

    @Override
    public void registerDevicesListener(IHMCDevicesListener listener) throws RemoteException {
        mHMCDevicesStore.registerDevicesListener(listener);
    }

    @Override
    public void unregisterDevicesListener(IHMCDevicesListener listener) throws RemoteException {
        mHMCDevicesStore.unregisterDevicesListener(listener);
    }


    @Override
    // TODO: change this to return list of descriptors ..
    public Map getListOfLocalDevices() throws RemoteException {
        HashMap<String, String> retVal = new HashMap<String, String>();

        Iterator<HMCDeviceProxy> iter = mHMCDevicesStore.getListOfLocalDevices().values()
                .iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            retVal.put(devPrx.getDeviceDescriptor().getFullJID(), devPrx.getDeviceDescriptor()
                                    .getDeviceName());
        }
        return (Map) retVal;
    }

    public HashMap<String, DeviceDescriptor> getListOfLocalDevicesDescriptors() {
        HashMap<String, DeviceDescriptor> retVal = mHMCDevicesStore
                .getListOfLocalDevicesDescriptors();
        return retVal;
    }

    public void deInit() {
        Log.d(TAG, "Deinitializing the HMCManager");
        mXMPPRoster.removeRosterListener(mRosterListener);
        // close OTR session with local devices
        Iterator<HMCDeviceProxy> iter = mHMCDevicesStore.getListOfLocalDevices().values()
                .iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            devPrx.cleanOTRSession();
        }

        iter = mAnonymousDevices.values().iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            devPrx.cleanOTRSession();
        }
    }

    @Override
    public IDeviceDescriptor getLocalDevDescriptor() throws RemoteException {
        return mLocalDeviceDescriptor;
    }

    // TODO: merge this one with the one above
    public DeviceDescriptor getLocalDevDesc() {
        return mLocalDeviceDescriptor;
    }

    @Override
    public void setUserReplyDeviceAddition(boolean val) throws RemoteException {
        mDeviceAdditionListener.setUserReply(val);
    }

    @Override
    public void setUserReplyHMCInterconnection(boolean val) throws RemoteException {
        mInterconnectionHMCListener.setUserReply(val);
    }

    public void updateListOfLocalDevices(HMCDevicesList devList) {
        if (devList == null) {
            Log.e(TAG, "Received corrupted list of devices");
        }

        mHMCDevicesStore.setLocalDevicesList(devList);
    }

    @Override
    public Map getListOfExternalDevices() throws RemoteException {
        HashMap<String, String> retVal = new HashMap<String, String>();

        Iterator<HMCDeviceProxy> iter = mHMCDevicesStore.getListOfExternalDevices().values()
                                .iterator();
        while (iter.hasNext()) {
            HMCDeviceProxy devPrx = iter.next();
            retVal.put(devPrx.getDeviceDescriptor().getFullJID(), devPrx.getDeviceDescriptor()
                                    .getDeviceName());
        }
        return (Map) retVal;
    }

    public void updateListOfExternalDevices(HMCDevicesList devList) {
        if (devList == null) {
            Log.e(TAG, "Received corrupted list of devices");
        }
        mHMCDevicesStore.setExternalDevicesList(devList);
    }
}
