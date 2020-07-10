package com.inledco.exoterra.group;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.aliot.ADevice;
import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.common.OnItemLongClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.GroupChangedEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.GroupUtil;
import com.inledco.exoterra.util.SensorUtil;
import com.inledco.exoterra.util.TimeFormatUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class GroupFragment extends BaseFragment {
    private ImageView group_icon;
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

    private String mGroupid;
    private Group mGroup;
    private GroupDevicesAdapter mAdapter;

    private boolean mGroupAdmin;

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

    public static GroupFragment newInstance(@NonNull final String groupid, @NonNull final String name) {
        Bundle args = new Bundle();
        args.putString("groupid", groupid);
        args.putString("name", name);
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
        getContext().unregisterReceiver(mTimeChangeReceiver);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupChangedEvent(GroupChangedEvent event) {
        if (TextUtils.equals(mGroupid, event.getGroupid())) {
            refreshData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupDeviceChangedEvent(GroupDeviceChangedEvent event) {
        if (event == null) {
            return;
        }
        if (TextUtils.equals(mGroupid, event.getGroupid())) {
            group_connected_devices.setText(getString(R.string.habitat_devcnt, mGroup.getDeviceCount()));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(ADevice adev) {
        if (adev == null || TextUtils.equals(adev.getProductKey(), AliotConsts.PRODUCT_KEY_EXOSOCKET) == false) {
            return;
        }
        refreshSensor();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_group;
    }

    @Override
    protected void initView(View view) {
        group_icon = view.findViewById(R.id.group_icon);
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
            mGroupid = args.getString("groupid");
            String name = args.getString("name");
            mGroup = GroupManager.getInstance().getGroup(mGroupid);
            if (mGroup != null) {
                mGroupAdmin = TextUtils.equals(UserManager.getInstance().getUserid(), mGroup.creator);
                mAdapter = new GroupDevicesAdapter(getContext(), mGroup.devices);
                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Group.Device device = mGroup.devices.get(position);
                        gotoDeviceActivity(device.product_key, device.device_name);
                    }
                });
                mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(int position) {
                        if (mGroupAdmin) {
                            Group.Device device = mGroup.devices.get(position);
                            showItemActionDialog(mGroupid, device);
                            return true;
                        }
                        return false;
                    }
                });
                group_rv.setAdapter(mAdapter);

                group_title.setText(name);
                refreshData();
                group_connected_devices.setText(getString(R.string.habitat_devcnt, mGroup.getDeviceCount()));
            }

            if (TextUtils.equals(UserManager.getInstance().getUserid(), mGroupid)) {
                group_add.setVisibility(View.VISIBLE);
            }
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(mTimeChangeReceiver, filter);
    }

    @Override
    protected void initEvent() {
        group_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.main_fl, HabitatDetailFragment.newInstance(mGroupid));
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
        int zone = mGroup.getZone();
        long time = System.currentTimeMillis();
        mDateFormat.setTimeZone(new SimpleTimeZone(zone*60000, ""));
        group_time.setText(mDateFormat.format(time));
    }

    private void refreshSensor() {
        for (Group.Device dev : mGroup.devices) {
            if (TextUtils.equals(dev.product_key, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
                String key = dev.product_key + "_" + dev.device_name;
                Device device = DeviceManager.getInstance().getDevice(key);
                if (device != null && device instanceof ExoSocket) {
                    ExoSocket socket = (ExoSocket) device;
                    boolean res = false;
                    if (socket.getSensorAvailable()) {
                        ExoSocket.Sensor[] sensors = socket.getSensor();
                        if (sensors == null) {
                            group_sensor1.setText(null);
                            group_sensor2.setText(null);
                            continue;
                        }
                        if (sensors.length >= 1) {
                            ExoSocket.Sensor sensor1 = sensors[0];
                            int value1 = sensor1.getValue();
                            int type1 = sensor1.getType();
                            String s1text = SensorUtil.getSensorValueText(value1, type1) + SensorUtil.getSensorUnit(type1);
                            group_sensor1.setText(s1text);
                            res = true;
                        }
                        if (sensors.length >= 2) {
                            ExoSocket.Sensor sensor2 = sensors[1];
                            int value2 = sensor2.getValue();
                            int type2 = sensor2.getType();
                            String s2text = SensorUtil.getSensorValueText(value2, type2) + SensorUtil.getSensorUnit(type2);
                            group_sensor2.setText(s2text);
                            res = true;
                        }
                    }
                    if (res) {
                        break;
                    }
                }
            }
        }
    }

    private void refreshData() {
        group_icon.setImageResource(GroupUtil.getGroupIcon(mGroup.remark2));
        group_title.setText(mGroup.name);
        refreshTime();
        group_sunrise.setText(TimeFormatUtil.formatMinutesTime(mTimeFormat, mGroup.getSunrise()));
        group_sunset.setText(TimeFormatUtil.formatMinutesTime(mTimeFormat, mGroup.getSunset()));
        refreshSensor();
    }

    private void gotoDeviceActivity(String productKey, String deviceName) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("productKey", productKey);
        intent.putExtra("deviceName", deviceName);
        startActivity(intent);
    }

    private void gotoAddDeviceActivity() {
        Intent intent = new Intent(getContext(), AddDeviceActivity.class);
        intent.putExtra(AppConstants.HOME_ID, mGroupid);
        startActivity(intent);
    }

    private void showItemActionDialog(final String groupid, final Group.Device device) {
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
        Button btn_remove = view.findViewById(R.id.dialog_action_act2);
        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
        btn_remove.setText(R.string.remove);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AliotServer.getInstance().removeDeviceFromGroup(groupid, device.product_key, device.device_name, new HttpCallback<UserApi.Response>() {
                    @Override
                    public void onError(String error) {
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.Response result) {
                        mGroup.removeDevice(device);
                        EventBus.getDefault().post(new GroupDeviceChangedEvent(mGroupid));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
    }
}
