/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.mediaclient;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCFacade;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmJoinHMC extends Activity {
    protected static final String TAG = "ConfirmJoinHMC";
    private IHMCFacade mHMCFacade;
    private HMCService mBoundService;
    private boolean mIsBound;
    private HMCApplication mHMCApplication;
    private ConfirmJoinHMC mContext;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);
            if (mHMCFacade != null) {
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(ConfirmJoinHMC.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        bindService(new Intent(ConfirmJoinHMC.this, HMCService.class), mConnection,
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
        setContentView(R.layout.confirm_join_hmc);

        TextView textViewHMCName = (TextView) findViewById(R.id.textView_hmc_name);
        TextView textViewHMCServerDetails = (TextView) findViewById(R.id.textView_hmcserver_details);

        String temp;

        temp = String.format(textViewHMCName.getText().toString(), hmcName);
        textViewHMCName.setText(temp);

        temp = String.format(textViewHMCServerDetails.getText().toString(), hmcServerName,
                                hmcServerFingerprint);

        Log.d(TAG, "Fingerprint of remote device: " + hmcServerFingerprint);
        textViewHMCServerDetails.setText(temp);

    }

    private OnClickListener mButtonsListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.confirm_join_button_yes: {
                    Log.d(TAG, "User replied with YES");
                }
                    break;
                case R.id.confirm_join_button_no: {
                    Log.d(TAG, "User replied with YES");
                }
                    break;
                default:
                    break;
            }
            finish();
        }
    };
}
