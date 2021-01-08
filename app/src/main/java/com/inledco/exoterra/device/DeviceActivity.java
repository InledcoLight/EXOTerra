package com.inledco.exoterra.device;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ADevice;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.DeviceViewModel;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.aliot.MonsoonViewModel;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.device.Monsoon.MonsoonModeFragment;
import com.inledco.exoterra.device.detail.DeviceDetailFragment;
import com.inledco.exoterra.device.light.LightModeFragment;
import com.inledco.exoterra.device.socket.SocketFragment;
import com.inledco.exoterra.device.socket.SocketModeFragment;
import com.inledco.exoterra.event.DeviceChangedEvent;
import com.inledco.exoterra.event.DeviceStatusChangedEvent;
import com.inledco.exoterra.event.DisconnectIotEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.DeviceIconUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DeviceActivity extends BaseActivity {
    private ImageView device_icon;
    private TextView device_name;
    private ImageButton device_detail;
    private TextView device_habitat_name;
    private ImageView device_status_state;
    private ImageView device_status_sensor;
    private Button device_btn_back;
    private LinearLayout device_ll_show;

    private String productKey;
    private String deviceName;
    private String deviceIp;
    private int devicePort;

    private Device mDevice;
    private DeviceViewModel mDeviceViewModel;
    private DeviceViewModel mDeviceBaseViewModel;
    private ExoProduct mProduct;

    private Fragment mModeFragment;
    private Fragment mDeviceFragment;

    private boolean authorized;

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
        if (mDeviceViewModel != null) {
            mDeviceViewModel.deinit();
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
        device_status_state = findViewById(R.id.device_status_state);
        device_status_sensor = findViewById(R.id.device_status_sensor);
        device_btn_back = findViewById(R.id.device_btn_back);
        device_ll_show = findViewById(R.id.device_ll_show);
    }

    @Override
    protected void initData() {
        authorized = UserManager.getInstance().isAuthorized();
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        productKey = intent.getStringExtra("productKey");
        deviceName = intent.getStringExtra("deviceName");
        deviceIp = intent.getStringExtra("deviceIp");
        devicePort = intent.getIntExtra("devicePort", 0);
        mProduct = ExoProduct.getExoProduct(productKey);
        if (mProduct == null) {
            return;
        }

        if (authorized) {
            String deviceTag = productKey + "_" + deviceName;
            mDevice = DeviceManager.getInstance().getDevice(deviceTag);
            if (mDevice == null) {
                return;
            }
        } else {
            switch (mProduct) {
                case ExoLed:
                    mDevice = new ExoLed(productKey, deviceName);
                    break;
                case ExoSocket:
                    mDevice = new ExoSocket(productKey, deviceName);
                    break;
                case ExoMonsoon:
                    mDevice = new ExoMonsoon(productKey, deviceName);
                    break;
                default:
                    return;
            }
            mDevice.setIp(deviceIp);
            mDevice.setPort(devicePort);
        }
//            LocalClient.getInstance().init(new UdpClient.Listener() {
//                @Override
//                public void onError(String error) {
//                    dismissLoadDialog();
//                }
//
//                @Override
//                public void onReceive(String ip, int port, byte[] bytes) {
//                    if (port == devicePort && TextUtils.equals(ip, deviceIp)) {
//                        String payload = new String(bytes);
//                        try {
//                            DeviceParams params = JSON.parseObject(payload, DeviceParams.class);
//                            if (params != null) {
//                                mDevice.updateValues(params.getParams());
//                                mDeviceViewModel.postValue();
//                                mDeviceBaseViewModel.postValue();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        dismissLoadDialog();
//                    }
//                }
//            });
//        }

        String name = mDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = mProduct.getDefaultName();
        }
        int iconRes = DeviceIconUtil.getDeviceIconRes(this, mDevice.getRemark2(), mProduct.getIcon());
        device_icon.setImageResource(iconRes);
        device_name.setText(name);
        final Group group = GroupManager.getInstance().getDeviceGroup(productKey, mDevice.getDeviceName());
        if (group != null) {
            device_habitat_name.setText(group.name);
        }
        if (authorized) {
            device_status_state.setImageResource(mDevice.isOnline() ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        }

        mDeviceBaseViewModel = ViewModelProviders.of(this).get(DeviceViewModel.class);
        mDeviceBaseViewModel.setData(mDevice);

        switch (mProduct) {
            case ExoLed:
                mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(LightViewModel.class);
                mDeviceViewModel.setData(mDevice);
                mModeFragment = new LightModeFragment();
//                mDeviceFragment = new LightFragment();
                break;
            case ExoSocket:
                mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(SocketViewModel.class);
                mDeviceViewModel.setData(mDevice);
                ExoSocket socket = (ExoSocket) mDevice;
                device_status_sensor.setVisibility(View.VISIBLE);
                device_status_sensor.setImageResource(socket.getSensorAvailable() ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
                mModeFragment = new SocketModeFragment();
                mDeviceFragment = new SocketFragment();
                break;
            case ExoMonsoon:
                mDeviceViewModel = ViewModelProviders.of(DeviceActivity.this).get(MonsoonViewModel.class);
                mDeviceViewModel.setData(mDevice);
                mModeFragment = new MonsoonModeFragment();
//                mDeviceFragment = new MonsoonFragment();
                break;
            default:
                return;
        }
        mDeviceViewModel.observe(this, o -> {
            mDeviceBaseViewModel.postValue();
            dismissLoadDialog();
            if (mDevice instanceof ExoSocket) {
                ExoSocket socket = (ExoSocket) mDevice;
                device_status_sensor.setImageResource(socket.getSensorAvailable() ? R.drawable.ic_sensor_on : R.drawable.ic_sensor);
            }
        });

        replaceFragment(R.id.device_fl_ext, mModeFragment);
        if (mDeviceFragment != null) {
            replaceFragment(R.id.device_fl_show, mDeviceFragment);
        }
        mDeviceViewModel.getAllProperties();
        showLoadDialog();
    }

    @Override
    protected void initEvent() {
        device_detail.setOnClickListener(v -> {
            if (mDevice == null) {
                return;
            }
            addFragmentToStack(R.id.device_root, DeviceDetailFragment.newInstance(mDevice.getProductKey()));
        });

        device_btn_back.setOnClickListener(v -> finish());
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDiconnectIotEvent(DisconnectIotEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceStatusChangedEvent(DeviceStatusChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getTag(), mDevice.getTag())
            && authorized) {
            device_status_state.setImageResource(mDevice.isOnline() ? R.drawable.ic_cloud_green_16dp : R.drawable.ic_cloud_grey_16dp);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(ADevice aDevice) {
        if (mDevice != null && TextUtils.equals(mDevice.getTag(), aDevice.getTag())) {
            mDeviceViewModel.postValue();
            mDeviceBaseViewModel.postValue();
            dismissLoadDialog();
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceChangedEvent(DeviceChangedEvent event) {
        if (event != null && mDevice != null && mProduct != null
            && TextUtils.equals(event.getTag(), mDevice.getTag())) {
            String name = mDevice.getName();
            if (TextUtils.isEmpty(name)) {
                name = mProduct.getDefaultName();
            }
            device_name.setText(name);

            int iconRes = DeviceIconUtil.getDeviceIconRes(this, mDevice.getRemark2(), mProduct.getIcon());
            device_icon.setImageResource(iconRes);
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
