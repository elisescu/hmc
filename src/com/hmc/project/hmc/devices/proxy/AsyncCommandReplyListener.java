/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.proxy;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving asyncCommandReply events.
 * The class that is interested in processing a asyncCommandReply
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addAsyncCommandReplyListener<code> method. When
 * the asyncCommandReply event occurs, that object's appropriate
 * method is invoked.
 *
 * @see AsyncCommandReplyEvent
 */
public interface AsyncCommandReplyListener {
    
    /**
     * On reply received.
     *
     * @param reply the reply
     */
    public void onReplyReceived(String reply);
}