/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.hmcserver;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCServerHndl;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.implementations.HMCServerImplementation;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.Login;
import com.hmc.project.hmc.utils.HMCUserNotifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
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
    private UserRequestsListener mUserRequestsListener = new UserRequestsListener();
    private ProgressDialog mAddDeviceProgressDialog;
    private IDeviceDescriptor mNewDeviceDesc;
    private boolean mValidJid;
    private Boolean mUserConfirmed = new Boolean(false);;
    private Object mUserConfirmedNotif = new Object();
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
                default:
                    break;
            }
        }

    };

    private void addNewDevice(String newDevFullJid) throws Exception {

        if (mHMCFacade == null) {
            throw new Exception();
        } else {
            IHMCManager hmcMng = mHMCFacade.getHMCManager();
            IHMCServerHndl hmcServerHmdl = hmcMng.implHMCServer();
            hmcServerHmdl.addUserRequestsListener(mUserRequestsListener);
            mAddDeviceProgressDialog = ProgressDialog.show(this, "Add new device",
                    "Getting device information.\nPlease wait...", true,
                    false);
            new AddDeviceAsyncTask().execute(hmcServerHmdl, newDevFullJid);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCFacade = IHMCFacade.Stub.asInterface(service);
            if (mHMCFacade != null) {

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(AddNewDeviceWizzard.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
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
    
    class UserRequestsListener extends IUserRequestsListener.Stub {

        @Override
        public boolean confirmDeviceAddition(IDeviceDescriptor newDevice) throws RemoteException {
            mNewDeviceDesc = newDevice;
            // mInfoTextView.setText(newDevice.toString());
            mAddDeviceProgressDialog.dismiss();
            // ask the user to confirm the device description

            return askUserConfirmation();
        }

        @Override
        public boolean verifyFingerprint(String localFingerprint, String remoteFingerprint,
                String deviceName) throws RemoteException {
            // TODO Auto-generated method stub
            return false;
        }
    }
    
    private boolean askUserConfirmation() throws RemoteException {
        Log.d(TAG, "Showing the dialog to ask user for confirmation");

        askUserConfirmationUITh();

        synchronized (mUserConfirmedNotif) {
            try {
                mUserConfirmedNotif.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // finish the rest of adding new device protocol
        AddNewDeviceWizzard.this.runOnUiThread(new Runnable() {
            public void run() {
                mAddDeviceProgressDialog = ProgressDialog.show(AddNewDeviceWizzard.this,
                        "Add new device",
                        "Sending HMC information.\nPlease wait...", true, false);
            }
        });

        return mUserConfirmed;

    }

    private void askUserConfirmationUITh() {
        AddNewDeviceWizzard.this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    new AlertDialog.Builder(AddNewDeviceWizzard.this)
                            .setTitle("Please confirm adding this device")
                            .setMessage(
                                    "Name: " + mNewDeviceDesc.getDeviceName() + "Fingerprint: "
                                            + mNewDeviceDesc.getFingerprint())
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    synchronized (mUserConfirmedNotif) {
                                        mUserConfirmed = Boolean.valueOf(true);
                                        mUserConfirmedNotif.notify();
                                    }
                                }
                            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    synchronized (mUserConfirmedNotif) {
                                        mUserConfirmed = Boolean.valueOf(false);
                                        mUserConfirmedNotif.notify();
                                    }
                                }
                            }).show();
                } catch (RemoteException e) {
                    Log.e(TAG, "Error retrieving descriptor attributes");
                    e.printStackTrace();
            }
            }
        });
    }

    private class AddDeviceAsyncTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... param) {
            IHMCServerHndl hmcServerHmdl = (IHMCServerHndl) param[0];
            String fullJID = (String) param[1];
            boolean addSuccess = true;
            try {
                addSuccess = hmcServerHmdl.addNewDevice(fullJID);
            } catch (RemoteException e) {
                Log.e(TAG,
                        "Problem calling remote method in HMCService: addNewDevice on HMCServerHandler");
                e.printStackTrace();
                addSuccess = false;
            }
            return new Boolean(addSuccess);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // mConnectionRemoteException = result;
            Log.d(TAG, "Device adition finished");
            mAddDeviceProgressDialog.dismiss();
            if (result == true) {
                doUnbindService();
                finish();
            } else {
                // show user notification
                AddNewDeviceWizzard.this.runOnUiThread(new Runnable() {
                    public void run() {
                        HMCUserNotifications.normalToast(AddNewDeviceWizzard.this,
                                "The new device was not added");
                    }
                });
            }
        }
    }
}

