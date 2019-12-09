package com.inledco.exoterra.main.homedevices;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.adddevice.AddDeviceActivity;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.bean.RoomDevice;
import com.inledco.exoterra.bean.XHome;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.event.DevicePropertyChangedEvent;
import com.inledco.exoterra.event.DeviceStateChangedEvent;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.event.SubscribeChangedEvent;
import com.inledco.exoterra.main.HomeViewModel;
import com.inledco.exoterra.main.homes.HomesAdapter;
import com.inledco.exoterra.main.homes.HomesFragment;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.scan.ScanActivity;
import com.inledco.exoterra.smartconfig.SmartconfigActivity;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class HomeDevicesFragment extends BaseFragment {
    private Toolbar homedevices_toolbar;
    private SwipeRefreshLayout homedevices_swipe;
    private RecyclerView homedevices_rv;
    private CheckedTextView homedevices_homename;

    private HomeViewModel mHomeViewModel;
    private XHome mXHome;

    private XlinkTaskCallback<List<Device>> mRefreshSubDevicesCallback = new XlinkTaskCallback<List<Device>>() {
        @Override
        public void onError(String error) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                 .show();
            homedevices_swipe.setRefreshing(false);
        }

        @Override
        public void onComplete(List<Device> devices) {
            mHomeViewModel.refreshHomeInfo();
        }
    };

    private final List<RoomDevice> mDevices = new ArrayList<>();
    private HomeDevicesAdapter mAdapter;

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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeDeviceChangedEvent(HomeDeviceChangedEvent event) {
        Log.e(TAG, "onHomeDeviceChangedEvent: ");
        mHomeViewModel.refreshHomeInfo();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onSubscribeChangedEvent(SubscribeChangedEvent event) {
        DeviceManager.getInstance().refreshSubcribeDevices(mRefreshSubDevicesCallback);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDevicePropertyChangedEvent(DevicePropertyChangedEvent event) {
        mHomeViewModel.refreshHomeInfo();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(DeviceStateChangedEvent event) {
        mHomeViewModel.refreshHomeInfo();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_homedevices;
    }

    @Override
    protected void initView(View view) {
        homedevices_toolbar = view.findViewById(R.id.homedevices_toolbar);
        homedevices_swipe = view.findViewById(R.id.homedevices_swipe);
        homedevices_rv = view.findViewById(R.id.homedevices_rv);
        homedevices_homename = view.findViewById(R.id.homedevices_homename);

        homedevices_toolbar.inflateMenu(R.menu.menu_devices);
        homedevices_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
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
                homedevices_swipe.setRefreshing(false);
            }
        });

        mAdapter = new HomeDevicesAdapter(getContext(), mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                HomeApi.HomeDevicesResponse.Device device = mDevices.get(position).getDevice();
                String roomId = mDevices.get(position).getRoomId();
                String deviceTag = device.productId + "_" + device.mac;
                gotoDeviceActivity(roomId, deviceTag);
            }
        });
        homedevices_rv.setAdapter(mAdapter);

        if (mXHome != null && mXHome.getHome2() != null) {
            homedevices_swipe.setRefreshing(true);
            DeviceManager.getInstance().refreshSubcribeDevices(mRefreshSubDevicesCallback);
            homedevices_homename.setText(mXHome.getHome2().name);
        }
    }

    @Override
    protected void initEvent() {
        homedevices_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
                        if (mXHome != null) {
                            startAdddeviceActivity();
                        }
                        break;
                }
                return true;
            }
        });

        homedevices_homename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mXHome == null || mXHome.getHome2() == null) {
                    return;
                }
                if (!homedevices_homename.isChecked()) {
                    homedevices_homename.setChecked(true);
                    showHomeManageDialog();
                }
            }
        });

        homedevices_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mXHome == null || mXHome.getHome2() == null) {
                    return;
                }
                if (XLinkUserManager.getInstance().isUserAuthorized()) {
                    DeviceManager.getInstance().refreshSubcribeDevices(mRefreshSubDevicesCallback);
                } else {
                    homedevices_swipe.setRefreshing(false);
                }
            }
        });
    }

    private void refreshData() {
        mDevices.clear();
        if (mXHome != null && mXHome.getHome2() != null) {
            homedevices_homename.setText(mXHome.getHome2().name);

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
        mAdapter.notifyDataSetChanged();
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

    private void gotoDeviceActivity(final String roomid, final String deviceTag) {
        Intent intent = new Intent(getContext(), DeviceActivity.class);
        intent.putExtra(AppConstants.ROOM_ID, roomid);
        intent.putExtra(AppConstants.DEVICE_TAG, deviceTag);
        startActivity(intent);
    }

    private void showHomeManageDialog() {
//        final String current_homeid = mXHome.getHome2().id;
        final List<Home2> home2s = Home2Manager.getInstance().getHome2List();
        HomesAdapter adapter = new HomesAdapter(getContext(), home2s, mXHome.getHome2().id, false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_home_management, null, false);
        RecyclerView rv = view.findViewById(R.id.dialog_home_management_rv);
        TextView manage = view.findViewById(R.id.dialog_home_management_manage);
        final TextView reset = view.findViewById(R.id.dialog_home_management_reset);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        final AlertDialog dialog = builder.setView(view)
                                          .setCancelable(true)
                                          .show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                homedevices_homename.setChecked(false);
            }
        });
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                final String homeid = home2s.get(position).id;
                if (!TextUtils.equals(mXHome.getHome2().id, homeid)) {
                    XlinkCloudManager.getInstance()
                                     .setCurrentHomeId(homeid, new XlinkRequestCallback<String>() {
                                         @Override
                                         public void onError(String error) {
                                             Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                                  .show();
                                         }

                                         @Override
                                         public void onSuccess(String s) {
                                             Home2Manager.getInstance().setCurrentHomeId(homeid);
                                             mHomeViewModel.refreshHomeInfo();
                                         }
                                     });
                }
                dialog.dismiss();
            }
        });
        rv.setAdapter(adapter);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                addFragmentToStack(R.id.main_fl, new HomesFragment());
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showConfirmDialog();
            }
        });
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Window window = dialog.getWindow();
        window.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = width;
        lp.y = homedevices_toolbar.getBottom();
        window.setAttributes(lp);
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Reset")
               .setMessage("This operation will delete all homes and devices except current home!")
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       new Thread(new Runnable() {
                           @Override
                           public void run() {
                               reset();
                           }
                       }).start();
                   }
               })
               .setCancelable(false)
               .show();
    }

    private void reset() {
        Set<Integer> devids = new HashSet<>();
        for (HomeApi.HomeDevicesResponse.Device dev : mXHome.getDevices()) {
            devids.add(dev.id);
        }
        Set<Integer> rmvDevids = new HashSet<>();
        for (Device dev : DeviceManager.getInstance().getAllDevices()) {
            int devid = dev.getXDevice().getDeviceId();
            if (!devids.contains(devid)) {
                rmvDevids.add(devid);
            }
        }
        for (Integer devid : rmvDevids) {
            XlinkCloudManager.getInstance().unsubscribeDevice(devid);
        }

        Set<String> homeids = new HashSet<>();
        for (Home2 home2 : Home2Manager.getInstance().getHome2List()) {
            if (!TextUtils.equals(home2.id, Home2Manager.getInstance().getCurrentHomeId())) {
                homeids.add(home2.id);
            }
        }
        for (String homeid : homeids) {
            XlinkCloudManager.getInstance()
                             .deleteHome(homeid);
        }
    }
}
