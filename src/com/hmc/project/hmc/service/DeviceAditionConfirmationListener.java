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
import com.hmc.project.hmc.ui.mediadevice.ConfirmJoinHMC;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving deviceAditionConfirmation events.
 * The class that is interested in processing a deviceAditionConfirmation
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addDeviceAditionConfirmationListener<code> method. When
 * the deviceAditionConfirmation event occurs, that object's appropriate
 * method is invoked.
 *
 * @see DeviceAditionConfirmationEvent
 */
public class DeviceAditionConfirmationListener {
    
    /** The Constant DEVICE_ADDITION_CONFIRMATION_ID. */
    private static final int DEVICE_ADDITION_CONFIRMATION_ID = 14; // out of the blue
    
    /** The Constant TAG. */
    private static final String TAG = "DeviceAditionConfirmationListener";
    
    /** The m hmc service. */
    private HMCService mHMCService;
    
    /** The m wait lock. */
    private String mWaitLock = null;
    
    /** The m user reply. */
    private Boolean mUserReply = false;

    /**
     * Instantiates a new device adition confirmation listener.
     *
     * @param serv the serv
     */
    public DeviceAditionConfirmationListener(HMCService serv) {
        mHMCService = serv;
    }

    /**
     * Confirm device addition.
     *
     * @param newDevice the new device
     * @param hmcName the hmc name
     * @param myFingerprint the my fingerprint
     * @return true, if successful
     */
    public boolean confirmDeviceAddition(DeviceDescriptor newDevice, String hmcName,
                            String myFingerprint) {
        boolean retVal = false;
        Log.d(TAG, "Notification to be send");
        Notification notification = new Notification(android.R.drawable.stat_notify_more,
                                "Join HMC", System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(mHMCService, ConfirmJoinHMC.class);

        // TODO: maybe used constants for keys used to put info on intent
        intent.putExtra("hmc_name", hmcName);
        intent.putExtra("hmc_srv_name", newDevice.getDeviceName());
        intent.putExtra("hmc_srv_fingerprint", newDevice.getFingerprint());
        intent.putExtra("my_fingerprint", myFingerprint);

        Log.d(TAG, "Sending the fingerprint: " + newDevice.getFingerprint());

        // TODO: improve the way of setting the info text
        CharSequence contentTitle = "Request to join";
        CharSequence contentText = "Join \"" + hmcName + "\"";

        notification.setLatestEventInfo(mHMCService, contentTitle, contentText, PendingIntent
                                .getActivity(mHMCService, 0, intent, PendingIntent.FLAG_ONE_SHOT));

        mHMCService.sendNotification(DEVICE_ADDITION_CONFIRMATION_ID, notification);

        Log.d(TAG, "Notification seeeent ");
        // now wait for the user to press one of the buttons in the confirmation
        // activity
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
        synchronized (mWaitLock) {
            mWaitLock.notify();
        }
    }
}
