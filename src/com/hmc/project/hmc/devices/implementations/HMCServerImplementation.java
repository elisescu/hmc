/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.util.HashMap;
import android.os.RemoteException;
import android.util.Log;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCServerItf;
import com.hmc.project.hmc.devices.proxy.AsyncCommandReplyListener;
import com.hmc.project.hmc.devices.proxy.HMCAnonymousDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCMediaDeviceProxy;
import com.hmc.project.hmc.devices.proxy.HMCServerProxy;
import com.hmc.project.hmc.security.HMCOTRManager;
import com.hmc.project.hmc.service.HMCInterconnectionConfirmationListener;
import com.hmc.project.hmc.service.HMCManager;
import com.hmc.project.hmc.ui.DevicesListAdapter;

public class HMCServerImplementation extends HMCDeviceImplementation implements HMCServerItf {

    private static final String TAG = "HMCServerImplementation";
    private HMCInterconnectionConfirmationListener mHMCInterconnectionConfirmationListener;
    private boolean addingSuccess = true;
    private boolean interconnectionSuccess = true;
    private HMCDevicesList mLocalHMCInfo;
    // TODO: make sure this is enough and we don't need a vector of pending
    // remote info given that we anyway can interconnect with a single external
    // HMC at a time
    private HMCDevicesList mPendingHMCInfo = null;

    public HMCServerImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        super(hmcManager, thisDeviceDesc);

