/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IConnectionListener;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IHMCFacade;

/**
 * @author elisescu
 *
 */
public class HMCFacade extends IHMCFacade.Stub {

    private static final String TAG = "XMPPFacade";
    private HMCManager mHMCManager;
    private Connection mXMPPConnection;
    private HMCService mHMCService;
    private ConnectionListener mConnectionListener = new HMCConnectionListener();
    private IConnectionListener mRemoteConnectionListener;
    private RemoteException mConnectionRemoteException;

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
    public void connect(String fullJID, String password) throws RemoteException {
        mXMPPConnection = mHMCService.createXMPPConnection(fullJID, password);

        Log.d(TAG,"Connection created");
        
        try {
            mXMPPConnection.connect();
            mXMPPConnection.login(fullJID, password);
        } catch (XMPPException e) {
            Log.e(TAG, "Error when connecting to XMPP Server");
            e.printStackTrace();
            throw new RemoteException();
        }

        Log.d(TAG,"Connected"+mXMPPConnection.isSecureConnection());
    }

    @Override
    public void registerXMPPAccount(String fullJID, String password) throws RemoteException {
        // TODO Auto-generated method stub
    }

    @Override
    public void unregisterConnectionListener(IConnectionListener conListener)
            throws RemoteException {
        // TODO Auto-generated method stub
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
            try {
                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.connectionClosed();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            try {
                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.reconnectingIn(arg0);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            // notify remote listener
            Log.e(TAG,"reconnectionFailed event received: "+arg0);
            try {
                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.reconnectionFailed(arg0.toString());
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void reconnectionSuccessful() {
            // notify remote listener
            Log.e(TAG,"reconnectionSuccessful event received: ");
            try {
                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.reconnectionSuccessful();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    //    private class LoginAsyncTask extends AsyncTask<XMPPConnection, Void, RemoteException> {
    //        private RemoteException mRemoteException = null;
    //        @Override
    //        protected RemoteException doInBackground(XMPPConnection... conn) {
    //            try {
    //                conn[0].connect();
    //            } catch (XMPPException e) {
    //                mRemoteException = new RemoteException();
    //                e.printStackTrace();
    //            }
    //            return null;
    //        }
    //        
    //        @Override
    //        protected void onPostExecute (RemoteException result) {
    //            mConnectionRemoteException = result;
    //        }
    //    }

}
