/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified
 * without prior written consent of the author.
**/

package com.hmc.project.hmc.devices.implementations;

import android.os.RemoteException;
import android.util.Log;

import com.hmc.project.hmc.aidl.IHMCRenderingListener;
import com.hmc.project.hmc.aidl.IMediaRenderer;
import com.hmc.project.hmc.aidl.IUserRequestsListener;
import com.hmc.project.hmc.devices.interfaces.HMCDeviceItf;
import com.hmc.project.hmc.devices.interfaces.HMCMediaDeviceItf;
import com.hmc.project.hmc.service.DeviceAditionConfirmationListener;
import com.hmc.project.hmc.devices.proxy.AsyncCommandReplyListener;
import com.hmc.project.hmc.devices.proxy.HMCDeviceProxy;
import com.hmc.project.hmc.service.HMCManager;

// TODO: Auto-generated Javadoc
/**
 * The Class HMCDeviceImplementation.
 */
public class HMCDeviceImplementation implements HMCDeviceItf {
    
    /** The Constant TAG. */
    private static final String TAG = "HMCDeviceImplementation";
    
    /** The m device descriptor. */
    protected DeviceDescriptor mDeviceDescriptor;
    
    /** The m hmc manager. */
    protected HMCManager mHMCManager;
    
    /** The m user requests listener. */
    protected IUserRequestsListener mUserRequestsListener;

    private IHMCRenderingListener mRenderingListener;

    private IMediaRenderer mLocalMediaRenderer;

    /**
     * Instantiates a new hMC device implementation.
     *
     * @param hmcManager the hmc manager
     * @param thisDeviceDesc the this device desc
     */
    public HMCDeviceImplementation(HMCManager hmcManager, DeviceDescriptor thisDeviceDesc) {
        mHMCManager = hmcManager;
        mDeviceDescriptor = thisDeviceDesc;
    }

    // this method has to be overridden by subclasses
    /**
     * Local execute.
     *
     * @param opCode the op code
     * @param params the params
     * @param fromDev the from dev
     * @return the string
     */
    public String onCommandReceived(int opCode, String params, HMCDeviceProxy fromDev) {
        if (authenticateRemoteDevice(opCode, fromDev.getDeviceDescriptor())) {
            switch (opCode) {
                case HMCDeviceItf.CMD_REMOTE_INCREMENT:
                    return _remoteIncrement(params);
                case HMCDeviceItf.CMD_TEST_ASYNC_COMMAND:
                    return _testAsyncCommand(params, fromDev);
                case HMCDeviceItf.CMD_TEST_SYNC_COMMAND:
                    return _testSyncCommand(params, fromDev);
                case HMCDeviceItf.CMD_INIT_RENDERING:
                    return _initLocalRender(params, fromDev);
                case HMCDeviceItf.CMD_CLOSE_RENDERING:
                    return _closeLocalRender(params, fromDev);
                case HMCDeviceItf.CMD_PLAY:
                    return _playOnLocalRender(params, fromDev);
                case HMCDeviceItf.CMD_PAUSE:
                    return _pauseLocalRender(params, fromDev);
                case HMCDeviceItf.CMD_STOP:
                    return _stopLocalRender(params, fromDev);
                default:
                    return "invalid-operation";
            }
        }
        return "not-authenticated";
    }

    private String _stopLocalRender(String params, HMCDeviceProxy fromDev) {
        boolean retVal = false;
        if (mLocalMediaRenderer != null) {
            try {
                retVal = mLocalMediaRenderer.stop();
            } catch (RemoteException e) {
                retVal = false;
                e.printStackTrace();
            }
        }
        return retVal + "";
    }

    private String _playOnLocalRender(String params, HMCDeviceProxy fromDev) {
        boolean retVal = false;
        if (mLocalMediaRenderer != null) {
            try {
                retVal = mLocalMediaRenderer.play(params);
            } catch (RemoteException e) {
                retVal = false;
                e.printStackTrace();
            }
        }
        return retVal + "";
    }

    private String _pauseLocalRender(String params, HMCDeviceProxy fromDev) {
        boolean retVal = false;
        if (mLocalMediaRenderer != null) {
            try {
                retVal = mLocalMediaRenderer.pause();
            } catch (RemoteException e) {
                retVal = false;
                e.printStackTrace();
            }
        }
        return retVal + "";
    }

