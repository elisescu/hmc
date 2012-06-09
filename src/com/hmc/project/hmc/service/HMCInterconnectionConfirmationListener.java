/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.devices.implementations.DeviceDescriptor;
import com.hmc.project.hmc.ui.hmcserver.ConfirmHMCInterconnection;
import com.hmc.project.hmc.ui.mediadevice.ConfirmJoinHMC;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving HMCInterconnectionConfirmation events.
 * The class that is interested in processing a HMCInterconnectionConfirmation
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addHMCInterconnectionConfirmationListener<code> method. When
 * the HMCInterconnectionConfirmation event occurs, that object's appropriate
 * method is invoked.
 *
 * @see HMCInterconnectionConfirmationEvent
 */
public class HMCInterconnectionConfirmationListener {
    
    /** The Constant HMC_INTERCONNECTION_CONFIRMATION_ID. */
    private static final int HMC_INTERCONNECTION_CONFIRMATION_ID = 15;
    
    /** The Constant TAG. */
    private static final String TAG = "InterconnectionHMCConfirmationListener";
    
    /** The m hmc service. */
    private HMCService mHMCService;
    
    /** The m wait lock. */
    private String mWaitLock = null;
    
    /** The m user reply. */
    private Boolean mUserReply = false;

    /**
     * Instantiates a new hMC interconnection confirmation listener.
     *
     * @param serv the serv
     */
    public HMCInterconnectionConfirmationListener(HMCService serv) {
        mHMCService = serv;
    }

    /**
     * Confirm hmc interconnection.
     *
     * @param newDevice the new device
     * @param hmcName the hmc name
     * @param myFingerprint the my fingerprint
     * @return true, if successful
     */
    public boolean confirmHMCInterconnection(DeviceDescriptor newDevice, String hmcName,
                            String myFingerprint) {
        boolean retVal = false;
        Notification notification = new Notification(android.R.drawable.stat_notify_more,
                                "Interconnect to external HMC", System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(mHMCService, ConfirmHMCInterconnection.class);

        // TODO: maybe used constants for keys used to put info on intent
        intent.putExtra("hmc_name", hmcName);
        intent.putExtra("hmc_srv_name", newDevice.getDeviceName());
        intent.putExtra("hmc_srv_fingerprint", newDevice.getFingerprint());
        intent.putExtra("my_fingerprint", myFingerprint);

        // TODO: improve the way of setting the info text
        CharSequence contentTitle = "Request to interconnect to external HMC";
        CharSequence contentText = "Interconnect to \"" + hmcName + "\"";

        notification.setLatestEventInfo(mHMCService, contentTitle, contentText, PendingIntent
                                .getActivity(mHMCService, 0, intent, PendingIntent.FLAG_ONE_SHOT));

        mHMCService.sendNotification(HMC_INTERCONNECTION_CONFIRMATION_ID, notification);

        // now wait for the user to press one of the buttons in the confirmation
        // activity

        // generate some string from current time to have an object to wait for
        mWaitLock = System.currentTimeMillis() + "";

        try {
            synchronized (mWaitLock) {
                mWaitLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mUserReply;
    }

    /**
     * Sets the user reply.
     *
     * @param val the new user reply
     */
    public void setUserReply(boolean val) {
        Log.d(TAG, "Got the user response.. now let the HMC server know");
        mUserReply = val;

        if (mWaitLock != null) {
            synchronized (mWaitLock) {
                mWaitLock.notify();
            }
        }
    }
}
