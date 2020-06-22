package com.inledco.exoterra.main.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.aliot.ADevice;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DeviceChangedEvent;
import com.inledco.exoterra.event.DeviceStatusChangedEvent;
import com.inledco.exoterra.event.DevicesRefreshedEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.event.GroupsRefreshedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.OnErrorCallback;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends BaseFragment {
    private SmartRefreshLayout devices_swipe_refresh;
    private View devices_warning;
    private TextView warning_tv_msg;
    private RecyclerView devices_rv_show;
    private ImageButton devices_ib_add;

    private final List<Device> mDevices = new ArrayList<>();
    private DevicesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        EventBus.getDefault().register(this);
        initData();
        initEvent();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void initHeader() {
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.loading);
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.pulldown_to_refresh);
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.release_to_refresh);
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.refreshing);
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.refresh_failed);
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.refresh_success);
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.last_update);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_devices;
    }

    @Override
    protected void initView(View view) {
        devices_swipe_refresh = view.findViewById(R.id.devices_swipe_refresh);
        devices_warning = view.findViewById(R.id.devices_warning);
        warning_tv_msg = view.findViewById(R.id.warning_tv_msg);
        devices_rv_show = view.findViewById(R.id.devices_rv_show);
        devices_ib_add = view.findViewById(R.id.devices_ib_add);

        warning_tv_msg.setText(R.string.no_device_warning);
        initHeader();
        ClassicsHeader header = new ClassicsHeader(getContext());
        devices_swipe_refresh.setRefreshHeader(header);
    }

    @Override
    protected void initData() {
        mDevices.addAll(DeviceManager.getInstance().getAllDevices());
        mAdapter = new DevicesAdapter(getContext(), mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final Device device = mDevices.get(position);
                gotoDeviceActivity(device.getProductKey(), device.getDeviceName());
            }
        });
        devices_rv_show.setAdapter(mAdapter);

        if (DeviceManager.getInstance().needSynchronize()) {
            refreshDevices();
        }
        if (DeviceManager.getInstance().isSynchronizing() && !DeviceManager.getInstance().isSynchronized()) {
            devices_swipe_refresh.autoRefresh();
        } else {
            devices_warning.setVisibility(mDevices.size() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void initEvent() {
        devices_swipe_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshDevices();
            }
        });

        devices_ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAdddeviceActivity();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(@NonNull ADevice event) {
//        if (event == null) {
//            return;
//        }
//        for (int i = 0; i < mDevices.size(); i++) {
//            Device device = mDevices.get(i);
//            if (TextUtils.equals(device.getTag(), event.getTag())) {
//                mAdapter.notifyItemChanged(i);
//            }
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupsRefreshedEvent(GroupsRefreshedEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupDeviceChangedEvent(GroupDeviceChangedEvent event) {
        if (event == null) {
            return;
        }
        for (int i = 0; i < mDevices.size(); i++) {
            Device device = mDevices.get(i);
            Group group = GroupManager.getInstance().getDeviceGroup(device.getProductKey(), device.getDeviceName());
            if (group != null && TextUtils.equals(event.getGroupid(), group.groupid)) {
                mAdapter.notifyItemChanged(i);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDevicesRefreshedEvent(DevicesRefreshedEvent event) {
        Log.e(TAG, "onDevicesRefreshedEvent: ");
        mDevices.clear();
        mDevices.addAll(DeviceManager.getInstance().getAllDevices());
        devices_swipe_refresh.finishRefresh(500);
        devices_warning.setVisibility(mDevices.size() == 0 ? View.VISIBLE : View.GONE);
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceStatusChangedEvent(DeviceStatusChangedEvent event) {
        Log.e(TAG, "onDeviceStatusChangedEvent: " + JSON.toJSONString(event));
        if (event != null) {
            for (int i = 0; i < mDevices.size(); i++) {
                Device device = mDevices.get(i);
                if (TextUtils.equals(event.getTag(), device.getTag())){
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceChangedEvent(DeviceChangedEvent event) {
        if (event != null) {
            for (int i = 0; i < mDevices.size(); i++) {
                Device device = mDevices.get(i);
                if (TextUtils.equals(device.getTag(), event.getTag())) {
                    mAdapter.notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    private void refreshDevices() {
        DeviceManager.getInstance().getSubscribedDevices(new OnErrorCallback() {
            @Override
            public void onError(String error) {
                devices_swipe_refresh.finishRefresh(1000, false, false);
            }
        });
    }

    private void gotoDeviceActivity(String productKey, String deviceName) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("productKey", productKey);
        intent.putExtra("deviceName", deviceName);
        startActivity(intent);
    }

    private void gotoDeviceActivity(String tag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("deviceTag", tag);
        startActivity(intent);
    }

    private void startSmartconfigActivity() {
        Intent intent = new Intent(getContext(), SmartconfigActivity.class);
        startActivity(intent);
    }

    private void startScanActivity() {
        Intent intent = new Intent(getContext(), ScanActivity.class);
        startActivity(intent);
    }

    private void startAdddeviceActivity() {
        Intent intent = new Intent(getContext(), AddDeviceActivity.class);
        startActivity(intent);
    }
}
