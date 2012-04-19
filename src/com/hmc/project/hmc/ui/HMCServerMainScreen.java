/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;

/**
 * @author elisescu
 *
 */
public class HMCServerMainScreen extends Activity {
    protected static final String TAG = "DeviceMainScreen";
    private boolean mIsBound;
    private HMCService mBoundService;
    private IHMCFacade mHMCFacade;
    private HMCApplication mHMCApplication;

    private OnClickListener mTestMethodListener = new OnClickListener() {
        public void onClick(View v) {
            if (mHMCFacade != null) {
                // test RPC communication
                try {
                    mHMCFacade.getHMCManager().testRPC("elisescu_2@jabber.org", 3);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);

            if (mHMCFacade != null) {
                try {
                    mHMCFacade.getHMCManager().init(HMCDeviceItf.TYPE.HMC_SERVER);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(HMCServerMainScreen.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        bindService(new Intent(HMCServerMainScreen.this, 
                HMCService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        mHMCApplication = (HMCApplication)getApplication();

        setContentView(R.layout.hmc_server_main_screen);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.test_method);
        button.setOnClickListener(mTestMethodListener);

        // make sure we ended up in this activity with the app connected to XMPP
        // server
        if (!mHMCApplication.isConnected()) {
            doUnbindService();
            finish();
        }
    }
    
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hmc_server_default_screen, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hmc_server_default_screen_stop_hmc:
                logOutAndExit();
                return true;
            default:
                return false;
        }
    }

    private void logOutAndExit() {
        if (mHMCFacade != null) {
            try {
                mHMCFacade.disconnect();
                mHMCApplication.setConnected(false);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        doUnbindService();
        stopService(new Intent(HMCServerMainScreen.this,HMCService.class));
        finish();
    }
}

