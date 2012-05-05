/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui.hmcserver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.DevicesListActivity;

public class HMCServerMainScreen extends Activity {
    protected static final String TAG = "DeviceMainScreen";
    private boolean mIsBound;
    private IHMCConnection mHMCConnection;
    private HMCApplication mHMCApplication;
    private Button mSeeDevicesButton;
    private Button mLogoutButton;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);

            if (mHMCConnection != null) {
                try {
                    mHMCConnection.getHMCManager().init(mHMCApplication.getDeviceName(), "",
                                            HMCDeviceItf.TYPE.HMC_SERVER,
                                            mHMCApplication.getHMCName());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Toast.makeText(HMCServerMainScreen.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        bindService(new Intent(HMCServerMainScreen.this, 
 HMCService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
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
        // make sure we ended up in this activity with the app connected to XMPP
        // server
        if (!mHMCApplication.isConnected()) {
            doUnbindService();
            finish();
        }

        mSeeDevicesButton = (Button) findViewById(R.id.hmcserver_main_screen_see_devices_button);
        mLogoutButton = (Button) findViewById(R.id.hmcserver_main_screen_logout_button);
        mSeeDevicesButton.setOnClickListener(mButtonsClickListener);
        mLogoutButton.setOnClickListener(mButtonsClickListener);
    }

    View.OnClickListener mButtonsClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.hmcserver_main_screen_see_devices_button:
                    startActivity(new Intent(HMCServerMainScreen.this, DevicesListActivity.class));
                    break;
                case R.id.hmcserver_main_screen_logout_button:
                    logOutAndExit();
                    break;
                default:
                    break;
            }
        }
    };

    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hmc_server_main_screen, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hmc_server_default_screen_stop_hmc:
                logOutAndExit();
                return true;
            case R.id.hmc_server_default_screen_add_new_device:
                startActivity(new Intent(this, AddNewDeviceWizzard.class));
                return true;
            case R.id.hmc_server_default_screen_hmc_interconnect:
                startActivity(new Intent(this, HMCInterconnectionWizzard.class));
                return true;
            default:
                return false;
        }
    }

    private void logOutAndExit() {
        if (mHMCConnection != null) {
            try {
                mHMCConnection.disconnect();
                mHMCApplication.setConnected(false);
            } catch (RemoteException e) {
                Log.e(TAG, "Fatal error: Unable to communicate with HMC Server");
                e.printStackTrace();
            }
        }
        doUnbindService();
        stopService(new Intent(HMCServerMainScreen.this,HMCService.class));
        finish();
    }
}