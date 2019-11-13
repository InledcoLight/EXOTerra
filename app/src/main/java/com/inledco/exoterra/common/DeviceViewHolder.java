package com.inledco.exoterra.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;

public class DeviceViewHolder extends RecyclerView.ViewHolder {
    public ImageView iv_icon;
    public TextView tv_name;
    public CheckedTextView ctv_state;
    public CheckBox cb_select;
//    public TextView tv_desc;

    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);

        iv_icon = itemView.findViewById(R.id.item_device_icon);
        tv_name = itemView.findViewById(R.id.item_device_name);
        ctv_state = itemView.findViewById(R.id.item_device_state);
        cb_select = itemView.findViewById(R.id.item_device_select);
//        tv_desc = itemView.findViewById(R.id.item_device_desc);
    }
}
