/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.io.IOException;
import java.io.OutputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class StringOutputStream.
 */
class StringOutputStream extends OutputStream {

    /** The m_string. */
    private StringBuilder m_string;

    /**
     * Instantiates a new string output stream.
     */
    StringOutputStream() {
        m_string = new StringBuilder();
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        m_string.append((char) b);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return m_string.toString();
    }
}