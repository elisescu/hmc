/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hmc.project.hmc.utils.HMCUserNotifications;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCApplication.
 */
public class HMCApplication extends Application {

    /** The Constant TAG. */
    private static final String TAG = "HMCApplication";

    public static String HMCAPP_SERVER_ADDRESS_KEY = "hmc_stream_server_addr";

    /** The m preference listener. */
    private final HMCPreferenceListener mPreferenceListener = new HMCPreferenceListener();

    /** The m settings. */
    private SharedPreferences mSettings;

    /** The m is configured. */
    private boolean mIsConfigured = false;

    /** The m is connected. */
    private boolean mIsConnected;

    /** The m username. */
    private String mUsername = "insert_something";

    /** The m password. */
    private String mPassword = "insert_something";

    /** The m device type. */
    private int mDeviceType = -1;

    /** The m device name. */
    private String mDeviceName = "no name";

    /** The m hmc name. */
    private String mHMCName = "no name";

    /** The m external storage available. */
    boolean mExternalStorageAvailable = false;

    private String mStreamingServerAddress;

    /** The m external storage writeable. */
    boolean mExternalStorageWriteable = false;

    public static String HMCAPP_FULLJID_KEY;

    public static String HMCAPP_PASSWORD_KEY;

    /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        HMCAPP_SERVER_ADDRESS_KEY = "hmc_srvaddr_key";
        HMCAPP_FULLJID_KEY = "hmc_jid_key";
        HMCAPP_PASSWORD_KEY = "hmc_pass_key";

        mUsername = mSettings.getString(HMCAPP_FULLJID_KEY, "");
        mPassword = mSettings.getString(HMCAPP_PASSWORD_KEY, "");
        mDeviceName = mSettings.getString("hmc_devname_key", "");
        mHMCName = mSettings.getString("hmc_hmcname_key", "");
        mStreamingServerAddress = mSettings.getString(HMCAPP_SERVER_ADDRESS_KEY, "127.0.0.1");

        try {
            mDeviceType = Integer.parseInt(mSettings.getString("hmc_device_type", "-1"));
        } catch (NumberFormatException e) {
            mDeviceType = -1;
        }

        String resource;
        resource = StringUtils.parseResource(mUsername);

        mIsConfigured = !("".equals(resource)) || !("".equals(mUsername) || "".equals(mPassword) ||
                                mDeviceType == -1);

        Log.d(TAG, "------------Configuration: " + mUsername + " " + mPassword.length() + " "
                                + mDeviceName
                                + " " + mDeviceType);

        mSettings.registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    /* (non-Javadoc)
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        mSettings.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    public String getStreamingAddress() {
        return mStreamingServerAddress;
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    public boolean isConnected() {
        return mIsConnected;
    }

    /**
     * Sets the connected.
     *
     * @param isConnected the new connected
     */
    public void setConnected(boolean isConnected) {
        mIsConnected = isConnected;
    }

    /**
     * Checks if is configured.
     *
     * @return true, if is configured
     */
    public boolean isConfigured() {
        // check whether we have access rights to write/read from SD card
        return mIsConfigured && externalStorageReady();
    }

    /**
     * External storage ready.
     *
     * @return true, if successful
     */
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

    public void setUsername(String s) {
        mUsername = s;

        // save to preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(HMCApplication.HMCAPP_FULLJID_KEY, s);
        editor.commit();
    }

    public void setPassword(String s) {
        mPassword= s;

        // save to preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(HMCApplication.HMCAPP_PASSWORD_KEY, s);
        editor.commit();

    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Gets the device type.
     *
     * @return the device type
     */
    public int getDeviceType() {
        return mDeviceType;
    }

    /**
     * Gets the device name.
     *
     * @return the device name
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * Gets the hMC name.
     *
     * @return the hMC name
     */
    public String getHMCName() {
        return mHMCName;
    }

    /**
     * The listener interface for receiving HMCPreference events.
     * The class that is interested in processing a HMCPreference
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addHMCPreferenceListener<code> method. When
     * the HMCPreference event occurs, that object's appropriate
     * method is invoked.
     *
     * @see HMCPreferenceEvent
     */
    private class HMCPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        /**
         * Instantiates a new hMC preference listener.
         */
        public HMCPreferenceListener() {
        }

        /* (non-Javadoc)
         * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key) {
            mUsername = mSettings.getString(HMCAPP_FULLJID_KEY, "");
            mPassword = mSettings.getString(HMCAPP_PASSWORD_KEY, "");
            mDeviceName = mSettings.getString("hmc_devname_key", "");
            mHMCName = mSettings.getString("hmc_hmcname_key", "");
            mStreamingServerAddress = mSettings.getString(HMCAPP_SERVER_ADDRESS_KEY, "127.0.0.1");

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
