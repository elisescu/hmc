/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.hmcserver;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.hmcserver.AddNewDeviceWizzard;
import com.hmc.project.hmc.utils.HMCUserNotifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmHMCInterconnection extends Activity {
    protected static final String TAG = "ConfirmJoinHMC";
    private IHMCConnection mHMCConnection;
    private HMCService mBoundService;
    private boolean mIsBound;
    private HMCApplication mHMCApplication;
    private ConfirmHMCInterconnection mContext;
    private IHMCManager mHMCManager;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);
            if (mHMCConnection != null) {
                try {
                    mHMCManager = mHMCConnection.getHMCManager();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Error. Couldn't retrieve the HMC serviec internals!");
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(ConfirmHMCInterconnection.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        bindService(new Intent(ConfirmHMCInterconnection.this, HMCService.class), mConnection,
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
        mContext = this;
        Intent sender = getIntent();
        String hmcName = sender.getExtras().getString("hmc_name");
        String hmcServerName = sender.getExtras().getString("hmc_srv_name");
        String hmcServerFingerprint = sender.getExtras().getString("hmc_srv_fingerprint");
        setContentView(R.layout.confirm_interconnection_hmc);

        TextView textViewHMCName = (TextView) findViewById(R.id.textView_hmc_name);
        TextView textViewHMCServerName = (TextView) findViewById(R.id.textView_confirmjoin_hmcserver_name);
        TextView textViewHMCSrvFingerprint = (TextView) findViewById(R.id.textView_confirmjoin_fingerprint);

        String temp;

        temp = String.format(textViewHMCName.getText().toString(), hmcName);
        textViewHMCName.setText(temp);

        Log.d(TAG, "Fingerprint of remote device: " + hmcServerFingerprint);
        textViewHMCServerName.setText(hmcServerName);
        textViewHMCSrvFingerprint.setText(hmcServerFingerprint);

        Button yesButt = (Button) findViewById(R.id.confirm_join_button_yes);
        Button noButt = (Button) findViewById(R.id.confirm_join_button_no);

        yesButt.setOnClickListener(mButtonsListener);
        noButt.setOnClickListener(mButtonsListener);
    }

    private OnClickListener mButtonsListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.confirm_join_button_yes: {
                    Log.d(TAG, "User replied with YES");
                    try {
                        mHMCManager.setUserReplyHMCInterconnection(true);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                    break;
                case R.id.confirm_join_button_no: {
                    Log.d(TAG, "User replied with NO");
                    try {
                        mHMCManager.setUserReplyHMCInterconnection(false);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                    break;
                default:
                    break;
            }
            finish();
        }
    };
}
