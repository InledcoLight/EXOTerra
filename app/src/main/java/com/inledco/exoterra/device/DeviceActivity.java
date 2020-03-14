package com.inledco.exoterra.device;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.device.Monsoon.MonsoonFragment;
import com.inledco.exoterra.device.Monsoon.MonsoonPowerFragment;
import com.inledco.exoterra.device.Monsoon.MonsoonViewModel;
import com.inledco.exoterra.device.detail.DeviceDetailFragment;
import com.inledco.exoterra.device.light.LightFragment;
import com.inledco.exoterra.device.light.LightPowerFragment;
import com.inledco.exoterra.device.light.LightViewModel;
import com.inledco.exoterra.device.socket.SocketFragment;
import com.inledco.exoterra.device.socket.SocketPowerFragment;
import com.inledco.exoterra.device.socket.SocketViewModel;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.DevicePropertyChangedEvent;
import com.inledco.exoterra.event.DeviceStateChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.manager.XLinkDeviceManager;
import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.model.XDevice;
import cn.xlink.sdk.v5.module.main.XLinkSDK;

public class DeviceActivity extends BaseActivity {
    private ImageView device_icon;
    private TextView device_name;
    private ImageButton device_detail;
    private TextView device_habitat_name;
    private ImageView device_status_local;
    private ImageView device_status_cloud;
    private ImageView device_status_sensor;

    private Device mDevice;
    private DeviceViewModel mDeviceViewModel;
    private DeviceBaseViewModel mDeviceBaseViewModel;

    private Fragment mPowerFragment;
    private Fragment mDeviceFragment;

