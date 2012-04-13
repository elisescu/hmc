/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc.ui;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IConnectionListener;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.utils.HMCUserNotifications;

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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Login extends Activity {
    protected static final String TAG = "LoginActivity";
    private SharedPreferences mSettings;
    private HMCService mBoundService;
    private HMCApplication mHMCApplication;
    private ServiceConnection mConnection = new HMCServiceConnection();
    private boolean mIsBound;
    private boolean mServiceStarted;
    private IHMCFacade mHMCFacade;
    private ProgressDialog mLoginProgressDialog;
    HMCConnectionListener mConnectionListener = new HMCConnectionListener();
    
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
                startActivityForResult(new Intent(this, HMCSettings.class),23);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("CheckStartActivity","onActivityResult and resultCode = "+resultCode);
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 23) {
            if (mHMCApplication.isAccountConfigured())
                HMCUserNotifications.normalToast(this, "Account configured");
            else {
                HMCUserNotifications.normalToast(this, "You must configure the account");
                startActivityForResult(new Intent(this, HMCSettings.class), 23);
            }
        }
        else{
            HMCUserNotifications.normalToast(this, "Returned from wrong activity: "+resultCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.local_service_controller);

        mHMCApplication = (HMCApplication)getApplication();

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.start);
        button.setOnClickListener(mStartListener);
        button = (Button)findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {

            // start HMCService
            startService(new Intent(Login.this,
                    HMCService.class));

            // bind to HMCService so that we get the HMCFacade
            doBindService();
            
            mIsBound = true;
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {

            // disconnect from XMPP server
            try {
                mHMCFacade.disconnect();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // unbind from HMCService
            doUnbindService();

            //stop HMCService
            stopService(new Intent(Login.this,
                    HMCService.class));
        }
    };
    
    void doBindService() {
        bindService(new Intent(Login.this, 
                HMCService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private class HMCServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);

            mLoginProgressDialog = ProgressDialog.show(Login.this, "Login", "wait please", true, 
                    false);

            try {
                mHMCFacade.registerConnectionListener(mConnectionListener);
                mHMCFacade.connectAsync("elisescu_1@jabber.org", "Cucurigu1",  5222);
            } catch (RemoteException e) {
                e.printStackTrace();
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
            // TODO Auto-generated method stub
            
        }

        @Override
        public void connectionSuccessful(boolean success) throws RemoteException {
            Log.d(TAG, "Connection successful"+success);
            mLoginProgressDialog.dismiss();
            if (success) {
                HMCUserNotifications.normalToast(Login.this, "Login successful");
            } else {
                HMCUserNotifications.normalToast(Login.this, "Login failed :(");
            }
        }
    }

}
