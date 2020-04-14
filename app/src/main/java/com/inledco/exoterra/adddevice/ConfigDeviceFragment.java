package com.inledco.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.util.LocalDevicePrefUtil;
import com.inledco.exoterra.view.GradientCornerButton;

public class ConfigDeviceFragment extends BaseFragment {

    private ImageView config_device_prdt;
    private TextInputEditText config_device_name;
    private GradientCornerButton config_device_save;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

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
        config_device_name = view.findViewById(R.id.config_device_name);
        config_device_save = view.findViewById(R.id.config_device_save);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }
        final String pkey = mConnectNetBean.getProductKey();
        config_device_prdt.setImageResource(DeviceUtil.getProductIcon(pkey));
        config_device_name.setText(DeviceUtil.getDefaultName(pkey));
        config_device_name.requestFocus();
    }

    @Override
    protected void initEvent() {
        config_device_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pkey = mConnectNetBean.getProductKey();
                final String dname = mConnectNetBean.getDeviceName();
                String name = config_device_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    name = DeviceUtil.getDefaultName(pkey);
                }
                boolean authorized = true;
                if (authorized) {
                    String token = UserManager.getInstance().getToken();
//                    AliotServer.getInstance().modifyDeviceName(token, pkey, dname);
//                    XlinkCloudManager.getInstance().renameDevice(pid, devid, name, new XlinkRequestCallback<DeviceApi.DeviceResponse>() {
//                        @Override
//                        public void onError(String error) {
//                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                                 .show();
//                        }
//
//                        @Override
//                        public void onSuccess(DeviceApi.DeviceResponse response) {
//                            final String homeid = mConnectNetBean.getHomeid();
//                            if (TextUtils.isEmpty(homeid)) {
//                                replaceFragment(R.id.adddevice_fl, new AssignHabitatFragment());
//                            } else {
//                                XlinkCloudManager.getInstance().addDeviceToHome(homeid, devid, new XlinkRequestCallback<String>() {
//                                    @Override
//                                    public void onError(String error) {
//                                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                                             .show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess(String s) {
//                                        getActivity().finish();
//                                    }
//                                });
//                            }
//                        }
//                    });
                } else {
                    final String mac = mConnectNetBean.getAddress();
                    LocalDevicePrefUtil.putLocalDevice(getContext(), pkey, mac, name);
                    getActivity().finish();
                }
            }
        });
    }
}
