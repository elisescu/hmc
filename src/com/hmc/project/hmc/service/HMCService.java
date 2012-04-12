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
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IHMCFacade;
import com.hmc.project.hmc.ui.LocalServiceActivities;

import de.duenndns.ssl.MemorizingTrustManager;


public class HMCService extends Service {
    private static final String TAG = "HMCService";
    private NotificationManager mNotificationManager;
    private int mNotificationId = 0xbaba;
    private SharedPreferences mSettings;
    private String mUsername;
    private String mPassword;
    private Connection mConnection;
    private String mXMPPServer;
    private ConnectionConfiguration mConnectionConfiguration;
    private SSLContext sslContext;
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private IHMCFacade.Stub mHMCFacade;
    private int mPort;
   

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public HMCService getService() {
            return HMCService.this;
        }
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mHMCFacade = new HMCFacade(this);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    /**
     * 
     */
    private void initConnectionConfiguration() {
        mXMPPServer = StringUtils.parseServer(mUsername);
        mPort = 5222;
        
        //TODO: check to see what's the deal with this ProviderManager on smack website
        configureProviderManager(ProviderManager.getInstance());
        
        Log.d(TAG,"creating ConnectionConfiguration with "+mXMPPServer+","+mPort);
        mConnectionConfiguration = new ConnectionConfiguration(mXMPPServer, mPort);
        
        // comment this out to disable debugging
        mConnectionConfiguration.setDebuggerEnabled(true);
        
        mConnectionConfiguration.setSendPresence(false);
        
        mConnectionConfiguration.setSecurityMode(SecurityMode.required);
        mConnectionConfiguration.setTruststoreType("BKS");
        mConnectionConfiguration.setTruststorePath("/system/etc/security/cacerts.bks");
        
        // taken from Beem project
        //TODO: activate SSL for communication with XMPP server
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, MemorizingTrustManager.getInstanceList(this),
                    new java.security.SecureRandom());
        } catch (GeneralSecurityException e) {
            Log.w(TAG, "Unable to use MemorizingTrustManager", e);
        }
        if (sslContext != null)
            mConnectionConfiguration.setCustomSSLContext(sslContext);        

    }

    //THIS METHOD IS TAKEN FROM BEEM project. //TODO: Understand and rewrite it!
    private void configureProviderManager(ProviderManager pm) {
        Log.d(TAG, "configure");
        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

        // Privacy
        //pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        // Delayed Delivery only the new version
        pm.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInfoProvider());

        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

        // Chat State
        ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
                chatState);
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", chatState);
        //Pubsub
        pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub", new PubSubProvider());
        pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub", new ItemsProvider());
        pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub", new ItemsProvider());
        pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new ItemProvider());

        pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub#event", new ItemsProvider());
        pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new ItemProvider());
        pm.addExtensionProvider("event", "http://jabber.org/protocol/pubsub#event", new EventProvider());
        //PEP avatar
        //TODO: check the deal with these two lines
        //pm.addExtensionProvider("metadata", "urn:xmpp:avatar:metadata", new AvatarMetadataProvider());
        //pm.addExtensionProvider("data", "urn:xmpp:avatar:data", new AvatarProvider());

        // ping
        // TODO: check the deal with Ping :-S
        //pm.addIQProvider(PingExtension.ELEMENT, PingExtension.NAMESPACE, PingExtension.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNotificationManager.cancel(mNotificationId);

        if (mConnection != null && mConnection.isConnected()) {
            mConnection.disconnect(new Presence(Presence.Type.unavailable));
        }

        Log.d(TAG,"HMC Service was stopped");
        // Tell the user we stopped.
        Toast.makeText(this, "HMC was closed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mHMCFacade;
    }

    // create the XMPPConnection and return it to the HMCFacade where it will 
    // be used for login, getting the chatmanager, and all other XMPP jobs
    public Connection createXMPPConnection(String username, String password) {
        if (mConnection == null) {
            //mUsername = mSettings.getString("hmc_username_key", "no_username");
            //mPassword = mSettings.getString("hmc_pass_key", "no_pass");
            mUsername = username;
            mPassword = password;

            initConnectionConfiguration();
            
            Log.d(TAG, "connection initialized with username=<"+mUsername+">"+
                    "password length="+mPassword.length()+" to server=<>"+mSettings+
                    "and sslContext="+sslContext);
            
            mConnection = new XMPPConnection(mConnectionConfiguration); 
        }
        return mConnection;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Tap here";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LocalServiceActivities.Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(mNotificationId, notification);
    }
}

