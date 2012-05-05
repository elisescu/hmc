/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmc.project.hmc.R;

public class DevicesListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "DevicesListAdapter";
    private Activity mActivity;
    private LinkedHashMap<String, String> mDeviceNames;
    private String mTempJid;
    private String mTempName;
    private HashMap<String, String> mTempList;

    public DevicesListAdapter(Activity activity) {
        super(activity, R.layout.list_item);
        mActivity = activity;
        mDeviceNames = new LinkedHashMap<String, String>();
    }

    public void setDevices(HashMap<String, String> list) {
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

    public String getJidFromPosition(int position) {
        return (String) mDeviceNames.keySet().toArray()[position];
    }

    public void add(String jid, String name) {
        mTempJid = jid;
        mTempName = name;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                _add(mTempJid, mTempName);
            }
        });
    }

    private void _add(String jid, String name) {
        if (!mDeviceNames.containsKey(jid)) {
            super.add(name);
            mDeviceNames.put(jid, name);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mActivity
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);

        imageView.setImageResource(R.drawable.no_device_icon);
        textView.setText("no device");

        if (mDeviceNames.size() > position) {
            String deviceName = (String) mDeviceNames.values().toArray()[position];
            if (deviceName != null) {
                textView.setText(deviceName);
                imageView.setImageResource(R.drawable.device_icon);
            }
        }

        return rowView;
    }
}