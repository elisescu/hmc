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

import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrEngineImpl;
import net.java.otr4j.OtrEngineListener;
import net.java.otr4j.OtrKeyManagerImpl;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.OtrPolicyImpl;
import net.java.otr4j.session.SessionID;
import android.util.Log;

public class HMCOTRManager implements OtrEngineHost {
    private static final String TAG = "HMCOTRManager";
    private static HMCOTRManager INSTANCE = new HMCOTRManager();
    private OtrKeyManagerImpl mOtrKeyManager;

    private HashMap<SessionID, SecureChat> mChats;
    private OtrEngineImpl mOtrEngine;
    private static final OtrPolicy mHMCOTRPolicy = new OtrPolicyImpl(OtrPolicy.ALLOW_V2
                            | OtrPolicy.ERROR_START_AKE);

    public HMCOTRManager() {
        try {
            mOtrKeyManager = new OtrKeyManagerImpl("/sdcard/HMCdata");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mOtrEngine = new OtrEngineImpl(this);
        mOtrEngine.addOtrEngineListener(new HMCOtrListener());
        mChats = new HashMap<SessionID, SecureChat>();
    }

    public void addChat(SessionID session, SecureChat chat) {
        mChats.put(session, chat);
        Log.d(TAG, "Added a new chat with " + chat.getParticipant() + "to session: " + session
                                + " Now have " + mChats.size() + " chats");
    }

    public void removeChat(SessionID session, SecureChat chat) {
        mChats.remove(session);
        Log.d(TAG, "Removed chat with " + chat.getParticipant() + "to session: " + session
                                + " Now have " + mChats.size() + " chats");
    }

    public static HMCOTRManager getInstance() {
        return INSTANCE;
    }

    public OtrEngineImpl getOtrEngine() {
        return mOtrEngine;
    }

    @Override
    public void injectMessage(SessionID sessionID, String msg) {
        SecureChat chat = mChats.get(sessionID);
        if (chat != null) {
            chat.injectMessage(msg);
        } else {
            Log.e(TAG, "Cannot find the device chat for session");
        }
    }

    @Override
    public void showWarning(SessionID sessionID, String warning) {
        // TODO Auto-generated method stub
        Log.w(TAG, "OTR warning for session <" + sessionID.toString() + ">: " + warning);
    }

    @Override
    public void showError(SessionID sessionID, String error) {
        // TODO Auto-generated method stub
        Log.e(TAG, "OTR error for session <" + sessionID.toString() + ">: " + error);
    }

    @Override
    public OtrPolicy getSessionPolicy(SessionID sessionID) {
        return mHMCOTRPolicy;
    }

    @Override
    public KeyPair getKeyPair(SessionID sessionID) {
        // TODO: taken from Beem project
        KeyPair kp = mOtrKeyManager.loadLocalKeyPair(sessionID);
        if (kp != null)
            return kp;
        mOtrKeyManager.generateLocalKeyPair(sessionID);
        return mOtrKeyManager.loadLocalKeyPair(sessionID);
    }

    public String getLocalFingerprint(String myFullJID) {
        // TODO: create a proper way to generate and get the key-pair
        SessionID dummySess = new SessionID(myFullJID, "dummy_JID_doesnt_matter", "xmpp");
        getKeyPair(dummySess);
        return mOtrKeyManager.getLocalFingerprint(dummySess);
    }
    private class HMCOtrListener implements OtrEngineListener {
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
