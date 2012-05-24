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

public class HMCInterconnectionConfirmationListener {
    private static final int HMC_INTERCONNECTION_CONFIRMATION_ID = 15;
    private static final String TAG = "InterconnectionHMCConfirmationListener";
    private HMCService mHMCService;
    private String mWaitLock = null;
    private Boolean mUserReply = false;

    public HMCInterconnectionConfirmationListener(HMCService serv) {
        mHMCService = serv;
    }

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

    public void setUserReply(boolean val) {
        Log.d(TAG, "Got the user response.. now let the HMC server know");
        mUserReply = val;
        synchronized (mWaitLock) {
            mWaitLock.notify();
        }
    }
}
