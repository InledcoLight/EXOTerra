package com.inledco.exoterra.main.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.DeviceStateChangedEvent;
import com.inledco.exoterra.event.DevicesRefreshedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DevicesFragment extends BaseFragment {
    private Toolbar devices_toolbar;
    private SwipeRefreshLayout devices_swipe_refresh;
    private RecyclerView devices_rv_show;

    private final List<Device> mDevices = DeviceManager.getInstance().getAllDevices();
    private DevicesAdapter mAdapter;

//    private AsyncTask<Void, Void, Void> mGetPropertyTask;

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
        devices_rv_show.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mAdapter = new DevicesAdapter(getContext(), mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final Device device = mDevices.get(position);
                gotoDeviceActivity(device.getDeviceTag());
            }
        });
        devices_rv_show.setAdapter(mAdapter);

        devices_swipe_refresh.setRefreshing(true);
        refreshDevices();
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
                refreshDevices();
            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDevicesRefreshedEvent(DevicesRefreshedEvent event) {
        mAdapter.notifyDataSetChanged();
        devices_swipe_refresh.setRefreshing(false);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(DeviceStateChangedEvent event) {
        if (event != null) {
            for (int i = 0; i < mDevices.size(); i++) {
                if (TextUtils.equals(event.getDeviceTag(), mDevices.get(i).getDeviceTag())) {
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDatapointChangedEvent(DatapointChangedEvent event) {
        if (event != null) {
            for (int i = 0; i < mDevices.size(); i++) {
                if (TextUtils.equals(event.getDeviceTag(), mDevices.get(i).getDeviceTag())) {
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    private void refreshDevices() {
        DeviceManager.getInstance().syncSubcribeDevices(new XlinkTaskCallback<List<Device>>() {
            @Override
            public void onError(String error) {
                devices_swipe_refresh.setRefreshing(false);
            }

            @Override
            public void onComplete(List<Device> devices) {

            }
        });
    }

//    private void getProperty() {
//        if (mGetPropertyTask != null) {
//            mGetPropertyTask.cancel(true);
//            mGetPropertyTask = null;
//        }
//        mGetPropertyTask = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                for (int i = 0; i < mDevices.size(); i++) {
//                    Device device = mDevices.get(i);
//                    String result = XlinkCloudManager.getInstance().getDeviceProperty(device.getXDevice());
//                    Log.e(TAG, "doInBackground: " + result);
//                    if (!TextUtils.isEmpty(result)) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(result);
//                            if (jsonObject.has(AppConstants.SPECIFICATION)) {
//                                String spec = jsonObject.getString(AppConstants.SPECIFICATION);
//                                device.setProperty(spec);
//                                DeviceManager.getInstance().getDevice(device.getDeviceTag()).setProperty(spec);
//                                continue;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    device.setProperty(null);
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                devices_swipe_refresh.setRefreshing(false);
//                mAdapter.notifyDataSetChanged();
//            }
//        };
//        mGetPropertyTask.execute();
//    }

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
