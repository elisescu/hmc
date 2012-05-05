/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.DevicesListAdapter;
import com.hmc.project.hmc.aidl.IHMCDevicesListener;

public class DevicesListActivity extends Activity {
    protected static final String TAG = "DevicesListActivity";
    private int REL_SWIPE_MIN_DISTANCE;
    private int REL_SWIPE_MAX_OFF_PATH;
    private int REL_SWIPE_THRESHOLD_VELOCITY;

    private boolean mIsBound;
    private IHMCConnection mHMCConnection;
    private HMCApplication mHMCApplication;
    private ListView mLocalDevicesListView;
    private DevicesListAdapter mLocalDeviceNamesAdapter;
    HashMap<String, String> mLocalDevNames;

    private ViewFlipper mListViewFlipper;
    private ListView mExternalDevicesListView;
    private DevicesListAdapter mExternalDeviceNamesAdapter;
    private HashMap<String, String> mExternalDevNames;
    private GestureDetector mGestureDetector;
    private TextView mListTitle;

    HMCDevicesListener mHMCDevicesListener = new HMCDevicesListener();

    private ServiceConnection mConnection = new ServiceConnection() {

        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);

            if (mHMCConnection != null) {
                try {
                    mHMCConnection.getHMCManager().init(mHMCApplication.getDeviceName(), "",
                                            HMCDeviceItf.TYPE.HMC_SERVER,
                                            mHMCApplication.getHMCName());
                    mHMCConnection.getHMCManager().registerDevicesListener(mHMCDevicesListener);

                    // it is save to type cast to HashMap because we know for
                    // sure the result is a HashMap. The reason for returning
                    // actually a reference of type Map is that IPC doesn't
                    // support HashMap
                    mLocalDevNames = (HashMap<String, String>) mHMCConnection.getHMCManager()
                                            .getListOfLocalDevices();

                    mExternalDevNames = (HashMap<String, String>) mHMCConnection.getHMCManager()
                                            .getListOfExternalDevices();

                    updateListOfLocalDevicesUIThread();
                    updateListOfRemoteDevicesUIThread();

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateListOfRemoteDevicesUIThread() {
            DevicesListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Iterator<String> iter = mLocalDevNames.keySet().iterator();
                    while (iter.hasNext()) {
                        String jid = iter.next();
                        mExternalDeviceNamesAdapter.add(jid, mExternalDevNames.get(jid));
                    }
                }
            });
        }

        private void updateListOfLocalDevicesUIThread() {
            DevicesListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Iterator<String> iter = mLocalDevNames.keySet().iterator();
                    while (iter.hasNext()) {
                        String jid = iter.next();
                        mLocalDeviceNamesAdapter.add(jid, mLocalDevNames.get(jid));
                    }
                }
            });

        }

        public void onServiceDisconnected(ComponentName className) {
            Toast.makeText(DevicesListActivity.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        bindService(new Intent(DevicesListActivity.this, HMCService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
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
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mHMCApplication = (HMCApplication) getApplication();

        setContentView(R.layout.devices_list_activity);
        // make sure we ended up in this activity with the app connected to XMPP
        // server
        if (!mHMCApplication.isConnected()) {
            doUnbindService();
            finish();
        }

        mListTitle = (TextView) this.findViewById(R.id.main_screen_list_title);
        mListViewFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));

        // list of local devices
        mLocalDevicesListView = (ListView) findViewById(R.id.hmc_local_devices_list);
        mLocalDeviceNamesAdapter = new DevicesListAdapter(this);
        mLocalDevicesListView.setAdapter(mLocalDeviceNamesAdapter);
        mLocalDevNames = new HashMap<String, String>();

        // list of external devices. For now support only a single external HMC
        // interconnection
        mExternalDevicesListView = (ListView) findViewById(R.id.hmc_external_devices_list);
        mExternalDeviceNamesAdapter = new DevicesListAdapter(this);
        mExternalDevicesListView.setAdapter(mExternalDeviceNamesAdapter);
        mExternalDevNames = new HashMap<String, String>();

        // compute constants used for swiping left and right
        REL_SWIPE_MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_MAX_OFF_PATH = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_THRESHOLD_VELOCITY = (int) (200.0f * dm.densityDpi / 160.0f + 0.5);

        mGestureDetector = new GestureDetector(new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        };
        mLocalDevicesListView.setOnTouchListener(gestureListener);
        mExternalDevicesListView.setOnTouchListener(gestureListener);
    }

    private class HMCDevicesListener extends IHMCDevicesListener.Stub {
        IDeviceDescriptor modifDeviceDescriptor = null;

        @Override
        public void onDeviceAdded(IDeviceDescriptor devDesc) throws RemoteException {
            modifDeviceDescriptor = devDesc;
            // add the device in the list, inside ui thread
            DevicesListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        String jid = modifDeviceDescriptor.getFullJID();
                        String name = modifDeviceDescriptor.getDeviceName();
                        mLocalDeviceNamesAdapter.add(jid, name);
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

        @Override
        public void onExternalDeviceAdded(String externalName, IDeviceDescriptor devDesc)
                                throws RemoteException {
            modifDeviceDescriptor = devDesc;
            // add the device in the list of external devices, inside UI thread
            DevicesListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        String jid = modifDeviceDescriptor.getFullJID();
                        String name = modifDeviceDescriptor.getDeviceName();
                        mExternalDeviceNamesAdapter.add(jid, name);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Cannot retrieve the details about modified device");
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    private void onDeviceListClick(int position, ListView lv) {
        String str = MessageFormat.format("Item clicked = {0,number}", position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void onLeftToRightFling() {
        mListViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
        mListViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
        mListViewFlipper.showPrevious();

        // TODO: make this in a proper way
        if (mListViewFlipper.getCurrentView().equals(mLocalDevicesListView)) {
            mListTitle.setText("Local devices");
        } else {
            mListTitle.setText("External devices");
        }

    }

    private void onRightToLeftFling() {
        mListViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
        mListViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
        mListViewFlipper.showNext();

        // TODO: make this in a proper way
        if (mListViewFlipper.getCurrentView().equals(mLocalDevicesListView)) {
            mListTitle.setText("Local devices");
        } else {
            mListTitle.setText("External devices");
        }

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
            onDeviceListClick(pos, lv);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                onRightToLeftFling();
            } else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                onLeftToRightFling();
            }
            return false;
        }

    }

}

