/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hmc.project.hmc.utils.HMCUserNotifications;

public class HMCApplication extends Application {

    private static final String TAG = "HMCApplication";
    private final HMCPreferenceListener mPreferenceListener = new HMCPreferenceListener();
    private SharedPreferences mSettings;
    private boolean mIsConfigured = false;
    private boolean mIsConnected;
    private String mUsername = "insert_something";
    private String mPassword = "insert_something";
    private int mDeviceType = -1;
    private String mDeviceName = "no name";
    private String mHMCName = "no name";
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;


    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mUsername = mSettings.getString("hmc_jid_key", "");
        mPassword = mSettings.getString("hmc_pass_key", "");
        mDeviceName = mSettings.getString("hmc_devname_key", "");
        mHMCName = mSettings.getString("hmc_hmcname_key", "");

        try {
            mDeviceType = Integer.parseInt(mSettings.getString("hmc_device_type", "-1"));
        } catch (NumberFormatException e) {
            mDeviceType = -1;
        }

        mIsConfigured = !("".equals(mUsername) || "".equals(mPassword) || mDeviceType == -1);
        Log.d(TAG, "------------Configuration: " + mUsername + " " + mPassword.length() + " "
                                + mDeviceName
                                + " " + mDeviceType);
        mSettings.registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mSettings.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setConnected(boolean isConnected) {
        mIsConnected = isConnected;
    }

    public boolean isConfigured() {
        // check whether we have access rights to write/read from SD card
        return mIsConfigured && externalStorageReady();
    }

    boolean externalStorageReady() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if (!(mExternalStorageAvailable && mExternalStorageWriteable)) {
            Log.e(TAG, "Fatal error: we cannot use the external storage!");
            HMCUserNotifications.normalToast(HMCApplication.this,
                    "Fatal error: the device has no writable external storage");
        }
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getDeviceType() {
        return mDeviceType;
    }
    
    public String getDeviceName() {
        return mDeviceName;
    }

    public String getHMCName() {
        return mHMCName;
    }

    private class HMCPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        public HMCPreferenceListener() {
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key) {
            mUsername = mSettings.getString("hmc_jid_key", "");
            mPassword = mSettings.getString("hmc_pass_key", "");
            mDeviceName = mSettings.getString("hmc_devname_key", "");
            mHMCName = mSettings.getString("hmc_hmcname_key", "");

            try {
                mDeviceType = Integer.parseInt(mSettings.getString("hmc_device_type", "-1"));
            } catch (NumberFormatException e) {
                mDeviceType = -1;
            }
            mIsConfigured = !("".equals(mUsername) || "".equals(mPassword) || mDeviceType == -1);

            if (mIsConfigured) {
                HMCUserNotifications.normalToast(HMCApplication.this,
                        "Application was correctly configured");
            } else {
                HMCUserNotifications.normalToast(HMCApplication.this,
                                        "Application was not configured:" + mUsername
                                                                + mPassword.length() + mDeviceName
                                                                + mDeviceType);
            }

            Log.d(TAG, "------------Configuration:" +
            		"\n username: " + mUsername +
            		"\n password.length " + mPassword.length() +
            		"\n device type:" + mDeviceType +
            		"\n device name: " + mDeviceName);
        }
    }
}
