/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.ui.mediadevice;

/**
 * @author elisescu
 *
 */
public interface MediaController {

    public boolean play(String path);

    public boolean stop();

    public boolean pause();

}
