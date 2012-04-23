/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.hmcserver;

import org.jivesoftware.smack.util.StringUtils;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.utils.HMCUserNotifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddNewDeviceWizzard extends Activity {
    protected static final String TAG = "DeviceMainScreen";
    private boolean mIsBound;
    private HMCService mBoundService;
    private IHMCFacade mHMCFacade;
    private HMCApplication mHMCApplication;
    private TextView mInfoTextView;
    private EditText mJidTextView;  
    private Context mContext; 


    private OnClickListener mButtonsListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_new_dev_butt_add: {
                    if (checkUsername(mJidTextView.getText().toString())) {
                        try {
                            addNewDevice(mJidTextView.getText().toString());
                        } catch (Exception e) {
                            HMCUserNotifications.normalToast(mContext, "Internal error");
                            e.printStackTrace();
                        }
                    } else {
                        HMCUserNotifications.normalToast(mContext, "invalid JID");
                    }
                    
                }
                case R.id.add_new_dev_butt_done:
                    // let the implementation know that we agree with the
                    // fingerprint and to continue with adding the device
                    break;
                case R.id.add_new_dev_butt_cancel:
                    // the user didn't agree with the fingerprint
                    break;
                default:
                    break;
            }
        }

        private void addNewDevice(String newDevFullJid) throws Exception {
            
            if (mHMCFacade == null) {
                throw new Exception();
            } else {
                IHMCManager hmcMng = mHMCFacade.getHMCManager();
                hmcMng.init();
                IHMCServerHndl hmcServerHmdl = hmcMng.implHMCServer();
                hmcServerHmdl.addNewDevice(newDevFullJid);
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
            Toast.makeText(AddNewDeviceWizzard.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };


    private boolean mValidJid;    void doBindService() {
        bindService(new Intent(AddNewDeviceWizzard.this,
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
        mContext = this;

        setContentView(R.layout.add_new_device_wizzard);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.add_new_dev_butt_add);
        button.setOnClickListener(mButtonsListener);

        button = (Button) findViewById(R.id.add_new_dev_butt_done);
        button.setOnClickListener(mButtonsListener);

        button = (Button) findViewById(R.id.add_new_dev_butt_cancel);
        button.setOnClickListener(mButtonsListener);

        mInfoTextView = (TextView) findViewById(R.id.add_new_dev_text_info);
        mJidTextView =  (EditText) findViewById(R.id.add_new_dev_text_jid);
        // make sure we ended up in this activity with the app connected to XMPP
        // server
        if (!mHMCApplication.isConnected()) {
            doUnbindService();
            finish();
        }
    }
    
    private boolean checkUsername(String username) {
        String name = StringUtils.parseName(username);
        String server = StringUtils.parseServer(username);
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(server)) {
            mValidJid = false;
        } else {
            mValidJid = true;
        }
        return mValidJid;
    }
    
}

