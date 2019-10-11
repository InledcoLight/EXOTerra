package com.inledco.exoterra.group;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.util.List;

public class AddGroupDeviceFragment extends BaseFragment {
    private Toolbar add_home_device_toolbar;
    private RecyclerView add_home_device_rv;

    private String mHomeId;
    private List<Device> mDevices = DeviceManager.getInstance()
                                                 .getAllDevices();
    private AddGroupDeviceAdapter mAdapter;

    private final XlinkRequestCallback<String> mCallback = new XlinkRequestCallback<String>() {
        @Override
        public void onStart() {

        }

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
        public void onSuccess(String s) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Add device to home success.", Toast.LENGTH_SHORT)
                         .show();
                }
            });
            getActivity().onBackPressed();
        }
    };

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
    protected int getLayoutRes() {
        return R.layout.fragment_add_home_device;
    }

    @Override
    protected void initView(View view) {
        add_home_device_toolbar = view.findViewById(R.id.add_home_device_toolbar);
        add_home_device_rv = view.findViewById(R.id.add_home_devie_rv);

        add_home_device_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mAdapter = new AddGroupDeviceAdapter(getContext(), mDevices) {
            @Override
            protected void onItemClick(int position) {
                Device device = mDevices.get(position);
                if (TextUtils.isEmpty(mHomeId) == false) {
                    XlinkCloudManager.getInstance()
                                     .addDeviceToHome(mHomeId, device.getXDevice().getDeviceId(), mCallback);
                }
            }
        };
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
                getActivity().onBackPressed();
            }
        });
    }
}
