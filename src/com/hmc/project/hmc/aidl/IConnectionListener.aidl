/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.aidl;

interface IConnectionListener {
   // void connectionClosed();
    void connectionClosedOnError(String arg0);
    //void reconnectingIn(int arg0);
    //void reconnectionFailed(String arg0);
    //void reconnectionSuccessful();
    void connectionSuccessful(boolean success);
    void connectionProgress(String status);
}
