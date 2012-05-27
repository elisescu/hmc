/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui;

import java.text.MessageFormat;
import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
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

// TODO: Auto-generated Javadoc
/**
 * The Class DevicesListActivity.
 */
public class DevicesListActivity extends Activity {
    public static String DEV_LIST_INTENT_KEY = "dev_list_intent";
    public static String DEV_LIST_INTENT_PICK = "dev_list_pick";
    public static String DEV_LIST_INTENT_DISPLAY = "dev_list_display";
    public static String DEV_LIST_JID_PICKED_KEY = "dev_list_jid";

    /** The Constant TAG. */
    protected static final String TAG = "DevicesListActivity";
    
    /** The RE l_ swip e_ mi n_ distance. */
    private int REL_SWIPE_MIN_DISTANCE;
    
    /** The RE l_ swip e_ ma x_ of f_ path. */
    private int REL_SWIPE_MAX_OFF_PATH;
    
    /** The RE l_ swip e_ threshol d_ velocity. */
    private int REL_SWIPE_THRESHOLD_VELOCITY;

    /** The m is bound. */
    private boolean mIsBound;
    
    /** The m hmc connection. */
    private IHMCConnection mHMCConnection;
    
    /** The m hmc application. */
    private HMCApplication mHMCApplication;
    
    /** The m local devices list view. */
    private ListView mLocalDevicesListView;
    
    /** The m local device list adapter. */
    private DevicesListAdapter mLocalDeviceListAdapter;

    /** The m list view flipper. */
    private ViewFlipper mListViewFlipper;
    
    /** The m external devices list view. */
    private ListView mExternalDevicesListView;
    
    /** The m external device list adapter. */
    private DevicesListAdapter mExternalDeviceListAdapter;
    
    /** The m gesture detector. */
    private GestureDetector mGestureDetector;

    /** The m lists adapters. */
    private HashMap<ListView, DevicesListAdapter> mListsAdapters;
    
    /** The m lists titles. */
    private HashMap<ListView, String> mListsTitles;

    /** The m list title. */
    private TextView mListTitle;

    /** The m hmc devices listener. */
    HMCDevicesListener mHMCDevicesListener = new HMCDevicesListener();

    private String mIntentType;

    /** The m connection. */
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
                    HashMap<String, String> mLocalDevNames = (HashMap<String, String>) mHMCConnection
                                            .getHMCManager()
                                            .getListOfLocalDevices();

                    HashMap<String, String> mExternalDevNames = (HashMap<String, String>) mHMCConnection
                                            .getHMCManager()
                                            .getListOfExternalDevices();

