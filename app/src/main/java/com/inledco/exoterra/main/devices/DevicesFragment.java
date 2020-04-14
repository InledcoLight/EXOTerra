package com.inledco.exoterra.main.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
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
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.StatusReponse;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DevicesRefreshedEvent;
import com.inledco.exoterra.event.GroupsRefreshedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends BaseFragment {
    private SwipeRefreshLayout devices_swipe_refresh;
    private View devices_warning;
    private TextView warning_tv_msg;
    private RecyclerView devices_rv_show;
    private ImageButton devices_ib_add;

    private final List<Device> mDevices = new ArrayList<>();
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
        devices_swipe_refresh = view.findViewById(R.id.devices_swipe_refresh);
        devices_warning = view.findViewById(R.id.devices_warning);
        warning_tv_msg = view.findViewById(R.id.warning_tv_msg);
        devices_rv_show = view.findViewById(R.id.devices_rv_show);
        devices_ib_add = view.findViewById(R.id.devices_ib_add);

        warning_tv_msg.setText(R.string.no_device_warning);
    }

    @Override
    protected void initData() {
        mDevices.addAll(DeviceManager.getInstance().getAllDevices());
        mAdapter = new DevicesAdapter(getContext(), mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final Device device = mDevices.get(position);
                Log.e(TAG, "onItemClick: " + device.getTag());
                gotoDeviceActivity(device.getTag());
            }
        });
        devices_rv_show.setAdapter(mAdapter);

        if (DeviceManager.getInstance().isSynchronized() == false) {
            devices_swipe_refresh.setRefreshing(true);
            refreshDevices();
        }
    }

    @Override
    protected void initEvent() {
        devices_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
//        if (event != null) {
//            for (int i = 0; i < mDevices.size(); i++) {
//                if (event.getDeviceId() == mDevices.get(i).getXDevice().getDeviceId()) {
//                    mDevices.get(i).getXDevice().setDeviceName(event.getDeviceName());
//                    mAdapter.notifyDataSetChanged();
//                    return;
//                }
//            }
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomesRefreshedEvent(GroupsRefreshedEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDevicesRefreshedEvent(DevicesRefreshedEvent event) {
        Log.e(TAG, "onDevicesRefreshedEvent: ");
        devices_swipe_refresh.setRefreshing(false);
        devices_warning.setVisibility(mDevices.size() == 0 ? View.VISIBLE : View.GONE);
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(@NonNull StatusReponse event) {
//        if (event != null) {
//            for (int i = 0; i < mDevices.size(); i++) {
//                if (TextUtils.equals(event.getDeviceTag(), mDevices.get(i).getDeviceTag())) {
//                    mAdapter.notifyItemChanged(i);
//                }
//            }
//        }
    }

    private void refreshDevices() {
        DeviceManager.getInstance().getSubscribedDevices(new HttpCallback<UserApi.UserSubscribedDevicesResponse>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: " + error);
                stopRefresh();
            }

            @Override
            public void onSuccess(UserApi.UserSubscribedDevicesResponse result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                mDevices.clear();
                mDevices.addAll(DeviceManager.getInstance().getAllDevices());
                EventBus.getDefault().post(new DevicesRefreshedEvent());
            }
        });
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

    private void stopRefresh() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devices_swipe_refresh.setRefreshing(false);
            }
        });
    }

//    private void initList() {
//        mDevices.clear();
//        //  ExoLed
//        Device exoLed = new ExoLed();
//        exoLed.setProductKey(AliotConsts.PRODUCT_KEY_EXOLED);
//        exoLed.setDeviceName("2CF432121FC9");
//        exoLed.setMac("2CF432121FC9");
//        exoLed.setName("2CF432121FC9");
//
//        //  ExoSocket
//        Device exoSocket = new ExoSocket();
//        exoSocket.setProductKey(AliotConsts.PRODUCT_KEY_EXOSOCKET);
//        exoSocket.setDeviceName("2CF432121F42");
//        exoSocket.setMac("2CF432121F42");
//        exoSocket.setName("2CF432121F42");
//
//        //ExoMonsoon
//        Device exoMonsoon = new ExoMonsoon();
//        exoMonsoon.setProductKey(AliotConsts.PRODUCT_KEY_EXOMONSOON);
//        exoMonsoon.setDeviceName("2CF4322CF664");
//        exoMonsoon.setMac("2CF4322CF664");
//        exoMonsoon.setName("2CF4322CF664");
//
//        DeviceManager.getInstance().addDevice(exoLed);
//        DeviceManager.getInstance().addDevice(exoSocket);
//        DeviceManager.getInstance().addDevice(exoMonsoon);
//        mDevices.addAll(DeviceManager.getInstance().getAllDevices());
//    }
}
