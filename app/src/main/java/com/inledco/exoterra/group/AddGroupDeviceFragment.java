package com.inledco.exoterra.group;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.xlink.restful.api.app.HomeApi;

public class AddGroupDeviceFragment extends BaseFragment {
    private Toolbar add_home_device_toolbar;
    private RecyclerView add_home_device_rv;

    private String mHomeId;
    private List<Device> mDevices = new ArrayList<>();
    private AddGroupDeviceAdapter mAdapter;

    private boolean mProcessing;
    private AsyncTask<Void, Void, Integer> mAddDeviceTask;

//    private final XlinkRequestCallback<String> mCallback = new XlinkRequestCallback<String>() {
//        @Override
//        public void onError(final String error) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                         .show();
//                }
//            });
//        }
//
//        @Override
//        public void onSuccess(String s) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getContext(), "Add device to habitat success.", Toast.LENGTH_SHORT)
//                         .show();
//                    HomeManager.getInstance().refreshHomeDevices(mHomeId);
//                }
//            });
//            getActivity().onBackPressed();
//        }
//    };

    public static AddGroupDeviceFragment newInstance(@NonNull final String homeid) {
        Bundle args = new Bundle();
        args.putString("homeid", homeid);
        AddGroupDeviceFragment fragment = new AddGroupDeviceFragment();
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
        if (mAddDeviceTask != null) {
            mAddDeviceTask.cancel(true);
            mAddDeviceTask = null;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_add_home_device;
    }

    @Override
    protected void initView(View view) {
        add_home_device_toolbar = view.findViewById(R.id.add_home_device_toolbar);
        add_home_device_rv = view.findViewById(R.id.add_home_devie_rv);

        add_home_device_toolbar.inflateMenu(R.menu.menu_save);
        add_home_device_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        Set<String> devtags = new HashSet<>();
        for (Home home : HomeManager.getInstance().getHomeList()) {
            for (HomeApi.HomeDevicesResponse.Device device : home.getDevices()) {
                devtags.add(device.productId + "_" + device.mac);
            }
        }
        for (Device device : DeviceManager.getInstance().getAllDevices()) {
            if (devtags.contains(device.getDeviceTag())) {
                continue;
            }
            mDevices.add(device);
        }

        mAdapter = new AddGroupDeviceAdapter(getContext(), mDevices);
//        mAdapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                Device device = mDevices.get(position);
//                if (TextUtils.isEmpty(mHomeId)) {
//                    return;
//                }
//                XlinkCloudManager.getInstance()
//                                 .addDeviceToHome(mHomeId, device.getXDevice().getDeviceId(), mCallback);
//            }
//        });
        add_home_device_rv.setAdapter(mAdapter);

        Bundle args = getArguments();
        if (args != null) {
            mHomeId = args.getString("homeid");
        }
    }

    @Override
    protected void initEvent() {
        add_home_device_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mProcessing) {
                    getActivity().onBackPressed();
                }
            }
        });

        add_home_device_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_save:
                        addDevicesToHabitat();
                        break;
                }
                return true;
            }
        });
    }

    private void addDevicesToHabitat() {
        final Set<Integer> devids = mAdapter.getAddDeviceIds();
        mAddDeviceTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                int cnt = 0;
                for (Integer id : devids) {
                    XlinkResult<String> res = XlinkCloudManager.getInstance().addDeviceToHome(mHomeId, id);
                    if (res.isSuccess()) {
                        cnt++;
                    }
                }
                return cnt;
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                mProcessing = false;
                HomeManager.getInstance().refreshHomeDevices(mHomeId);
                if (result < devids.size()) {
                    Toast.makeText(getContext(), "Success: " + result + ", Failed: " + (devids.size()-result), Toast.LENGTH_SHORT)
                         .show();
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        };
        mProcessing = true;
        mAddDeviceTask.execute();
    }
}
