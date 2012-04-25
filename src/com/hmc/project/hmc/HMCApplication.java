/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc;

import android.app.Application;
import android.content.SharedPreferences;
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

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mUsername = mSettings.getString("hmc_jid_key", "");
        mPassword = mSettings.getString("hmc_pass_key", "");
        mDeviceName = mSettings.getString("hmc_devname_key", "");

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
        return mIsConfigured;
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

    private class HMCPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        public HMCPreferenceListener() {
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key) {
            mUsername = mSettings.getString("hmc_jid_key", "");
            mPassword = mSettings.getString("hmc_pass_key", "");
            mDeviceName = mSettings.getString("hmc_device_name", "");

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
            Log.d(TAG, "------------Configuration: " + mUsername + " " + mPassword.length() + " "
                                    + mDeviceName + " " + mDeviceType);

        }
    }
}
