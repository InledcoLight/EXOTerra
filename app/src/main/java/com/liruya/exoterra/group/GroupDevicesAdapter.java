package com.liruya.exoterra.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.util.DeviceUtil;

import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public abstract class GroupDevicesAdapter extends RecyclerView.Adapter<GroupDevicesAdapter.HomeDevicesViewHolder> {
    private Context mContext;
    private List<HomeApi.HomeDevicesResponse.Device> mDevices;

    public GroupDevicesAdapter(@NonNull final Context context, List<HomeApi.HomeDevicesResponse.Device> devices) {
        mContext = context;
        mDevices = devices;
    }

    @NonNull
    @Override
    public HomeDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        HomeDevicesViewHolder holder = new HomeDevicesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device_content, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeDevicesViewHolder holder, final int i) {
        HomeApi.HomeDevicesResponse.Device device = mDevices.get(i);
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
                onItemClick(i);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onItemLongClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices == null ? 0: mDevices.size();
    }

    protected abstract void onItemClick(int position);

    protected abstract boolean onItemLongClick(int position);

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
