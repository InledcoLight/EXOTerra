package com.liruya.exoterra.adddevice;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.helper.WifiHelper;
import com.liruya.exoterra.view.AdvancedTextInputEditText;

public class ConnectNetFragment extends BaseFragment {

    private TextInputLayout connect_net_tl1;
    private AdvancedTextInputEditText connect_net_router;
    private TextInputLayout connect_net_tl2;
    private AdvancedTextInputEditText connect_net_password;
    private Button connect_net_smartconfig;
    private Button connect_net_apconfig;

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
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.getConfiguredNetworks();
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
        connect_net_tl1 = view.findViewById(R.id.connect_net_tl1);
        connect_net_router = view.findViewById(R.id.connect_net_router);
        connect_net_tl2 = view.findViewById(R.id.connect_net_tl2);
        connect_net_password = view.findViewById(R.id.connect_net_password);
        connect_net_smartconfig = view.findViewById(R.id.connect_net_smartconfig);
        connect_net_apconfig = view.findViewById(R.id.connect_net_apconfig);

        connect_net_router.bindTextInputLayout(connect_net_tl1);
        connect_net_password.bindTextInputLayout(connect_net_tl2);
    }

    @Override
    protected void initData() {
        mWifiHelper = new WifiHelper(getContext());
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
    }

    @Override
    protected void initEvent() {
        connect_net_router.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                gotoSystemWifiSettings();
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
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .add(R.id.adddevice_fl, new SmartConfigFragment())
                             .addToBackStack("")
                             .commit();
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
                mConnectNetViewModel.getData().setSsid(getSsid());
                mConnectNetViewModel.getData().setBssid(mWifiHelper.getGatewayMacAddress());
                mConnectNetViewModel.getData().setPassword(getPassword());
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .add(R.id.adddevice_fl, new APConfigGuideFragment())
                             .addToBackStack("")
                             .commit();
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
        if (wifiInfo == null) {
            return;
        }
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        for (WifiConfiguration cfg : wifiManager.getConfiguredNetworks()) {
            if (cfg.networkId == wifiInfo.getNetworkId()) {
                mOpen = cfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE);
                Log.e(TAG, "onWiFiChanged: " + mOpen);
            }
        }
        connect_net_tl2.setError(null);

        String ssid = wifiInfo.getSSID();
        Log.e(TAG, "onWiFiChanged: " + wifiInfo.getMacAddress() + "  " + wifiInfo.getBSSID() + " " + (dhcpInfo==null?"dhcpinfo=null":dhcpInfo.toString()));
        if (ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        connect_net_router.setText(ssid);
        boolean connected = !TextUtils.isEmpty(wifiInfo.getBSSID());
        if (connected) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && wifiInfo.getFrequency() > 4900 && wifiInfo.getFrequency() < 5900) {
                connect_net_tl1.setError(getString(R.string.warn_device_donot_support_5g));
                connect_net_smartconfig.setEnabled(false);
                connect_net_apconfig.setEnabled(false);
            } else {
                connect_net_tl1.setError(null);
                connect_net_password.requestFocus();
                connect_net_smartconfig.setEnabled(true);
                connect_net_apconfig.setEnabled(true);
            }
        } else {
            connect_net_tl1.setError("Please connect router.");
            connect_net_smartconfig.setEnabled(false);
            connect_net_apconfig.setEnabled(false);
        }
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
}
