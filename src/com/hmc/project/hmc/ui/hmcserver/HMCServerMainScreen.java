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
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.DevicesListAdapter;
import com.hmc.project.hmc.ui.HMCSettings;

import com.hmc.project.hmc.aidl.IHMCDevicesListener;

public class HMCServerMainScreen extends Activity {
    protected static final String TAG = "DeviceMainScreen";
    private boolean mIsBound;
    private HMCService mBoundService;
    private IHMCConnection mHMCConnection;
    private HMCApplication mHMCApplication;
    private ListView mDevicesListView;
    private DevicesListAdapter mDeviceNamesAdapter;
    HMCDevicesListener mHMCDevicesListener = new HMCDevicesListener();
    HashMap<String, String> mLocalDevNames;

    private OnClickListener mTestMethodListener = new OnClickListener() {
        public void onClick(View v) {
            if (mHMCConnection != null) {
                // mDeviceNamesAdapter.add("New device");
            }
        }
    };;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);

            if (mHMCConnection != null) {
                try {
                    mHMCConnection.getHMCManager().init(mHMCApplication.getDeviceName(), "",
                                            HMCDeviceItf.TYPE.HMC_SERVER);
                    mHMCConnection.getHMCManager().registerDevicesListener(mHMCDevicesListener);

                    mLocalDevNames = (HashMap<String, String>) mHMCConnection.getHMCManager()
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
                Iterator<String> iter = mLocalDevNames.keySet().iterator();
                while (iter.hasNext()) {
                    String jid = iter.next();
                    mDeviceNamesAdapter.add(jid, mLocalDevNames.get(jid));
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
                mHMCConnection.getHMCManager().unregisterDevicesListener(mHMCDevicesListener);
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
        mDeviceNamesAdapter = new DevicesListAdapter(this);
        mDevicesListView.setAdapter(mDeviceNamesAdapter);
        mLocalDevNames = new HashMap<String, String>();

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
        }

        @Override
        public void onDeviceAdded(IDeviceDescriptor devDesc) throws RemoteException {
            modifDeviceDescriptor = devDesc;
            // add the device in the list, inside ui thread
            HMCServerMainScreen.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        String jid = modifDeviceDescriptor.getFullJID();
                        String name = modifDeviceDescriptor.getDeviceName();
                        mDeviceNamesAdapter.add(jid, name);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Cannot retrieve the details about modified device");
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onDeviceRemoved(IDeviceDescriptor devDesc) throws RemoteException {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPresenceChanged(String presence, IDeviceDescriptor devDesc)
                throws RemoteException {
            // TODO Auto-generated method stub

        }

    }
}

