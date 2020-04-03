package com.inledco.exoterra.main.devices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.LocalDevice;
import com.inledco.exoterra.common.SimpleAdapter;

import java.util.List;

public class LocalDevicesAdapter extends SimpleAdapter<LocalDevice, LocalDevicesAdapter.DevicesViewHolder> {
    private final String TAG = "LocalDevicesAdapter";

    public LocalDevicesAdapter(@NonNull Context context, List<LocalDevice> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_local_device;
    }

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DevicesViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final DevicesViewHolder holder, int i) {
//        LocalDevice device = mData.get(i);
//        String pid = device.getPid();
//        String name = DeviceUtil.getDefaultName(pid);
//        String mac = device.getMac();
//        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
//        holder.tv_name.setText(name);
//        boolean state = device.getxDevice() != null ? true : false;
//        holder.ctv_state.setChecked(state);
//        holder.ctv_state.setText(state ? R.string.local_online : R.string.local_offline);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mItemClickListener != null) {
//                    mItemClickListener.onItemClick(holder.getAdapterPosition());
//                }
//            }
//        });
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (mItemLongClickListener != null) {
//                    mItemLongClickListener.onItemLongClick(holder.getAdapterPosition());
//                }
//                return false;
//            }
//        });
    }

    public class DevicesViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private CheckedTextView ctv_state;
//        private TextView ctv_habitat;

        public DevicesViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_icon = itemView.findViewById(R.id.item_local_device_icon);
            tv_name = itemView.findViewById(R.id.item_local_device_name);
            ctv_state = itemView.findViewById(R.id.item_local_device_state);
//            ctv_habitat = itemView.findViewById(R.id.item_device_desc);
        }
    }
}
