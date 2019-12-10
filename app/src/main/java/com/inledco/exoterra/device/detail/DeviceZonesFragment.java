package com.inledco.exoterra.device.detail;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.device.DeviceBaseViewModel;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.ZoneApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceZonesFragment extends BaseFragment {

    private Toolbar device_zones_toolbar;
    private SwipeRefreshLayout device_zones_swipe;
    private RecyclerView device_zones_rv;
    private TextView device_zones_add;

    private DeviceBaseViewModel mDeviceBaseViewModel;
    private List<Home2.Zone> mZones = new ArrayList<>();
    private DeviceZonesAdapter mAdapter;
    private final XlinkRequestCallback<Home2> mGetHomeCallback = new XlinkRequestCallback<Home2>() {
        @Override
        public void onError(String error) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                 .show();
            device_zones_swipe.setRefreshing(false);
        }

        @Override
        public void onSuccess(Home2 home2) {
            mZones.clear();
            mZones.addAll(home2.zones);
            mAdapter.refreshData();
            device_zones_swipe.setRefreshing(false);
        }
    };

    private boolean mProcessing;
    private AsyncTask<Void, Void, Boolean> mChangeZonesTask;

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
        if (mChangeZonesTask != null) {
            mChangeZonesTask.cancel(false);
            mChangeZonesTask = null;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_device_zones;
    }

    @Override
    protected void initView(View view) {
        device_zones_toolbar = view.findViewById(R.id.device_zones_toolbar);
        device_zones_swipe = view.findViewById(R.id.device_zones_swipe);
        device_zones_rv = view.findViewById(R.id.device_zones_rv);
        device_zones_add = view.findViewById(R.id.device_zones_add);

        device_zones_toolbar.inflateMenu(R.menu.menu_device_zones);
        device_zones_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        device_zones_add.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_add_circle_outline_white_32dp, 0, 0);
    }

    @Override
    protected void initData() {
        mDeviceBaseViewModel = ViewModelProviders.of(getActivity()).get(DeviceBaseViewModel.class);

        mAdapter = new DeviceZonesAdapter(getContext(), mZones, null);
        device_zones_rv.setAdapter(mAdapter);

        device_zones_swipe.setRefreshing(true);
        XlinkCloudManager.getInstance().getHomeInfo(Home2Manager.getInstance().getCurrentHomeId(), mGetHomeCallback);
    }

    @Override
    protected void initEvent() {
        device_zones_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProcessing) {
                    return;
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        device_zones_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_device_zones_save:
                        if (!mProcessing) {
                            Set<String> removeZones = mAdapter.getRemoveZoneIds();
                            Set<String> addZones = mAdapter.getAddZoneIds();
                            changeZones(removeZones, addZones);
                        }
                        break;
                }
                return false;
            }
        });

        device_zones_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                XlinkCloudManager.getInstance().getHomeInfo(Home2Manager.getInstance().getCurrentHomeId(), mGetHomeCallback);
            }
        });

        device_zones_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddHabitatDialog();
            }
        });
    }

    private void changeZones(@NonNull final Set<String> removeZoneIds, @NonNull final Set<String> addZoneIds) {
        final String homeid = Home2Manager.getInstance().getCurrentHomeId();
        final String roomid = null;
        mChangeZonesTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean result = true;
                for (String zoneid : removeZoneIds) {
                    boolean res = XlinkCloudManager.getInstance().removeZoneRoom(homeid, zoneid, roomid);
                    if (!res) {
                        result = false;
                    }
                }
                for (String zoneid : addZoneIds) {
                    boolean res = XlinkCloudManager.getInstance().addZoneRoom(homeid, zoneid, roomid);
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
                if (result) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    XlinkCloudManager.getInstance().getHomeInfo(Home2Manager.getInstance().getCurrentHomeId(), mGetHomeCallback);
                }
            }
        };
        mProcessing = true;
        mChangeZonesTask.execute();
    }

    private void showAddHabitatDialog() {
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
                    XlinkCloudManager.getInstance().createZone(Home2Manager.getInstance().getCurrentHomeId(), name, new XlinkRequestCallback<ZoneApi.ZoneResponse>() {
                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(ZoneApi.ZoneResponse response) {
                            XlinkCloudManager.getInstance().getHomeInfo(Home2Manager.getInstance().getCurrentHomeId(), mGetHomeCallback);
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }
}
