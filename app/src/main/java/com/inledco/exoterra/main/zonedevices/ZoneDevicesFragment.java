package com.inledco.exoterra.main.zonedevices;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.bean.RoomDevice;
import com.inledco.exoterra.bean.XHome;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.main.HomeViewModel;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.ZoneApi;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class ZoneDevicesFragment extends BaseFragment {
    private Toolbar zonedevices_toolbar;
    private SwipeRefreshLayout zonedevices_swipe;
    private RecyclerView zonedevices_rv;

    private HomeViewModel mHomeViewModel;
    private XHome mXHome;
    private String mZoneId;

    private final List<RoomDevice> mDevices = new ArrayList<>();
    private ZoneDevicesAdapter mAdapter;

    public static ZoneDevicesFragment newInstance(@NonNull final String zoneid) {
        Bundle args = new Bundle();
        args.putString(AppConstants.ZONE_ID, zoneid);
        ZoneDevicesFragment fragment = new ZoneDevicesFragment();
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
    protected int getLayoutRes() {
        return R.layout.fragment_zonedevices;
    }

    @Override
    protected void initView(View view) {
        zonedevices_toolbar = view.findViewById(R.id.zonedevices_toolbar);
        zonedevices_swipe = view.findViewById(R.id.zonedevices_swipe);
        zonedevices_rv = view.findViewById(R.id.zonedevices_rv);

        zonedevices_toolbar.inflateMenu(R.menu.menu_habitat);
        zonedevices_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mHomeViewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        mXHome = mHomeViewModel.getData();
        mHomeViewModel.observe(this, new Observer<XHome>() {
            @Override
            public void onChanged(@Nullable XHome xhome) {
                mXHome = xhome;
                refreshData();
                zonedevices_swipe.setRefreshing(false);
            }
        });
        Bundle args = getArguments();
        if (args != null) {
            mZoneId = args.getString(AppConstants.ZONE_ID);
        }

        mAdapter = new ZoneDevicesAdapter(getContext(), mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                HomeApi.HomeDevicesResponse.Device device = mDevices.get(position).getDevice();
                String roomid = mDevices.get(position).getRoomId();
                String dev_tag = device.productId + "_" + device.mac;
                gotoDeviceActivity(roomid, dev_tag);
            }
        });
        zonedevices_rv.setAdapter(mAdapter);

        refreshData();
    }

    @Override
    protected void initEvent() {
        zonedevices_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        zonedevices_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_habitat_detail:
                        addFragmentToStack(R.id.main_fl, ZoneDetailFragment.newInstance(mZoneId));
                        break;
                }
                return false;
            }
        });

        zonedevices_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHomeViewModel.refreshHomeInfo();
            }
        });
    }

    private void refreshData() {
        mDevices.clear();
        if (mXHome != null && mXHome.getHome() != null && mXHome.getDevices() != null) {
            for (int i = 0; i < mXHome.getHome().zones.size(); i++) {
                Home.Zone zone = mXHome.getHome().zones.get(i);
                if (TextUtils.equals(zone.id, mZoneId)) {
                    zonedevices_toolbar.setTitle(zone.name);
                    for (int j = 0; j < zone.room_ids.size(); j++) {
                        String roomid = zone.room_ids.get(j);
                        int devid = getDeviceIdByRoomId(roomid);
                        if (devid != 0) {
                            HomeApi.HomeDevicesResponse.Device device = getDeviceById(devid);
                            if (device != null) {
                                mDevices.add(new RoomDevice(roomid, device));
                            }
                        }
                    }
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    private void showRenameDialog() {
        if (mXHome == null || mXHome.getHome() == null) {
            return;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.habitat_name));
        et_name.setText(zonedevices_toolbar.getTitle());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.rename_habitat)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                XlinkCloudManager.getInstance().renameZone(mXHome.getHome().id, mZoneId, name, new XlinkRequestCallback<ZoneApi.ZoneResponse>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(ZoneApi.ZoneResponse response) {
                        mHomeViewModel.refreshHomeInfo();
                        dialog.dismiss();
                    }
                });
            }
        });
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

    private int getDeviceIdByRoomId(final String roomid) {
        for (int i = 0; i < mXHome.getHome().rooms.size(); i++) {
            Home.Room room = mXHome.getHome().rooms.get(i);
            if (TextUtils.equals(roomid, room.id)) {
                if (room.device_ids != null && room.device_ids.size() > 0) {
                    return Integer.parseInt(room.device_ids.get(0));
                }
            }
        }
        return 0;
    }

    private void gotoDeviceActivity(final String roomid, final String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra(AppConstants.ROOM_ID, roomid);
        intent.putExtra(AppConstants.DEVICE_TAG, deviceTag);
        startActivity(intent);
    }
}
