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
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IConnectionListener;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IUserRequestsListener;

import de.duenndns.ssl.MemorizingTrustManager;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCConnection.
 *
 * @author elisescu
 */
public class HMCConnection extends IHMCConnection.Stub {

    /** The Constant TAG. */
    private static final String TAG = "HMCConnection";
    
    /** The m hmc manager. */
    private HMCManager mHMCManager = null;
    
    /** The m xmpp connection. */
    private Connection mXMPPConnection = null;
    
    /** The m hmc service. */
    private HMCService mHMCService = null;
    
    /** The m connection listener. */
    private ConnectionListener mConnectionListener = new HMCConnectionListener();
    
    /** The m remote connection listener. */
    private IConnectionListener mRemoteConnectionListener;
    
    /** The m connection remote exception. */
    private RemoteException mConnectionRemoteException;
    
    /** The m full jid. */
    private String mFullJID;
    
    /** The m password. */
    private String mPassword;
    
    /** The m port. */
    private int mPort;

    /**
     * Instantiates a new hMC connection.
     *
     * @param hmcService the hmc service
     */
    public HMCConnection(HMCService hmcService) {
        mHMCService = hmcService;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#getHMCManager()
     */
    @Override
    public IHMCManager getHMCManager() throws RemoteException {
        if (mHMCManager == null) {
            if (mXMPPConnection != null && mXMPPConnection.isAuthenticated()) {
                mHMCManager = new HMCManager(mXMPPConnection, mHMCService);
            }
        }
        return mHMCManager;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#registerConnectionListener(com.hmc.project.hmc.aidl.IConnectionListener)
     */
    @Override
    public void registerConnectionListener(IConnectionListener conListener) throws RemoteException {
        mRemoteConnectionListener = conListener;
    }

    /**
     * Connect l.
     *
     * @param fullJID the full jid
     * @param password the password
     * @param port the port
     * @throws XMPPException the xMPP exception
     */
    private void connectL(String fullJID, String password, int port) throws XMPPException {
        String lXMPPServer = StringUtils.parseServer(fullJID);
        String lBareJid = StringUtils.parseBareAddress(fullJID);
        String lResource = StringUtils.parseResource(fullJID);
        String lUsername = StringUtils.parseName(fullJID);
        
        if (mXMPPConnection == null) {
            mXMPPConnection = createXMPPConnection(lXMPPServer, port);
        }
        if (mXMPPConnection != null) {
            mXMPPConnection.connect();
            if (mXMPPConnection.isConnected()) {
                try {
                    mRemoteConnectionListener.connectionProgress("Authenticating...");
                    mXMPPConnection.login(lUsername, password, lResource);
                } catch (java.lang.IllegalStateException e) {
                    throw new XMPPException();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Fatal error: cannot connect to server");
                try {
                    mRemoteConnectionListener.connectionSuccessful(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            mXMPPConnection.addConnectionListener(mConnectionListener);
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Online");
            presence.setFrom(fullJID);
            mXMPPConnection.sendPacket(presence);
            
            if (mXMPPConnection.isAuthenticated()) {
                Log.d(TAG, "Connected. Secure=" + mXMPPConnection.isSecureConnection());
            }
        } else {
            throw new XMPPException();
        }

    }
    // connect and login to XMPP server. this is a blocking call so it should not be 
    // called in UI thread
    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#connect(java.lang.String, java.lang.String, int)
     */
    @Override
    public void connect(String fullJID, String password, int port) throws RemoteException {
        try {
            connectL(fullJID, password, port);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RemoteException();
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#connectAsync(java.lang.String, java.lang.String, int)
     */
    @Override
    public void connectAsync(String fullJID, String password, int port) throws RemoteException {
        mFullJID = fullJID;
        mPassword = password;
        mPort = port;
        new Thread(new Runnable() {
            public void run() {
                boolean success = new Boolean(true);
                
                Looper.prepare();

                try {
                    connectL(mFullJID, mPassword, mPort);
                } catch (XMPPException e) {
                    e.printStackTrace();
                    success = false;
                }

                if (mRemoteConnectionListener != null) {
                    try {
                        mRemoteConnectionListener.connectionSuccessful(success);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // create the XMPPConnection to be used for login, for getting the chatmanager, etc
    /**
     * Creates the xmpp connection.
     *
     * @param xmppServer the xmpp server
     * @param port the port
     * @return the connection
     */
    public Connection createXMPPConnection(String xmppServer, int port) {
        ConnectionConfiguration lConnConfig = null;
        Connection lXMPPConnection = null;
        lConnConfig = initConnectionConfiguration(xmppServer, port);

        if (lConnConfig != null) {
            lXMPPConnection = new XMPPConnection(lConnConfig);
        }
        return lXMPPConnection;
    }


    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#registerXMPPAccount(java.lang.String, java.lang.String)
     */
    @Override
    public void registerXMPPAccount(String fullJID, String password) throws RemoteException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#unregisterConnectionListener(com.hmc.project.hmc.aidl.IConnectionListener)
     */
    @Override
    public void unregisterConnectionListener(IConnectionListener conListener)
            throws RemoteException {
        mRemoteConnectionListener = null;
    }
    
    /* (non-Javadoc)
     * @see com.hmc.project.hmc.aidl.IHMCConnection#disconnect()
     */
    @Override
    public void disconnect() throws RemoteException {
       if (mXMPPConnection != null && mXMPPConnection.isConnected()) {
            mHMCManager.deInit();
            mXMPPConnection.disconnect(new Presence(Type.unavailable));
       }
        
    }
    
    /**
     * The listener interface for receiving HMCConnection events.
     * The class that is interested in processing a HMCConnection
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addHMCConnectionListener<code> method. When
     * the HMCConnection event occurs, that object's appropriate
     * method is invoked.
     *
     * @see HMCConnectionEvent
     */
    private class HMCConnectionListener implements ConnectionListener {
        
        /* (non-Javadoc)
         * @see org.jivesoftware.smack.ConnectionListener#connectionClosed()
         */
        @Override
        public void connectionClosed() {
            // notify remote listener
            Log.w(TAG, " connectionClosed() event received ");
        }

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.ConnectionListener#connectionClosedOnError(java.lang.Exception)
         */
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

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.ConnectionListener#reconnectingIn(int)
         */
        @Override
        public void reconnectingIn(int arg0) {
            // notify remote listener
            Log.w(TAG, "reconnectingIn event received: " + arg0);
        }

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.ConnectionListener#reconnectionFailed(java.lang.Exception)
         */
        @Override
        public void reconnectionFailed(Exception arg0) {
            // notify remote listener
            Log.e(TAG,"reconnectionFailed event received: "+arg0);
         }

        /* (non-Javadoc)
         * @see org.jivesoftware.smack.ConnectionListener#reconnectionSuccessful()
         */
        @Override
        public void reconnectionSuccessful() {
            // notify remote listener
            Log.i(TAG, "reconnectionSuccessful event received: ");
        }

    }
    

    /**
     * Inits the connection configuration.
     *
     * @param xmppServer the xmpp server
     * @param port the port
     * @return the connection configuration
     */
    private ConnectionConfiguration initConnectionConfiguration(String xmppServer, int port) {
    
        //TODO: check to see what's the deal with this ProviderManager on smack website
        configureProviderManager(ProviderManager.getInstance());
        
        Log.d(TAG,"creating ConnectionConfiguration with "+xmppServer+","+port);
        ConnectionConfiguration lConnectionConfiguration = new ConnectionConfiguration(xmppServer, port);
        
        // comment this out to disable debugging
        // lConnectionConfiguration.setDebuggerEnabled(true);
        
        lConnectionConfiguration.setSendPresence(false);
        
        lConnectionConfiguration.setSecurityMode(SecurityMode.enabled);
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
    /**
     * Configure provider manager.
     *
     * @param pm the pm
     */
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
}
