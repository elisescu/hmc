/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 *
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.security;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving securedMessage events.
 * The class that is interested in processing a securedMessage
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSecuredMessageListener<code> method. When
 * the securedMessage event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SecuredMessageEvent
 */
public interface SecuredMessageListener {

    /**
     * Process message.
     *
     * @param chat the chat
     * @param msg the msg
     */
    public void processMessage(SecureChat chat, String msg);
}
