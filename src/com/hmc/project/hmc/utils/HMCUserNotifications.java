/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.utils;

import android.content.Context;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCUserNotifications.
 */
public class HMCUserNotifications {

    /**
     * Normal toast.
     *
     * @param ctx the ctx
     * @param msg the msg
     */
    public static void normalToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
}
