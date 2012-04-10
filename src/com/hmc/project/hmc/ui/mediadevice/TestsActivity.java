/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.mediadevice;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCMediaClientHndl;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.hmcserver.ConfirmHMCInterconnection;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vasile Popescu
 *
 */
public class TestsActivity extends Activity {
    protected static final String TAG = "TestsAactivity";
    public static final String TESTS_JID_KEY = null;
    private IHMCConnection mHMCConnection;
    private IHMCManager mHMCManager;
    private HMCService mBoundService;
    private boolean mIsBound;
    private HMCApplication mHMCApplication;
    private String mJIDToDoTests;
    private TextView mResultsTextView;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hmc_tests_activity);

        doBindService();
        mHMCApplication = (HMCApplication) getApplication();
        Intent sender = getIntent();
        mJIDToDoTests = sender.getExtras().getString(TESTS_JID_KEY);

        Button mRunTestsButton = (Button) findViewById(R.id.run_tests);
        mRunTestsButton.setOnClickListener(mButtonsClickListener);
        mResultsTextView = (TextView) findViewById(R.id.tests_results);
    }

    private void runTests() {
        if (mIsBound && mHMCApplication.isConnected()) {
            try {
                IHMCMediaClientHndl implHndl = mHMCManager.implHMCMediaClient();
                mResultsTextView.setText("Please wait...");
                String testsResults = implHndl.runTests(mJIDToDoTests);
                mResultsTextView.setText(testsResults);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mResultsTextView.setText("not connected !");
        }
    }

    View.OnClickListener mButtonsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.run_tests:
                    runTests();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    /** The connection to HMCService. */
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
            Toast.makeText(TestsActivity.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Bind the HMCService
     */
    void doBindService() {
        bindService(new Intent(TestsActivity.this, HMCService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Unbind the HMCService
     */
    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }
}
