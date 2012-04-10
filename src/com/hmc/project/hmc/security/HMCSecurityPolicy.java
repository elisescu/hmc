/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCSecurityPolicy.
 */
public class HMCSecurityPolicy {
    // trust all devices that are able to negotiate the OTR session with my
    // device
    /** The Constant TRUST_EVERYBODY_ALWAYS. */
    public final static int TRUST_EVERYBODY_ALWAYS = 0;

    // trust all devices that were authenticated by a HMCServer and which I got
    // knowledge about when my device joined HMC
    /** The Constant TRUST_HMC_ALWAYS. */
    public final static int TRUST_HMC_ALWAYS = 1;

    // trust the remote device only if the time period of its fingerprint didn't
    // end
    /** The Constant TRUST_HMC_VALID. */
    public final static int TRUST_HMC_VALID = 2;

    // trust the device only if an HMCServer is telling me now that the device
    // is trusted
    /** The Constant TRUST_HMC_SERVER. */
    public final static int TRUST_HMC_SERVER = 3;

    // trust the device only if the user verifies the fingerprint. For usability
    // reasons, we can add a nicer way for the user to verify the fingerprints:
    // using NFC for mobile devices or barcode scanning for pairing a mobile
    // device with a TY (the TV generates the barcode and the mobile device
    // scans is and authenticate each other).
    /** The Constant TRUST_HMC_USER. */
    public final static int TRUST_HMC_USER = 4;

    /** The Constant INSTANCE. */
    private final static HMCSecurityPolicy INSTANCE = new HMCSecurityPolicy();

    /** The m current hmc security policy. */
    private int mCurrentHMCSecurityPolicy;

    /**
     * Gets the hMC security policy.
     *
     * @return the hMC security policy
     */
    public int getHMCSecurityPolicy() {
        return mCurrentHMCSecurityPolicy;
    }

    /**
     * Sets the hMC security policy.
     *
     * @param pol the new hMC security policy
     */
    public void setHMCSecurityPolicy(int pol) {
        mCurrentHMCSecurityPolicy = pol;
    }

    /**
     * Gets the single instance of HMCSecurityPolicy.
     *
     * @return single instance of HMCSecurityPolicy
     */
    public static HMCSecurityPolicy getInstance() {
        return INSTANCE;
    }
}
