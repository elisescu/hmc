/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import java.util.HashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.os.RemoteException;

import com.hmc.project.hmc.aidl.IHMCDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCServerProxy;

/**
 * @author elisescu
 *
 */
public class HMCManager extends IHMCManager.Stub 
implements ChatManagerListener, MessageListener, HMCFingerprintsVerifier {
    
    HashMap<String, HMCDeviceProxy> mLocalDevices;
    HMCServerProxy mLocalServer;
    HashMap<String, HashMap<String, HMCDeviceProxy>> mExternalHMCs;
    Connection mXMMPConnection;

    public HMCManager(Connection xmppConnection) {
        mExternalHMCs = new HashMap<String, HashMap<String,HMCDeviceProxy>>();
        mLocalDevices = new HashMap<String, HMCDeviceProxy>();
        mXMMPConnection = xmppConnection;
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
    public void chatCreated(Chat arg0, boolean local) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean verifyFingerprints(String localFingerprint, String remoteFingerprint,
            String remoteName) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
