package com.inledco.exoterra.main.devices;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.base.BasePermissionFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.login.LoginActivity;
import com.inledco.exoterra.scan.ScanAdapter;
import com.inledco.exoterra.scan.ScanDeviceTask;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class LocalDevicesFragment extends BasePermissionFragment {
    private final int REMOTE_PORT = 8899;
    private final int SCAN_DEVICE_TIMEOUT = 10000;
    private final int SCAN_RETRY_INTERVAL = 1000;

    private LinearLayout devices_msg;
    private TextView devices_signin;
    private SmartRefreshLayout devices_swipe_refresh;
    private View devices_warning;
    private TextView warning_tv_msg;
    private RecyclerView devices_rv_show;
    private ImageButton devices_ib_add;

    private final List<Device> mDevices = new ArrayList<>();
    private ScanAdapter mAdapter;

    private ScanDeviceTask mTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        initData();
        initEvent();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_devices;
    }

    @Override
    protected void initView(View view) {
        devices_msg = view.findViewById(R.id.devices_msg);
        devices_signin = view.findViewById(R.id.devices_signin);
        devices_swipe_refresh = view.findViewById(R.id.devices_swipe_refresh);
        devices_warning = view.findViewById(R.id.devices_warning);
        warning_tv_msg = view.findViewById(R.id.warning_tv_msg);
        devices_rv_show = view.findViewById(R.id.devices_rv_show);
        devices_ib_add = view.findViewById(R.id.devices_ib_add);

        devices_signin.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_24dp, 0);
        devices_msg.setVisibility(View.VISIBLE);
        warning_tv_msg.setText(R.string.scan_devices_warning);
        BezierRadarHeader header = new BezierRadarHeader(getContext());
        header.setPrimaryColor(0x00000000);
        devices_swipe_refresh.setRefreshHeader(header);
    }

    @Override
    protected void initData() {
        mAdapter = new ScanAdapter(getContext(), mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                stopScan();
                Device result = mDevices.get(position);
                startDeviceActivity(result.getProductKey(), result.getDeviceName(), result.getIp(), result.getPort());
            }
        });
        devices_rv_show.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        devices_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });

        devices_swipe_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (!startScan()) {
                    devices_swipe_refresh.finishRefresh();
                } else {
                    devices_warning.setVisibility(View.GONE);
                }
            }
        });

        devices_ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAdddeviceActivity();
            }
        });

        devices_swipe_refresh.autoRefresh();
    }

    private void startAdddeviceActivity() {
        Intent intent = new Intent(getContext(), AddDeviceActivity.class);
        startActivity(intent);
    }

    private boolean startScan() {
        // check location permission first, otherwise check wifi would be incorrect
        if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermission(0, Manifest.permission.ACCESS_COARSE_LOCATION);
            return false;
        }
        if (!checkLocation()) {
            showLocationDialog();
            return false;
        }
        if (!checkWifi()) {
            showWifiSettingsDialog();
            return false;
        }
        if (mTask == null) {
            mDevices.clear();
            mAdapter.notifyDataSetChanged();
            mTask = new ScanDeviceTask(REMOTE_PORT, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL) {
                @Override
                public void onDeviceScanned(Device device) {
                    mDevices.add(device);
                    if (mAdapter != null) {
                        mAdapter.notifyItemInserted(mDevices.size()-1);
                    }
                }

                @Override
                public void onFinished() {
                    stopScan();
                    devices_warning.setVisibility(mDevices.size() == 0 ? View.VISIBLE : View.GONE);
                }
            };
            mTask.start(ExoProduct.ExoLed.getProductKey(),
                        ExoProduct.ExoSocket.getProductKey(),
                        ExoProduct.ExoMonsoon.getProductKey());
            return true;
        }
        return false;
    }

    private void stopScan() {
        if (mTask != null) {
            mTask.stop();
            mTask = null;

            devices_swipe_refresh.finishRefresh();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getActivity().startActivityForResult(intent, 1);
    }

    private void startDeviceActivity(String productKey, String deviceName, String deviceIp, int devicePort) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("productKey", productKey);
        intent.putExtra("deviceName", deviceName);
        intent.putExtra("deviceIp", deviceIp);
        intent.putExtra("devicePort", devicePort);
        startActivity(intent);
    }

    private boolean checkWifi() {
        WifiManager manager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!manager.isWifiEnabled()) {
            return false;
            //            manager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = manager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getNetworkId() == -1) {
            return false;
        }
        return true;
    }

    private void gotoSystemWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showWifiSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.connect_wifi)
               .setMessage(R.string.scan_device_msg)
               .setCancelable(false)
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       gotoSystemWifiSettings();
                   }
               })
               .show();
    }

    @Override
    protected void onPermissionGranted(String permission) {
        if (TextUtils.equals(Manifest.permission.ACCESS_COARSE_LOCATION, permission)) {
            devices_swipe_refresh.autoRefresh();
        }
    }

    @Override
    protected void onPermissionDenied(String permission) {
        if (TextUtils.equals(Manifest.permission.ACCESS_COARSE_LOCATION, permission)) {
            showToast(R.string.msg_location_permission);
        }
    }

    @Override
    protected void onPermissionPermanentDenied(String permission) {
        if (TextUtils.equals(Manifest.permission.ACCESS_COARSE_LOCATION, permission)) {
            showPermissionDialog(getString(R.string.title_location_permission),
                                 getString(R.string.msg_location_permission));
        }
    }

    private void gotoLoactionSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.turnon_gps)
               .setMessage(R.string.msg_turnon_gps)
               .setCancelable(false)
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       gotoLoactionSettings();
                   }
               })
               .show();
    }

    private boolean checkLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (manager.isLocationEnabled()) {
                return true;
            }
            return false;
        }
        return true;
    }
}
