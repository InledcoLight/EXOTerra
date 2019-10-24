package com.inledco.exoterra.main.devices;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DevicePropertyChangedEvent;
import com.inledco.exoterra.event.DeviceStateChangedEvent;
import com.inledco.exoterra.event.SubscribeChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.sdk.v5.manager.XLinkUserManager;
import cn.xlink.sdk.v5.model.XDevice;

public class DevicesFragment extends BaseFragment {
    private Toolbar devices_toolbar;
    private SwipeRefreshLayout devices_swipe_refresh;
    private RecyclerView devices_rv_show;

    private final List<Device> mSubscribedDevices = DeviceManager.getInstance().getAllDevices();
    private DevicesAdapter mAdapter;

    private AsyncTask<Void, Void, Void> mGetPropertyTask;

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
            EventBus.getDefault()
                    .unregister(this);
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onSubscribeChangedEvent(SubscribeChangedEvent event) {
        refreshSubcribeDevices();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(DevicePropertyChangedEvent event) {
        if (event != null) {
            for (int i = 0; i < mSubscribedDevices.size(); i++) {
                XDevice device = mSubscribedDevices.get(i).getXDevice();
                if (device.getDeviceId() == event.getDeviceId()) {
                    device.setDeviceName(event.getDeviceName());
                    mAdapter.notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(DeviceStateChangedEvent event) {
        if (event != null) {
            Device device = DeviceManager.getInstance().getDevice(event.getDeviceTag());
            if (device != null) {
                for (int i = 0; i < mSubscribedDevices.size(); i++) {
                    if (TextUtils.equals(event.getDeviceTag(), mSubscribedDevices.get(i).getDeviceTag())) {
                        mSubscribedDevices.get(i).setXDevice(device.getXDevice());
                        mAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_devices;
    }

    @Override
    protected void initView(View view) {
        devices_toolbar = view.findViewById(R.id.devices_toolbar);
        devices_swipe_refresh = view.findViewById(R.id.devices_swipe_refresh);
        devices_rv_show = view.findViewById(R.id.devices_rv_show);

        devices_toolbar.inflateMenu(R.menu.menu_devices);
        Menu menu = devices_toolbar.getMenu();
        if (menu != null) {
            boolean login = XLinkUserManager.getInstance().isUserAuthorized();
            menu.findItem(R.id.menu_devices_scan).setVisible(!login);
            menu.findItem(R.id.menu_devices_add).setVisible(login);
        }
        devices_rv_show.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mAdapter = new DevicesAdapter(getContext(), mSubscribedDevices) {
            @Override
            protected void onItemClick(int position) {
                String deviceTag = mSubscribedDevices.get(position).getDeviceTag();
                gotoDeviceActivity(deviceTag);
            }

            @Override
            protected boolean onItemLongClick(int position) {
                return false;
            }
        };
        devices_rv_show.setAdapter(mAdapter);
        if (XLinkUserManager.getInstance().isUserAuthorized()) {
            refreshSubcribeDevices();
        }
    }

    @Override
    protected void initEvent() {
        devices_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_devices_smartconfig:
                        startSmartconfigActivity();
                        break;
                    case R.id.menu_devices_scan:
                        startScanActivity();
                        break;
                    case R.id.menu_devices_add:
                        startAdddeviceActivity();
                        break;
                }
                return true;
            }
        });
        devices_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (XLinkUserManager.getInstance().isUserAuthorized()) {
                    refreshSubcribeDevices();
                } else {
                    devices_swipe_refresh.setRefreshing(false);
                }
            }
        });
    }

    private void refreshSubcribeDevices() {
        DeviceManager.getInstance().refreshSubcribeDevices(new XlinkTaskCallback<List<Device>>() {
            @Override
            public void onError(String error) {
                devices_swipe_refresh.setRefreshing(false);
            }

            @Override
            public void onComplete(List<Device> devices) {
//                devices_swipe_refresh.setRefreshing(false);
                Collections.sort(devices, new Comparator<Device>() {
                    @Override
                    public int compare(Device o1, Device o2) {
                        if (o1.getXDevice().isOnline() && !o2.getXDevice().isOnline()) {
                            return -1;
                        }
                        if (!o1.getXDevice().isOnline() && o2.getXDevice().isOnline()) {
                            return 1;
                        }
                        return 0;
                    }
                });
                mSubscribedDevices.clear();
                mSubscribedDevices.addAll(devices);
                getProperty();
//                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getProperty() {
        if (mGetPropertyTask != null) {
            mGetPropertyTask.cancel(true);
            mGetPropertyTask = null;
        }
        mGetPropertyTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i < mSubscribedDevices.size(); i++) {
                    Device device = mSubscribedDevices.get(i);
                    String result = XlinkCloudManager.getInstance().getDeviceProperty(device.getXDevice());
                    Log.e(TAG, "doInBackground: " + result);
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has(AppConstants.SPECIFICATION)) {
                                String spec = jsonObject.getString(AppConstants.SPECIFICATION);
                                device.setProperty(spec);
                                DeviceManager.getInstance().getDevice(device.getDeviceTag()).setProperty(spec);
                                continue;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    device.setProperty(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                devices_swipe_refresh.setRefreshing(false);
                mAdapter.notifyDataSetChanged();
            }
        };
        mGetPropertyTask.execute();
    }

    private void gotoDeviceActivity(String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("device_tag", deviceTag);
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