    private XlinkTaskCallback<XDevice> mSetCallback;
    private XlinkTaskCallback<List<XLinkDataPoint>> mGetCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
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

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_device;
    }

    @Override
    protected void initView() {
        device_icon = findViewById(R.id.device_icon);
        device_name = findViewById(R.id.device_name);
        device_detail = findViewById(R.id.device_detail);
        device_habitat_name = findViewById(R.id.device_habitat_name);
        device_status_local = findViewById(R.id.device_status_local);
        device_status_cloud = findViewById(R.id.device_status_cloud);
        device_status_sensor = findViewById(R.id.device_status_sensor);
    }

    @Override
    protected void initData() {
        XLinkSDK.start();
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        final String deviceTag = intent.getStringExtra(AppConstants.DEVICE_TAG);
        mDevice = DeviceManager.getInstance().getDevice(deviceTag);
        if (mDevice == null) {
            return;
        }

        final String pid = mDevice.getXDevice().getProductId();
        final String name = mDevice.getXDevice().getDeviceName();
        final Home home = HomeManager.getInstance().getDeviceHome(mDevice);
        device_icon.setImageResource(DeviceUtil.getProductIconSmall(pid));
        device_name.setText(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
        if (home != null) {
            device_habitat_name.setText(home.getHome().name);
        }
        device_status_local.setImageResource(mDevice.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? R.drawable.ic_wifi_on : R.drawable.ic_wifi);
        device_status_cloud.setImageResource(mDevice.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);

        mDeviceBaseViewModel = ViewModelProviders.of(this).get(DeviceBaseViewModel.class);
        mDeviceBaseViewModel.setData(mDevice);

        XLinkDeviceManager.getInstance().addDeviceConnectionFlags(mDevice.getXDevice().getDeviceTag(), 1);
        XLinkDeviceManager.getInstance().connectDeviceLocal(mDevice.getXDevice().getDeviceTag());

        if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_LEDSTRIP)) {
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(LightViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mPowerFragment = new LightPowerFragment();
            mDeviceFragment = new LightFragment();
        } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_MONSOON)) {
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(MonsoonViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mPowerFragment = new MonsoonPowerFragment();
            mDeviceFragment = new MonsoonFragment();
        } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_SOCKET)) {
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(SocketViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mDeviceViewModel.observe(this, new Observer() {
                @Override
                public void onChanged(@Nullable Object o) {
                    EXOSocket exoSocket = (EXOSocket) mDevice;
                    device_status_sensor.setImageResource(exoSocket.getS1Available() ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
                }
            });
            EXOSocket socket = (EXOSocket) mDevice;
            device_status_sensor.setVisibility(View.VISIBLE);
            device_status_sensor.setImageResource(socket.getS1Available() ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
            mPowerFragment = new SocketPowerFragment();
            mDeviceFragment = new SocketFragment();
        } else {
            throw new RuntimeException("Invalid ProductID.");
        }

        mSetCallback = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: setDataPointError - " + error);
            }

            @Override
            public void onComplete(XDevice xDevice) {
                Log.e(TAG, "onComplete: setDataPointComplete");
            }
        };
        mGetCallback = new XlinkTaskCallback<List<XLinkDataPoint>>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: getDataPoints- " + error);
            }

            @Override
            public void onComplete(List<XLinkDataPoint> dataPoints) {
                Collections.sort(dataPoints, new Comparator<XLinkDataPoint>() {
                    @Override
                    public int compare(XLinkDataPoint o1, XLinkDataPoint o2) {
                        return o1.getIndex() - o2.getIndex();
                    }
                });
                mDevice.setDataPointList(dataPoints);

//                StringBuilder sb = new StringBuilder();
//                for (XLinkDataPoint dp : dataPoints) {
//                    sb.append(dp.toString()).append("\n");
//                }
//                Log.e(TAG, "onComplete: " + "    " + sb);
                mDeviceBaseViewModel.postValue();
                mDeviceViewModel.postValue();
                if (mPowerFragment != null) {
                    replaceFragment(R.id.device_fl_power, mPowerFragment);
                }
                replaceFragment(R.id.device_fl_show, mDeviceFragment);

                if (!XLinkUserManager.getInstance().isUserAuthorized()) {
                    mDeviceViewModel.syncDeviceDatetime();
                }
            }
        };

        mDeviceViewModel.setGetCallback(mGetCallback);
        mDeviceViewModel.setSetCallback(mSetCallback);

        mDeviceViewModel.getDatapoints();

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                XlinkCloudManager.getInstance().getNewsetVersion(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceNewestVersionResponse>() {
//                    @Override
//                    public void onError(String error) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(DeviceApi.DeviceNewestVersionResponse response) {
//                        int current_version = Integer.parseInt(response.current);
//                        int newest_version = Integer.parseInt(response.newest);
//                        if (newest_version > current_version && (newest_version-current_version)%2 == 1) {
//                            device_toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_menu_moreoverflow_material_badge));
//                        }
//                    }
//                });
//            }
//        }, 1000);

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
        device_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.device_root, new DeviceDetailFragment());
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(DeviceStateChangedEvent event) {
        Log.e(TAG, "onDeviceStateChangedEvent: ");
        if (event != null && mDevice != null
            && TextUtils.equals(event.getDeviceTag(), mDevice.getDeviceTag())) {
            device_status_local.setImageResource(mDevice.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? R.drawable.ic_wifi_on : R.drawable.ic_wifi);
            device_status_cloud.setImageResource(mDevice.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(DevicePropertyChangedEvent event) {
        if (event != null && mDevice != null && mDevice.getXDevice().getDeviceId() == event.getDeviceId()) {
            device_name.setText(event.getDeviceName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatapointChangedEvent(DatapointChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getDeviceTag(), mDevice.getDeviceTag())) {
            Device device = DeviceManager.getInstance().getDevice(event.getDeviceTag());
            if (device != null) {
//                for (XLinkDataPoint dp : device.getDataPointList()) {
//                    mDevice.setDataPoint(dp);
//                }
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
            public void onComplete(DeviceApi.ShareDeviceResponse response) {
                Toast.makeText(DeviceActivity.this, "Share Success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }
}
