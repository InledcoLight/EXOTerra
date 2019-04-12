package com.liruya.exoterra.main.devices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.util.DeviceUtil;
import com.liruya.swiperecyclerview.SimpleSwipeAdapter;
import com.liruya.swiperecyclerview.SwipeLayout;
import com.liruya.swiperecyclerview.SwipeViewHolder;

import java.util.List;

public class DevicesAdapter extends SimpleSwipeAdapter<Device, DevicesAdapter.DevicesViewHolder> {
    private final String TAG = "DevicesAdapter";

    public DevicesAdapter(@NonNull Context context, List<Device> list) {
        super(context, list);
    }

    @Override
    protected int getLayoutResID(int viewType) {
        return R.layout.item_device;
    }

    @Override
    protected DevicesViewHolder onCreateSwipeViewHolder(SwipeLayout swipeLayout) {
        return new DevicesViewHolder(swipeLayout);
    }

    @Override
    protected void onBindSwipeViewHolder(@NonNull DevicesViewHolder holder, int position) {
        Device device = getItem(holder.getAdapterPosition());
        String pid = device.getXDevice().getProductId();
        String name = device.getXDevice().getDeviceName();
        String mac = device.getXDevice().getMacAddress();
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
        holder.tv_product.setText(DeviceUtil.getProductType(pid));
        holder.tv_desc.setText(mac);
    }

    public class DevicesViewHolder extends SwipeViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_product;
        private TextView tv_desc;

        public DevicesViewHolder(@NonNull SwipeLayout itemView) {
            super(itemView);

            View contentView = getContentView();
            if (contentView != null) {
                iv_icon = contentView.findViewById(R.id.item_device_icon);
                tv_name = contentView.findViewById(R.id.item_device_name);
                tv_product = contentView.findViewById(R.id.item_device_product);
                tv_desc = contentView.findViewById(R.id.item_device_desc);
            }
        }
    }
}
