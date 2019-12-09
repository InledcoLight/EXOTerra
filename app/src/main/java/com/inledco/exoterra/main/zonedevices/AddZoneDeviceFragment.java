package com.inledco.exoterra.main.zonedevices;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
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

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.bean.RoomDevice;
import com.inledco.exoterra.bean.XHome;
import com.inledco.exoterra.main.HomeViewModel;
import com.inledco.exoterra.xlink.XlinkCloudManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.xlink.restful.api.app.HomeApi;

public class AddZoneDeviceFragment extends BaseFragment {

    private Toolbar add_zone_device_toolbar;
    private SwipeRefreshLayout add_zone_device_swipe;
    private RecyclerView add_zone_device_rv;

    private HomeViewModel mHomeViewModel;
    private XHome mXHome;

    private String mZoneId;
    private final List<RoomDevice> mDevices = new ArrayList<>();
    private Home2.Zone mZone;
    private AddZoneDeviceAdapter mAdapter;

    private boolean mProcessing;
    private AsyncTask<Void, Void, Boolean> mChangeDevicesTask;

    public static AddZoneDeviceFragment newInstance(@NonNull final String zoneid) {
        Bundle args = new Bundle();
        args.putString(AppConstants.ZONE_ID, zoneid);
        AddZoneDeviceFragment fragment = new AddZoneDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mChangeDevicesTask != null) {
            mChangeDevicesTask.cancel(false);
            mChangeDevicesTask = null;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_add_zone_device;
    }

    @Override
    protected void initView(View view) {
        add_zone_device_toolbar = view.findViewById(R.id.add_zone_device_toolbar);
        add_zone_device_swipe = view.findViewById(R.id.add_zone_device_swipe);
        add_zone_device_rv = view.findViewById(R.id.add_zone_device_rv);

        add_zone_device_toolbar.inflateMenu(R.menu.menu_add_zone_device);
        add_zone_device_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mHomeViewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        mXHome = mHomeViewModel.getData();
        mHomeViewModel.observe(this, new Observer<XHome>() {
            @Override
            public void onChanged(@Nullable XHome xHome) {
                mXHome = xHome;
                refreshData();
                add_zone_device_swipe.setRefreshing(false);
            }
        });
        if (mXHome == null || mXHome.getHome2() == null) {
            return;
        }
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        mZoneId = args.getString(AppConstants.ZONE_ID);

        for (Home2.Zone zone : mXHome.getHome2().zones) {
            if (TextUtils.equals(mZoneId, zone.id)) {
                mZone = zone;
                add_zone_device_toolbar.setTitle(zone.name);
                mAdapter = new AddZoneDeviceAdapter(getContext(), mDevices, mZone);
                add_zone_device_rv.setAdapter(mAdapter);
                break;
            }
        }
//        refreshData();

        add_zone_device_swipe.setRefreshing(true);
        mHomeViewModel.refreshHomeInfo();
    }

    @Override
    protected void initEvent() {
        add_zone_device_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mProcessing) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        add_zone_device_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_add_zone_device_save:
                        if (!mProcessing) {
                            changeDevices();
                        }
                        break;
                }
                return false;
            }
        });

        add_zone_device_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mProcessing) {
                    mHomeViewModel.refreshHomeInfo();
                } else {
                    add_zone_device_swipe.setRefreshing(false);
                }
            }
        });
    }

    private void refreshData() {
        mDevices.clear();
        if (mXHome != null && mXHome.getHome2() != null) {
            for (Home2.Zone zone : mXHome.getHome2().zones) {
                if (TextUtils.equals(mZoneId, zone.id)) {
                    add_zone_device_toolbar.setTitle(zone.name);
                    break;
                }
            }

            List<Home2.Room> rooms = mXHome.getHome2().rooms;
            if (rooms != null) {
                for (int i = 0; i < rooms.size(); i++) {
                    List<String> devids = rooms.get(i).device_ids;
                    if (devids != null && devids.size() > 0) {
                        String roomid = rooms.get(i).id;
                        int devid = Integer.parseInt(devids.get(0));
                        HomeApi.HomeDevicesResponse.Device device = getDeviceById(devid);
                        if (device != null) {
                            mDevices.add(new RoomDevice(roomid, device));
                        }
                    }
                }
            }
        }
        mAdapter.refreshData();
    }

    private HomeApi.HomeDevicesResponse.Device getDeviceById(final int devid) {
        for (int i = 0; i < mXHome.getDevices().size(); i++) {
            HomeApi.HomeDevicesResponse.Device device = mXHome.getDevices().get(i);
            if (devid == device.id) {
                return device;
            }
        }
        return null;
    }

    private void changeDevices() {
        if (mAdapter == null || mXHome == null || mXHome.getHome2() == null) {
            return;
        }
        final String homeid = mXHome.getHome2().id;
        final Set<String> addRoomIds = mAdapter.getAddRoomIds();
        final Set<String> removeRoomIds = mAdapter.getRemoveRoomIds();
        mChangeDevicesTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean result = true;
                for (String roomid : removeRoomIds) {
                    boolean res = XlinkCloudManager.getInstance().removeZoneRoom(homeid, mZoneId, roomid);
                    if (!res) {
                        result = false;
                    }
                }
                for (String roomid : addRoomIds) {
                    boolean res = XlinkCloudManager.getInstance().addZoneRoom(homeid, mZoneId, roomid);
                    if (!res) {
                        result = false;
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                mProcessing = false;
                mHomeViewModel.refreshHomeInfo();
                if (result) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    mHomeViewModel.refreshHomeInfo();
                }
            }
        };
        mProcessing = true;
        mChangeDevicesTask.execute();
    }
}
