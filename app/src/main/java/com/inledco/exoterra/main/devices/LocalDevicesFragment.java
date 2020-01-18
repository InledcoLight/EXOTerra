package com.inledco.exoterra.main.devices;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.LocalDevice;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.inledco.exoterra.util.LocalDevicePrefUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkScanDeviceListener;
import cn.xlink.sdk.v5.model.XDevice;

public class LocalDevicesFragment extends BaseFragment {
    private SwipeRefreshLayout devices_swipe_refresh;
    private View devices_warning;
    private TextView warning_tv_msg;
    private RecyclerView devices_rv_show;
    private ImageButton devices_ib_add;

    private final List<LocalDevice> mLocalDevices = new ArrayList<>();
    private LocalDevicesAdapter mAdapter;

    private ProgressDialog mProgressDialog;

//    private AsyncTask<Void, Void, Void> mGetPropertyTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        initData();
        initEvent();

        return view;
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
    }

    @Override
    protected void initData() {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mLocalDevices.addAll(LocalDevicePrefUtil.getLocalDevices(getContext()));
        mAdapter = new LocalDevicesAdapter(getContext(), mLocalDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final LocalDevice ld = mLocalDevices.get(position);
                if (!DeviceManager.getInstance().contains(ld.getTag())) {
                    return;
                }
                mProgressDialog.show();
                XlinkCloudManager.getInstance().addDevice(ld.getxDevice(), 5000, new XlinkTaskCallback<XDevice>() {
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "onError: " + error);
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onComplete(XDevice device) {
                        mProgressDialog.dismiss();
                        gotoDeviceActivity(ld.getTag());
                    }
                });
            }
        });
        devices_rv_show.setAdapter(mAdapter);

        devices_swipe_refresh.setRefreshing(true);
        refreshLocalDevices();
    }

    @Override
    protected void initEvent() {
//        devices_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
//                    case R.id.menu_devices_smartconfig:
//                        startSmartconfigActivity();
//                        break;
//                    case R.id.menu_devices_scan:
//                        startScanActivity();
//                        break;
//                    case R.id.menu_devices_add:
//                        startAdddeviceActivity();
//                        break;
//                }
//                return true;
//            }
//        });
        devices_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLocalDevices();
            }
        });

        devices_ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAdddeviceActivity();
            }
        });
    }

    private void refreshLocalDevices() {
        DeviceManager.getInstance().clear();
        final List<LocalDevice> localDevices = LocalDevicePrefUtil.getLocalDevices(getContext());
        if (localDevices == null || localDevices.size() == 0) {
            mLocalDevices.clear();
            mAdapter.notifyDataSetChanged();
            devices_swipe_refresh.setRefreshing(false);
            devices_warning.setVisibility(View.VISIBLE);
            return;
        }
        devices_warning.setVisibility(View.GONE);
        XlinkCloudManager.getInstance().scanDevice(XlinkConstants.XLINK_PRODUCTS, 5000, 1000, new XLinkScanDeviceListener() {
            @Override
            public void onScanResult(final XDevice xDevice) {
                final String pid = xDevice.getProductId();
                final String mac = xDevice.getMacAddress();
                int count = 0;
                for (LocalDevice ld : localDevices) {
                    if (ld.getxDevice() != null) {
                        count++;
                    }
                    if (TextUtils.equals(pid, ld.getPid()) && TextUtils.equals(mac, ld.getMac())) {
                        xDevice.setDeviceName(ld.getName());
                        ld.setxDevice(xDevice);
                        DeviceManager.getInstance().updateDevice(xDevice);
                        count++;
                        if (count == localDevices.size()) {
                            onComplete(null);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onError(XLinkCoreException e) {
                onComplete(null);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(Void aVoid) {
                Collections.sort(localDevices, new Comparator<LocalDevice>() {
                    @Override
                    public int compare(LocalDevice o1, LocalDevice o2) {
                        if (o1.getxDevice() == null && o2.getxDevice() != null) {
                            return 1;
                        }
                        if (o1.getxDevice() != null && o2.getxDevice() == null) {
                            return -1;
                        }
                        return 0;
                    }
                });
                mLocalDevices.clear();
                mLocalDevices.addAll(localDevices);
                mAdapter.notifyDataSetChanged();
                devices_swipe_refresh.setRefreshing(false);
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
//                for (int i = 0; i < mLocalDevices.size(); i++) {
//                    Device device = mLocalDevices.get(i);
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
