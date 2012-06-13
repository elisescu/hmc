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
import com.hmc.project.hmc.aidl.IHMCRenderingListener;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.DevicesListActivity;
import com.hmc.project.hmc.ui.mediaclientdevice.HMCMediaClientDeviceMainScreen;
import com.hmc.project.hmc.ui.mediadevice.VideoPlayerActivity;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCServerMainScreen.
 */
public class HMCServerMainScreen extends Activity {
    
    /** The Constant TAG. */
    protected static final String TAG = "DeviceMainScreen";
    
    /** The m is bound. */
    private boolean mIsBound;
    
    /** The m hmc connection. */
    private IHMCConnection mHMCConnection;
    
    /** The m hmc application. */
    private HMCApplication mHMCApplication;
    
    /** The m see devices button. */
    private Button mSeeDevicesButton;
    
    /** The m logout button. */
    private Button mLogoutButton;

    private IHMCRenderingListener mRenderingListener = new IHMCRenderingListener.Stub() {

        @Override
        public boolean initRendering() throws RemoteException {
            Log.d(TAG, "Starting rendering on local device");
            Intent intentDemo = new Intent(HMCServerMainScreen.this,
                    VideoPlayerActivity.class);
            intentDemo.putExtra(VideoPlayerActivity.PLAYER_MODE_KEY,
                    VideoPlayerActivity.PLAYER_MODE_LOCAL);
            startActivity(intentDemo);
            return true;
        }
    };

    /** The m connection. */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);

            if (mHMCConnection != null) {
                try {
                    mHMCConnection.getHMCManager().init(mHMCApplication.getDeviceName(), "",
                                            HMCDeviceItf.TYPE.HMC_SERVER,
                                            mHMCApplication.getHMCName());

                    mHMCConnection.getHMCManager().setLocalRenderingListener(mRenderingListener);
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

    /**
     * Do bind service.
     */
    void doBindService() {
        bindService(new Intent(HMCServerMainScreen.this, 
 HMCService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Do unbind service.
     */
    void doUnbindService() {
        if (mIsBound) {
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

    /** The m buttons click listener. */
    View.OnClickListener mButtonsClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.hmcserver_main_screen_see_devices_button:
                    Intent intent = new Intent(HMCServerMainScreen.this, DevicesListActivity.class);

                    intent.putExtra(DevicesListActivity.DEV_LIST_INTENT_KEY,
                                            DevicesListActivity.DEV_LIST_INTENT_DISPLAY);
                    startActivity(intent);
                    break;
                case R.id.hmcserver_main_screen_logout_button:
                    logOutAndExit();
                    break;
                case R.id.hmcserver_main_screen_demo_button:
                    Intent intentDemo = new Intent(HMCServerMainScreen.this,
                                            VideoPlayerActivity.class);

                    intentDemo.putExtra(VideoPlayerActivity.PLAYER_MODE_KEY,
                                            VideoPlayerActivity.PLAYER_MODE_LOCAL);
                    startActivity(intentDemo);
                    break;
                default:
                    break;
            }
        }
    };

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hmc_server_main_screen, menu);
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
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

    /**
     * Log out and exit.
     */
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