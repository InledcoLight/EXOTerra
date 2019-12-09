package com.inledco.exoterra.main.homezones;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.bean.RoomDevice;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeZonesAdapter extends SimpleAdapter<Home2.Zone, HomeZonesAdapter.ZoneViewHolder> {
    private List<RoomDevice> mDevices;

    public HomeZonesAdapter(@NonNull Context context, List<Home2.Zone> data, List<RoomDevice> devices) {
        super(context, data);
        mDevices = devices;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_group;
    }

    @NonNull
    @Override
    public ZoneViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ZoneViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull ZoneViewHolder holder, final int position) {
        Home2.Zone zone = mData.get(position);
        holder.zone_name.setText(zone.name);
        holder.zone_devcnt.setText("" + zone.room_ids.size() + " devices");
        List<String> pids = new ArrayList<>();
        for (int i = 0; i < mDevices.size(); i++) {
            RoomDevice device = mDevices.get(i);
            String roomid = device.getRoomId();
            if (zone.room_ids.contains(roomid)) {
                pids.add(device.getDevice().productId);
                if (pids.size() >= 4) {
                    break;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            int visible = pids.size() > i ? View.VISIBLE : View.INVISIBLE;
            holder.zone_icon[i].setVisibility(visible);
            if (visible == View.VISIBLE) {
                holder.zone_icon[i].setImageResource(DeviceUtil.getProductIconSmall(pids.get(i)));
            }
        }
        holder.zone_icon[3].setVisibility(pids.size() >= 4 ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    class ZoneViewHolder extends RecyclerView.ViewHolder {
        private TextView zone_name;
        private TextView zone_devcnt;
        private ImageView[] zone_icon;
        public ZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            zone_name = itemView.findViewById(R.id.item_group_name);
            zone_devcnt = itemView.findViewById(R.id.item_group_devcnt);
            zone_icon = new ImageView[4];
            zone_icon[0] = itemView.findViewById(R.id.item_group_icon1);
            zone_icon[1] = itemView.findViewById(R.id.item_group_icon2);
            zone_icon[2] = itemView.findViewById(R.id.item_group_icon3);
            zone_icon[3] = itemView.findViewById(R.id.item_group_icon4);
        }
    }
}
