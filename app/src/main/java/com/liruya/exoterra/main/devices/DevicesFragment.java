package com.liruya.exoterra.main.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.device.DeviceActivity;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.swiperecyclerview.OnSwipeItemClickListener;
import com.liruya.swiperecyclerview.OnSwipeItemTouchListener;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;

public class DevicesFragment extends BaseFragment {
    private ProgressBar devices_progress;
    private RecyclerView devices_rv_show;

    private final List<Device> mSubscribedDevices = new ArrayList<>();
    private DevicesAdapter mAdapter;

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
        devices_progress = view.findViewById(R.id.devices_progress);
        devices_rv_show = view.findViewById(R.id.devices_rv_show);
        devices_rv_show.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        devices_rv_show.addOnItemTouchListener(new OnSwipeItemTouchListener());
    }

    @Override
    protected void initData() {
        mAdapter = new DevicesAdapter(getContext(), mSubscribedDevices);
        mAdapter.setOnSwipeItemClickListener(new OnSwipeItemClickListener() {
            @Override
            public void onContentClick(int position) {
                String deviceTag = mSubscribedDevices.get(position).getDeviceTag();
                gotoDeviceActivity(deviceTag);
            }

            @Override
            public void onActionClick(int position, int actionid) {
                final Device device = mSubscribedDevices.get(position);
                switch (actionid) {
                    case R.id.item_device_unsubsribe:
                        XlinkCloudManager.getInstance().unsubscribeDevice(device.getXDevice(), new XLinkTaskListener<String>() {
                            @Override
                            public void onError(XLinkCoreException e) {
                                Toast.makeText(getContext(), "Unsubscribe failed", Toast.LENGTH_LONG)
                                     .show();
                            }

                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onComplete(String s) {
                                Toast.makeText(getContext(), "Unsubscribe success", Toast.LENGTH_LONG)
                                     .show();
                                DeviceManager.getInstance().removeDevice(device);
                                for (int i = 0; i < mSubscribedDevices.size(); i++) {
                                    if (TextUtils.equals(device.getDeviceTag(), mSubscribedDevices.get(i).getDeviceTag())) {
                                        mSubscribedDevices.remove(i);
                                        mAdapter.notifyItemRemoved(i);
                                        return;
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        });
        devices_rv_show.setAdapter(mAdapter);
        DeviceManager.getInstance().refreshSubcribeDevices(new XLinkTaskListener<List<Device>>() {
            @Override
            public void onError(XLinkCoreException e) {
                devices_progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStart() {
                devices_progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(List<Device> devices) {
                devices_progress.setVisibility(View.INVISIBLE);
                mSubscribedDevices.clear();
                mSubscribedDevices.addAll(devices);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void initEvent() {

    }

    private void gotoDeviceActivity(String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("device_tag", deviceTag);
        startActivity(intent);
    }
}
