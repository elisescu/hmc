package com.hmc.project.hmc.ui;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.R.id;
import com.hmc.project.hmc.R.layout;
import com.hmc.project.hmc.R.string;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.service.HMCConnection;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.utils.HMCUserNotifications;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LocalServiceActivities {
    /**
     * <p>Example of explicitly starting and stopping the local service.
     * This demonstrates the implementation of a service that runs in the same
     * process as the rest of the application, which is explicitly started and stopped
     * as desired.</p>
     * 
     * <p>Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {


        private SharedPreferences mSettings;

        private HMCApplication mHMCApplication;

        @Override
        public final boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.controler_service, menu);
            return true;
        }

        @Override
        public final boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.controler_list_menu_preferences:
                    startActivityForResult(new Intent(this, HMCSettings.class),23);
                    return true;
                default:
                    return false;
            }
        }
        
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.d("CheckStartActivity","onActivityResult and resultCode = "+resultCode);
            // TODO Auto-generated method stub
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == 23) {
                if (mHMCApplication.isConfigured())
                    HMCUserNotifications.normalToast(this, "Account configured");
                else {
                    HMCUserNotifications.normalToast(this, "You must configure the account");
                    startActivityForResult(new Intent(this, HMCSettings.class),23);
                }
            }
            else{
                HMCUserNotifications.normalToast(this, "Returned from wrong activity: "+resultCode);
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.local_service_controller);
            
            mHMCApplication = (HMCApplication)getApplication();

            // Watch for button clicks.
            Button button = (Button)findViewById(R.id.start);
            button.setOnClickListener(mStartListener);
            button = (Button)findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);

            mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        }

        private OnClickListener mStartListener = new OnClickListener() {
            public void onClick(View v) {
                // Make sure the service is started.  It will continue running
                // until someone calls stopService().  The Intent we use to find
                // the service explicitly specifies our service component, because
                // we want it running in our own process and don't want other
                // applications to replace it.
                startService(new Intent(Controller.this,
                        HMCService.class));
                
                startActivity(new Intent(Controller.this, Binding.class));
                finish();
                
            }
        };

        private OnClickListener mStopListener = new OnClickListener() {
            public void onClick(View v) {
                // Cancel a previous call to startService().  Note that the
                // service will not actually stop at this point if there are
                // still bound clients.
                stopService(new Intent(Controller.this,
                        HMCService.class));
            }
        };
    }

    // ----------------------------------------------------------------------

    /**
     * Example of binding and unbinding to the local service.
     * This demonstrates the implementation of a service which the client will
     * bind to, receiving an object through which it can communicate with the service.</p>
     * 
     * <p>Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Binding extends Activity {
        protected static final String TAG = "BindingActivity";


        private boolean mIsBound;
        private HMCService mBoundService;

        private ServiceConnection mConnection = new ServiceConnection() {
            private IHMCConnection mHMCConnection;

            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                // mBoundService = ((IHMCConnection)service).getService();
                
                
                mHMCConnection = IHMCConnection.Stub.asInterface(service);
                
                try {
                    mHMCConnection.connect("elisescu_1@jabber.org", "Cucurigu1", 5222);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                
                Log.d(TAG,"Successfully binded the service");

                // Tell the user about this for our demo.
                Toast.makeText(Binding.this, R.string.local_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                mBoundService = null;
                Toast.makeText(Binding.this, R.string.local_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };

        void doBindService() {
            // Establish a connection with the service.  We use an explicit
            // class name because we want a specific service implementation that
            // we know will be running in our own process (and thus won't be
            // supporting component replacement by other applications).
            bindService(new Intent(Binding.this, 
                    HMCService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        void doUnbindService() {
            if (mIsBound) {
                // Detach our existing connection.
                unbindService(mConnection);
                mIsBound = false;
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            doUnbindService();
        }


        private OnClickListener mBindListener = new OnClickListener() {
            public void onClick(View v) {
                doBindService();
            }
        };

        private OnClickListener mUnbindListener = new OnClickListener() {
            public void onClick(View v) {
                doUnbindService();
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.local_service_binding);

            // Watch for button clicks.
            Button button = (Button)findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button)findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
        }
    }
}
