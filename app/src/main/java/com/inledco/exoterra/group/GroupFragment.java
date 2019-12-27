package com.inledco.exoterra.group;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.common.OnItemLongClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.event.HomePropertyChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.restful.api.app.HomeApi;

public class GroupFragment extends BaseFragment {
    private Toolbar group_toolbar;
    private TextView group_time;
    private TextView group_sunrise;
    private TextView group_sunset;
    private TextView group_daynight;
    private TextView group_sensor1;
    private TextView group_sensor2;

    private TextView group_connected_devices;
    private ImageButton group_add;
    private RecyclerView group_rv;

    private String mHomeId;
    private Home mHome;
    private List<HomeApi.HomeDevicesResponse.Device> mDevices;
    private GroupDevicesAdapter mAdapter;

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
            group_toolbar.setTitle(mHome.getHome().name);
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
        group_toolbar = view.findViewById(R.id.group_toolbar);
        group_time = view.findViewById(R.id.group_time);
        group_sunrise = view.findViewById(R.id.group_sunrise);
        group_sunset = view.findViewById(R.id.group_sunset);
        group_daynight = view.findViewById(R.id.group_daynight);
        group_sensor1 = view.findViewById(R.id.group_sensor1);
        group_sensor2 = view.findViewById(R.id.group_sensor2);
        group_connected_devices = view.findViewById(R.id.group_conneted_devices);
        group_add = view.findViewById(R.id.group_add);
        group_rv = view.findViewById(R.id.group_rv);

        group_toolbar.inflateMenu(R.menu.menu_group);
        group_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        group_sensor1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_temperature, 0, 0, 0);
        group_sensor2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_humidity, 0, 0, 0);
    }

    @Override
    protected void initData() {
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
                mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(int position) {
                        showItemActionDialog(mDevices.get(position));
                        return true;
                    }
                });
                group_rv.setAdapter(mAdapter);

                group_toolbar.setTitle(mHome.getHome().name);
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
        group_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        group_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_group_add:
                        addFragmentToStack(R.id.main_fl, AddGroupDeviceFragment.newInstance(mHomeId));
                        break;
                    case R.id.menu_group_more:
                        addFragmentToStack(R.id.main_fl, HabitatDetailFragment.newInstance(mHomeId));
                        break;
                }
                return true;
            }
        });

        group_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddDeviceActivity();
//                addFragmentToStack(R.id.main_fl, AddGroupDeviceFragment.newInstance(mHomeId));
            }
        });
    }

    private String getTimeText(final int time) {
        DecimalFormat df = new DecimalFormat("00");
        return df.format(time/60) + ":" + df.format(time%60);
    }

    private void refreshTime() {
        final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
        int zone = mHome.getZone();
        long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
        DateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
        Date date = new Date(time);
        group_time.setText(format.format(date));
        int dt = date.getHours()*60 + date.getMinutes();
        int sunrise = mHome.getSunrise();
        int sunset = mHome.getSunset();
        if (sunrise < sunset) {
            if (dt >= sunrise && dt < sunset) {
                group_daynight.setText(R.string.daytime);
            } else {
                group_daynight.setText(R.string.nighttime);
            }
        } else {
            if (dt >= sunrise || dt < sunset) {
                group_daynight.setText(R.string.daytime);
            } else {
                group_daynight.setText(R.string.nighttime);
            }
        }
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
                        group_sensor1.setText("" + (float) socket.getS1Value() / 10 + " ℃");
                        res = true;
                    } else {
                        group_sensor1.setText(null);
                    }
                    if (socket.getS2Available()) {
                        group_sensor2.setText("" + (float) socket.getS2Value() / 10 + " %RH");
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
        group_sunrise.setText(getTimeText(mHome.getSunrise()));
        group_sunset.setText(getTimeText(mHome.getSunset()));
        refreshSensor();
    }

    private void gotoDeviceActivity(String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra("device_tag", deviceTag);
        startActivity(intent);
    }

    private void showItemActionDialog(@NonNull final HomeApi.HomeDevicesResponse.Device device) {
        final Device dev = DeviceManager.getInstance().getDeviceByDevid(device.id);
        if (dev == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BottomDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
        Button btn_delete = view.findViewById(R.id.dialog_action_act2);
        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
        btn_delete.setText(R.string.delete);
        final AlertDialog dialog = builder.setView(view)
                                          .setCancelable(false)
                                          .show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = Resources.getSystem().getDisplayMetrics().widthPixels;
        window.setAttributes(lp);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().deleteDeviceFromHome(mHomeId, device.id, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        mDevices.remove(device);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                dialog.dismiss();
            }
        });
    }

    private void gotoAddDeviceActivity() {
        Intent intent = new Intent(getContext(), AddDeviceActivity.class);
        intent.putExtra(AppConstants.HOME_ID, mHomeId);
        startActivity(intent);
    }
}
