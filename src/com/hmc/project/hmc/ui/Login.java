/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IConnectionListener;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.hmcserver.HMCServerMainScreen;
import com.hmc.project.hmc.ui.mediaclient.HMCMediaClientDeviceMainScreen;
import com.hmc.project.hmc.utils.HMCUserNotifications;

public class Login extends Activity {
    protected static final String TAG = "LoginActivity";
    private HMCService mBoundService;
    private HMCApplication mHMCApplication;
    private ServiceConnection mConnection = new HMCServiceConnection();
    private boolean mServiceIsBound;
    private boolean mServiceIsStarted;
    private IHMCFacade mHMCFacade;
    private ProgressDialog mLoginProgressDialog;
    HMCConnectionListener mConnectionListener = new HMCConnectionListener();
    private Button mStartButton;
    private Button mStopButton;
    private String mPassword;
    private String mUsername;
    private int mDeviceType;

    // private final HMCPreferenceListener mPreferenceListener = new
    // HMCPreferenceListener();

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.controler_service, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.controler_list_menu_preferences:
                startActivity(new Intent(this, HMCSettings.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.local_service_controller);

        // initialize preferences listener and HMCApplication global state 
        mHMCApplication = (HMCApplication)getApplication();
        
        mUsername = mHMCApplication.getUsername();
        mPassword = mHMCApplication.getPassword();
        mDeviceType = mHMCApplication.getDeviceType();

        // if we are already connected, then go directly to main screen
        if (mHMCApplication.isConnected()) {
            switch (mDeviceType) {
            case HMCDeviceItf.TYPE.HMC_SERVER:
                startActivity(new Intent(Login.this, HMCServerMainScreen.class));
                break;
            case HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE:
                startActivity(new Intent(Login.this, HMCMediaClientDeviceMainScreen.class));
                break;
            default:
                Log.e(TAG, "Very baad error: we don't know what type of device we are");
                disconnectAndStopService();
                break;
            }
            finish();
        }

        // Watch for button clicks.
        mStartButton = (Button)findViewById(R.id.start);
        mStartButton.setOnClickListener(mStartListener);

        mStopButton = (Button)findViewById(R.id.stop);
        mStopButton.setOnClickListener(mStopListener);
        mStopButton.setEnabled(false);
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceIsBound) {
            doUnbindService();
            mServiceIsBound = false;
        }

        if (!mHMCApplication.isConnected()) {
            stopService(new Intent(Login.this, HMCService.class));
        }
    }
    
    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {

            // start HMCService
            if (!mServiceIsStarted) {
                startService(new Intent(Login.this,
                        HMCService.class));
                mServiceIsStarted = true;
            }

            // bind to HMCService so that we get the HMCFacade
            if (!mServiceIsBound) {
                doBindService();
            }
            
            if (!mHMCApplication.isConnected() && mHMCApplication.isConfigured()) {
                mLoginProgressDialog = ProgressDialog.show(Login.this, "Login", "wait please", true, 
                        false);

                mUsername = mHMCApplication.getUsername();
                mPassword = mHMCApplication.getPassword();
                mDeviceType = mHMCApplication.getDeviceType();

                if (mHMCFacade != null) {
                    try {
                        mHMCFacade.registerConnectionListener(mConnectionListener);
                        mHMCFacade.connectAsync(mUsername, mPassword, 5222);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    
    private void disconnectAndStopService() {
        // disconnect from XMPP server\
        if (mHMCApplication.isConnected() && mHMCFacade != null) {
            try {
                mHMCFacade.disconnect();
                mHMCApplication.setConnected(false);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // unbind from HMCService
        if (mServiceIsBound){
            doUnbindService();
            mServiceIsBound = false;
        }

        //stop HMCService
        if (mServiceIsStarted) {
            stopService(new Intent(Login.this,
                    HMCService.class));
            mServiceIsStarted = false;
        }
    }

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            disconnectAndStopService();
        }
    };


    void doBindService() {
        bindService(new Intent(Login.this, 
                HMCService.class), mConnection, Context.BIND_AUTO_CREATE);
        mServiceIsBound = true;
    }

    void doUnbindService() {
        if (mServiceIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mServiceIsBound = false;
        }
    }

    private class HMCServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);
            if (mHMCApplication.isConfigured())  {
                try {
                    mHMCFacade.registerConnectionListener(mConnectionListener);
                    mHMCFacade.connectAsync(mUsername, mPassword,  5222);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG,"Successfully binded the service");

            // Tell the user about this for our demo.
            //Toast.makeText(Login.this, R.string.local_service_connected,
            //        Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(Login.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private class HMCConnectionListener extends IConnectionListener.Stub {
        @Override
        public void connectionClosedOnError(String arg0) throws RemoteException {
            mHMCApplication.setConnected(false);
            Log.e(TAG, "XMPP connection was closed with the error:" + arg0);
        }

        @Override
        public void connectionSuccessful(boolean success) throws RemoteException {
            Log.d(TAG, "Connection successful"+success);
            mLoginProgressDialog.dismiss();
            mHMCApplication.setConnected(success);
            if (success) {
                HMCUserNotifications.normalToast(Login.this, "Login successful");

                switch (mDeviceType) {
                    case HMCDeviceItf.TYPE.HMC_SERVER:
                        startActivity(new Intent(Login.this, HMCServerMainScreen.class));
                        break;
                    case HMCDeviceItf.TYPE.HMC_CLIENT_DEVICE:
                        startActivity(new Intent(Login.this, HMCMediaClientDeviceMainScreen.class));
                        break;
                    default:
                        Log.e(TAG, "Very baad error: we don't know what type of device we are");
                        disconnectAndStopService();
                        break;
                }
                finish();
            } else {
                HMCUserNotifications.normalToast(Login.this, "Login failed");
            }
        }
    }

//    private class HMCPreferenceListener implements
//            SharedPreferences.OnSharedPreferenceChangeListener {
//        public HMCPreferenceListener() {
//        }
//
//        @Override
//        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            mUsername = mSettings.getString("hmc_username_key", "");
//            mPassword = mSettings.getString("hmc_pass_key", "");
//            try {
//                mDeviceType = Integer.parseInt(mSettings.getString("hmc_device_type", "-1"));
//            } catch (NumberFormatException e) {
//                mDeviceType = -1;
//            }
//            Log.e("EEEEEEEEEEE", "devicetype = " + mDeviceType);
//        }
//    }

}
