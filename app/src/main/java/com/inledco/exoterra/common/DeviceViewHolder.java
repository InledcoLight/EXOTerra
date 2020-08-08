package com.inledco.exoterra.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;

public class DeviceViewHolder extends RecyclerView.ViewHolder {
    public ImageView iv_icon;
    public TextView tv_name;
    public CheckedTextView ctv_habitat;
    public ImageView iv_state;
    public ImageView iv_sensor;
    public ImageView iv_mode;

    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);

        iv_icon = itemView.findViewById(R.id.item_device_icon);
        tv_name = itemView.findViewById(R.id.item_device_name);
        iv_state = itemView.findViewById(R.id.item_device_state);
        iv_sensor = itemView.findViewById(R.id.item_device_sensor);
        iv_mode = itemView.findViewById(R.id.item_device_mode);
        ctv_habitat = itemView.findViewById(R.id.item_device_habitat);
    }
}
