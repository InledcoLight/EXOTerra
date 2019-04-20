package com.liruya.exoterra.scan;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.connection.XLinkScanDeviceTask;

public class ScanActivity extends BaseActivity {
    private final int SCAN_DEVICE_TIMEOUT = 30000;
    private final int SCAN_RETRY_INTERVAL = 2000;

    private Toolbar scan_toolbar;
    private ToggleButton scan_tb_scan;
    private RecyclerView scan_rv_show;

    private Set<String> mSubscribedDevices;
    private List<XDevice> mScannedDevices;
    private ScanAdapter mAdapter;
    private XLinkScanDeviceTask mScanDeviceTask;
    private XLinkScanDeviceListener mScanDeviceListener;
    private boolean mScanning;

    @Override
    protected void onStart() {
        super.onStart();

        initData();
        initEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_scan_scan);
        scan_tb_scan = menuItem.getActionView()
                               .findViewById(R.id.scan_tb_scan);
        final ProgressBar scan_progress = menuItem.getActionView()
                                                  .findViewById(R.id.scan_progress);
        scan_tb_scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                scan_progress.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked) {
                    startScan();
                } else {
                    stopScan();
                }
            }
        });
        scan_tb_scan.setChecked(true);
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
        scan_rv_show = findViewById(R.id.scan_rv_show);
        scan_rv_show.addItemDecoration(new DividerItemDecoration(ScanActivity.this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mSubscribedDevices = DeviceManager.getInstance().getAllDeviceAddress();
        mScannedDevices = new ArrayList<>();
        mAdapter = new ScanAdapter(ScanActivity.this, mScannedDevices, mSubscribedDevices);
        scan_rv_show.setAdapter(mAdapter);
        mScanDeviceListener = new XLinkScanDeviceListener() {
            @Override
            public void onScanResult(XDevice xDevice) {
                if (xDevice != null) {
                    if (!containsDevice(xDevice)) {
                        mScannedDevices.add(xDevice);
                        mAdapter.notifyItemInserted(mScannedDevices.size()-1);
                    }
                }
            }

            @Override
            public void onError(XLinkCoreException e) {
                mScanning = false;
                scan_tb_scan.setChecked(false);
                stopScan();
            }

            @Override
            public void onStart() {
                mScanning = true;
            }

            @Override
            public void onComplete(Void aVoid) {
                mScanning = false;
                scan_tb_scan.setChecked(false);
                stopScan();
            }
        };
    }

    @Override
    protected void initEvent() {
        scan_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private boolean containsDevice(XDevice xDevice) {
        for (XDevice xd : mScannedDevices) {
            if (TextUtils.equals(xDevice.getDeviceTag(), xd.getMacAddress())) {
                return true;
            }
        }
        return false;
    }

    private void startScan() {
        if (mScanning) {
            return;
        }
        mScannedDevices.clear();
        mAdapter.notifyDataSetChanged();
        List<String> pids = new ArrayList<>();
        pids.add(XlinkConstants.PRODUCT_ID_LEDSTRIP);
        pids.add(XlinkConstants.PRODUCT_ID_MONSOON);
        pids.add(XlinkConstants.PRODUCT_ID_SOCKET);
        XlinkCloudManager.getInstance().scanDevice(pids, SCAN_DEVICE_TIMEOUT, SCAN_RETRY_INTERVAL, mScanDeviceListener);
    }

    private void stopScan() {
        if (mScanDeviceTask != null) {
            mScanDeviceTask.cancel();
            mScanDeviceTask = null;
        }
        mScanning = false;
    }
}
