package com.inledco.exoterra.device;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ADevice;
import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.DeviceBaseViewModel;
import com.inledco.exoterra.aliot.DeviceViewModel;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.aliot.MonsoonViewModel;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.device.Monsoon.MonsoonFragment;
import com.inledco.exoterra.device.Monsoon.MonsoonPowerFragment;
import com.inledco.exoterra.device.detail.DeviceDetailFragment;
import com.inledco.exoterra.device.light.LightFragment;
import com.inledco.exoterra.device.light.LightPowerFragment;
import com.inledco.exoterra.device.socket.SocketFragment;
import com.inledco.exoterra.device.socket.SocketPowerFragment;
import com.inledco.exoterra.event.DeviceChangedEvent;
import com.inledco.exoterra.event.DeviceStatusChangedEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.util.DeviceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DeviceActivity extends BaseActivity {
    private ImageView device_icon;
    private TextView device_name;
    private ImageButton device_detail;
    private TextView device_habitat_name;
    private ImageView device_status_local;
    private ImageView device_status_cloud;
    private ImageView device_status_sensor;

//    private String productKey;
//    private String deviceName;
    private Device mDevice;
    private DeviceViewModel mDeviceViewModel;
    private DeviceBaseViewModel mDeviceBaseViewModel;

    private Fragment mPowerFragment;
    private Fragment mDeviceFragment;

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
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String deviceTag = intent.getStringExtra("deviceTag");
        mDevice = DeviceManager.getInstance().getDevice(deviceTag);
        if (mDevice == null) {
            return;
        }
        String pkey = mDevice.getProductKey();
        String name = mDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = DeviceUtil.getDefaultName(pkey);
        }
        device_icon.setImageResource(DeviceUtil.getProductIconSmall(pkey));
        device_name.setText(name);
        final Group group = GroupManager.getInstance().getDeviceGroup(pkey, mDevice.getDeviceName());
        if (group != null) {
            device_habitat_name.setText(group.name);
        }
//        device_status_local.setImageResource(mDevice.isOnline() ? R.drawable.ic_wifi_on : R.drawable.ic_wifi);
        device_status_cloud.setImageResource(mDevice.isOnline() ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);

        mDeviceBaseViewModel = ViewModelProviders.of(this).get(DeviceBaseViewModel.class);
        mDeviceBaseViewModel.setData(mDevice);

        if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOLED)) {
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(LightViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mPowerFragment = new LightPowerFragment();
            mDeviceFragment = new LightFragment();
        } else if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOMONSOON)) {
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(MonsoonViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mPowerFragment = new MonsoonPowerFragment();
            mDeviceFragment = new MonsoonFragment();
        } else if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
            mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(SocketViewModel.class);
            mDeviceViewModel.setData(mDevice);
            mDeviceViewModel.observe(this, new Observer() {
                @Override
                public void onChanged(@Nullable Object o) {
                    ExoSocket exoSocket = (ExoSocket) mDevice;
                    device_status_sensor.setImageResource(exoSocket.getSensorAvailable() ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
                }
            });
            ExoSocket socket = (ExoSocket) mDevice;
            device_status_sensor.setVisibility(View.VISIBLE);
            device_status_sensor.setImageResource(socket.getSensorAvailable() ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
            mPowerFragment = new SocketPowerFragment();
            mDeviceFragment = new SocketFragment();
        } else {
            throw new RuntimeException("Invalid productKey.");
        }

        replaceFragment(R.id.device_fl_power, mPowerFragment);
        replaceFragment(R.id.device_fl_show, mDeviceFragment);
        mDeviceViewModel.getAllProperties();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceStatusChangedEvent(DeviceStatusChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getTag(), mDevice.getTag())) {
            device_status_cloud.setImageResource(mDevice.isOnline() ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(ADevice aDevice) {
        if (mDevice != null && TextUtils.equals(mDevice.getTag(), aDevice.getTag())) {
            mDeviceViewModel.postValue();
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceChangedEvent(DeviceChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getTag(), mDevice.getTag())) {
            String name = mDevice.getName();
            if (TextUtils.isEmpty(name)) {
                name = DeviceUtil.getDefaultName(mDevice.getProductKey());
            }
            device_name.setText(name);
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onGroupDeviceChangedEvent(GroupDeviceChangedEvent event) {
        if (event != null && mDevice != null) {
            Group group = GroupManager.getInstance().getDeviceGroup(mDevice.getProductKey(), mDevice.getDeviceName());
            if (group != null && TextUtils.equals(group.groupid, event.getGroupid())) {
                device_habitat_name.setText(group.name);
            }
        }
    }
}
