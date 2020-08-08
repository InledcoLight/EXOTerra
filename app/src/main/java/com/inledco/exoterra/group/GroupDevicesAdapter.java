package com.inledco.exoterra.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.util.DeviceIconUtil;

import java.util.List;

public class GroupDevicesAdapter extends SimpleAdapter<Group.Device, GroupDevicesAdapter.GroupDevicesViewHolder> {
    public GroupDevicesAdapter(@NonNull Context context, List<Group.Device> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_device_content;
    }

    @NonNull
    @Override
    public GroupDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GroupDevicesViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupDevicesViewHolder holder, int i) {
        final int position = holder.getAdapterPosition();
        Group.Device device = mData.get(position);
        String pkey = device.product_key;
        String name = device.name;
        ExoProduct product = ExoProduct.getExoProduct(pkey);
        Device dev = DeviceManager.getInstance().getDevice(pkey + "_" + device.device_name);
        if (product != null) {
            if (TextUtils.isEmpty(name)) {
                name = product.getDefaultName();
            }
            int iconRes = product.getIcon();
            if (dev != null) {
                iconRes = DeviceIconUtil.getDeviceIconRes(mContext, dev.getRemark2(), iconRes);
            }
            holder.iv_icon.setImageResource(iconRes);
        }
        holder.tv_name.setText(name);
        holder.ctv_habitat.setCompoundDrawablesRelativeWithIntrinsicBounds(0 , 0, 0, 0);
        holder.ctv_habitat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6);
        boolean state = false;
        if (dev != null) {
            state = dev.isOnline();
        }
        holder.iv_state.setImageResource(state ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        if (dev instanceof ExoSocket) {
            ExoSocket socket = (ExoSocket) dev;
            holder.iv_sensor.setImageResource((state & socket.getSensorAvailable()) ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
            holder.iv_sensor.setVisibility(View.VISIBLE);
        } else {
            holder.iv_sensor.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(position);
                }
                return false;
            }
        });
    }

    class GroupDevicesViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView ctv_habitat;
        private ImageView iv_state;
        private ImageView iv_sensor;
        public GroupDevicesViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_device_icon);
            tv_name = itemView.findViewById(R.id.item_device_name);
            ctv_habitat = itemView.findViewById(R.id.item_device_habitat);
            iv_state = itemView.findViewById(R.id.item_device_state);
            iv_sensor = itemView.findViewById(R.id.item_device_sensor);
        }
    }
}
