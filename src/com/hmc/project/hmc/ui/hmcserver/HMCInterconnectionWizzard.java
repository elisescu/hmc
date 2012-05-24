/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui.hmcserver;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IDeviceDescriptor;
import com.hmc.project.hmc.aidl.IHMCConnection;
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

// TODO: Auto-generated Javadoc
/**
 * The Class HMCInterconnectionWizzard.
 */
public class HMCInterconnectionWizzard extends Activity {

    /** The Constant TAG. */
    protected static final String TAG = "DeviceMainScreen";

    /** The m is bound. */
    private boolean mIsBound;

    /** The m bound service. */
    private HMCService mBoundService;

    /** The m hmc connection. */
    private IHMCConnection mHMCConnection;

    /** The m hmc application. */
    private HMCApplication mHMCApplication;

    /** The m info text view. */
    private TextView mInfoTextView;

    /** The m jid text view. */
    private EditText mJidTextView;  

    /** The m context. */
    private Context mContext; 

    /** The m user requests listener. */
    private UserRequestsListener mUserRequestsListener = new UserRequestsListener();

    /** The m interconnect progress dialog. */
    private ProgressDialog mInterconnectProgressDialog;

    /** The m external hmc server. */
    private IDeviceDescriptor mExternalHMCServer;

    /** The m valid jid. */
    private boolean mValidJid;

    /** The m user confirmed. */
    private Boolean mUserConfirmed = new Boolean(false);;

    /** The m user confirmed notif. */
    private Object mUserConfirmedNotif = new Object();

    /** The m external hmc name. */
    private String mExternalHMCName;

    /** The m buttons listener. */
    private OnClickListener mButtonsListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.interconnect_hmc_butt_interc: {
                    if (checkUsername(mJidTextView.getText().toString())) {
                        try {
                            interconnectToHMC(mJidTextView.getText().toString());
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

    /**
     * Interconnect to hmc.
     * 
     * @param newDevFullJid
     *            the new dev full jid
     * @throws Exception
     *             the exception
     */
    private void interconnectToHMC(String newDevFullJid) throws Exception {
        if (mHMCConnection == null) {
            throw new Exception();
        } else {
            IHMCManager hmcMng = mHMCConnection.getHMCManager();
            IHMCServerHndl hmcServerHmdl = hmcMng.implHMCServer();
            hmcServerHmdl.registerUserRequestsListener(mUserRequestsListener);
            mInterconnectProgressDialog = ProgressDialog.show(this, "Interconnecting",
                    "Getting information from external HMC server.\nPlease wait...", true,
                    false);
            new HMCInterconnectionAsyncTask().execute(hmcServerHmdl, newDevFullJid);
        }
    }

    /** The m connection. */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);
            if (mHMCConnection != null) {

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(HMCInterconnectionWizzard.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Do bind service.
     */
    void doBindService() {
        bindService(new Intent(HMCInterconnectionWizzard.this,
                HMCService.class), mConnection, Context.BIND_AUTO_CREATE);
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

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        mHMCApplication = (HMCApplication)getApplication();
        mContext = this;

        setContentView(R.layout.interconnect_hmc_wizzard);

        mJidTextView = (EditText) findViewById(R.id.add_new_dev_text_jid);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.interconnect_hmc_butt_interc);
        button.setOnClickListener(mButtonsListener);

        // make sure we ended up in this activity with the app connected to XMPP
        // server
        if (!mHMCApplication.isConnected()) {
            doUnbindService();
            finish();
        }
    }
    
    /**
     * Check username.
     * 
     * @param username
     *            the username
     * @return true, if successful
     */
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
    
    /**
     * The listener interface for receiving userRequests events. The class that
     * is interested in processing a userRequests event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addUserRequestsListener<code> method. When
     * the userRequests event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see UserRequestsEvent
     */
    class UserRequestsListener extends IUserRequestsListener.Stub {

        /*
         * (non-Javadoc)
         * @see
         * com.hmc.project.hmc.aidl.IUserRequestsListener#confirmDeviceAddition
         * (com.hmc.project.hmc.aidl.IDeviceDescriptor)
         */
        @Override
        public boolean confirmDeviceAddition(IDeviceDescriptor newDevice) throws RemoteException {
            return false;
        }

        /*
         * (non-Javadoc)
         * @see
         * com.hmc.project.hmc.aidl.IUserRequestsListener#verifyFingerprint(
         * java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public boolean verifyFingerprint(String localFingerprint, String remoteFingerprint,
                String deviceName) throws RemoteException {
            // TODO Auto-generated method stub
            return false;
        }

        /*
         * (non-Javadoc)
         * @see
         * com.hmc.project.hmc.aidl.IUserRequestsListener#confirmHMCInterconnection
         * (com.hmc.project.hmc.aidl.IDeviceDescriptor, java.lang.String)
         */
        @Override
        public boolean confirmHMCInterconnection(IDeviceDescriptor remoteHMCServer,
                                String remoteHMCName) throws RemoteException {
            mExternalHMCServer = remoteHMCServer;
            mExternalHMCName = remoteHMCName;
            // mInfoTextView.setText(newDevice.toString());
            mInterconnectProgressDialog.dismiss();
            // ask the user to confirm the device description

            return askUserConfirmation();
        }
    }
    
    /**
     * Ask user confirmation.
     * 
     * @return true, if successful
     * @throws RemoteException
     *             the remote exception
     */
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
        HMCInterconnectionWizzard.this.runOnUiThread(new Runnable() {
            public void run() {
                mInterconnectProgressDialog = ProgressDialog.show(HMCInterconnectionWizzard.this,
                        "Add new device",
                        "Sending HMC information.\nPlease wait...", true, false);
            }
        });

        return mUserConfirmed;

    }

    /**
     * Ask user confirmation ui th.
     */
    private void askUserConfirmationUITh() {
        HMCInterconnectionWizzard.this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    new AlertDialog.Builder(HMCInterconnectionWizzard.this)
                                            .setTitle("Please confirm interconnection to "
                                                                    + mExternalHMCName)
                            .setMessage("Name: " + mExternalHMCServer.getDeviceName() + 
                                        "\nFingerprint: "+ mExternalHMCServer.getFingerprint()+
                                        "\n\n\nMy fingerprint:\n" + 
                                        mHMCConnection.getHMCManager().getLocalDevDescriptor()
                                        .getFingerprint())
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

    /**
     * The Class HMCInterconnectionAsyncTask.
     */
    private class HMCInterconnectionAsyncTask extends AsyncTask<Object, Void, Boolean> {

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Boolean doInBackground(Object... param) {
            IHMCServerHndl hmcServerHndl = (IHMCServerHndl) param[0];
            String fullJID = (String) param[1];
            boolean interconnectionSuccess = true;
            try {
                interconnectionSuccess = hmcServerHndl.interconnectTo(fullJID);
            } catch (RemoteException e) {
                Log.e(TAG,
                        "Problem calling remote method in HMCService: addNewDevice on HMCServerHandler");
                e.printStackTrace();
                interconnectionSuccess = false;
            }
            return new Boolean(interconnectionSuccess);
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Boolean result) {
            // mConnectionRemoteException = result;
            Log.d(TAG, "Interconnection finished with success = " + result.booleanValue());
            mInterconnectProgressDialog.dismiss();
            if (result == true) {
                doUnbindService();
                finish();
            } else {
                // show user notification
                HMCInterconnectionWizzard.this.runOnUiThread(new Runnable() {
                    public void run() {
                        HMCUserNotifications.normalToast(HMCInterconnectionWizzard.this,
                                                "The interconnection failed");
                    }
                });
            }
        }
    }
}

