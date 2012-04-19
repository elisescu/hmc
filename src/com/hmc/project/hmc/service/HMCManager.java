/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import java.util.Collection;
import java.util.HashMap;

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

import com.hmc.project.hmc.aidl.IHMCDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.devices.implementations.HMCDeviceImplementationItf;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
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
    public IHMCDeviceDescriptor getDeviceDescriptor(String devJID) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
        
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        if (!createdLocally) {
            HMCDeviceProxy devProxy = new HMCDeviceProxy(chat, this);
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
    public void init(int locImplementation) throws RemoteException {
        if (mState == STATE_NOT_INITIALIZED) {
            // initialize the implementation for local device
            switch (locImplementation) {
                case HMCDeviceItf.TYPE.HMC_SERVER:
                    mLocalImplementation = new HMCServerImplementation();
                    break;
                case HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE:
                    mLocalImplementation = null;
                    // TODO: add implementation for this case
                    break;
                case HMCDeviceItf.TYPE.HMC_SERVICE_DEVICE:
                    mLocalImplementation = null;
                    // TODO: add implementation for this case
                    break;
                default:
                    mLocalImplementation = null;
                    break;
            }

            Collection<RosterEntry> entries = mXMPPRoster.getEntries();
            Log.d(TAG, "We have " + entries.size() + "devices we can connect with");

            // TODO: change the way I initialize the list of devices. For now
            // trust the XMPP server, but later check with HMCServer to see if
            // the list is consistent. However, here we trust the XMPP server
            // only for getting the list of devices, but later on we anyway have
            // to authenticate the device we communicate with, based on the
            // fingerprint we get from it.
            for (RosterEntry entry : entries) {
                Log.d(TAG, "Device name " + entry.getName() + ", bareJID:" + entry.getUser());
                HMCDeviceProxy devProxy = new HMCDeviceProxy(mXMPPChatManager, entry.getUser(),
                                        this);
                mLocalDevices.put(entry.getUser(), devProxy);
            }

            mState = STATE_INITIALIZED;
        } else {
            Log.w(TAG, "Already initialized");
        }
        Log.d(TAG, "Now we have " + mLocalDevices.size() + " deviceeeeeeessss");
    }

    @Override
    public int testRPC(String JID, int val) throws RemoteException {
        if (mState == STATE_INITIALIZED) {
            HMCDeviceProxy dev = mLocalDevices.get(JID);
            if (dev != null) {
                dev.remoteIncrement(val);
            } else {
                Log.e(TAG, "Device " + JID + "is not in our list of devices");
            }
        }
        return 0;
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
            HMCDeviceProxy dev = mLocalDevices.get(StringUtils.parseBareAddress(pres.getFrom()));
            if (dev != null) {
                dev.presenceChanged(pres);
            } else {
                Log.e(TAG, "Received presence information from unknown device: " + pres.getFrom());
            }

        }

    }

}
