/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui.mediadevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.xbill.DNS.MINFORecord;

import com.hmc.project.hmc.HMCApplication;
import com.hmc.project.hmc.R;
import com.hmc.project.hmc.aidl.IAsyncRPCReplyListener;
import com.hmc.project.hmc.aidl.IHMCConnection;
import com.hmc.project.hmc.aidl.IHMCManager;
import com.hmc.project.hmc.service.HMCService;
import com.hmc.project.hmc.ui.DevicesListAdapter;
import com.hmc.project.hmc.ui.Login;
import com.hmc.project.hmc.ui.mediadevice.VideoPlayerActivity.LocalMediaRenderer;
import com.hmc.project.hmc.ui.mediadevice.VideoPlayerActivity.RemoteMediaRenderer;
import com.hmc.project.hmc.utils.HMCUserNotifications;
import com.hmc.project.hmc.aidl.IMediaRenderer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Vasile Popescu
 * 
 */
public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback,
                        OnBufferingUpdateListener, OnVideoSizeChangedListener,
                        OnCompletionListener, OnPreparedListener {
    /** The Constant TAG. */
    protected static final String TAG = "VideoPlayerActivity";

    public static final String PLAYER_MODE_KEY = "player_mode";
    public static final String PLAYER_MODE_REMOTE = "remote";
    public static final String PLAYER_MODE_LOCAL = "local";

    /** The m hmc connection. */
    private IHMCConnection mHMCConnection;

    /** The m bound service. */
    private HMCService mBoundService;

    /** The m is bound. */
    private boolean mIsBound;

    /** The m hmc application. */
    private HMCApplication mHMCApplication;

    /** The m context. */
    private VideoPlayerActivity mContext;

    /** The m hmc manager. */
    private IHMCManager mHMCManager;

    private int mResourceSelectedPosition = -1;

    private CharSequence[] mLocalDevicesJIDs;
    private CharSequence[] mLocalDevicesNames;
    private HashMap<String, String>  mLocalDevNamesHashMap;
    private String mSelectedRenderFullJID = "local";
    private HashMap<String, String> mVideoResourcesList;
    private RemoteMediaRenderer mSelectedRender;
    private HashMap<String, RemoteMediaRenderer> mRemoteRenderers;
    
    /** The m connection. */
    private ServiceConnection mConnection = new ServiceConnection() {


        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName className, IBinder service) {
            mHMCConnection = IHMCConnection.Stub.asInterface(service);
            if (mHMCConnection != null) {
                try {
                    mHMCManager = mHMCConnection.getHMCManager();

                    mLocalDevNamesHashMap = (HashMap<String, String>) mHMCConnection
                                            .getHMCManager().getListOfLocalDevices();
                    Iterator<String> iter = mLocalDevNamesHashMap.keySet().iterator();
                    mLocalDevicesJIDs = new CharSequence[mLocalDevNamesHashMap.size()];
                    mLocalDevicesNames = new CharSequence[mLocalDevNamesHashMap.size()];
                    int i = 0;
                    mLocalDevicesJIDs[i] = "local";
                    mLocalDevicesNames[i] = "Local device";
                    i++;
                    while (iter.hasNext()) {
                        String val = iter.next();
                        // don't add the local device two times, as we have it
                        // in the list of devices
                        if (!val.equals(mHMCApplication.getUsername())) {
                            mLocalDevicesJIDs[i] = val;
                            mLocalDevicesNames[i] = mLocalDevNamesHashMap.get(val);
                            i++;
                        }
                    }
                    
                    mVideoResourcesList = getVideoResourcesList();
                    mVideoResourcesListAdapter.setVideoResources(mVideoResourcesList);
                    
                    mHMCManager.setLocalMediaRender(mLocalMediaRenderer);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Error. Couldn't retrieve the HMC serviec internals!");
            }


            if (mPlayerMode.equals(PLAYER_MODE_LOCAL)) {
                VideoPlayerActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        // run these on ui thread
                        mStatusTextView.setText("Idle");
                        mSmallProgressbar.setVisibility(View.INVISIBLE);
                    }
                });
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(VideoPlayerActivity.this, R.string.local_service_disconnected,
                                    Toast.LENGTH_SHORT).show();
        }
    };

    private HashMap<String, String> getVideoResourcesList() {
        HashMap<String, String> vidRes = new HashMap<String, String>();
        // vidRes.put("http://62.107.84.14/vid1.mp4", "vid1.mp4");
        // vidRes.put("http://62.107.84.14/vid2.mp4", "vid2.mp4");
        vidRes.put("rtsp://62.107.84.14:1234/stream.sdp", "vid1.mp4");
        vidRes.put("rtsp://62.107.84.14:1235/stream.sdp", "vid2.mp4");
        vidRes.put("rtsp://62.107.84.14:1236/stream.sdp", "vid3.mp4");
        return vidRes;
    }

    private String mPlayerMode = "not-set";

    private ProgressBar mSmallProgressbar;

    private ProgressBar mBigProgressbar;

    private TextView mStatusTextView;

    private Button mPlayStopButton;

    private Button mPauseResumeButton;

    private Button mPreviousButton;

    private Button mNextButton;

    private TextView mRendererNameTxtView;

    private ListView mVideoListView;

    private VideoResAdapter mVideoResourcesListAdapter;

    private TextView mVideoTitle;

    private SurfaceView mSurfaceView;

    private SurfaceHolder holder;

    private MediaPlayer mMediaPlayer;

    private LocalMediaRenderer mLocalMediaRenderer;

    private Button mRemoteInitButton;

    private Button mCloseButton;

    /**
     * Do bind service.
     */
    void doBindService() {
        bindService(new Intent(VideoPlayerActivity.this, HMCService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Do unbind service.
     */
    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
        doCleanUp();
        doUnbindService();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
        doUnbindService();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(onBlooey);

        Intent sender = getIntent();
        mPlayerMode = sender.getExtras().getString(PLAYER_MODE_KEY);

        if (mPlayerMode.equals(PLAYER_MODE_LOCAL)) {
            setContentView(R.layout.video_player_activity_small);

            mSmallProgressbar = (ProgressBar) findViewById(R.id.vidAct_smallProgressBar);
            mStatusTextView = (TextView) findViewById(R.id.vidAct_statusText);

            mStatusTextView.setText("Wait please");
            mSmallProgressbar.setVisibility(View.VISIBLE);

            mHMCApplication = (HMCApplication) getApplication();

            mPlayStopButton = (Button) findViewById(R.id.vidAct_PlayStop);
            mPauseResumeButton = (Button) findViewById(R.id.vidAct_PauseResume);
            mPreviousButton = (Button) findViewById(R.id.vidAct_ButtPrev);
            mNextButton = (Button) findViewById(R.id.vidAct_ButtNext);
            mRendererNameTxtView = (TextView) findViewById(R.id.vidAct_RenderName);
            mVideoListView = (ListView) findViewById(R.id.vidAct_ResourcesList);
            mVideoTitle = (TextView) findViewById(R.id.vidAct_streamTitle);
            mRemoteInitButton = (Button) findViewById(R.id.vidAct_initRemoteButton);
            mCloseButton = (Button) findViewById(R.id.vidAct_Close);

            mPlayStopButton.setOnClickListener(mOnClickListener);
            mPauseResumeButton.setOnClickListener(mOnClickListener);
            mPreviousButton.setOnClickListener(mOnClickListener);
            mNextButton.setOnClickListener(mOnClickListener);
            mRendererNameTxtView.setOnClickListener(mOnClickListener);
            mRemoteInitButton.setOnClickListener(mOnClickListener);
            mCloseButton.setOnClickListener(mOnClickListener);

            mVideoListView.setOnItemClickListener(mOnListItemListener);
            mVideoResourcesListAdapter = new VideoResAdapter(this);
            mVideoListView.setAdapter(mVideoResourcesListAdapter);

            mRemoteRenderers = new HashMap<String, VideoPlayerActivity.RemoteMediaRenderer>();
            mContext = this;
            mBigProgressbar = (ProgressBar) findViewById(R.id.vidAct_bigProgressBar);
        } else if (mPlayerMode.equals(PLAYER_MODE_REMOTE)) {

        } else {
            HMCUserNotifications.normalToast(this, "Unknow demo mode");
            finish();
        }
        mSurfaceView = (SurfaceView) findViewById(R.id.vidAct_SurfaceView);
        holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        doBindService();
    }

    
    @Override
    public void onResume() {
        super.onResume();

        // Create a new media player and set the listeners
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(holder);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mLocalMediaRenderer = new LocalMediaRenderer();

    }
    // resources list listener
    OnItemClickListener mOnListItemListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // When clicked, show a toast with the TextView text
            mResourceSelectedPosition = position;
            String selectedResource = mVideoResourcesListAdapter
                                    .getResourceNameFromPosition(mResourceSelectedPosition);
            Toast.makeText(getApplicationContext(), "Selected " + selectedResource,
                                    Toast.LENGTH_SHORT)
                                    .show();
            mVideoTitle.setText(selectedResource);
        }
    };

    // butons and etc listener
    View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.vidAct_RenderName:
                    pickRendererDevice();
                    break;
                case R.id.vidAct_PlayStop:
                    playSelectedResource();
                    break;
                case R.id.vidAct_PauseResume:
                    pauseResume();
                    break;
                case R.id.vidAct_initRemoteButton:
                    initRemoteRender();
                    break;
                case R.id.vidAct_Close:
                    closeRender();
                    break;
                default:
                    break;
            }

        }
    };

    private void closeRender() {
        boolean result = false;
        try {
            if (mSelectedRenderFullJID.equals("local")) {
                result = mLocalMediaRenderer.close();
            } else {
                // send command to remote
                result = mSelectedRender.close();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!result) {
            setStatusMessage("Cannot close", Color.RED);
            HMCUserNotifications.normalToast(mContext, "Error: cannot play");
        }
    }

    private void initRemoteRender() {
        setStatusMessage("Initializing remote...: " + mSelectedRenderFullJID, Color.WHITE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = mSelectedRender.init();
                Log.d(TAG, "!!!!!!!!!!!Initialized = " + result);
                if (!result) {
                    setStatusMessage("Cannot initialize", Color.RED);
                } else {
                    setStatusMessage(" ", Color.BLACK);
                }
            }
        }).start();
    }

    private void pauseResume() {
        boolean result = false;
        try {
            if (mSelectedRenderFullJID.equals("local")) {
                result = mLocalMediaRenderer.pause();
            } else {
                // send command to remote
                result = mSelectedRender.pause();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (!result) {
            setStatusMessage("Cannot pause", Color.RED);
            HMCUserNotifications.normalToast(mContext, "Error: cannot play");
        }
    }

    private void playSelectedResource() {
        String videoResource = "";

        try {
            videoResource = mVideoResourcesListAdapter
                                    .getResourceURIFromPosition(mResourceSelectedPosition);
            boolean playbackResult = false;

            if (mSelectedRenderFullJID.equals("local")) {
                playbackResult = mLocalMediaRenderer.play(videoResource);
            } else {
                // send command to remote
                playbackResult = mSelectedRender.play(videoResource);
            }

            if (!playbackResult) {
                setStatusMessage("Cannot play " + videoResource, Color.RED);
                HMCUserNotifications.normalToast(mContext, "Error: cannot play");
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IndexOutOfBoundsException e) {
            setStatusMessage("Select a valid resource", Color.RED);
        }

    }


    private void pickRendererDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Select a device to render");
        builder.setItems(mLocalDevicesNames,
                                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                HMCUserNotifications.normalToast(mContext, "Selected " + mLocalDevicesNames[item]
                                        + " (" + mLocalDevicesJIDs[item] + ") ");
                mSelectedRenderFullJID = mLocalDevicesJIDs[item].toString();
                mRendererNameTxtView.setText(mLocalDevicesNames[item]);
                
                if (!mSelectedRenderFullJID.equals("local")) {
                    mRemoteInitButton.setVisibility(View.VISIBLE);
                } else {
                    mRemoteInitButton.setVisibility(View.INVISIBLE);
                }

                if (mRemoteRenderers.containsKey(mSelectedRenderFullJID)) {
                    mSelectedRender = mRemoteRenderers.get(mSelectedRenderFullJID);
                } else {
                    mSelectedRender = new RemoteMediaRenderer(mSelectedRenderFullJID);
                    mRemoteRenderers.put(mSelectedRenderFullJID, mSelectedRender);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        alert.show();
    }

    private Thread.UncaughtExceptionHandler onBlooey = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, "Uncaught exception", ex);
            goBlooey(ex);
        }
    };

    private boolean mSurfaceCreated = false;

    private int mVideoWidth;

    private int mVideoHeight;

    private boolean mIsVideoReadyToBePlayed = false;

    private boolean mIsVideoSizeKnown;

    private boolean mMediaPlayerPreparing = false;

    private int mStatusColor;

    private String mStatusText;

    private void goBlooey(Throwable t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exception!").setMessage(t.toString()).setPositiveButton("OK", null)
                                .show();
    }

    private class VideoResAdapter extends ArrayAdapter<String> {
        public LinkedHashMap<String, String> mResourcesNames;
        private Activity mActivity;
        /** The Constant TAG. */
        private static final String TAG = "VideoResAdapter";
        /** The m temp jid. */
        private String mTempUri;

        /** The m temp name. */
        private String mTempName;

        /** The m temp list. */
        private HashMap<String, String> mTempList;
        /**
         * @param context
         * @param textViewResourceId
         */
        public VideoResAdapter(Activity activity) {
            super(activity, R.layout.video_list_item);
            mActivity = activity;
            mResourcesNames = new LinkedHashMap<String, String>();
        }

        /**
         * Sets the devices.
         * 
         * @param list
         *            the list
         */
        public void setVideoResources(HashMap<String, String> list) {
            mTempList = list;
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Iterator<String> iter = mTempList.keySet().iterator();
                    while (iter.hasNext()) {
                        String val = iter.next();
                        add(val, mTempList.get(val));
                    }
                }
            });
        }

        public String getResourceURIFromPosition(int position) {
           return (String) mResourcesNames.keySet().toArray()[position];
       }

        public String getResourceNameFromPosition(int position) {
            String retVal = (String) mResourcesNames.values().toArray()[position];
            return retVal;
        }
       
        /**
         * Adds the.
         * 
         * @param uri
         *            the uri
         * @param name
         *            the name
         */
       public void add(String uri, String name) {
           mTempUri = uri;
           mTempName = name;
           mActivity.runOnUiThread(new Runnable() {
               public void run() {
                   _add(mTempUri, mTempName);
               }
           });
       }

       /**
        * _add.
        *
        * @param uri the jid
        * @param name the name
        */
       private void _add(String uri, String name) {
           if (!mResourcesNames.containsKey(uri)) {
               super.add(name);
               mResourcesNames.put(uri, name);
           }
       }

       /* (non-Javadoc)
        * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
        */
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           LayoutInflater inflater = (LayoutInflater) mActivity
                                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.video_list_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.video_title);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.video_logo);

           imageView.setImageResource(R.drawable.no_device_icon);
            textView.setText("no resource");

           if (mResourcesNames.size() > position) {
               String resourceName = (String) mResourcesNames.values().toArray()[position];
               if (resourceName != null) {
                   textView.setText(resourceName);
                    imageView.setImageResource(R.drawable.video_file_icon);
               }
           }
           return rowView;
       }
       
    }

    private void startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        try {
        mMediaPlayer.start();
        } catch (IllegalStateException e) {

            e.printStackTrace();
        }

        VideoPlayerActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mBigProgressbar.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void playVideoResource(String path) throws IOException {
        doCleanUp();
        Log.v(TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        try {
            
            if (!mMediaPlayerPreparing) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }

                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepareAsync();
                mMediaPlayerPreparing = true;

                VideoPlayerActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mBigProgressbar.setVisibility(View.VISIBLE);
                    }
                });
            }
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
            setStatusMessage("Called with Illegal argument", Color.RED);
            // TODO: handle exception
        } catch (IllegalStateException e2) {
            e2.printStackTrace();
            setStatusMessage("Called in Illegal state", Color.RED);
            // TODO: handle exception
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
     * , int, int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged called");

    }

    /*
     * (non-Javadoc)
     * @see
     * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
     * )
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceCreated = true;
        setStatusMessage("Ready", Color.WHITE);
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
     * SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed called");
        mSurfaceCreated = false;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.media.MediaPlayer.OnPreparedListener#onPrepared(android.media
     * .MediaPlayer)
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        mMediaPlayerPreparing = false;
        setStatusMessage(" ", Color.BLACK);
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown && !mMediaPlayer.isPlaying()) {
            startVideoPlayback();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media
     * .MediaPlayer)
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion called");
        if (!mIsVideoReadyToBePlayed) {
            VideoPlayerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mBigProgressbar.setVisibility(View.INVISIBLE);
                    setStatusMessage("Couldn't start playbacl", Color.RED);
                }
            });
        }

        doCleanUp();
    }

    /*
     * (non-Javadoc)
     * @see
     * android.media.MediaPlayer.OnVideoSizeChangedListener#onVideoSizeChanged
     * (android.media.MediaPlayer, int, int)
     */
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown && !mMediaPlayer.isPlaying()) {
            startVideoPlayback();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * android.media.MediaPlayer.OnBufferingUpdateListener#onBufferingUpdate
     * (android.media.MediaPlayer, int)
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);
        setStatusMessage("Buffering: " + percent + "%", Color.WHITE);
    }

    public void setStatusMessage(String text, int color) {
        mStatusColor = color;
        mStatusText = text;
        VideoPlayerActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mStatusTextView.setTextColor(mStatusColor);
                mStatusTextView.setText(mStatusText);
            }
        });
    }

    class RemoteMediaRenderer extends IMediaRenderer.Stub {

        private String mFullJID;
        private boolean mInitialized;
        private boolean mRemoteResult;
        IMediaRenderer mRemoteMediaController = null;
        private String mStringPath;

        public RemoteMediaRenderer(String fullJID) {
            mFullJID = fullJID;
            mInitialized = false;
        }

        public boolean init() {
            try {
                mRemoteMediaController = mHMCManager.initRemoteRender(mFullJID);
            } catch (RemoteException e) {
                e.printStackTrace();
                mRemoteMediaController = null;
            }
            return (mRemoteMediaController != null);
        }

        /*
         * (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IMediaController#play(java.lang.String)
         */
        @Override
        public boolean play(String path) throws RemoteException {
            if (mRemoteMediaController == null)
                return false;

            mStringPath = path;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mRemoteResult = mRemoteMediaController.play(mStringPath);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mRemoteResult = false;
                    }
                    if (mRemoteResult == false) {
                        setStatusMessage("Cannot play remote", Color.RED);
                    }
                }
            }).start();
            return mRemoteResult;
        }

        /*
         * (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IMediaController#stop()
         */
        @Override
        public boolean stop() throws RemoteException {
            if (mRemoteMediaController == null)
                return false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mRemoteResult = mRemoteMediaController.stop();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mRemoteResult = false;
                    }
                    if (mRemoteResult == false) {
                        setStatusMessage("Cannot stop remote", Color.RED);
                    }
                }
            }).start();
            return mRemoteResult;
        }

        /*
         * (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IMediaController#pause()
         */
        @Override
        public boolean pause() throws RemoteException {
            if (mRemoteMediaController == null)
                return false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mRemoteResult = mRemoteMediaController.pause();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mRemoteResult = false;
                    }
                    if (mRemoteResult == false) {
                        setStatusMessage("Cannot pause remote", Color.RED);
                    }
                }
            }).start();
            return mRemoteResult;
        }

        @Override
        public boolean close() throws RemoteException {
            if (mRemoteMediaController == null)
                return false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mRemoteResult = mRemoteMediaController.close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mRemoteResult = false;
                    }
                    if (mRemoteResult == false) {
                        setStatusMessage("Cannot close remote renderer", Color.RED);
                    }
                }
            }).start();
            return mRemoteResult;
        }

    }

    class LocalMediaRenderer extends IMediaRenderer.Stub {

        private boolean mMediaPlayerPaused;

        /*
         * (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IMediaController#play(java.lang.String)
         */
        @Override
        public boolean play(String path) throws RemoteException {
            HMCUserNotifications.normalToast(mContext, "play(" + path + ")");
            boolean res = false;
            
            if (mSurfaceCreated) {
                try {
                    playVideoResource(path);
                    res = true;
                } catch (IOException e) {
                    res = false;
                    e.printStackTrace();
                }
            } else {
                HMCUserNotifications.normalToast(mContext, "Cannot render video!");
                setStatusMessage("Error: surface view", Color.RED);
                res = false;
            }
            
            if (res == false) {
                VideoPlayerActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mBigProgressbar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            return res;
        }

        /*
         * (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IMediaController#stop()
         */
        @Override
        public boolean stop() throws RemoteException {
            boolean retVal = false;
            HMCUserNotifications.normalToast(mContext, "stop()");
            try {
                mMediaPlayer.stop();
                retVal = true;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return retVal;
        }

        /*
         * (non-Javadoc)
         * @see com.hmc.project.hmc.aidl.IMediaController#pause()
         */
        @Override
        public boolean pause() throws RemoteException {
            boolean retVal = false;
            HMCUserNotifications.normalToast(mContext, "pause()/resume()");
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPauseResumeButton.setText("Resume");
                    mMediaPlayerPaused = true;
                    retVal = true;
                } else if (mMediaPlayerPaused) {
                    mMediaPlayer.start();
                    mPauseResumeButton.setText("Pause");
                    mMediaPlayerPaused = false;
                    retVal = true;
                } else {
                    retVal = false;
                }
            } catch (IllegalStateException e) {
                retVal = false;
                e.printStackTrace();
            }
            return retVal;
        }

        @Override
        public boolean close() throws RemoteException {
            if (mContext != null) {
                mContext.finish();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }
}
