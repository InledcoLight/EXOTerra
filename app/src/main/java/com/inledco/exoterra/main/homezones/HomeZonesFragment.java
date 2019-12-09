package com.inledco.exoterra.main.homezones;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.bean.RoomDevice;
import com.inledco.exoterra.bean.XHome;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.main.HomeViewModel;
import com.inledco.exoterra.main.homes.HomesAdapter;
import com.inledco.exoterra.main.homes.HomesFragment;
import com.inledco.exoterra.main.zonedevices.ZoneDevicesFragment;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.ZoneApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class HomeZonesFragment extends BaseFragment {

    private Toolbar homezones_toolbar;
    private CheckedTextView homezones_homename;
    private SwipeRefreshLayout homezones_swipe;
    private RecyclerView homezones_rv;

    private HomeViewModel mHomeViewModel;
    private XHome mXHome;
    private List<RoomDevice> mDevices = new ArrayList<>();
    private List<Home2.Zone> mZones = new ArrayList<>();

    private HomeZonesAdapter mAdapter;

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
        return R.layout.fragment_homezones;
    }

    @Override
    protected void initView(View view) {
        homezones_toolbar = view.findViewById(R.id.homezones_toolbar);
        homezones_homename = view.findViewById(R.id.homezones_homename);
        homezones_swipe = view.findViewById(R.id.homezones_swipe);
        homezones_rv = view.findViewById(R.id.homezones_rv);

        homezones_toolbar.inflateMenu(R.menu.menu_groups);
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
                homezones_swipe.setRefreshing(false);
            }
        });

        mAdapter = new HomeZonesAdapter(getContext(), mZones, mDevices);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                addFragmentToStack(R.id.main_fl, ZoneDevicesFragment.newInstance(mZones.get(position).id));
            }
        });
        homezones_rv.setAdapter(mAdapter);

        DeviceManager.getInstance().refreshSubcribeDevices(null);
        if (mXHome != null && mXHome.getHome2() != null) {
            homezones_swipe.setRefreshing(true);
            homezones_homename.setText(mXHome.getHome2().name);
            mHomeViewModel.refreshHomeInfo();
        }
    }

    @Override
    protected void initEvent() {
        homezones_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_groups_add:
                        showAddHabitatDialog();
                        break;
                }
                return false;
            }
        });

        homezones_homename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!homezones_homename.isChecked()) {
                    homezones_homename.setChecked(true);
                    showHomeManageDialog();
                }
            }
        });

        homezones_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mXHome == null || mXHome.getHome2() == null) {
                    return;
                }
                if (XLinkUserManager.getInstance().isUserAuthorized()) {
                    DeviceManager.getInstance().refreshSubcribeDevices(null);
                    mHomeViewModel.refreshHomeInfo();
                } else {
                    homezones_swipe.setRefreshing(false);
                }
            }
        });
    }

    private void refreshData() {
        if (mXHome != null && mXHome.getHome2() != null) {
            homezones_homename.setText(mXHome.getHome2().name);

            mZones.clear();
            mZones.addAll(mXHome.getHome2().zones);

            mDevices.clear();
            List<Home2.Room> rooms = mXHome.getHome2().rooms;
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

            mAdapter.notifyDataSetChanged();
        }


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

    private void showAddHabitatDialog() {
        if (mXHome == null || mXHome.getHome2() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_add_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_add_name);
        til.setHint(getString(R.string.habitat_name));
        builder.setTitle(R.string.add_habitat);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);
        final AlertDialog dialog = builder.show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    til.setError(getString(R.string.input_empty));
                } else {
                    XlinkCloudManager.getInstance().createZone(mXHome.getHome2().id, name, new XlinkRequestCallback<ZoneApi.ZoneResponse>() {
                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(ZoneApi.ZoneResponse response) {
                            mHomeViewModel.refreshHomeInfo();
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    private void showRenameZoneDialog(@NonNull final Home2.Zone zone) {
        if (mXHome == null || mXHome.getHome2() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_add_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_add_name);
        et_name.setText(zone.name);
        builder.setTitle(R.string.rename_group);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);
        final AlertDialog dialog = builder.show();
        et_name.requestFocus();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    til.setError(getString(R.string.input_empty));
                } else {
                    XlinkCloudManager.getInstance().renameZone(mXHome.getHome2().id, zone.id, name, new XlinkRequestCallback<ZoneApi.ZoneResponse>() {
                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(ZoneApi.ZoneResponse response) {
                            mHomeViewModel.refreshHomeInfo();
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    private void showHomeManageDialog() {
        final List<Home2> home2s = Home2Manager.getInstance().getHome2List();
        HomesAdapter adapter = new HomesAdapter(getContext(), home2s, mXHome.getHome2().id, false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_home_management, null, false);
        RecyclerView rv = view.findViewById(R.id.dialog_home_management_rv);
        TextView manage = view.findViewById(R.id.dialog_home_management_manage);
        TextView reset = view.findViewById(R.id.dialog_home_management_reset);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        final AlertDialog dialog = builder.setView(view)
                                          .setCancelable(true)
                                          .show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                homezones_homename.setChecked(false);
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
        lp.y = homezones_toolbar.getBottom();
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
