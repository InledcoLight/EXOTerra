package com.inledco.exoterra.adddevice;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.xlink.XlinkConstants;

import java.util.List;

public abstract class APDeviceAdapter extends SimpleAdapter<ScanResult, APDeviceAdapter.APDeviceViewHolder> {

    public APDeviceAdapter(@NonNull Context context, List<ScanResult> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_apdevice;
    }

    @NonNull
    @Override
    public APDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new APDeviceViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final APDeviceViewHolder holder, int i) {
        ScanResult result = mData.get(i);
        if (DeviceUtil.isEXOStrip(result)) {
            holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(XlinkConstants.PRODUCT_ID_LEDSTRIP));
        } else if (DeviceUtil.isEXOSocket(result)) {
            holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(XlinkConstants.PRODUCT_ID_SOCKET));
        } else if (DeviceUtil.isEXOMonsoon(result)) {
            holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(XlinkConstants.PRODUCT_ID_MONSOON));
        }
        holder.tv_ssid.setText(result.SSID);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    class APDeviceViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_ssid;
        public APDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_apdevice_icon);
            tv_ssid = itemView.findViewById(R.id.item_apdevice_ssid);
        }
    }
}