        mLocalHMCInfo = new HMCDevicesList(mHMCManager.getHMCName(), false);
        mLocalHMCInfo.addDevice(mDeviceDescriptor);
    }

    public boolean interconnectTo(String externalHMCServerAddress) {
        Log.d(TAG, "Going to intercoonecto to " + externalHMCServerAddress);
        boolean userConfirmation = false;
        // TODO: for now use HMCDevicesList to exchange information about the
        // two HMCs so the user can accept or reject the interconnection. Later
        // change this and make it more nice
        HMCDevicesList remoteHMCInfo = null;
        DeviceDescriptor remoteHMCServerDesc = null;
        String remoteHMCName = "";

        HMCAnonymousDeviceProxy newDevProxy = mHMCManager
                                .createAnonymousProxy(externalHMCServerAddress);
        newDevProxy.setLocalImplementation(this);
        // send a hello message to remote anonymous device to negotiate OTR and
        // get information about device which will be approved by the user
        remoteHMCInfo = newDevProxy.exchangeHMCInfo(mLocalHMCInfo);
        remoteHMCName = remoteHMCInfo.getHMCName();
        // we should have only one device in this "pseudo-list". this will be
        // changed in future according with the previous TODO
        remoteHMCServerDesc = remoteHMCInfo.getIterator().next();

        // check the descriptor received
        if (remoteHMCServerDesc == null
                                || remoteHMCServerDesc.getDeviceType() != HMCDeviceItf.TYPE.HMC_SERVER) {
            mHMCManager.deleteAnonymousProxy(externalHMCServerAddress);
            return false;
        }

        String remoteRealFingerprint = HMCOTRManager.getInstance().getRemoteFingerprint(
                                externalHMCServerAddress);
        if (!remoteRealFingerprint.equals(remoteHMCServerDesc.getFingerprint())) {
            Log.e(TAG, "The fingerprint received from remote "
                       + remoteHMCServerDesc.getFingerprint()
                       + " doesn't match the one he uses("
                       + remoteRealFingerprint + ")");
            mHMCManager.deleteAnonymousProxy(externalHMCServerAddress);
            return false;
        }

        newDevProxy.setDeviceDescriptor(remoteHMCServerDesc);

        Log.d(TAG, "Got remote dev desc: " + remoteHMCServerDesc.toString()
                                + "\n Now send the joining request");
        

        // sending the join request
        newDevProxy.interconnectionRequest(mHMCManager.getHMCName(),
                                new AsyncCommandReplyListener() {
            public void onReplyReceived(String reply) {
                interconnectionSuccess = Boolean.parseBoolean(reply);
                Log.d(TAG, "Got the reply from media deviec user:" + interconnectionSuccess);
            }
        });

        // now the user should confirm or deny adding the device with
        // information present in remoteDevDesc data
        try {
            userConfirmation = mUserRequestsListener.confirmHMCInterconnection(remoteHMCServerDesc,
                                    remoteHMCName);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't retrieve the user confirmation");
            e.printStackTrace();
        }
        Log.d(TAG, "The user replied with :" + userConfirmation);

        if (!userConfirmation) {
            // the user didn't confirm the addition of the device
            mHMCManager.deleteAnonymousProxy(externalHMCServerAddress);
            return false;
        }

        // if remote device accepted to join HMC, then send it the list of
        // devices
        // TODO: wait here for the remote reply! use a different variable that I
        // can wait for and the notify on in when the reply arrived
        if (interconnectionSuccess) {
            try {
                // TODO:elis: shouldn't I use true so that I notify the rest of
                // devices as well?
                HMCServerProxy specificDevPrxy = (HMCServerProxy) mHMCManager
                                        .promoteAnonymousProxyToExternal(newDevProxy,
                                                                remoteHMCName, false);
                // now that we have the specific proxy, add it also in our list
                // of devices
                HMCDevicesList devList = specificDevPrxy
                                        .exchangeListsOfLocalDevices(getListOfLocalHMCDevices());

                mHMCManager.updateListOfExternalDevices(devList, true);
            } catch (ClassCastException e) {
                Log.e(TAG, "Fatal error: the remote device we interconnect with "
                                        + "is not a HMC server");
                e.printStackTrace();
                mHMCManager.deleteAnonymousProxy(externalHMCServerAddress);
                return false;
            }
        }

        return addingSuccess;
    }

    public boolean interconnectionRequest(String requesterName, HMCDeviceProxy fromDev) {
        boolean retVal = true;

        // we have received an interconnection request from an external
        // HMCServer so we send a notification to the user to confirm using the
        // HMCInterconnectionConfirmationListener
        if (mPendingHMCInfo != null && mHMCInterconnectionConfirmationListener != null) {
            // TODO: improve this bad practice to send the "iterator.next()" as
            // parameter
            DeviceDescriptor remoteHMCServer = mPendingHMCInfo.getIterator().next();

            // Log.d(TAG, "Ask the user to confirm joining with HMCServer: " +
            // remoteHMCServer);
            retVal = mHMCInterconnectionConfirmationListener.confirmHMCInterconnection(
                                    remoteHMCServer, mPendingHMCInfo.getHMCName(),
                                    mDeviceDescriptor.getFingerprint());

            // if the user accepted addition, then add the remote server to our
            // list of external devices
            if (retVal) {
                try {
                    Log.d(TAG, "The user confirmed addition so add the device to the list");
                    mHMCManager.promoteAnonymousProxyToExternal((HMCAnonymousDeviceProxy) fromDev,
                                            mPendingHMCInfo.getHMCName(), false);
                    Log.d(TAG, "The device was promoted");
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // this shouldn't happen
                    Log.e(TAG, "FATAL: Received interconnection request from non-anonymous device");
                }
            }
        } else {
            Log.e(TAG, "Cannot ask the user for confirmation");
            retVal = false;
        }
        return retVal;
    }

    private HMCDevicesList getListOfLocalHMCDevices() {
        HashMap<String, DeviceDescriptor> ourLocalDevices = mHMCManager
                                .getListOfLocalDevicesDescriptors();
        HMCDevicesList locDevsList = new HMCDevicesList(mHMCManager.getHMCName(), true,
                                ourLocalDevices);

        return locDevsList;
    }

    @Override
    public void getListOfNewHMCDevices(String hashOfMyListOfDevices) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHMCDevice() {
        // TODO Auto-generated method stub

    }

    @Override
    public String localExecute(int opCode, String params, HMCDeviceProxy fromDev) {
        String retVal = null;
        switch (opCode) {
            case CMD_INTERCONNECTION_REQUEST:
                retVal = _interconnectionRequest(params, fromDev);
                break;
            case CMD_EXCHANGE_HMC_INFO:
                retVal = _exchangeHMCInfo(params, fromDev);
                break;
            case CMD_EXCHANGE_LISTS_OF_LOCAL_DEVICES:
                retVal = _exchangeListsOfLocalDevices(params, fromDev);
                break;
            default:
                retVal = super.localExecute(opCode, params, fromDev);
                break;
        }
        return retVal;
    }

    private String _exchangeListsOfLocalDevices(String params, HMCDeviceProxy fromDev) {
        HMCDevicesList devList = HMCDevicesList.fromXMLString(params);
        // update the HMCManager about the new list of devices

        mHMCManager.updateListOfExternalDevices(devList, true);

        // now we have to send the list back as well
        HashMap<String, DeviceDescriptor> list = mHMCManager.getListOfLocalDevicesDescriptors();
        devList = new HMCDevicesList(mHMCManager.getHMCName(), false, list);

        return devList.toXMLString();
    }

    private String _exchangeHMCInfo(String params, HMCDeviceProxy fromDev) {
        HMCDevicesList localList = exchangeHMCInfo(HMCDevicesList.fromXMLString(params), fromDev);
        return localList.toXMLString();
    }

    private HMCDevicesList exchangeHMCInfo(HMCDevicesList remoteHMCInfo, HMCDeviceProxy fromDev) {
        mPendingHMCInfo = remoteHMCInfo;

        fromDev.setDeviceDescriptor(remoteHMCInfo.getIterator().next());

        return mLocalHMCInfo;
    }

    private String _interconnectionRequest(String params, HMCDeviceProxy fromDev) {
        return interconnectionRequest(params, fromDev) + "";
    }

    public boolean addNewDevice(String fullJID) {
        Log.d(TAG, "Have to add new device: !!" + fullJID);
        boolean userConfirmation = false;
        DeviceDescriptor remoteDevDesc = null;
        HMCAnonymousDeviceProxy newDevProxy = mHMCManager.createAnonymousProxy(fullJID);
        newDevProxy.setLocalImplementation(this);
        // send a hello message to remote anonymous device to negotiate OTR and
        // get information about device which will be approved by the user
        remoteDevDesc = newDevProxy.hello(mDeviceDescriptor);
        // check the descriptor received
        if (remoteDevDesc == null) {
            mHMCManager.deleteAnonymousProxy(fullJID);
            return false;
        }
        String remoteRealFingerprint = HMCOTRManager.getInstance().getRemoteFingerprint(fullJID);
        if (!remoteRealFingerprint.equals(remoteDevDesc.getFingerprint())) {
            Log.e(TAG, "The fingerprint received from remote " + remoteDevDesc.getFingerprint()
                                    + " doesn't match the one he uses(" + remoteRealFingerprint
                                    + ")");
            mHMCManager.deleteAnonymousProxy(fullJID);
            return false;
        }
        
        newDevProxy.setDeviceDescriptor(remoteDevDesc);

        Log.d(TAG, "Got remote dev desc: " + remoteDevDesc.toString()
                                + "\n Now send the joining request");
        // sending the join request
        newDevProxy.joinHMC(mHMCManager.getHMCName(), new AsyncCommandReplyListener() {
            public void onReplyReceived(String reply) {
                addingSuccess = Boolean.parseBoolean(reply);
                Log.d(TAG, "Got the reply from media deviec user:" + addingSuccess);
            }
        });

        // now the user should confirm or deny adding the device with
        // information present in remoteDevDesc data
        try {
            userConfirmation = mUserRequestsListener.confirmDeviceAddition(remoteDevDesc);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't retrieve the user confirmation");
            e.printStackTrace();
        }
        Log.d(TAG, "The user replied with :" + userConfirmation);

        if (!userConfirmation) {
            // the user didn't confirm the addition of the device
            mHMCManager.deleteAnonymousProxy(fullJID);
            return false;
        }

        // if remote device accepted to join HMC, then send it the list of
        // devices
        if (addingSuccess) {
            // TODO: maybe do all this in a separate thread
            HMCMediaDeviceProxy specificDevPrxy = (HMCMediaDeviceProxy)mHMCManager
                                    .promoteAnonymousProxyToLocal(newDevProxy, true);
            // now that we have the specific proxy, added also in our list of
            // devices
            specificDevPrxy.sendListOfDevices(getListOfLocalHMCDevices());
        }

        return addingSuccess;
    }


    public void registerDeviceAditionConfirmationListener(
                            HMCInterconnectionConfirmationListener listener) {
        mHMCInterconnectionConfirmationListener = listener;

    }
}
