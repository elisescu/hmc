/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.hmcserver;

import java.text.MessageFormat;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
    private int REL_SWIPE_MIN_DISTANCE;
    private int REL_SWIPE_MAX_OFF_PATH;
    private int REL_SWIPE_THRESHOLD_VELOCITY;

    private boolean mIsBound;
    private HMCService mBoundService;
    private IHMCConnection mHMCConnection;
    private HMCApplication mHMCApplication;
    private ListView mLocalDevicesListView;
    private DevicesListAdapter mLocalDeviceNamesAdapter;
    HashMap<String, String> mLocalDevNames;

    private ViewFlipper mListViewFlipper;
    private ListView mExternalDevicesListView;
    private DevicesListAdapter mExternalDeviceNamesAdapter;
    private HashMap<String, String> mExternalDevNames;


    HMCDevicesListener mHMCDevicesListener = new HMCDevicesListener();

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);

            if (mHMCConnection != null) {
                try {
                    mHMCConnection.getHMCManager().init(mHMCApplication.getDeviceName(), "",
                            HMCDeviceItf.TYPE.HMC_SERVER, mHMCApplication.getHMCName());
                    mHMCConnection.getHMCManager().registerDevicesListener(mHMCDevicesListener);

                    mLocalDevNames = (HashMap<String, String>) mHMCConnection.getHMCManager()
                                            .getListOfLocalDevices();

                    mExternalDevNames = (HashMap<String, String>) mHMCConnection.getHMCManager()
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
                    mLocalDeviceNamesAdapter.add(jid, mLocalDevNames.get(jid));
                    mExternalDeviceNamesAdapter.add(jid, mExternalDevNames.get(jid));
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

        mListViewFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));

        mLocalDevicesListView = (ListView) findViewById(R.id.hmc_local_devices_list);
        mLocalDeviceNamesAdapter = new DevicesListAdapter(this);
        mLocalDevicesListView.setAdapter(mLocalDeviceNamesAdapter);
        mLocalDevNames = new HashMap<String, String>();

        mExternalDevicesListView = (ListView) findViewById(R.id.hmc_external_devices_list);
        mExternalDeviceNamesAdapter = new DevicesListAdapter(this);
        mExternalDevicesListView.setAdapter(mExternalDeviceNamesAdapter);
        mExternalDevNames = new HashMap<String, String>();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        REL_SWIPE_MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_MAX_OFF_PATH = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_THRESHOLD_VELOCITY = (int) (200.0f * dm.densityDpi / 160.0f + 0.5);

        final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        mLocalDevicesListView.setOnTouchListener(gestureListener);
        mExternalDevicesListView.setOnTouchListener(gestureListener);

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
                        mLocalDeviceNamesAdapter.add(jid, name);
                        mExternalDeviceNamesAdapter.add(jid, name);
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

    private void myOnItemClick(int position) {
        String str = MessageFormat.format("Item clicked = {0,number}", position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void onLTRFling() {
        mListViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
        mListViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
        mListViewFlipper.showPrevious();
        Toast.makeText(this, "Left-to-right fling (prev)", Toast.LENGTH_SHORT).show();
    }

    private void onRTLFling() {
        mListViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
        mListViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
        mListViewFlipper.showNext();
        Toast.makeText(this, "Right-to-left fling (next)", Toast.LENGTH_SHORT).show();
    }

    private ListView getCurrentListView() {
        return (ListView) mListViewFlipper.getCurrentView();
    }
    
    private class MyGestureDetector extends SimpleOnGestureListener {
        // Detect a single-click and call my own handler.
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ListView lv = getCurrentListView();
            int pos = lv.pointToPosition((int) e.getX(), (int) e.getY());
            myOnItemClick(pos);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                onRTLFling();
            } else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                onLTRFling();
            }
            return false;
        }

    }

}

