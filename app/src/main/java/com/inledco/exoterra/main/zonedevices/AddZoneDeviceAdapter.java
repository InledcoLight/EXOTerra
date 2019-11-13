package com.inledco.exoterra.main.zonedevices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.bean.RoomDevice;
import com.inledco.exoterra.common.DeviceViewHolder;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.xlink.restful.api.app.HomeApi;

public class AddZoneDeviceAdapter extends SimpleAdapter<RoomDevice, DeviceViewHolder> {

    private final Home.Zone mZone;
    private final Set<String> mCurrentRoomIds;
    private final Set<String> mTargetRoomIds;

    public AddZoneDeviceAdapter(@NonNull Context context, List<RoomDevice> data, @NonNull Home.Zone zone) {
        super(context, data);
        mZone = zone;
        mCurrentRoomIds = new HashSet<>();
        mTargetRoomIds = new HashSet<>();
        refresh();
    }

    private void refresh() {
        mCurrentRoomIds.clear();
        mTargetRoomIds.clear();
        for (RoomDevice device : mData) {
            if (mZone.room_ids.contains(device.getRoomId())) {
                String roomid = device.getRoomId();
                mCurrentRoomIds.add(roomid);
                mTargetRoomIds.add(roomid);
            }
        }
    }

    public void refreshData() {
        refresh();
        notifyDataSetChanged();
    }

    public Set<String> getAddRoomIds() {
        Set<String> roomids = new HashSet<>();
        for (String roomid : mTargetRoomIds) {
            if (!mCurrentRoomIds.contains(roomid)) {
                roomids.add(roomid);
            }
        }
        return roomids;
    }

    public Set<String> getRemoveRoomIds() {
        Set<String> roomids = new HashSet<>();
        for (String roomid : mCurrentRoomIds) {
            if (!mTargetRoomIds.contains(roomid)) {
                roomids.add(roomid);
            }
        }
        return roomids;
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
        final RoomDevice roomDevice = mData.get(i);
        final HomeApi.HomeDevicesResponse.Device device = roomDevice.getDevice();
        String pid = device.productId;
        String name = TextUtils.isEmpty(device.name) ? DeviceUtil.getDefaultName(pid) : device.name;
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(name);
        boolean state = device.isOnline;
        holder.ctv_state.setChecked(state);
        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
        holder.cb_select.setVisibility(View.VISIBLE);
        holder.cb_select.setChecked(mTargetRoomIds.contains(roomDevice.getRoomId()));
        //        holder.tv_desc.setText(device.mac);

        holder.cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTargetRoomIds.add(roomDevice.getRoomId());
                } else {
                    mTargetRoomIds.remove(roomDevice.getRoomId());
                }
            }
        });
    }
}
