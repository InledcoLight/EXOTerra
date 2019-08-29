package com.liruya.exoterra.device;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liruya.exoterra.AppConstants;
import com.liruya.exoterra.R;
import com.liruya.exoterra.base.BaseActivity;
import com.liruya.exoterra.base.BaseViewModel;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.bean.EXOMonsoon;
import com.liruya.exoterra.bean.EXOSocket;
import com.liruya.exoterra.device.Monsoon.MonsoonFragment;
import com.liruya.exoterra.device.Monsoon.MonsoonViewModel;
import com.liruya.exoterra.device.detail.DeviceDetailFragment;
import com.liruya.exoterra.device.light.LightFragment;
import com.liruya.exoterra.device.light.LightViewModel;
import com.liruya.exoterra.device.socket.SocketFragment;
import com.liruya.exoterra.device.socket.SocketViewModel;
import com.liruya.exoterra.event.DatapointChangedEvent;
import com.liruya.exoterra.event.DeviceStateChangedEvent;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.util.DeviceUtil;
import com.liruya.exoterra.util.RegexUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkConstants;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.manager.XLinkDeviceManager;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class DeviceActivity extends BaseActivity {
    private Toolbar device_toolbar;
    private CheckableImageButton device_cib_cloud;
    private CheckableImageButton device_cib_wifi;

    private Device mDevice;
    private DeviceViewModel mDeviceViewModel;
    private BaseViewModel<Device> mDeviceBaseViewModel;

    private Fragment mDeviceFragment;

    private XlinkTaskCallback<XDevice> mSetCallback;
    private XlinkTaskCallback<List<XLinkDataPoint>> mGetCallback;

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mDevice != null) {
            XLinkDeviceManager.getInstance().removeDeviceConnectionFlags(mDevice.getXDevice().getDeviceTag(), 1);
            XLinkDeviceManager.getInstance().disconnectDeviceLocal(mDevice.getXDevice().getDeviceTag());
        }
    }

    @SuppressLint ("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        View view = menu.findItem(R.id.menu_device_status).getActionView();
        device_cib_cloud = view.findViewById(R.id.device_cib_cloud);
        device_cib_wifi = view.findViewById(R.id.device_cib_wifi);
        if (mDevice != null) {
            device_cib_cloud.setChecked(mDevice.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? true : false);
            device_cib_wifi.setChecked(mDevice.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? true : false);
        }
        MenuItem device_share = menu.findItem(R.id.menu_device_share);
        MenuItem device_detail = menu.findItem(R.id.menu_device_detail);
        device_share.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showShareDeviceDialog();
                return true;
            }
        });
        device_detail.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addFragmentToStack(R.id.device_root, new DeviceDetailFragment());
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_device;
    }

    @Override
    protected void initView() {
        device_toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(device_toolbar);
    }

    @Override
    protected void initData() {
        XLinkSDK.start();
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        final String deviceTag = intent.getStringExtra(AppConstants.DEVICE_TAG);
        final Device device = DeviceManager.getInstance().getDevice(deviceTag);
        if (device == null) {
            return;
        }
        mDeviceBaseViewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        mDeviceBaseViewModel.setData(device);

        final String pid = device.getXDevice().getProductId();
        final String name = device.getXDevice().getDeviceName();
        device_toolbar.setTitle(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
        setSupportActionBar(device_toolbar);

        XLinkDeviceManager.getInstance().addDeviceConnectionFlags(device.getXDevice().getDeviceTag(), 1);
        XLinkDeviceManager.getInstance().connectDeviceLocal(device.getXDevice().getDeviceTag());
        EventBus.getDefault().register(this);

        if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_LEDSTRIP)) {
            mDevice = new EXOLedstrip(device);
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(LightViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mDeviceFragment = new LightFragment();
//            replaceFragment(R.id.device_fl_show, new LightFragment());
        } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_MONSOON)) {
            mDevice = new EXOMonsoon(device);
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(MonsoonViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mDeviceFragment = new MonsoonFragment();
//            replaceFragment(R.id.device_fl_show, new MonsoonFragment());
        } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_SOCKET)) {
            mDevice = new EXOSocket(device);
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(SocketViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mDeviceFragment = new SocketFragment();
//            replaceFragment(R.id.device_fl_show, new SocketFragment());
        } else {
            throw new RuntimeException("Invalid ProductID.");
        }

        mSetCallback = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: setDataPointError - " + error);
            }

            @Override
            public void onStart() {
                Log.e(TAG, "onStart: setDataPointStart");
            }

            @Override
            public void onComplete(XDevice xDevice) {
                Log.e(TAG, "onComplete: setDataPointComplete");
            }
        };
        mGetCallback = new XlinkTaskCallback<List<XLinkDataPoint>>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: getDataPoints- " + error );
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<XLinkDataPoint> dataPoints) {
                Collections.sort(dataPoints, new Comparator<XLinkDataPoint>() {
                    @Override
                    public int compare(XLinkDataPoint o1, XLinkDataPoint o2) {
                        return o1.getIndex() - o2.getIndex();
                    }
                });
                device.setDataPointList(dataPoints);
                mDevice.setDataPointList(dataPoints);

//                StringBuilder sb = new StringBuilder();
//                for (XLinkDataPoint dp : dataPoints) {
//                    sb.append(dp.getIndex()).append(" ")
//                      .append(dp.getName()).append(" ")
//                      .append(dp.getType()).append(" ")
//                      .append(dp.getValue()).append(" ").append(dp.getRawValue()).append("\n");
//                }
//                Log.e(TAG, "onComplete: " + (dataPoints==null || dataPoints.size() == 0) + "    " + sb);
                mDeviceViewModel.postValue();
                replaceFragment(R.id.device_fl_show, mDeviceFragment);
            }
        };

        mDeviceViewModel.setGetCallback(mGetCallback);
        mDeviceViewModel.setSetCallback(mSetCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDeviceViewModel.getDatapoints();
            }
        }, 100);