    private String _closeLocalRender(String params, HMCDeviceProxy fromDev) {
        boolean retVal = false;
        if (mLocalMediaRenderer != null) {
            try {
                retVal = mLocalMediaRenderer.close();
            } catch (RemoteException e) {
                retVal = false;
                e.printStackTrace();
            }
        }
        return retVal + "";
    }

    private String _initLocalRender(String params, HMCDeviceProxy fromDev) {
        Log.d(TAG, "Starting the video activity");
        boolean res = false;
        if (mRenderingListener != null) {
            try {
                res = mRenderingListener.initRendering();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                res = false;
            }
        }
        return res + "";
    }

    public void setRenderingListener(IHMCRenderingListener rendList) {
        mRenderingListener = rendList;
    }

    public void setLocalRender(IMediaRenderer rend) {
        mLocalMediaRenderer = rend;
    }

    /**
     * @param params
     * @param fromDev
     * @return
     */
    private String _testSyncCommand(String params, HMCDeviceProxy fromDev) {
        return testSyncCommand(params);
    }

    /**
     * @param params
     * @return
     */
    private String testSyncCommand(String params) {
        return params;
    }

    /**
     * @param params
     * @param fromDev
     * @return
     */
    private String _testAsyncCommand(String params, HMCDeviceProxy fromDev) {
        return testAsyncCommand(params, null);
    }

    // this method should be overridden by subclasses if the operations they
    // provide are special (i.e. the media devices can receive addition requests
    // from any device)
    /**
     * Authenticate remote device.
     *
     * @param opCode the op code
     * @param fromDevDesc the from dev desc
     * @return true, if successful
     */
    protected boolean authenticateRemoteDevice(int opCode, DeviceDescriptor fromDevDesc) {
        // allow these generic test operations only for devices in our or
        // external HMC
        return mHMCManager.authenticateDevice(fromDevDesc);
    }

    /**
     * _remote increment.
     *
     * @param params the params
     * @return the string
     */
    public String _remoteIncrement(String params) {
        String retVal = null;
        try {
        int _param=Integer.parseInt(params);
        retVal=Integer.toString(remoteIncrement(_param));
        } catch (NumberFormatException e) {
            retVal = "bad-params";
        }
        return retVal;
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCDeviceItf#remoteIncrement(int)
     */
    @Override
    public int remoteIncrement(int val) {
        return (val + 1);
    }

    // this method should be implemented by the subclasses of device
    // implementation
    /**
     * On notification received.
     *
     * @param opCode the op code
     * @param params the params
     * @param fromDev the from dev
     */
    public void onNotificationReceived(int opCode, String params, HMCDeviceProxy fromDev) {
        switch (opCode) {
            case HMCDeviceItf.CMD_TEST_NOTIFICATION:
                testNotification(params);
                break;
            default:
                Log.e(TAG, "Received unknown notification:" + opCode + " with params: " + params);
                break;
        }
    }

    /* (non-Javadoc)
     * @see com.hmc.project.hmc.devices.interfaces.HMCDeviceItf#testNotification(java.lang.String)
     */
    @Override
    public void testNotification(String notifString) {
        Log.d(TAG, "Recieved notification: " + notifString);
    }

    /**
     * Register user requests listener.
     *
     * @param usrReqListener the usr req listener
     */
    public void registerUserRequestsListener(IUserRequestsListener usrReqListener) {
        mUserRequestsListener = usrReqListener;
    }

    /**
     * Unregister user requests listener.
     *
     * @param userReqListener the user req listener
     */
    public void unregisterUserRequestsListener(IUserRequestsListener userReqListener) {
        if (mUserRequestsListener == userReqListener) {
            mUserRequestsListener = null;
        } else {
            Log.e(TAG, "Unknown listerner for de-registration (mUserRequestsListener)");
        }
    }

    /**
     * Test async command.
     *
     * @param param the param
     * @param listener the listener
     * @return the string
     */
    public String testAsyncCommand(String param, AsyncCommandReplyListener listener) {
        String retVal = "relpy async";
        Log.d(TAG, "Received test async command with param: " + param);

        // simulate long processing
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Returning the value for async test command: " + retVal);

        return retVal;
    }
}
