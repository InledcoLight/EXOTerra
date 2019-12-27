package com.inledco.exoterra.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class GroupDevicesAdapter extends SimpleAdapter<HomeApi.HomeDevicesResponse.Device, GroupDevicesAdapter.HomeDevicesViewHolder> {
    public GroupDevicesAdapter(@NonNull Context context, List<HomeApi.HomeDevicesResponse.Device> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_device_content;
    }

    @NonNull
    @Override
    public HomeDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HomeDevicesViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeDevicesViewHolder holder, int i) {
        final int position = holder.getAdapterPosition();
        HomeApi.HomeDevicesResponse.Device device = mData.get(position);
        String pid = device.productId;
        String name = TextUtils.isEmpty(device.name) ? DeviceUtil.getDefaultName(pid) : device.name;
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(name);
        boolean state = device.isOnline;
        holder.ctv_state.setChecked(state);
        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
//        holder.tv_desc.setText(device.mac);
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

    class HomeDevicesViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private CheckedTextView ctv_state;
//        private TextView tv_desc;
        public HomeDevicesViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_device_icon);
            tv_name = itemView.findViewById(R.id.item_device_name);
            ctv_state = itemView.findViewById(R.id.item_device_state);
//            tv_desc = itemView.findViewById(R.id.item_device_desc);
        }
    }
}