//        XlinkCloudManager.getInstance().getDeviceMetaDatapoints(mDevice.getXDevice(), new XlinkTaskCallback<List<XLinkDataPoint>>() {
//            @Override
//            public void onError(String error) {
//                Log.e(TAG, "onError: getMetaDatapoints- " + error );
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onComplete(List<XLinkDataPoint> dataPoints) {
//                Log.e(TAG, "onComplete: getMetaDatapoints");
//                device.setDataPointList(dataPoints);
//                mDevice.setDataPointList(dataPoints);
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mDeviceViewModel.getDatapoints();
//                    }
//                }, 500);
//            }
//        });
    }

    @Override
    protected void initEvent() {
        device_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(DeviceStateChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getDeviceTag(), mDevice.getDeviceTag())) {
            device_cib_cloud.setChecked(mDevice.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? true : false);
            device_cib_wifi.setChecked(mDevice.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? true : false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatapointChangedEvent(DatapointChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getDeviceTag(), mDevice.getDeviceTag())) {
            Device device = DeviceManager.getInstance().getDevice(event.getDeviceTag());
            if (device != null) {
                for (XLinkDataPoint dp : device.getDataPointList()) {
                    mDevice.setDataPoint(dp);
                }
                mDeviceBaseViewModel.postValue(device);
                mDeviceViewModel.postValue();
            }
        }
    }

    private void shareDevice(@NonNull String email) {
        XlinkCloudManager.getInstance().shareDevice(mDevice.getXDevice(), email, new XlinkTaskCallback<DeviceApi.ShareDeviceResponse>() {
            @Override
            public void onError(String error) {
                Toast.makeText(DeviceActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(DeviceApi.ShareDeviceResponse response) {
                Toast.makeText(DeviceActivity.this, "Share Success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    private void showShareDeviceDialog() {
        View view = LayoutInflater.from(DeviceActivity.this).inflate(R.layout.dialog_share, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_share_til);
        final TextInputEditText et_email = view.findViewById(R.id.dialog_share_email);
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        final AlertDialog dialog = builder.setTitle(R.string.share_device)
                                          .setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.share, null)
                                          .show();
        Button btn_share = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                if (RegexUtil.isEmail(email)) {
                    shareDevice(email);
                    dialog.dismiss();
                } else {
                    til.setError(getString(R.string.error_email));
                }
            }
        });
    }
}
