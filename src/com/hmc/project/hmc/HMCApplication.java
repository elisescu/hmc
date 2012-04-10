/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc;

import com.hmc.project.hmc.utils.HMCUserNotifications;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HMCApplication extends Application {

    private final HMCPreferenceListener mPreferenceListener = new HMCPreferenceListener();
    private SharedPreferences mSettings;
    private boolean mIsAccountConfigured;
    private boolean mIsConnected;


    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String login = mSettings.getString("hmc_username_key", "");
        String password = mSettings.getString("hmc_pass_key", "");
        mIsAccountConfigured = !("".equals(login) || "".equals(password));
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

    private class HMCPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        public HMCPreferenceListener() {
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key) {
            if (key.equals("hmc_username_key") || key.equals("hmc_pass_key")) {
                String login = mSettings.getString("hmc_username_key", "");
                String password = mSettings.getString("hmc_pass_key", "");
                mIsAccountConfigured = !("".equals(login) || "".equals(password));
                //HMCUserNotifications.normalToast(HMCApplication.this, "Account configured:"+mIsAccountConfigured);
            }
        }
    }

}
