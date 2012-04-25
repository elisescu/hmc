/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.hmcserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.DevicesListAdapter;
import com.hmc.project.hmc.ui.HMCSettings;

import com.hmc.project.hmc.aidl.IHMCDevicesListener;

public class HMCServerMainScreen extends Activity {
    protected static final String TAG = "DeviceMainScreen";
    private boolean mIsBound;
    private HMCService mBoundService;
    private IHMCFacade mHMCFacade;
    private HMCApplication mHMCApplication;
    private ArrayList<String> mDevicesNames;
    private ListView mDevicesListView;
    private DevicesListAdapter mDeviceNamesAdapter;
    HMCDevicesListener mHMCDevicesListener = new HMCDevicesListener();
    HashMap<String, String> mLocalDevDescriptors;

    private OnClickListener mTestMethodListener = new OnClickListener() {
        public void onClick(View v) {
            if (mHMCFacade != null) {
                mDeviceNamesAdapter.add("New device");
            }
        }
    };;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);

            if (mHMCFacade != null) {
                try {
                    mHMCFacade.getHMCManager().init(mHMCApplication.getDeviceName(), "",
                                            HMCDeviceItf.TYPE.HMC_SERVER);
                    mHMCFacade.getHMCManager().registerDevicesListener(mHMCDevicesListener);

                    mLocalDevDescriptors = (HashMap<String, String>) mHMCFacade.getHMCManager()
                                            .getListOfLocalDevices();

                    updateListOfLocalDevicesUIThread();

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

    private void updateListOfLocalDevicesUIThread() {
        HMCServerMainScreen.this.runOnUiThread(new Runnable() {
            public void run() {

                Iterator<String> iter = mLocalDevDescriptors.values().iterator();
                while (iter.hasNext()) {
                    mDeviceNamesAdapter.add(iter.next());
                }
            }
        });

    }

    void doBindService() {
        bindService(new Intent(HMCServerMainScreen.this, 
                HMCService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            try {
                mHMCFacade.getHMCManager().unregisterDevicesListener(mHMCDevicesListener);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

        mDevicesListView = (ListView) findViewById(R.id.hmc_devices_list);
        mDevicesNames = new ArrayList<String>();
        mDeviceNamesAdapter = new DevicesListAdapter(this, mDevicesNames);
        mDevicesListView.setAdapter(mDeviceNamesAdapter);

        mLocalDevDescriptors = new HashMap<String, String>();

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

    private class HMCDevicesListener extends IHMCDevicesListener.Stub {
        IDeviceDescriptor modifDeviceDescriptor = null;
        @Override
        public void onDevicesListChanged(String whatChanged, IDeviceDescriptor devDesc)
                                throws RemoteException {
            modifDeviceDescriptor = devDesc;
            if (whatChanged.equals("added")) {
                // add the device in the list, inside ui thread
                HMCServerMainScreen.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            mDeviceNamesAdapter.add(modifDeviceDescriptor.getDeviceName());
                        } catch (RemoteException e) {
                            Log.e(TAG, "Cannot retrieve the details about modified device");
                            e.printStackTrace();
                        }
                    }
                });

            }
        }

    }
}

