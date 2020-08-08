package com.inledco.exoterra.scan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.DeviceViewHolder;
import com.inledco.exoterra.common.SimpleAdapter;

import java.util.List;

public class ScanAdapter extends SimpleAdapter<Device, DeviceViewHolder> {
    private final String TAG = "ScanAdapter";

    public ScanAdapter(@NonNull Context context, List<Device> data) {
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
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int i) {
        final int postion = holder.getAdapterPosition();
        Device device = mData.get(postion);
        final String pkey = device.getProductKey();
        final String dname = device.getDeviceName();
        String name = device.getName();
        ExoProduct product = ExoProduct.getExoProduct(pkey);
        if (product != null) {
            if (TextUtils.isEmpty(name)) {
                name = product.getDefaultName();
            }
            holder.iv_icon.setImageResource(product.getIcon());
        }
        holder.tv_name.setText(name);
        holder.ctv_habitat.setText(dname);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(postion);
                }
            }
        });
    }
}
