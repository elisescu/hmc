/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmc.project.hmc.R;

public class DevicesListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "DevicesListAdapter";
    private final Context context;
    //private List<String> mDeviceNames;
    private LinkedHashMap<String, String> mDevices;

    public DevicesListAdapter(Context context) {
        super(context, R.layout.list_item);
        this.context = context;
        // mDeviceNames = new ArrayList<String>();
        mDevices = new LinkedHashMap<String, String>();
    }

    public void add(String jid, String name) {
        if (!mDevices.containsKey(jid)) {
            super.add(name);
            mDevices.put(jid, name);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);

        imageView.setImageResource(R.drawable.no_device_icon);
        textView.setText("no device");

        if (mDevices.size() > position) {
            String deviceName = (String) mDevices.values().toArray()[position];
            if (deviceName != null) {
                textView.setText(deviceName);
                imageView.setImageResource(R.drawable.device_icon);
            }
        }


        return rowView;
    }
}