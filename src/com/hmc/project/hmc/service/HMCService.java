/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
 **/

package com.hmc.project.hmc.service;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.EventProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.PubSubProvider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.ui.Login;

import de.duenndns.ssl.MemorizingTrustManager;


// TODO: Auto-generated Javadoc
/**
 * The Class HMCService.
 */
public class HMCService extends Service {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCService";
    
    /** The m notification manager. */
    private NotificationManager mNotificationManager;
    
    /** The m notification id. */
    private int mNotificationId = 0xbaba;
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    /** The m hmc connection. */
    private IHMCConnection.Stub mHMCConnection;
   

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        
        /**
         * Gets the service.
         *
         * @return the service
         */
        public HMCService getService() {
            return HMCService.this;
        }
    }

    /* (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // create the XMPPFacade that the UI activities will use 
        mHMCConnection = new HMCConnection(this);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }
    
    /* (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /* (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNotificationManager.cancel(mNotificationId);

        Log.d(TAG,"HMC Service was stopped");
        // Tell the user we stopped.
        Toast.makeText(this, "HMC was closed", Toast.LENGTH_SHORT).show();
    }

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mHMCConnection;
    }

    // TODO: taken from Beem. Rewrite it!
    /**
     * Show notification.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Tap here";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Login.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(mNotificationId, notification);
    }

    // TODO: taken from Beem. Rewrite it!
    /**
     * Send notification.
     *
     * @param id the id
     * @param notif the notif
     */
    public void sendNotification(int id, Notification notif) {
        notif.ledARGB = 0xff0000ff; // Blue color
        notif.ledOnMS = 1000;
        notif.ledOffMS = 1000;
        notif.defaults |= Notification.DEFAULT_LIGHTS;
        mNotificationManager.notify(id, notif);
    }
}

