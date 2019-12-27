package com.inledco.exoterra.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.common.DeviceViewHolder;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.xlink.sdk.v5.model.XDevice;

public class AddGroupDeviceAdapter extends SimpleAdapter<Device, DeviceViewHolder> {
    private final Set<Integer> mAddDeviceIds;

    public AddGroupDeviceAdapter(@NonNull Context context, List<Device> data) {
        super(context, data);
        mAddDeviceIds = new HashSet<>();
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
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int i) {
        final int position = holder.getAdapterPosition();
        final Device device = mData.get(position);
        String pid = device.getXDevice().getProductId();
        String dev_name = device.getXDevice().getDeviceName();
        String name = TextUtils.isEmpty(dev_name) ? DeviceUtil.getDefaultName(pid) : dev_name;
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(name);
        boolean state = device.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? true : false;
        holder.ctv_state.setChecked(state);
        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
        holder.cb_select.setVisibility(View.VISIBLE);
        holder.cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Integer devid = device.getXDevice().getDeviceId();
                if (isChecked) {
                    mAddDeviceIds.add(devid);
                } else {
                    mAddDeviceIds.remove(devid);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    public Set<Integer> getAddDeviceIds() {
        return mAddDeviceIds;
    }
}
