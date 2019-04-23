package com.liruya.exoterra.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.liruya.base.BaseActivity;
import com.liruya.exoterra.R;
import com.liruya.exoterra.adddevice.AddDeviceActivity;
import com.liruya.exoterra.main.devices.DevicesFragment;
import com.liruya.exoterra.scan.ScanActivity;
import com.liruya.exoterra.smartconfig.SmartconfigActivity;

import cn.xlink.sdk.common.ByteUtil;
import cn.xlink.sdk.core.model.DataPointValueType;
import cn.xlink.sdk.core.model.XLinkDataPoint;

public class MainActivity extends BaseActivity {

    private Toolbar main_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_main_smartconfig).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startSmartconfigActivity();
                return false;
            }
        });
        menu.findItem(R.id.menu_main_add).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startScanActivity();
//                startAdddeviceActivity();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String code = result.getContents();
            String rawOcde = new String(Base64.decode(code.getBytes(), Base64.DEFAULT));
            Log.e(TAG, "onActivityResult: " + code + "\n" + rawOcde);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
    }

    @Override
    protected void initData() {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.main_fl_show, new DevicesFragment())
                                   .commit();
//        if (checkCameraPermissions() == false) {
//            requestCameraPermission();
//        } else {
//            scanQrCode();
//            test();
//        }
    }

    @Override
    protected void initEvent() {

    }

    private void startSmartconfigActivity() {
        Intent intent = new Intent(MainActivity.this, SmartconfigActivity.class);
        startActivity(intent);
    }

    private void startScanActivity() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
    }

    private void startAdddeviceActivity() {
        Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
        startActivity(intent);
    }

    private boolean checkCameraPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi (Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
    }

    private void scanQrCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(false).initiateScan();
    }

    private void test() {
        XLinkDataPoint dp0 = new XLinkDataPoint(0, DataPointValueType.BOOL, false);
        XLinkDataPoint dp1 = new XLinkDataPoint(1, DataPointValueType.BYTE, (byte) 0x5A);
        XLinkDataPoint dp2 = new XLinkDataPoint(2, DataPointValueType.SHORT, (short) 0x1234);
        XLinkDataPoint dp3 = new XLinkDataPoint(3, DataPointValueType.USHORT, (short) 0x4321);
        XLinkDataPoint dp4 = new XLinkDataPoint(4, DataPointValueType.INT, 0x1234);
        XLinkDataPoint dp5 = new XLinkDataPoint(5, DataPointValueType.UINT, 0x4321);
        XLinkDataPoint dp6 = new XLinkDataPoint(6, DataPointValueType.LONG, (long) 0x1234);
        XLinkDataPoint dp7 = new XLinkDataPoint(7, DataPointValueType.ULONG, (long) 0x4321);
        XLinkDataPoint dp8 = new XLinkDataPoint(8, DataPointValueType.FLOAT, (float) 0.1234);
        XLinkDataPoint dp9 = new XLinkDataPoint(9, DataPointValueType.DOUBLE, 0.1234);
        XLinkDataPoint dp10 = new XLinkDataPoint(10, DataPointValueType.BYTE_ARRAY, new byte[]{0x12, 0x34});
        XLinkDataPoint dp11 = new XLinkDataPoint(11, DataPointValueType.STRING, "1234");
        Log.e(TAG, "test: 0 " + dp0.getAsBoolean() + "  " + dp0.getAsByte() + " " + dp0.getRawValue());
        Log.e(TAG, "test: 1 " + String.valueOf(dp1.getValue()) + " " + dp1.getRawValue());
        Log.e(TAG, "test: 2 " + String.valueOf(dp2.getValue()) + " " + dp2.getRawValue());
        Log.e(TAG, "test: 3 " + String.valueOf(dp3.getValue()) + " " + dp3.getRawValue());
        Log.e(TAG, "test: 4 " + String.valueOf(dp4.getValue()) + " " + dp4.getRawValue());
        Log.e(TAG, "test: 5 " + String.valueOf(dp5.getValue()) + " " + dp5.getRawValue());
        Log.e(TAG, "test: 6 " + String.valueOf(dp6.getValue()) + " " + dp6.getRawValue());
        Log.e(TAG, "test: 7 " + String.valueOf(dp7.getValue()) + " " + dp7.getRawValue());
        Log.e(TAG, "test: 8 " + String.valueOf(dp8.getValue()) + " " + dp8.getRawValue());
        Log.e(TAG, "test: 9 " + String.valueOf(dp9.getValue()) + " " + dp9.getRawValue());
        Log.e(TAG, "test: 10 " + ByteUtil.bytesToHex((byte[]) dp10.getValue()) + " " + dp10.getRawValue());
        Log.e(TAG, "test: 11 " + dp11.getValue() + " " + dp11.getRawValue());
    }
}
