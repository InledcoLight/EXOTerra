package com.liruya.exoterra.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liruya.exoterra.R;
import com.liruya.exoterra.util.DeviceUtil;
import com.liruya.exoterra.xlink.IXlinkRegisterDeviceCallback;
import com.liruya.exoterra.xlink.XlinkCloudManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder> {
    private final String TAG = "ScanAdapter";

    private Context mContext;
    private List<XDevice> mScannedDevices;
    private Set<String> mSubscribedDevices;
    private Map<String, Boolean> mSubscribings;

    public ScanAdapter(Context context, List<XDevice> scannedDevices, Set<String> subscribedDevices) {
        mContext = context;
        mScannedDevices = scannedDevices;
        mSubscribedDevices = subscribedDevices;
        mSubscribings = new HashMap<>();
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
        boolean subscribed = isSubscribed(device);
        boolean subscribing = isSubscribing(device);
        holder.iv_icon.setImageResource(DeviceUtil.getProductIcon(pid));
        holder.tv_name.setText(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
        holder.tv_product.setText(DeviceUtil.getProductType(pid));
        holder.tv_desc.setText(mac);
        holder.cib_subscribe.setChecked(subscribed);
        holder.cib_subscribe.setEnabled(!subscribing);
        holder.progress.setVisibility(subscribing ? View.VISIBLE : View.GONE);
        holder.cib_subscribe.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (!holder.cib_subscribe.isChecked()) {
                    holder.cib_subscribe.setEnabled(false);
                    holder.progress.setVisibility(View.VISIBLE);
                    mSubscribings.put(device.getDeviceTag(), true);
                    XlinkCloudManager.getInstance()
                                     .registerDevice(device, null, new IXlinkRegisterDeviceCallback() {
                                         @Override
                                         public void onDeviceAlreadyExists(XDevice xDevice) {
                                             XlinkCloudManager.getInstance()
                                                              .subscribeDevice(device, null, 35000, new XLinkTaskListener<XDevice>() {
                                                                  @Override
                                                                  public void onError(final XLinkCoreException e) {
                                                                      Log.e(TAG, "onError: " + e.getErrorName());
                                                                      mSubscribings.remove(device.getDeviceTag());
                                                                      holder.itemView.post(new Runnable() {
                                                                          @Override
                                                                          public void run() {
                                                                              Toast.makeText(mContext, mContext.getString(R.string.msg_subscribe_fail) + e.getErrorName(),
                                                                                             Toast.LENGTH_SHORT)
                                                                                   .show();
                                                                              holder.progress.setVisibility(View.GONE);
                                                                              holder.cib_subscribe.setEnabled(true);
                                                                          }
                                                                      });
                                                                  }

                                                                  @Override
                                                                  public void onStart() {
                                                                      Log.e(TAG, "onStart: ");
                                                                  }

                                                                  @Override
                                                                  public void onComplete(XDevice device) {
                                                                      Log.e(TAG, "onComplete: ");
                                                                      mSubscribings.remove(device.getDeviceTag());
                                                                      holder.itemView.post(new Runnable() {
                                                                          @Override
                                                                          public void run() {
                                                                              Toast.makeText(mContext, R.string.msg_subscribe_success, Toast.LENGTH_SHORT)
                                                                                   .show();
                                                                              holder.progress.setVisibility(View.GONE);
                                                                              holder.cib_subscribe.setChecked(true);
                                                                              holder.cib_subscribe.setEnabled(true);
                                                                          }
                                                                      });
                                                                  }
                                                              });
                                         }

                                         @Override
                                         public void onStart() {

                                         }

                                         @Override
                                         public void onError(final String error) {
                                             mSubscribings.remove(device.getDeviceTag());
                                             holder.itemView.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     Toast.makeText(mContext, mContext.getString(R.string.msg_subscribe_fail) + error, Toast.LENGTH_SHORT)
                                                          .show();
                                                     holder.progress.setVisibility(View.GONE);
                                                     holder.cib_subscribe.setEnabled(true);
                                                 }
                                             });
                                         }

                                         @Override
                                         public void onSuccess(XDevice xDevice) {

                                         }
                                     });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mScannedDevices == null ? 0 : mScannedDevices.size();
    }

    private boolean isSubscribed(final XDevice device) {
        if (mSubscribedDevices == null || mSubscribedDevices.size() == 0) {
            return false;
        }
        for (String deviceTag : mSubscribedDevices) {
            if (TextUtils.equals(deviceTag, device.getDeviceTag())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSubscribing(final XDevice device) {
        if (mSubscribings == null || !mSubscribings.containsKey(device.getDeviceTag())) {
            return false;
        }
        return mSubscribings.get(device.getDeviceTag());
    }

    public void setSubscribedDevices(Set<String> keys) {
        mSubscribedDevices = keys;
        notifyDataSetChanged();
    }

    public class ScanViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_product;
        private TextView tv_desc;
        private CheckableImageButton cib_subscribe;
        private ProgressBar progress;

        public ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.item_scan_icon);
            tv_name = itemView.findViewById(R.id.item_scan_name);
            tv_product = itemView.findViewById(R.id.item_scan_product);
            tv_desc = itemView.findViewById(R.id.item_scan_desc);
            cib_subscribe = itemView.findViewById(R.id.item_scan_subscribe);
            progress = itemView.findViewById(R.id.item_scan_progress);
        }
    }
}
