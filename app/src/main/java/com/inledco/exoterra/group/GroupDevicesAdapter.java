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
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.DeviceManager;

import java.util.List;

public class GroupDevicesAdapter extends SimpleAdapter<Group.Device, GroupDevicesAdapter.HomeDevicesViewHolder> {
    public GroupDevicesAdapter(@NonNull Context context, List<Group.Device> data) {
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
        Group.Device device = mData.get(position);
        String pkey = device.product_key;
        String name = device.name;
        ExoProduct product = ExoProduct.getExoProduct(pkey);
        if (product != null) {
            if (TextUtils.isEmpty(name)) {
                name = product.getDefaultName();
            }
            holder.iv_icon.setImageResource(product.getIconSmall());
        }
        holder.tv_name.setText(name);
        boolean state = false;
        Device dev = DeviceManager.getInstance().getDevice(pkey + "_" + device.device_name);
        if (dev != null) {
            state = dev.isOnline();
        }
        holder.ctv_state.setChecked(state);
        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
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
//        private TextView ctv_habitat;
        public HomeDevicesViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_device_icon);
            tv_name = itemView.findViewById(R.id.item_device_name);
            ctv_state = itemView.findViewById(R.id.item_device_state);
//            ctv_habitat = itemView.findViewById(R.id.item_device_desc);
        }
    }
}
