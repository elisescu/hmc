/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.aidl;

import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.aidl.IConnectionListener;

interface IHMCFacade {
    
    // get the HMC manager from the HMC service
    IHMCManager getHMCManager();
    
    // register a connection listener for getting the connection status
    void registerConnectionListener(IConnectionListener conListener);
    
    // register a connection listener for getting the connection status
    void unregisterConnectionListener(IConnectionListener conListener);
    
    
    // connect to XMPP server using user's credentials entered by user
    void connect(String fullJID, String password, int port);
    
    // register an XMPP account on the XMPP server using in-band-registration
    void registerXMPPAccount(String fullJID, String password);
    
    // disconnect from XMPP server
    void disconnect();
    
}