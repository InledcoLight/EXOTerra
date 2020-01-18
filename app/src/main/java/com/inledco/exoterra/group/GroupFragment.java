package com.inledco.exoterra.group;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.event.HomePropertyChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.util.TimeFormatUtil;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.restful.api.app.HomeApi;

public class GroupFragment extends BaseFragment {
    private TextView group_title;
    private ImageButton group_detail;
    private TextView group_time;
    private TextView group_sunrise;
    private TextView group_sunset;
    private TextView group_sensor1;
    private TextView group_sensor2;
    private Button group_exit;

    private TextView group_connected_devices;
    private ImageButton group_add;
    private RecyclerView group_rv;

    private String mHomeId;
    private Home mHome;
    private List<HomeApi.HomeDevicesResponse.Device> mDevices;
    private GroupDevicesAdapter mAdapter;

    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;
    private final int defaultZone = TimeZone.getDefault().getRawOffset()/60000;

    private final BroadcastReceiver mTimeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    refreshTime();
                    break;
            }
        }
    };

    private final XlinkRequestCallback<HomeApi.HomeDevicesResponse> mHomeDevicesCallback = new XlinkRequestCallback<HomeApi.HomeDevicesResponse>() {
        @Override
        public void onError(final String error) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                         .show();
                }
            });
        }

        @Override
        public void onSuccess(HomeApi.HomeDevicesResponse response) {
            mDevices.clear();
            mDevices.addAll(response.list);
            mAdapter.notifyDataSetChanged();
        }
    };

    public static GroupFragment newInstance(@NonNull final String homeid, @NonNull final String homename) {
        Bundle args = new Bundle();
        args.putString("homeid", homeid);
        args.putString("homename", homename);
        GroupFragment frag = new GroupFragment();
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

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
        getActivity().unregisterReceiver(mTimeChangeReceiver);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeChangedEvent(HomeChangedEvent event) {
        Log.e(TAG, "onHomeChangedEvent: " + mHomeId + " " + event.getHomeid());
        if (TextUtils.equals(mHomeId, event.getHomeid())) {
            group_title.setText(mHome.getHome().name);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomePropertyChangedEvent(HomePropertyChangedEvent event) {
        if (TextUtils.equals(mHomeId, event.getHomeid())) {
            refreshData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeDeviceChangedEvent(HomeDeviceChangedEvent event) {
        if (TextUtils.equals(mHomeId, event.getHomeid())) {
            group_connected_devices.setText(getString(R.string.habitat_devcnt, mHome.getDeviceCount()));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatapointChangedEvent(DatapointChangedEvent event) {
        for (int j = 0; j < mHome.getDevices().size(); j++) {
            HomeApi.HomeDevicesResponse.Device device = mHome.getDevices().get(j);
            String tag = device.productId + "_" + device.mac;
            if (TextUtils.equals(device.productId, XlinkConstants.PRODUCT_ID_SOCKET) && TextUtils.equals(tag, event.getDeviceTag())) {
                refreshSensor();
                return;
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_group;
    }

    @Override
    protected void initView(View view) {
        group_title = view.findViewById(R.id.group_title);
        group_detail = view.findViewById(R.id.group_detail);
        group_time = view.findViewById(R.id.group_time);
        group_sunrise = view.findViewById(R.id.group_sunrise);
        group_sunset = view.findViewById(R.id.group_sunset);
        group_sensor1 = view.findViewById(R.id.group_sensor1);
        group_sensor2 = view.findViewById(R.id.group_sensor2);
        group_connected_devices = view.findViewById(R.id.group_conneted_devices);
        group_add = view.findViewById(R.id.group_add);
        group_rv = view.findViewById(R.id.group_rv);
        group_exit = view.findViewById(R.id.group_exit);
    }

    @Override
    protected void initData() {
        mDateFormat = GlobalSettings.getDateTimeFormat();
        mTimeFormat = GlobalSettings.getTimeFormat();
        Bundle args = getArguments();
        if (args != null) {
            mHomeId = args.getString("homeid");
            mHome = HomeManager.getInstance().getHome(mHomeId);
            if (mHome != null) {
                mDevices = mHome.getDevices();
                mAdapter = new GroupDevicesAdapter(getContext(), mDevices);
                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        HomeApi.HomeDevicesResponse.Device device = mDevices.get(position);
                        String deviceTag = device.productId + "_" + device.mac;
                        gotoDeviceActivity(deviceTag);
                    }
                });
                group_rv.setAdapter(mAdapter);

                group_title.setText(mHome.getHome().name);
                refreshData();
                group_connected_devices.setText(getString(R.string.habitat_devcnt, mHome.getDeviceCount()));
                HomeManager.getInstance().refreshHome(mHome);
            }
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeChangeReceiver, filter);
    }

    @Override
    protected void initEvent() {
        group_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.main_fl, HabitatDetailFragment.newInstance(mHomeId));
            }
        });

        group_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddDeviceActivity();
            }
        });

        group_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void refreshTime() {
        int zone = mHome.getZone();
        long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
        group_time.setText(mDateFormat.format(time));
//        int dt = date.getHours()*60 + date.getMinutes();
//        int sunrise = mHome.getSunrise();
//        int sunset = mHome.getSunset();
//        if (sunrise < sunset) {
//            if (dt >= sunrise && dt < sunset) {
//                group_daynight.setText(R.string.daytime);
//                group_daynight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sun, 0, 0, 0);
//            } else {
//                group_daynight.setText(R.string.nighttime);
//                group_daynight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_moon, 0, 0, 0);
//            }
//        } else {
//            if (dt >= sunrise || dt < sunset) {
//                group_daynight.setText(R.string.daytime);
//                group_daynight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sun, 0, 0, 0);
//            } else {
//                group_daynight.setText(R.string.nighttime);
//                group_daynight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_moon, 0, 0, 0);
//            }
//        }
    }

    private void refreshSensor() {
        for (HomeApi.HomeDevicesResponse.Device dev : mHome.getDevices()) {
            if (TextUtils.equals(dev.productId, XlinkConstants.PRODUCT_ID_SOCKET)) {
                String tag = dev.productId + "_" + dev.mac;
                Device device = DeviceManager.getInstance().getDevice(tag);
                if (device != null && device instanceof EXOSocket) {
                    EXOSocket socket = (EXOSocket) device;
                    boolean res = false;
                    if (socket.getS1Available()) {
                        group_sensor1.setText(GlobalSettings.getTemperatureText(socket.getS1Value()));
                        res = true;
                    } else {
                        group_sensor1.setText(null);
                    }
                    if (socket.getS2Available()) {
                        group_sensor2.setText(GlobalSettings.getHumidityText(socket.getS2Value()));
                        res = true;
                    } else {
                        group_sensor2.setText(null);
                    }
                    if (res) {
                        break;
                    }
                }
            }
        }
    }

    private void refreshData() {
        refreshTime();
        group_sunrise.setText(TimeFormatUtil.formatMinutesTime(mTimeFormat, mHome.getSunrise()));
        group_sunset.setText(TimeFormatUtil.formatMinutesTime(mTimeFormat, mHome.getSunset()));
        refreshSensor();
    }

    private void gotoDeviceActivity(String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("device_tag", deviceTag);
        startActivity(intent);
    }

    private void gotoAddDeviceActivity() {
        Intent intent = new Intent(getContext(), AddDeviceActivity.class);
        intent.putExtra(AppConstants.HOME_ID, mHomeId);
        startActivity(intent);
    }
}
