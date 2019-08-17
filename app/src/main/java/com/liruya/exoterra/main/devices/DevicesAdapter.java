package com.liruya.exoterra.main.devices;

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
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.util.DeviceUtil;

import java.util.List;

import cn.xlink.sdk.v5.model.XDevice;

public abstract class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder> {
    private final String TAG = "DevicesAdapter";

    private Context mContext;
    private List<Device> mDevices;

    public DevicesAdapter(@NonNull final Context context, List<Device> devices) {
        mContext = context;
        mDevices = devices;
    }

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        DevicesViewHolder holder = new DevicesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device_content, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DevicesViewHolder holder, int i) {
        Device device = mDevices.get(i);
        String pid = device.getXDevice().getProductId();
        String name = device.getXDevice().getDeviceName();
        String mac = device.getXDevice().getMacAddress();
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
        boolean state = device.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? true : false;
        holder.ctv_state.setChecked(state);
        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
//        holder.tv_desc.setText(mac);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onItemLongClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    protected abstract void onItemClick(int position);

    protected abstract boolean onItemLongClick(int position);

    public class DevicesViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private CheckedTextView ctv_state;
//        private TextView tv_desc;

        public DevicesViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_icon = itemView.findViewById(R.id.item_device_icon);
            tv_name = itemView.findViewById(R.id.item_device_name);
            ctv_state = itemView.findViewById(R.id.item_device_state);
//            tv_desc = itemView.findViewById(R.id.item_device_desc);
        }
    }
}
