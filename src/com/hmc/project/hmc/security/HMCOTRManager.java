/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Iterator;

import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrEngineImpl;
import net.java.otr4j.OtrEngineListener;
import net.java.otr4j.OtrKeyManagerImpl;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.OtrPolicyImpl;
import net.java.otr4j.session.SessionID;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCOTRManager.
 */
public class HMCOTRManager implements OtrEngineHost {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCOTRManager";
    
    /** The INSTANCE. */
    private static HMCOTRManager INSTANCE = new HMCOTRManager();
    
    /** The m otr key manager. */
    private OtrKeyManagerImpl mOtrKeyManager;

    /** The m chats. */
    private HashMap<SessionID, SecureChat> mChats;
    
    /** The m otr engine. */
    private OtrEngineImpl mOtrEngine;
    
    /** The Constant mHMCOTRPolicy. */
    private static final OtrPolicy mHMCOTRPolicy = new OtrPolicyImpl(OtrPolicy.ALLOW_V2
                            | OtrPolicy.ERROR_START_AKE);

    /**
     * Instantiates a new hMCOTR manager.
     */
    public HMCOTRManager() {
        try {
            mOtrKeyManager = new OtrKeyManagerImpl("/sdcard/HMCkeystore.dat");
        } catch (IOException e) {
            Log.d(TAG, "Fatal error: we can't write to SD card");
            e.printStackTrace();
        }

        mOtrEngine = new OtrEngineImpl(this);
        mOtrEngine.addOtrEngineListener(new HMCOtrListener());
        mChats = new HashMap<SessionID, SecureChat>();
    }

    /**
     * Adds the chat.
     *
     * @param session the session
     * @param chat the chat
     */
    public void addChat(SessionID session, SecureChat chat) {
        mChats.put(session, chat);
        Log.d(TAG, "Added a new chat with " + chat.getParticipant() + "to session: " + session
                                + " Now have " + mChats.size() + " chats");
    }

    /**
     * Removes the chat.
     *
     * @param session the session
     * @param chat the chat
     */
    public void removeChat(SessionID session, SecureChat chat) {
        mChats.remove(session);
        Log.d(TAG, "Removed chat with " + chat.getParticipant() + "to session: " + session
                                + " Now have " + mChats.size() + " chats");
    }

    /**
     * Gets the single instance of HMCOTRManager.
     *
     * @return single instance of HMCOTRManager
     */
    public static HMCOTRManager getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the otr engine.
     *
     * @return the otr engine
     */
    public OtrEngineImpl getOtrEngine() {
        return mOtrEngine;
    }

    /* (non-Javadoc)
     * @see net.java.otr4j.OtrEngineHost#injectMessage(net.java.otr4j.session.SessionID, java.lang.String)
     */
    @Override
    public void injectMessage(SessionID sessionID, String msg) {
        SecureChat chat = mChats.get(sessionID);
        if (chat != null) {
            chat.injectMessage(msg);
        } else {
            Log.e(TAG, "Cannot find the device chat for session");
        }
    }

    /* (non-Javadoc)
     * @see net.java.otr4j.OtrEngineHost#showWarning(net.java.otr4j.session.SessionID, java.lang.String)
     */
    @Override
    public void showWarning(SessionID sessionID, String warning) {
        // TODO Auto-generated method stub
        Log.w(TAG, "OTR warning for session <" + sessionID.toString() + ">: " + warning);
    }

    /* (non-Javadoc)
     * @see net.java.otr4j.OtrEngineHost#showError(net.java.otr4j.session.SessionID, java.lang.String)
     */
    @Override
    public void showError(SessionID sessionID, String error) {
        // TODO Auto-generated method stub
        Log.e(TAG, "OTR error for session <" + sessionID.toString() + ">: " + error);
    }

    /* (non-Javadoc)
     * @see net.java.otr4j.OtrEngineHost#getSessionPolicy(net.java.otr4j.session.SessionID)
     */
    @Override
    public OtrPolicy getSessionPolicy(SessionID sessionID) {
        return mHMCOTRPolicy;
    }

    /* (non-Javadoc)
     * @see net.java.otr4j.OtrEngineHost#getKeyPair(net.java.otr4j.session.SessionID)
     */
    @Override
    public KeyPair getKeyPair(SessionID sessionID) {
        // TODO: taken from Beem project
        KeyPair kp = mOtrKeyManager.loadLocalKeyPair(sessionID);
        if (kp != null)
            return kp;
        mOtrKeyManager.generateLocalKeyPair(sessionID);
        return mOtrKeyManager.loadLocalKeyPair(sessionID);
    }

    /**
     * Gets the local fingerprint.
     *
     * @param myFullJID the my full jid
     * @return the local fingerprint
     */
    public String getLocalFingerprint(String myFullJID) {
        // TODO: create a proper way to generate and get the key-pair
        SessionID dummySess = new SessionID(myFullJID, "dummy_JID_doesnt_matter", "xmpp");
        getKeyPair(dummySess);
        return mOtrKeyManager.getLocalFingerprint(dummySess);
    }

    /**
     * Gets the remote fingerprint.
     *
     * @param remoteFullJID the remote full jid
     * @return the remote fingerprint
     */
    public String getRemoteFingerprint(String remoteFullJID) {
        // TODO: create a proper way to generate and get the key-pair
        SessionID sessId = new SessionID("dummy_JID_doesnt_matter", remoteFullJID, "xmpp");
        // getKeyPair(sessId);
        return mOtrKeyManager.getRemoteFingerprint(sessId);
    }

    /**
     * The listener interface for receiving HMCOtr events.
     * The class that is interested in processing a HMCOtr
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addHMCOtrListener<code> method. When
     * the HMCOtr event occurs, that object's appropriate
     * method is invoked.
     *
     * @see HMCOtrEvent
     */
    private class HMCOtrListener implements OtrEngineListener {
        
        /* (non-Javadoc)
         * @see net.java.otr4j.OtrEngineListener#sessionStatusChanged(net.java.otr4j.session.SessionID)
         */
        @Override
        public void sessionStatusChanged(final SessionID sessionID) {
            Log.d(TAG,"OTR Status changed for "+ sessionID+ " : " + 
                                    mOtrEngine.getSessionStatus(sessionID));
            
            if (mOtrKeyManager.loadRemotePublicKey(sessionID) == null) {
                mOtrKeyManager.savePublicKey(sessionID, mOtrEngine.getRemotePublicKey(sessionID));
            }

            SecureChat chat = mChats.get(sessionID);
            if (chat != null) {
                chat.otrStatusChanged(mOtrEngine.getSessionStatus(sessionID));
            }
        }
    }

}
