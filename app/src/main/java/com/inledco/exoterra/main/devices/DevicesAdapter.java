package com.inledco.exoterra.main.devices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.DeviceViewHolder;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.util.DeviceIconUtil;

import java.util.List;

public class DevicesAdapter extends SimpleAdapter<Device, DeviceViewHolder> {
    private final String TAG = "DevicesAdapter";

    public DevicesAdapter(@NonNull Context context, List<Device> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_device_content;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DeviceViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceViewHolder holder, int i) {
        Device device = mData.get(i);
        String pkey = device.getProductKey();
        String dname = device.getDeviceName();
        String name = device.getName();
        boolean state = device.isOnline();
        ExoProduct product =ExoProduct.getExoProduct(pkey);
        if (product != null) {
            if (TextUtils.isEmpty(name)) {
                name = product.getDefaultName();
            }
            int iconRes = DeviceIconUtil.getDeviceIconRes(mContext, device.getRemark2(), product.getIcon());
            holder.iv_icon.setImageResource(iconRes);


            if (device instanceof ExoLed) {
                ExoLed led = (ExoLed) device;
                holder.iv_mode.setImageResource(led.getMode() == ExoLed.MODE_MANUAL ? R.drawable.ic_manual : R.drawable.ic_timer_mode);
            } else if (device instanceof ExoSocket) {
                ExoSocket socket = (ExoSocket) device;
                switch (socket.getMode()) {
                    case ExoSocket.MODE_TIMER:
                        holder.iv_mode.setImageResource(R.drawable.ic_timer_mode);
                        break;
                    case ExoSocket.MODE_SENSOR1:
                        holder.iv_mode.setImageResource(R.drawable.ic_temperature);
                        break;
                    case ExoSocket.MODE_SENSOR2:
                        holder.iv_mode.setImageResource(R.drawable.ic_humidity);
                        break;
                }
                holder.iv_sensor.setImageResource((state && socket.getSensorAvailable()) ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
            } else if (device instanceof ExoMonsoon) {
                holder.iv_mode.setImageResource(0);
            } else {
                holder.iv_mode.setImageResource(0);
            }
        }
        holder.tv_name.setText(name);
        Group group = GroupManager.getInstance().getDeviceGroup(pkey, dname);
        holder.ctv_habitat.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.selector_habitat, 0, 0, 0);
        if (group != null) {
            holder.ctv_habitat.setChecked(true);
            holder.ctv_habitat.setText(group.name);
        } else {
            holder.ctv_habitat.setChecked(false);
            holder.ctv_habitat.setText(null);
        }

        holder.iv_state.setImageResource(state ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        holder.iv_sensor.setVisibility(product == ExoProduct.ExoSocket ? View.VISIBLE : View.GONE);
        holder.iv_state.setImageResource(state ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(holder.getAdapterPosition());
                }
                return false;
            }
        });
    }
}
