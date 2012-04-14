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
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;

import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IHMCDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCServerProxy;
import com.hmc.project.hmc.security.HMCFingerprintsVerifier;
import com.hmc.project.hmc.security.HMCSecurityPolicy;

public class HMCManager extends IHMCManager.Stub 
implements ChatManagerListener, MessageListener, HMCFingerprintsVerifier {
    
    private static final String TAG = "HMCManager";
    HashMap<String, HMCDeviceProxy> mLocalDevices;
    HMCServerProxy mLocalServer;
    HashMap<String, HashMap<String, HMCDeviceProxy>> mExternalHMCs;
    Connection mXMPPConnection;
    private Roster mXMPPRoster;
    private ChatManager mXMPPChatManager;


    public HMCManager(Connection xmppConnection) {
        mExternalHMCs = new HashMap<String, HashMap<String,HMCDeviceProxy>>();
        mLocalDevices = new HashMap<String, HMCDeviceProxy>();
        mXMPPConnection = xmppConnection;
        mXMPPChatManager = mXMPPConnection.getChatManager();
        mXMPPRoster = mXMPPConnection.getRoster();

        // set the subscription mode to manually
        mXMPPRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
    }

    @Override
    public IHMCDeviceDescriptor getDeviceDescriptor(String devJID) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
        
    }

    @Override
    public void processMessage(Chat arg0, Message arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        if (!createdLocally)
            chat.addMessageListener(this);
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
    public void init() throws RemoteException {
        Collection<RosterEntry> entries = mXMPPRoster.getEntries();
        Log.d(TAG, "We have " + entries.size() + "devices we can connect with");

        // TODO: change the way I initialize the list of devices. For now just
        // trust the XMPP server, but later check with HMCServer to see if the
        // list is consistent. However, here we trust the XMPP server only for
        // getting the list of devices, but later on we anyway have to
        // authenticate the device we communicate with, based on the fingerprint
        // we get from it.
        for (RosterEntry entry : entries) {
            Log.d(TAG, "Device name " + entry.getName() + ", bareJID:" + entry.getUser());
            HMCDeviceProxy devProxy = new HMCDeviceProxy(mXMPPChatManager, entry.getUser(), this);
            mLocalDevices.put(entry.getUser(), devProxy);
        }
    }
}
