/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

public class HMCSecurityPolicy {
    // trust all devices that are able to negotiate the OTR session with my
    // device
    public final static int TRUST_EVERYBODY_ALWAYS = 0;

    // trust all devices that were authenticated by a HMCServer and which I got
    // knowledge about when my device joined HMC
    public final static int TRUST_HMC_ALWAYS = 1;

    // trust the remote device only if the time period of its fingerprint didn't
    // end
    public final static int TRUST_HMC_VALID = 2;

    // trust the device only if an HMCServer is telling me now that the device
    // is trusted
    public final static int TRUST_HMC_SERVER = 3;

    // trust the device only if the user verifies the fingerprint. For usability
    // reasons, we can add a nicer way for the user to verify the fingerprints:
    // using NFC for mobile devices or barcode scanning for pairing a mobile
    // device with a TY (the TV generates the barcode and the mobile device
    // scans is and authenticate each other).
    public final static int TRUST_HMC_USER = 4;

    private final static HMCSecurityPolicy INSTANCE = new HMCSecurityPolicy();
    private int mCurrentHMCSecurityPolicy;

    public int getHMCSecurityPolicy() {
        return mCurrentHMCSecurityPolicy;
    }

    public void setHMCSecurityPolicy(int pol) {
        mCurrentHMCSecurityPolicy = pol;
    }

    public static HMCSecurityPolicy getInstance() {
        return INSTANCE;
    }
}
