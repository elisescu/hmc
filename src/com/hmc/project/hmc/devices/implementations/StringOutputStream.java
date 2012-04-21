/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import java.io.IOException;
import java.io.OutputStream;

class StringOutputStream extends OutputStream {
    private StringBuilder m_string;

    StringOutputStream() {
        m_string = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {
        m_string.append((char) b);
    }

    @Override
    public String toString() {
        return m_string.toString();
    }
}