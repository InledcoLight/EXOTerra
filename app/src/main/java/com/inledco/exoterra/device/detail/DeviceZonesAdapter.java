package com.inledco.exoterra.device.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.google.gson.Gson;
import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.bean.Home;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeviceZonesAdapter extends SimpleAdapter<Home.Zone, DeviceZonesAdapter.DeviceZoneViewHolder> {

    private final String mRoomId;
    private final Set<String> mCurrentZoneIds;
    private final Set<String> mTargetZoneIds;

    public DeviceZonesAdapter(@NonNull Context context, List<Home.Zone> data, String roomId) {
        super(context, data);
        Log.e("TAG", "DeviceZonesAdapter: " + new Gson().toJson(data));
        mRoomId = roomId;
        mCurrentZoneIds = new HashSet<>();
        mTargetZoneIds = new HashSet<>();
        refresh();
    }

    private void refresh() {
        mCurrentZoneIds.clear();
        mTargetZoneIds.clear();
        for (Home.Zone zone : mData) {
            if (zone.room_ids.contains(mRoomId)) {
                mCurrentZoneIds.add(zone.id);
                mTargetZoneIds.add(zone.id);
            }
        }
    }

    public void refreshData() {
        notifyDataSetChanged();
        refresh();
    }

    public Set<String> getAddZoneIds() {
        Set<String> zoneIds = new HashSet<>();
        for (String zid : mTargetZoneIds) {
            if (!mCurrentZoneIds.contains(zid)) {
                zoneIds.add(zid);
            }
        }
        return zoneIds;
    }

    public Set<String> getRemoveZoneIds() {
        Set<String> zoneIds = new HashSet<>();
        for (String zid : mCurrentZoneIds) {
            if (!mTargetZoneIds.contains(zid)) {
                zoneIds.add(zid);
            }
        }
        return zoneIds;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_device_zone;
    }

    @NonNull
    @Override
    public DeviceZoneViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DeviceZoneViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceZoneViewHolder holder, int i) {
        final Home.Zone zone = mData.get(i);
        Log.e("TAG", "onBindViewHolder: " + zone.id + " " + zone.name);
        holder.ctv_zone.setText(zone.name);
        holder.ctv_zone.setChecked(mTargetZoneIds.contains(zone.id));

        holder.ctv_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ctv_zone.setChecked(!holder.ctv_zone.isChecked());
                if (holder.ctv_zone.isChecked()) {
                    mTargetZoneIds.add(zone.id);
                } else {
                    mTargetZoneIds.remove(zone.id);
                }
            }
        });
    }

    class DeviceZoneViewHolder extends RecyclerView.ViewHolder {
        private CheckedTextView ctv_zone;

        public DeviceZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            ctv_zone = itemView.findViewById(R.id.item_device_zone);
        }
    }
}
