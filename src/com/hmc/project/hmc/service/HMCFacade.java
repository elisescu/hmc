/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
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

import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IConnectionListener;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCFacade;

import de.duenndns.ssl.MemorizingTrustManager;

/**
 * @author elisescu
 *
 */
public class HMCFacade extends IHMCFacade.Stub {

    private static final String TAG = "XMPPFacade";
    private HMCManager mHMCManager;
    private Connection mXMPPConnection = null;
    private HMCService mHMCService;
    private ConnectionListener mConnectionListener = new HMCConnectionListener();
    private IConnectionListener mRemoteConnectionListener;
    private RemoteException mConnectionRemoteException;
    //private SSLContext mSslContext;

    public HMCFacade(HMCService hmcService) {
        mHMCManager = new HMCManager();
        mHMCService = hmcService;
        // TODO Auto-generated constructor stub
    }

    @Override
    public IHMCManager getHMCManager() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerConnectionListener(IConnectionListener conListener) throws RemoteException {
        mRemoteConnectionListener = conListener;
    }

    // connect and login to XMPP server. this is a blocking call so it should not be 
    // called in UI thread
    @Override
    public void connect(String fullJID, String password, int port) throws RemoteException {
        String lXMPPServer = StringUtils.parseServer(fullJID);
        
        if (mXMPPConnection == null) {
            mXMPPConnection = createXMPPConnection(lXMPPServer, port);
        }

        Log.d(TAG, "created connection to =<"+lXMPPServer+":"+port+"> server");

        try {
            mXMPPConnection.connect();
            mXMPPConnection.login(fullJID, password);
        } catch (XMPPException e) {
            Log.e(TAG, "Error when connecting to XMPP Server");
            e.printStackTrace();
            throw new RemoteException();
        }

        
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Online");
        // Send the packet (assume we have a Connection instance called "con").
        mXMPPConnection.sendPacket(presence);
        
        Log.d(TAG,"Connected. Secure="+mXMPPConnection.isSecureConnection());
    }
    
    
    @Override
    public void connectAsync(String fullJID, String password, int port) throws RemoteException {
        String lXMPPServer = StringUtils.parseServer(fullJID);
        
        if (mXMPPConnection == null) {
            mXMPPConnection = createXMPPConnection(lXMPPServer, port);
        }
        
        new LoginAsyncTask().execute(mXMPPConnection, fullJID, password);
    }

    // create the XMPPConnection to be used for login, for getting the chatmanager, etc
    public Connection createXMPPConnection(String xmppServer, int port) {
        ConnectionConfiguration lConnConfig = null;
        Connection lXMPPConnection = null;
        lConnConfig = initConnectionConfiguration(xmppServer, port);

        if (lConnConfig != null) {
            lXMPPConnection = new XMPPConnection(lConnConfig);
        }
        return lXMPPConnection;
    }


    @Override
    public void registerXMPPAccount(String fullJID, String password) throws RemoteException {
        // TODO Auto-generated method stub
    }

    @Override
    public void unregisterConnectionListener(IConnectionListener conListener)
            throws RemoteException {
        mRemoteConnectionListener = null;
    }
    
    @Override
    public void disconnect() throws RemoteException {
       if (mXMPPConnection != null && mXMPPConnection.isConnected()) {
           mXMPPConnection.disconnect(new Presence(Type.unavailable));
       }
        
    }
    
    private class HMCConnectionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            // notify remote listener
            Log.e(TAG," connectionClosed() event received ");
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            // notify remote listener
            Log.e(TAG,"connectionClosedOnError event received: "+arg0.toString());
            try {
                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.connectionClosedOnError(arg0.toString());
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void reconnectingIn(int arg0) {
            // notify remote listener
            Log.e(TAG,"reconnectingIn event received: "+arg0);
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            // notify remote listener
            Log.e(TAG,"reconnectionFailed event received: "+arg0);
         }

        @Override
        public void reconnectionSuccessful() {
            // notify remote listener
            Log.e(TAG,"reconnectionSuccessful event received: ");
        }

    }
    

    private ConnectionConfiguration initConnectionConfiguration(String xmppServer, int port) {
    
        //TODO: check to see what's the deal with this ProviderManager on smack website
        //configureProviderManager(ProviderManager.getInstance());
        
        Log.d(TAG,"creating ConnectionConfiguration with "+xmppServer+","+port);
        ConnectionConfiguration lConnectionConfiguration = new ConnectionConfiguration(xmppServer, port);
        
        // comment this out to disable debugging
        lConnectionConfiguration.setDebuggerEnabled(true);
        
        lConnectionConfiguration.setSendPresence(false);
        
        lConnectionConfiguration.setSecurityMode(SecurityMode.required);
        lConnectionConfiguration.setTruststoreType("BKS");
        lConnectionConfiguration.setTruststorePath("/system/etc/security/cacerts.bks");
        
        SSLContext lSslContext = null;
        // taken from Beem project
        //TODO: activate SSL for communication with XMPP server
        try {
            lSslContext = SSLContext.getInstance("TLS");
            lSslContext.init(null, MemorizingTrustManager.getInstanceList(mHMCService),
                    new java.security.SecureRandom());
        } catch (GeneralSecurityException e) {
            Log.w(TAG, "Unable to use MemorizingTrustManager", e);
        }
        if (lSslContext != null)
            lConnectionConfiguration.setCustomSSLContext(lSslContext);
        
        return lConnectionConfiguration;

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




        private class LoginAsyncTask extends AsyncTask<Object, Void, Boolean> {
            private RemoteException mRemoteException = null;
            @Override
            protected Boolean doInBackground(Object... param) {
                XMPPConnection conn = (XMPPConnection)param[0];
                String username = (String)param[1];
                String password = (String)param[2];
                //Integer port = (Integer)param[3];

                Boolean success = new Boolean(true);
                //connect(username, password, port.intValue());
                try {
                    conn.connect();
                    conn.login(username, password);
                } catch (XMPPException e) {
                    mRemoteException = new RemoteException();
                    e.printStackTrace();
                    success = new Boolean(false);
                }
                
                Log.d(TAG,"Logged in");
                
                return success;
            }
            
            @Override
            protected void onPostExecute(Boolean result) {
                //mConnectionRemoteException = result;
                Log.d(TAG,"Connection and login finished:"+result.booleanValue());
                if (mRemoteConnectionListener != null) {
                    try {
                        mRemoteConnectionListener.connectionSuccessful(result.booleanValue());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

}
