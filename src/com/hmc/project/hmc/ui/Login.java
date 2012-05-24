/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.opengl.Visibility;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IConnectionListener;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.hmcserver.AddNewDeviceWizzard;
import com.hmc.project.hmc.ui.hmcserver.HMCServerMainScreen;
import com.hmc.project.hmc.ui.mediaclientdevice.HMCMediaClientDeviceMainScreen;
import com.hmc.project.hmc.utils.HMCUserNotifications;

import de.duenndns.ssl.MemorizingTrustManager;

public class Login extends Activity {
    protected static final String TAG = "LoginActivity";
    private HMCService mBoundService;
    private HMCApplication mHMCApplication;
    private ServiceConnection mConnection = new HMCServiceConnection();
    private boolean mServiceIsBound;
    private boolean mServiceIsStarted;
    private IHMCConnection mHMCConnection;
    private ProgressBar mLoginProgressbar;
    HMCConnectionListener mConnectionListener = new HMCConnectionListener();
    private Button mStartButton;
    private Button mStopButton;
    private String mPassword;
    private String mUsername;
    private int mDeviceType;
    private String mDeviceName;
    private BroadcastReceiver mSslReceiver;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

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

        setContentView(R.layout.login_screen);

        // get HMCApplication state
        mHMCApplication = (HMCApplication)getApplication();

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
        mStartButton = (Button) findViewById(R.id.LoginButton);
        mStartButton.setOnClickListener(mLoginListener);
        
        mUsernameEditText = (EditText) findViewById(R.id.login_username_editText);
        mPasswordEditText = (EditText) findViewById(R.id.login_password_editText);

        mUsernameEditText.setText(mHMCApplication.getUsername());
        mPasswordEditText.setText(mHMCApplication.getPassword());


        mLoginProgressbar = (ProgressBar) findViewById(R.id.loginProgressBar);

        mSslReceiver = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent i) {
                try {
                    Log.i(TAG, "Interception the SSL notification");
                    PendingIntent pi = i.getParcelableExtra(MemorizingTrustManager.INTERCEPT_DECISION_INTENT_LAUNCH);
                    pi.send();
                    abortBroadcast();
                } catch (PendingIntent.CanceledException e) {
                    Log.e(TAG, "Error while displaying the SSL dialog", e);
                }
            }
        };

        IntentFilter filter = new IntentFilter(MemorizingTrustManager.INTERCEPT_DECISION_INTENT
                                + "/" + getPackageName());
        filter.setPriority(50);
        registerReceiver(mSslReceiver, filter);

    }

    
    @Override
    protected void onResume() {
        super.onResume();
        mUsernameEditText.setText(mHMCApplication.getUsername());
        mPasswordEditText.setText(mHMCApplication.getPassword());
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

        unregisterReceiver(mSslReceiver);
    }
    
    private OnClickListener mLoginListener = new OnClickListener() {
        public void onClick(View v) {

            // start HMCService
            if (!mServiceIsStarted) {
                startService(new Intent(Login.this,
                        HMCService.class));
                mServiceIsStarted = true;
            }

            // bind to HMCService so that we get the HMCConnection
            if (!mServiceIsBound) {
                doBindService();
            }
        }
    };
    
    private void disconnectAndStopService() {
        // disconnect from XMPP server\
        if (mHMCApplication.isConnected() && mHMCConnection != null) {
            try {
                mHMCConnection.disconnect();
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
            mHMCConnection = IHMCConnection.Stub.asInterface(service);

            if (!mHMCApplication.isConnected() && mHMCApplication.isConfigured()) {
                mLoginProgressbar.setVisibility(ProgressBar.VISIBLE);

                mUsername = mHMCApplication.getUsername();
                mPassword = mHMCApplication.getPassword();
                mDeviceType = mHMCApplication.getDeviceType();
                mDeviceName = mHMCApplication.getDeviceName();

                if (mHMCConnection != null) {
                    try {
                        Log.d(TAG, "Username: " + mUsername + " on device: " + mDeviceName);
                        mHMCConnection.registerConnectionListener(mConnectionListener);
                        mHMCConnection.connectAsync(mUsername, mPassword, 5222);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
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

            Login.this.runOnUiThread(new Runnable() {
                public void run() {
                    mLoginProgressbar.setVisibility(ProgressBar.INVISIBLE);
                }
            });

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
                disconnectAndStopService();
            }
        }
    }
}
