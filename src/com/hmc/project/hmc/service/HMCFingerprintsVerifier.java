/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.service;

/**
 * @author elisescu
 *
 */
public interface HMCFingerprintsVerifier {
    public boolean verifyFingerprints(String localFingerprint, String remoteFingerprint, String remoteName);
}
