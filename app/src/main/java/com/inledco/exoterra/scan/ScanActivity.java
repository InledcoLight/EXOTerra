package com.inledco.exoterra.scan;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.base.BasePermissionActivity;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends BasePermissionActivity {
    private final int REMOTE_PORT = 8899;
    private final int SCAN_DEVICE_TIMEOUT = 10000;
    private final int SCAN_RETRY_INTERVAL = 1000;

    private Toolbar scan_toolbar;
    private SmartRefreshLayout scan_refresh;
    private RecyclerView scan_rv_show;

    private final List<Device> mLocalDevices = new ArrayList<>();
    private ScanAdapter mAdapter;

    private ScanDeviceTask mTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        MenuItem menuConfig = menu.findItem(R.id.menu_scan_config);
        menuConfig.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                stopScan();
                startAdddeviceActivity();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_scan;
    }

    @Override
    protected void initView() {
        scan_toolbar = findViewById(R.id.scan_toolbar);
        setSupportActionBar(scan_toolbar);
        scan_refresh = findViewById(R.id.scan_refresh);
        scan_rv_show = findViewById(R.id.scan_rv_show);
        BezierRadarHeader header = new BezierRadarHeader(this);
        header.setPrimaryColor(0x00000000);
        scan_refresh.setRefreshHeader(header);
    }

    @Override
    protected void initData() {
        mAdapter = new ScanAdapter(this, mLocalDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                stopScan();
                Device result = mLocalDevices.get(position);
                gotoDeviceActivity(result.getProductKey(), result.getDeviceName(), result.getIp(), result.getPort());
            }
        });
        scan_rv_show.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        scan_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        scan_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (!startScan()) {
                    scan_refresh.finishRefresh();
                }
            }
        });

        startScan();
    }

    private boolean startScan() {
        // check location permission first, otherwise check wifi would be incorrect
        if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermission(0, Manifest.permission.ACCESS_COARSE_LOCATION);
            return false;
        }
        if (!checkWifi()) {
            showWifiSettingsDialog();
            return false;
        }
        if (mTask == null) {
            mLocalDevices.clear();
            mAdapter.notifyDataSetChanged();
            mTask = new ScanDeviceTask(REMOTE_PORT, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL) {
                @Override
                public void onDeviceScanned(Device device) {
                    mLocalDevices.add(device);
                    if (mAdapter != null) {
                        mAdapter.notifyItemInserted(mLocalDevices.size()-1);
                    }
                }

                @Override
                public void onFinished() {
                    stopScan();
                }
            };
            mTask.start(ExoProduct.ExoLed.getProductKey(),
                        ExoProduct.ExoSocket.getProductKey(),
                        ExoProduct.ExoMonsoon.getProductKey());

            scan_refresh.autoRefresh();
            return true;
        }
        return false;
    }

    private void stopScan() {
        if (mTask != null) {
            mTask.stop();
            mTask = null;

            scan_refresh.finishRefresh();
        }
    }

    private void gotoDeviceActivity(String productKey, String deviceName, String deviceIp, int devicePort) {
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra("productKey", productKey);
        intent.putExtra("deviceName", deviceName);
        intent.putExtra("deviceIp", deviceIp);
        intent.putExtra("devicePort", devicePort);
        startActivity(intent);
    }

    private boolean checkWifi() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            startScan();
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

    private void startAdddeviceActivity() {
        Intent intent = new Intent(this, AddDeviceActivity.class);
        startActivity(intent);
    }
}
