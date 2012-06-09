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
import com.hmc.project.hmc.devices.implementations.HMCMediaServiceDeviceImplementation;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaClientDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaServiceDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCServerProxy;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.HMCOTRManager;
import com.hmc.project.hmc.security.HMCSecurityPolicy;
import com.hmc.project.hmc.ui.mediadevice.ConfirmJoinHMC;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCManager.
 */
public class HMCManager extends IHMCManager.Stub implements ChatManagerListener,
                        HMCFingerprintsVerifier {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCManager";

    /** The Constant STATE_NOT_INITIALIZED. */
    private static final int STATE_NOT_INITIALIZED = 0;
    
    /** The Constant STATE_INITIALIZED. */
    private static final int STATE_INITIALIZED = 1;

    /** The Constant DEVICES_STORE_PATH. */
    private static final String DEVICES_STORE_PATH = "/sdcard/HMCDeviceStore.dat";

    /** The m anonymous devices. */
    private HashMap<String, HMCDeviceProxy> mAnonymousDevices;

    /** The m xmpp connection. */
    Connection mXMPPConnection;
    
    /** The m xmpp roster. */
    private Roster mXMPPRoster;
    
    /** The m xmpp chat manager. */
    private ChatManager mXMPPChatManager;
    
    /** The m state. */
    private int mState;
    
    /** The m roster listener. */
    private RosterListener mRosterListener = new HMCRosterListener();
    
    /** The m local implementation. */
    private HMCDeviceImplementation mLocalImplementation;
    
    /** The m local impl handler. */
    private Object mLocalImplHandler = null;
    
    /** The m local device descriptor. */
    private DeviceDescriptor mLocalDeviceDescriptor;
    
    /** The m hmc name. */
    private String mHMCName;
    
    /** The m hmc service. */
    private HMCService mHMCService;
    
    /** The m device addition listener. */
    private DeviceAditionConfirmationListener mDeviceAdditionListener;
    
    /** The m interconnection hmc listener. */
    private HMCInterconnectionConfirmationListener mInterconnectionHMCListener;
    
    /** The m hmc devices store. */
    private HMCDevicesStore mHMCDevicesStore;

    /** The m temp devices list. */
    private HMCDevicesList mTempDevicesList;

    /** The m temp device desc. */
    private HMCDeviceProxy mTempDeviceDesc;

    /**
     * Instantiates a new hMC manager.
     *
     * @param xmppConnection the xmpp connection
     * @param service the service
     */
    public HMCManager(Connection xmppConnection, HMCService service) {
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

    /* (non-Javadoc)
     * @see org.jivesoftware.smack.ChatManagerListener#chatCreated(org.jivesoftware.smack.Chat, boolean)
     */
    @Override
    public synchronized void chatCreated(Chat chat, boolean createdLocally) {
        if (!createdLocally) {
            HMCDeviceProxy devProxy = new HMCAnonymousDeviceProxy(chat, mXMPPConnection.getUser(),
                                    this);
            devProxy.setLocalImplementation(mLocalImplementation);
            mAnonymousDevices.put(chat.getParticipant(), devProxy);
        }
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.security.HMCFingerprintsVerifier#authenticateDevice(com.hmc.project.hmc.devices.implementations.DeviceDescriptor)
     */
    @Override
    public synchronized boolean authenticateDevice(DeviceDescriptor remoteDevice) {
        switch (HMCSecurityPolicy.getInstance().getHMCSecurityPolicy()) {
            case HMCSecurityPolicy.TRUST_EVERYBODY_ALWAYS:
                return true;
            case HMCSecurityPolicy.TRUST_HMC_ALWAYS: {
                // search in the list of local or remote devices and see whether
                // the fingerprints match
                HashMap<String, DeviceDescriptor> localDevs = getListOfLocalDevicesDescriptors();
                DeviceDescriptor dev = localDevs.get(remoteDevice.getFullJID());
                
                // try the external list if the dev is not in the local list
                if (dev == null) {
                    HashMap<String, DeviceDescriptor> externalDevs = getListOfExternalDevicesDescriptors();
                    dev = externalDevs.get(remoteDevice.getFullJID());
                }

                if (dev != null && dev.getFullJID().equals(remoteDevice.getFullJID()) && 
                                   dev.getFingerprint().equals(remoteDevice.getFingerprint())) {
                    Log.d(TAG, "Successfuly authenticated device: " + remoteDevice);
                    return true;
                } else {
                    Log.e(TAG, "remote dev not authenticated: " + remoteDevice + "  vs:  " + dev);
                }
                return false;
            }
            // TODO: implement the below policies
            case HMCSecurityPolicy.TRUST_HMC_VALID:
            case HMCSecurityPolicy.TRUST_HMC_SERVER:
                return false;
            default:
                return false;
        }
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#init(java.lang.String, java.lang.String, int, java.lang.String)
     */
    @Override
    public synchronized void init(String deviceName, String userName, int devType, String hmcName)
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
                case HMCDeviceItf.TYPE.HMC_SERVICE_DEVICE: {
                    if (mLocalImplementation == null) {
                        mLocalDeviceDescriptor.setDeviceType(HMCDeviceItf.TYPE.HMC_SERVICE_DEVICE);
                        mLocalImplementation = new HMCMediaServiceDeviceImplementation(this,
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

            HMCSecurityPolicy.getInstance()
                                    .setHMCSecurityPolicy(HMCSecurityPolicy.TRUST_HMC_ALWAYS);

            mState = STATE_INITIALIZED;
        } else {
            Log.w(TAG, "Already initialized");
        }
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#implHMCServer()
     */
    @Override
    public IHMCServerHndl implHMCServer() throws RemoteException {
        if (mLocalImplHandler == null) {
            mLocalImplHandler = new HMCServerHandler((HMCServerImplementation) mLocalImplementation);
        }
        return (IHMCServerHndl) mLocalImplHandler;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#implHMCMediaClient()
     */
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

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#implHMCMediaService()
     */
    @Override
    public IHMCMediaServiceHndl implHMCMediaService() throws RemoteException {
        // TODO implement this. leaving it null for now
        return null;
    }

    /**
     * The listener interface for receiving HMCRoster events.
     * The class that is interested in processing a HMCRoster
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addHMCRosterListener<code> method. When
     * the HMCRoster event occurs, that object's appropriate
     * method is invoked.
     *
     * @see HMCRosterEvent
     */
    class HMCRosterListener implements RosterListener {
        
        /* (non-Javadoc)
         * @see org.jivesoftware.smack.RosterListener#entriesAdded(java.util.Collection)
         */
        @Override
        public void entriesAdded(Collection<String> arg0) {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.RosterListener#entriesDeleted(java.util.Collection)
         */
        @Override
        public void entriesDeleted(Collection<String> arg0) {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.RosterListener#entriesUpdated(java.util.Collection)
         */
        @Override
        public void entriesUpdated(Collection<String> arg0) {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.RosterListener#presenceChanged(org.jivesoftware.smack.packet.Presence)
         */
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

    /**
     * Creates the anonymous proxy.
     *
     * @param fullJID the full jid
     * @return the hMC anonymous device proxy
     */
    public HMCAnonymousDeviceProxy createAnonymousProxy(String fullJID) {
        HMCAnonymousDeviceProxy retVal = null;
        retVal = new HMCAnonymousDeviceProxy(mXMPPChatManager, mXMPPConnection.getUser(), fullJID,
                this);

        mAnonymousDevices.put(fullJID, retVal);

        return retVal;
    }

    /**
     * Delete anonymous proxy.
     *
     * @param fullJID the full jid
     */
    public void deleteAnonymousProxy(String fullJID) {
        HMCAnonymousDeviceProxy dev = (HMCAnonymousDeviceProxy) mAnonymousDevices.get(fullJID);
        if (dev != null) {
            dev.cleanOTRSession();
            mAnonymousDevices.remove(fullJID);
        } else {
            Log.e(TAG, "Asked to remove unknown anonymous proxy:" + fullJID);
        }
    }

    /**
     * Gets the hMC name.
     *
     * @return the hMC name
     */
    public String getHMCName() {
        return mHMCName;
    }

    /**
     * Sets the hMC name.
     *
     * @param name the new hMC name
     */
    public void setHMCName(String name) {
        mHMCName = name;
    }

    /**
     * Promote anonymous proxy to local.
     *
     * @param newDevProxy the new dev proxy
     * @param notifyRestOfDevices the notify rest of devices
     * @return the hMC device proxy
     */
    public synchronized HMCDeviceProxy promoteAnonymousProxyToLocal(
                            HMCAnonymousDeviceProxy newDevProxy, boolean notifyRestOfDevices) {
        // create a specific proxy for the newly added device and add it to the
        // devices list
        HMCDeviceProxy knownDevice = null;
        knownDevice = newDevProxy.promoteToSpecificProxy();
        if (knownDevice != null ) {
            // add the new device in local store
            mHMCDevicesStore.addNewLocalDevice(knownDevice);
            mTempDeviceDesc = knownDevice;

            if (notifyRestOfDevices) {
                // TODO: use a more elegant way to run this in a separate thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // notify the local devices about the addition
                        Iterator<HMCDeviceProxy> devicesIter = mHMCDevicesStore
                                                .getListOfLocalDevices().values().iterator();

                        while (devicesIter.hasNext()) {
                            HMCDeviceProxy localDev = devicesIter.next();

                            // TODO: fix this bad approach
                            if (localDev.getDeviceDescriptor().getDeviceType() != HMCDeviceItf.TYPE.HMC_SERVER) {
                                HMCMediaDeviceProxy mediaDev = (HMCMediaDeviceProxy) localDev;
                                mediaDev.localDeviceAddedNotification(mTempDeviceDesc
                                                        .getDeviceDescriptor());
                            }
                        }

                        // notify the external devices as well
                        HashMap<String, HMCDeviceProxy> extDevLit = mHMCDevicesStore
                                                .getListOfExternalDevices();
                        if (extDevLit != null) {
                            devicesIter = extDevLit.values().iterator();

                            while (devicesIter.hasNext()) {
                                HMCDeviceProxy localDev = devicesIter.next();

                                // TODO: fix this bad approach
                                if (localDev.getDeviceDescriptor().getDeviceType() != HMCDeviceItf.TYPE.HMC_SERVER) {
                                    HMCMediaDeviceProxy mediaDev = (HMCMediaDeviceProxy) localDev;
                                    mediaDev.externalDeviceAddedNotification(
                                                            mTempDeviceDesc.getDeviceDescriptor(),
                                                            mHMCName);
                                }
                            }
                        } else {
                            Log.w(TAG, "No external devices to notify about this addition");
                        }

                    }
                }).start();
            }
        } else {
            Log.e(TAG, "Couldn't promote the anonymous device");
        }
        Log.d(TAG, knownDevice.getDeviceDescriptor().getDeviceName() + "("
                                + knownDevice.getDeviceDescriptor().getFullJID()
                                + ") was promoted and added to our list of devices");
        return knownDevice;
    }

    /**
     * Creates the new device proxy.
     *
     * @param devDesc the dev desc
     * @return the hMC device proxy
     */
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
            case HMCDeviceItf.TYPE.HMC_SERVICE_DEVICE:
                devProxy = new HMCMediaServiceDeviceProxy(mXMPPChatManager,
                                        mLocalDeviceDescriptor.getFullJID(), devDesc.getFullJID(),
                                        this);
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
    
    /**
     * Local device added notification.
     *
     * @param devDesc the dev desc
     */
    public void localDeviceAddedNotification(DeviceDescriptor devDesc) {
        mHMCDevicesStore.addNewLocalDevice(devDesc);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#registerDevicesListener(com.hmc.project.hmc.aidl.IHMCDevicesListener)
     */
    @Override
    public void registerDevicesListener(IHMCDevicesListener listener) throws RemoteException {
        mHMCDevicesStore.registerDevicesListener(listener);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#unregisterDevicesListener(com.hmc.project.hmc.aidl.IHMCDevicesListener)
     */
    @Override
    public void unregisterDevicesListener(IHMCDevicesListener listener) throws RemoteException {
        mHMCDevicesStore.unregisterDevicesListener(listener);
    }


    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#getListOfLocalDevices()
     */
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

    /**
     * Gets the list of local devices descriptors.
     *
     * @return the list of local devices descriptors
     */
    public HashMap<String, DeviceDescriptor> getListOfLocalDevicesDescriptors() {
        HashMap<String, DeviceDescriptor> retVal = mHMCDevicesStore
                .getListOfLocalDevicesDescriptors();
        return retVal;
    }

    public HashMap<String, DeviceDescriptor> getListOfExternalDevicesDescriptors() {
        HashMap<String, DeviceDescriptor> retVal = mHMCDevicesStore
                .getListOfExternalDevicesDescriptors("fix-me");
        return retVal;
    }
    /**
     * De init.
     */
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

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#getLocalDevDescriptor()
     */
    @Override
    public IDeviceDescriptor getLocalDevDescriptor() throws RemoteException {
        return mLocalDeviceDescriptor;
    }

    // TODO: merge this one with the one above
    /**
     * Gets the local dev desc.
     *
     * @return the local dev desc
     */
    public DeviceDescriptor getLocalDevDesc() {
        return mLocalDeviceDescriptor;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#setUserReplyDeviceAddition(boolean)
     */
    @Override
    public void setUserReplyDeviceAddition(boolean val) throws RemoteException {
        mDeviceAdditionListener.setUserReply(val);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#setUserReplyHMCInterconnection(boolean)
     */
    @Override
    public void setUserReplyHMCInterconnection(boolean val) throws RemoteException {
        mInterconnectionHMCListener.setUserReply(val);
    }

    /**
     * Update list of local devices.
     *
     * @param devList the dev list
     */
    public void updateListOfLocalDevices(HMCDevicesList devList) {
        if (devList == null) {
            Log.e(TAG, "Received corrupted list of devices");
        }

        mHMCDevicesStore.setLocalDevicesList(devList);
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCManager#getListOfExternalDevices()
     */
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

    /**
     * Update list of external devices.
     *
     * @param devList the dev list
     * @param updateRestOfDevices the update rest of devices
     */
    public void updateListOfExternalDevices(HMCDevicesList devList, boolean updateRestOfDevices) {
        if (devList == null) {
            Log.e(TAG, "Received corrupted list of devices");
            return;
        } else {
            Log.i(TAG, "__________________________________Received list of external devices: "
                                    + devList.toXMLString());
        }
        mHMCDevicesStore.setExternalDevicesList(devList);

        if (updateRestOfDevices) {
            // TODO: maybe use a more elegant way of running it in a separate
            // thread
            mTempDevicesList = devList;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // notify our local devices about the new list of external
                    // devices
                    Iterator<HMCDeviceProxy> devicesIter = mHMCDevicesStore.getListOfLocalDevices()
                                            .values().iterator();
                    while (devicesIter.hasNext()) {
                        HMCDeviceProxy localDev = devicesIter.next();

                        // TODO: fix this bad approach
                        if (localDev.getDeviceDescriptor().getDeviceType() != HMCDeviceItf.TYPE.HMC_SERVER) {
                            HMCMediaDeviceProxy mediaDev = (HMCMediaDeviceProxy) localDev;
                            mediaDev.setExternalDevicesList(mTempDevicesList);
                        }
                    }
                    Log.i(TAG, "Local devices updated about the new list of external devices");
                }
            }).start();
        }
    }

    /**
     * External device added notification.
     *
     * @param newDev the new dev
     */
    public void externalDeviceAddedNotification(DeviceDescriptor newDev) {
        mHMCDevicesStore.addNewExternalDevice("fix-me", newDev);
    }

    /**
     * Promote anonymous proxy to external.
     *
     * @param newDevProxy the new dev proxy
     * @param hmcName the hmc name
     * @param notifyRestOfDevices the notify rest of devices
     * @return the hMC device proxy
     */
    public HMCDeviceProxy promoteAnonymousProxyToExternal(HMCAnonymousDeviceProxy newDevProxy,
                            String hmcName, boolean notifyRestOfDevices) {
        // create a specific proxy for the newly added device and add it to the
        // devices list
        HMCDeviceProxy knownDevice = null;
        Log.d(TAG, "Try to promote anonymous proxy: " + newDevProxy.getDeviceDescriptor()
                                + "to a known device");
        knownDevice = newDevProxy.promoteToSpecificProxy();
        if (knownDevice != null) {
            // add the new device in local store
            mHMCDevicesStore.addNewExternalDevice(hmcName, knownDevice);

            // TODO: make sure we still need the bellow code (the local devices
            // will be notified anyway about the interconnection when they will
            // receive the complete list of devices in the external HMC
            if (notifyRestOfDevices) {
                // notify the local devices about the interconnection
                Iterator<HMCDeviceProxy> devicesIter = mHMCDevicesStore.getListOfLocalDevices()
                                        .values().iterator();
                while (devicesIter.hasNext()) {
                    HMCDeviceProxy localDev = devicesIter.next();

                    // TODO: fix this bad approach
                    if (localDev.getDeviceDescriptor().getDeviceType() != HMCDeviceItf.TYPE.HMC_SERVER) {
                        HMCMediaDeviceProxy mediaDev = (HMCMediaDeviceProxy) localDev;
                        mediaDev.externalDeviceAddedNotification(knownDevice.getDeviceDescriptor(),
                                                hmcName);
                    }
                }
            }
        } else {
            Log.e(TAG, "Couldn't promote the anonymous device");
        }
        Log.d(TAG, knownDevice.getDeviceDescriptor().getDeviceName() + "("
                                + knownDevice.getDeviceDescriptor().getFullJID()
                                + ") was promoted and added to our list of devices");
        return knownDevice;
    }
}
