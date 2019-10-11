package com.inledco.exoterra.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.util.DeviceUtil;

import java.util.List;
import java.util.Set;

import cn.xlink.sdk.v5.model.XDevice;

public abstract class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder> {
    private final String TAG = "ScanAdapter";

    private Context mContext;
    private List<XDevice> mScannedDevices;

    public ScanAdapter(Context context, List<XDevice> scannedDevices) {
        mContext = context;
        mScannedDevices = scannedDevices;
    }

    public ScanAdapter(Context context, List<XDevice> scannedDevices, Set<String> subscribedDevices) {
        mContext = context;
        mScannedDevices = scannedDevices;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ScanViewHolder holder = new ScanViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_scan_device, viewGroup, false));
        return holder;
    }

    @SuppressLint ("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull final ScanViewHolder holder, int i) {
        final XDevice device = mScannedDevices.get(i);
        String pid = device.getProductId();
        String name = device.getDeviceName();
        String mac = device.getMacAddress();
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
        holder.tv_desc.setText(mac);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceManager.getInstance().updateDevice(device);
                onItemClick(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mScannedDevices == null ? 0 : mScannedDevices.size();
    }

    public abstract void onItemClick(XDevice device);

    public class ScanViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_desc;

        public ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_scan_icon);
            tv_name = itemView.findViewById(R.id.item_scan_name);
            tv_desc = itemView.findViewById(R.id.item_scan_desc);
        }
    }
}
