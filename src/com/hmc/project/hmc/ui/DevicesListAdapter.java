/**
 * Copyright (c) 2012 Vasile Popescu (elisescu@gmail.com)
 * 
 * This source file CANNOT be distributed and/or modified without prior written
 * consent of the author.
 **/

package com.hmc.project.hmc.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmc.project.hmc.R;

public class DevicesListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private List<String> mDeviceNames;

    public DevicesListAdapter(Context context, List<String> mDevicesNames) {
        super(context, R.layout.list_item, mDevicesNames);
        this.context = context;
        this.mDeviceNames = mDevicesNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        textView.setText(mDeviceNames.get(position));

        imageView.setImageResource(R.drawable.device_icon);

        return rowView;
    }
}