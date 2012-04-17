/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.utils;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;


public class StringEncoder {
    XmlSerializer mSerializer;
    StringWriter mWriter;
    public StringEncoder() {
        mSerializer = Xml.newSerializer();
        mWriter = new StringWriter();
        try {
            mSerializer.setOutput(mWriter);
            mSerializer.startDocument("UTF-8", true);

            // mSerializer.startTag("", "message");
            // mSerializer.attribute("", "date", "a");
            // mSerializer.startTag("", "title");
            // mSerializer.text("text");
            // mSerializer.endTag("", "title");

            // mSerializer.endDocument();
            // mWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void putInt(int val) {

    }


}
