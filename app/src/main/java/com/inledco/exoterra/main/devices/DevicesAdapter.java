package com.inledco.exoterra.main.devices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.common.DeviceViewHolder;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.util.DeviceUtil;

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
        String pid = device.getXDevice().getProductId();
        String name = device.getXDevice().getDeviceName();
        if (TextUtils.isEmpty(name)) {
            name = DeviceUtil.getDefaultName(pid);
        }
        String mac = device.getXDevice().getMacAddress();
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(name);
        Home home = HomeManager.getInstance().getDeviceHome(device);
        if (home != null) {
            holder.ctv_habitat.setChecked(true);
            holder.ctv_habitat.setText(home.getHome().name);
        } else {
            holder.ctv_habitat.setChecked(false);
            holder.ctv_habitat.setText(null);
        }
        holder.ctv_habitat.setVisibility(View.VISIBLE);
        boolean state = device.getXDevice().isOnline();
        holder.ctv_state.setChecked(state);
        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
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

//    public class DeviceViewHolder extends RecyclerView.ViewHolder {
//        private ImageView iv_icon;
//        private TextView tv_name;
//        private CheckedTextView ctv_state;
//        private CheckedTextView ctv_habitat;
//
//        public DeviceViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            iv_icon = itemView.findViewById(R.id.item_device_icon);
//            tv_name = itemView.findViewById(R.id.item_device_name);
//            ctv_state = itemView.findViewById(R.id.item_device_state);
//            ctv_habitat = itemView.findViewById(R.id.item_device_habitat);
//        }
//    }
}
