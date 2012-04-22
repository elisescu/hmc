/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.mediaclient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.hmcserver.HMCServerMainScreen;

/**
 * @author elisescu
 *
 */
public class HMCMediaClientDeviceMainScreen extends Activity {
    protected static final String TAG = "DeviceMainScreen";
    private boolean mIsBound;
    private HMCService mBoundService;
    private IHMCFacade mHMCFacade;
    private HMCApplication mHMCApplication;
    ArrayList<String> mDevicesNames;
    private ListView mDevicesListView;
    private DevicesListAdapter mDeviceNamesAdapter;

    private OnClickListener mTestMethodListener = new OnClickListener() {
        public void onClick(View v) {
            if (mHMCFacade != null) {
                // test RPC communication
                mDeviceNamesAdapter.add("New device");
            }
        }
    };;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);

            if (mHMCFacade != null) {
                try {
                    mHMCFacade.getHMCManager().init();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(HMCMediaClientDeviceMainScreen.this,
                                    R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        bindService(new Intent(HMCMediaClientDeviceMainScreen.this, HMCService.class), mConnection,
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
        mHMCApplication = (HMCApplication) getApplication();

        setContentView(R.layout.hmc_mediaclient_main_screen);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.test_method_media_client);
        button.setOnClickListener(mTestMethodListener);

        mDevicesListView = (ListView) findViewById(R.id.hmc_devices_list);
        mDevicesNames = new ArrayList<String>();
        mDeviceNamesAdapter = new DevicesListAdapter(this, mDevicesNames);
        mDevicesListView.setAdapter(mDeviceNamesAdapter);
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
        stopService(new Intent(HMCMediaClientDeviceMainScreen.this, HMCService.class));
        finish();
    }

}

