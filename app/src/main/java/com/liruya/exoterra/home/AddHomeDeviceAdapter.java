package com.liruya.exoterra.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.util.DeviceUtil;

import java.util.List;

public abstract class AddHomeDeviceAdapter extends RecyclerView.Adapter<AddHomeDeviceAdapter.AddHomeDeviceViewHolder> {

    private Context mContext;
    private List<Device> mDevices;

    public AddHomeDeviceAdapter(@NonNull final Context context, List<Device> devices) {
        mContext = context;
        mDevices = devices;
    }

    @NonNull
    @Override
    public AddHomeDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        AddHomeDeviceViewHolder holder = new AddHomeDeviceViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device_content, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddHomeDeviceViewHolder holder, final int i) {
        Device device = mDevices.get(i);
        String pid = device.getXDevice().getProductId();
        String dev_name = device.getXDevice().getDeviceName();
        String name = TextUtils.isEmpty(dev_name) ? DeviceUtil.getDefaultName(pid) : dev_name;
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(name);
        holder.tv_product.setText(DeviceUtil.getProductType(pid));
        holder.tv_desc.setText(device.getXDevice().getMacAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices == null ? 0 : mDevices.size();
    }

    protected abstract void onItemClick(int position);

    class AddHomeDeviceViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_product;
        private TextView tv_desc;
        public AddHomeDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_device_icon);
            tv_name = itemView.findViewById(R.id.item_device_name);
            tv_product = itemView.findViewById(R.id.item_device_product);
            tv_desc = itemView.findViewById(R.id.item_device_desc);
        }
    }
}
