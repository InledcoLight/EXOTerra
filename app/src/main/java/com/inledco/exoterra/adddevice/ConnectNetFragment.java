package com.inledco.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.helper.WifiHelper;
import com.inledco.exoterra.util.RouterUtil;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.inledco.exoterra.view.PasswordEditText;

public class ConnectNetFragment extends BaseFragment {

    private ImageView connect_net_prdt;
    private TextInputLayout connect_net_tl1;
    private AdvancedTextInputEditText connect_net_router;
    private TextInputLayout connect_net_tl2;
    private PasswordEditText connect_net_password;
    private Button connect_net_smartconfig;
    private Button connect_net_apconfig;
    private Button connect_net_back;

    private WifiHelper mWifiHelper;
    private ConnectNetViewModel mConnectNetViewModel;

    private boolean mRegistered;
    private boolean mOpen;                          //是否开放式热点

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (!checkLocation()) {
                connect_net_router.setText("<unknown ssid>");
                connect_net_tl1.setError(getString(R.string.msg_turnon_gps));
                connect_net_smartconfig.setEnabled(false);
                connect_net_apconfig.setEnabled(false);
                return;
            } else {
                connect_net_tl1.setError(null);
            }
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                switch (action) {
                    case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                        onWiFiChanged(wifiInfo, dhcpInfo);
                        break;
                    case LocationManager.PROVIDERS_CHANGED_ACTION:
                        onWiFiChanged(wifiInfo, dhcpInfo);
                        onLocationChanged();
                        break;
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        registerWiFiStateReceiver();
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterWiFiStateReceiver();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_connect_net;
    }

    @Override
    protected void initView(View view) {
        connect_net_prdt = view.findViewById(R.id.connect_net_prdt);
        connect_net_tl1 = view.findViewById(R.id.connect_net_tl1);
        connect_net_router = view.findViewById(R.id.connect_net_router);
        connect_net_tl2 = view.findViewById(R.id.connect_net_tl2);
        connect_net_password = view.findViewById(R.id.connect_net_password);
        connect_net_smartconfig = view.findViewById(R.id.connect_net_smartconfig);
        connect_net_apconfig = view.findViewById(R.id.connect_net_apconfig);
        connect_net_back = view.findViewById(R.id.connect_net_back);

        connect_net_router.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_router_white_24dp, 0, R.drawable.ic_search_white_24dp, 0);
        connect_net_password.setIcon(R.drawable.ic_lock_white_24dp, R.drawable.design_ic_visibility, R.drawable.design_ic_visibility_off);
    }

    @Override
    protected void initData() {
        mWifiHelper = new WifiHelper(getContext());
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        ConnectNetBean bean = mConnectNetViewModel.getData();
        if (bean != null) {
            ExoProduct product = ExoProduct.getExoProduct(bean.getProductKey());
            if (product != null) {
                connect_net_prdt.setImageResource(product.getIcon());
            }
        }
    }

    @Override
    protected void initEvent() {
        connect_net_router.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                if (!checkLocation()) {
                    gotoLoactionSettings();
                } else {
                    gotoSystemWifiSettings();
                }
            }
        });

        connect_net_smartconfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String error = checkPassword();
                if (error != null) {
                    connect_net_tl2.setError(error);
                    return;
                }

                mConnectNetViewModel.getData().setSsid(getSsid());
                mConnectNetViewModel.getData().setBssid(mWifiHelper.getGatewayMacAddress());
                mConnectNetViewModel.getData().setPassword(getPassword());
                mConnectNetViewModel.getData().setCompatibleMode(false);
                addFragmentToStack(R.id.adddevice_fl, new ConfigGuideFragment());
            }
        });
        connect_net_apconfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String error = checkPassword();
                if (error != null) {
                    connect_net_tl2.setError(error);
                    return;
                }
                WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        mConnectNetViewModel.getData().setNetworkId(wifiInfo.getNetworkId());
                    }
                }
                mConnectNetViewModel.getData().setSsid(getSsid());
                mConnectNetViewModel.getData().setBssid(mWifiHelper.getGatewayMacAddress());
                mConnectNetViewModel.getData().setPassword(getPassword());
                mConnectNetViewModel.getData().setCompatibleMode(true);
                addFragmentToStack(R.id.adddevice_fl, new ConfigGuideFragment());
            }
        });

        connect_net_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private String getSsid() {
        return connect_net_router.getText().toString();
    }

    private String getPassword() {
        return connect_net_password.getText().toString();
    }

    private String checkPassword() {
        String psw = getPassword();
        if (mOpen) {
            return null;
        } else {
            if (TextUtils.isEmpty(psw) || psw.length() < 8) {
                return getString(R.string.error_wifi_password);
            }
        }
        return null;
    }

    private void registerWiFiStateReceiver() {
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        getActivity().registerReceiver(mReceiver, filter);
        mRegistered = true;
    }

    private void unregisterWiFiStateReceiver() {
        if (mRegistered) {
            getActivity().unregisterReceiver(mReceiver);
            mRegistered = false;
        }
    }

    private void onWiFiChanged(WifiInfo wifiInfo, DhcpInfo dhcpInfo) {
        if (wifiInfo == null || wifiInfo.getNetworkId() == -1) {
            connect_net_router.setText("<unknown ssid>");
            connect_net_tl1.setError(getString(R.string.please_connect_router));
            connect_net_smartconfig.setEnabled(false);
            connect_net_apconfig.setEnabled(false);
            return;
        }
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        for (WifiConfiguration cfg : wifiManager.getConfiguredNetworks()) {
            if (cfg.networkId == wifiInfo.getNetworkId()) {
                mOpen = cfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE);
            }
        }
        connect_net_tl2.setError(null);

        String ssid = wifiInfo.getSSID();
        if (ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        connect_net_router.setText(ssid);
        connect_net_password.setText(RouterUtil.getRouterPassword(getContext(), ssid));
        boolean connected = !TextUtils.isEmpty(wifiInfo.getBSSID());
        if (connected) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && wifiInfo.getFrequency() > 4900 && wifiInfo.getFrequency() < 5900) {
                connect_net_tl1.setError(getString(R.string.warn_device_donot_support_5g));
            } else {
                connect_net_tl1.setError(null);
                connect_net_password.requestFocus();
            }
        } else {
            connect_net_tl1.setError(getString(R.string.please_connect_router));
        }
        connect_net_smartconfig.setEnabled(connected);
        connect_net_apconfig.setEnabled(connected);

        boolean conflict = false;
        if (dhcpInfo != null) {
            int gw1 = dhcpInfo.gateway&0xFF;
            int gw2 = (dhcpInfo.gateway&0xFF00)>>8;
            int gw3 = (dhcpInfo.gateway&0xFF0000)>>16;
            if (gw1 == 192 && gw2 == 168 && gw3 == 4) {
                conflict = true;
            }
        }
        mConnectNetViewModel.getData().setConflict(conflict);
    }

    private void onLocationChanged() {

    }

    private void gotoSystemWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void gotoLoactionSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private boolean checkLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (manager.isLocationEnabled()) {
                return true;
            }
            return false;
        }
        return true;
    }
}
