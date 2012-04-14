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

import com.hmc.project.hmc.utils.HMCUserNotifications;

public class HMCApplication extends Application {

    private final HMCPreferenceListener mPreferenceListener = new HMCPreferenceListener();
    private SharedPreferences mSettings;
    private boolean mIsAccountConfigured;
    private boolean mIsConnected;
    private String mUsername = "insert_something";
    private String mPassword = "insert_something";

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = mSettings.getString("hmc_username_key", "");
        mPassword = mSettings.getString("hmc_pass_key", "");
        mIsAccountConfigured = !("".equals(mUsername) || "".equals(mPassword));
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

    public boolean isAccountConfigured() {
        return mIsAccountConfigured;
    }
    
    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }
    
    private class HMCPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        public HMCPreferenceListener() {
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key) {
            if (key.equals("hmc_username_key") || key.equals("hmc_pass_key")) {
                mUsername = mSettings.getString("hmc_username_key", "");
                mPassword = mSettings.getString("hmc_pass_key", "");
                mIsAccountConfigured = !("".equals(mUsername) || "".equals(mPassword));
                HMCUserNotifications.normalToast(HMCApplication.this, "Account configured="
                        + mIsAccountConfigured);
            }
        }
    }

}