                    mLocalDeviceListAdapter.setDevices(mLocalDevNames);
                    mExternalDeviceListAdapter.setDevices(mExternalDevNames);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Toast.makeText(DevicesListActivity.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };
    /**
     * Do bind service.
     */
    void doBindService() {
        bindService(new Intent(DevicesListActivity.this, HMCService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Do unbind service.
     */
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

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mHMCApplication = (HMCApplication) getApplication();

        Intent sender = getIntent();
        mIntentType = sender.getExtras().getString(DEV_LIST_INTENT_KEY);

        Log.d(TAG, "intent type got: " + mIntentType);

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
        mLocalDeviceListAdapter = new DevicesListAdapter(this);
        mLocalDevicesListView.setAdapter(mLocalDeviceListAdapter);

        // list of external devices. For now support only a single external HMC
        // interconnection
        mExternalDevicesListView = (ListView) findViewById(R.id.hmc_external_devices_list);
        mExternalDeviceListAdapter = new DevicesListAdapter(this);
        mExternalDevicesListView.setAdapter(mExternalDeviceListAdapter);

        mListsAdapters = new HashMap<ListView, DevicesListAdapter>();
        mListsAdapters.put(mLocalDevicesListView, mLocalDeviceListAdapter);
        mListsAdapters.put(mExternalDevicesListView, mExternalDeviceListAdapter);

        mListsTitles = new HashMap<ListView, String>();
        mListsTitles.put(mLocalDevicesListView, "Local devices");
        mListsTitles.put(mExternalDevicesListView, "External devices");

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

    /**
     * The listener interface for receiving HMCDevices events.
     * The class that is interested in processing a HMCDevices
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addHMCDevicesListener<code> method. When
     * the HMCDevices event occurs, that object's appropriate
     * method is invoked.
     *
     * @see HMCDevicesEvent
     */
    private class HMCDevicesListener extends IHMCDevicesListener.Stub {
        
        /** The modif device descriptor. */
        IDeviceDescriptor modifDeviceDescriptor = null;

        /* (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IHMCDevicesListener#onDeviceAdded(com.hmc.project.hmc.aidl.IDeviceDescriptor)
         */
        @Override
        public void onDeviceAdded(IDeviceDescriptor devDesc) throws RemoteException {
            modifDeviceDescriptor = devDesc;
            // add the device in the list, inside ui thread
            try {
                String jid = modifDeviceDescriptor.getFullJID();
                String name = modifDeviceDescriptor.getDeviceName();
                mLocalDeviceListAdapter.add(jid, name);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot retrieve the details about modified device");
                e.printStackTrace();
            }

        }

        /* (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IHMCDevicesListener#onDeviceRemoved(com.hmc.project.hmc.aidl.IDeviceDescriptor)
         */
        @Override
        public void onDeviceRemoved(IDeviceDescriptor devDesc) throws RemoteException {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IHMCDevicesListener#onPresenceChanged(java.lang.String, com.hmc.project.hmc.aidl.IDeviceDescriptor)
         */
        @Override
        public void onPresenceChanged(String presence, IDeviceDescriptor devDesc)
                                throws RemoteException {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IHMCDevicesListener#onExternalDeviceAdded(java.lang.String, com.hmc.project.hmc.aidl.IDeviceDescriptor)
         */
        @Override
        public void onExternalDeviceAdded(String externalName, IDeviceDescriptor devDesc)
                                throws RemoteException {
            modifDeviceDescriptor = devDesc;
            // add the device in the list of external devices, inside UI thread
            try {
                String jid = modifDeviceDescriptor.getFullJID();
                String name = modifDeviceDescriptor.getDeviceName();
                mExternalDeviceListAdapter.add(jid, name);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot retrieve the details about modified device");
                e.printStackTrace();
            }
        }

    }

    /**
     * On device list click.
     *
     * @param position the position
     * @param lv the lv
     */
    private void onDeviceListClick(int position, ListView lv) {
        if (position >= 0) {
            String clickedJID = mListsAdapters.get(mListViewFlipper.getCurrentView())
                                    .getJidFromPosition(position);
            Toast.makeText(this, clickedJID, Toast.LENGTH_SHORT).show();
            if (DEV_LIST_INTENT_PICK.equals(mIntentType)) {
                Intent resultData = new Intent();
                resultData.putExtra(DEV_LIST_JID_PICKED_KEY, clickedJID);
                setResult(Activity.RESULT_OK, resultData);
                finish();
            }
        }
    }

    /**
     * On left to right fling.
     */
    private void onLeftToRightFling() {
        mListViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
        mListViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
        mListViewFlipper.showPrevious();

        mListTitle.setText(mListsTitles.get(mListViewFlipper.getCurrentView()));
    }

    /**
     * On right to left fling.
     */
    private void onRightToLeftFling() {
        mListViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
        mListViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
        mListViewFlipper.showNext();

        mListTitle.setText(mListsTitles.get(mListViewFlipper.getCurrentView()));
    }

    /**
     * Gets the current list view.
     *
     * @return the current list view
     */
    private ListView getCurrentListView() {
        return (ListView) mListViewFlipper.getCurrentView();
    }

    /**
     * The Class MyGestureDetector.
     */
    private class MyGestureDetector extends SimpleOnGestureListener {
        // Detect a single-click and call my own handler.
        /* (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapUp(android.view.MotionEvent)
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ListView lv = getCurrentListView();
            int pos = lv.pointToPosition((int) e.getX(), (int) e.getY());
            onDeviceListClick(pos, lv);
            return false;
        }

        /* (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
         */
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

