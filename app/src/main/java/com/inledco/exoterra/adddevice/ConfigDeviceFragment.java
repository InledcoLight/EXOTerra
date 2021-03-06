package com.inledco.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.device.DeviceIconAdapter;
import com.inledco.exoterra.event.DeviceChangedEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.LocalDevicePrefUtil;
import com.inledco.exoterra.view.GradientCornerButton;

import org.greenrobot.eventbus.EventBus;

public class ConfigDeviceFragment extends BaseFragment {

    private ImageView config_device_prdt;
    private TextInputLayout config_device_til;
    private TextInputEditText config_device_name;
    private RecyclerView config_device_rv;
    private GradientCornerButton config_device_save;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

    private DeviceIconAdapter mAdapter;
    private int mIconRes;

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
        return R.layout.fragment_config_device;
    }

    @Override
    protected void initView(View view) {
        config_device_prdt = view.findViewById(R.id.config_device_prdt);
        config_device_til = view.findViewById(R.id.config_device_til);
        config_device_name = view.findViewById(R.id.config_device_name);
        config_device_rv = view.findViewById(R.id.config_device_rv);
        config_device_save = view.findViewById(R.id.config_device_save);

        config_device_rv.setLayoutManager(new GridLayoutManager(getContext(), 5));
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }
        ExoProduct product = ExoProduct.getExoProduct(mConnectNetBean.getProductKey());
        if (product != null) {
            mIconRes = product.getIcon();
            config_device_prdt.setImageResource(mIconRes);

            config_device_name.setCompoundDrawablesRelativeWithIntrinsicBounds(mIconRes, 0, 0, 0);
            config_device_name.setText(product.getDefaultName());

            mAdapter = new DeviceIconAdapter(getContext(), mIconRes);
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    mIconRes = mAdapter.getSelected();
                    config_device_name.setCompoundDrawablesRelativeWithIntrinsicBounds(mIconRes, 0, 0, 0);
                }
            });
            config_device_rv.setAdapter(mAdapter);
        }
        config_device_name.requestFocus();
    }

    @Override
    protected void initEvent() {
        config_device_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pkey = mConnectNetBean.getProductKey();
                final String dname = mConnectNetBean.getDeviceName();
                final String name = config_device_name.getText().toString();
                String icon = null;
                try {
                    icon = getContext().getResources().getResourceEntryName(mIconRes);
                    Log.e(TAG, "onClick: " + icon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(name)) {
                    config_device_til.setError(getString(R.string.error_empty));
                    return;
                }
                boolean authorized = UserManager.getInstance().isAuthorized();
                if (authorized) {
                    final UserApi.ModifyDeviceInfoRequest request = new UserApi.ModifyDeviceInfoRequest();
                    request.name = name;
                    if (icon != null) {
                        request.remark2 = icon;
                    }
                    AliotServer.getInstance().modifyDeviceInfo(pkey, dname, request, new HttpCallback<UserApi.Response>() {
                        @Override
                        public void onError(String error) {
                            dismissLoadDialog();
                            showToast(error);
                        }

                        @Override
                        public void onSuccess(UserApi.Response result) {
                            final String pkey = mConnectNetBean.getProductKey();
                            final String dname = mConnectNetBean.getDeviceName();
                            Device device = DeviceManager.getInstance().getDevice(pkey + "_" + dname);
                            if (device != null) {
                                device.setName(name);
                                device.setRemark2(request.remark2);
                                EventBus.getDefault().post(new DeviceChangedEvent(pkey, dname));
                            }
                            mConnectNetBean.setName(name);
                            final String groupid = mConnectNetBean.getGroupid();
                            if (TextUtils.isEmpty(groupid)) {
                                dismissLoadDialog();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        replaceFragment(R.id.adddevice_fl, new AssignHabitatFragment());
                                    }
                                });
                            } else {
                                AliotServer.getInstance().addDeviceToGroup(groupid, pkey, dname, new HttpCallback<UserApi.Response>() {
                                    @Override
                                    public void onError(String error) {
                                        dismissLoadDialog();
                                        showToast(error);
                                    }

                                    @Override
                                    public void onSuccess(UserApi.Response result) {
                                        Group group = GroupManager.getInstance().getGroup(groupid);
                                        if (group != null) {
                                            group.addDevice(pkey, dname, name);
                                            EventBus.getDefault().post(new GroupDeviceChangedEvent(groupid));
                                        }
                                        dismissLoadDialog();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                getActivity().finish();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                    showLoadDialog();
                } else {
                    final String mac = mConnectNetBean.getAddress();
                    LocalDevicePrefUtil.putLocalDevice(getContext(), pkey, mac, name);
                    getActivity().finish();
                }
            }
        });
    }
}
