package com.liruya.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.helper.WifiHelper;

public class ConnectNetFragment extends BaseFragment {

    private TextInputLayout connect_net_tl1;
    private TextInputEditText connect_net_router;
    private TextInputLayout connect_net_tl2;
    private TextInputEditText connect_net_password;
    private ImageButton connect_net_change;
    private Button connect_net_next;

    private WifiHelper mWifiHelper;
    private String mSsid;
    private String mAddress;
    private ConnectNetViewModel mConnectNetViewModel;

    private boolean mRegistered;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                switch (action) {
                    case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                        onWiFiChanged(wifiManager.getConnectionInfo());
                        break;
                    case LocationManager.PROVIDERS_CHANGED_ACTION:
                        onWiFiChanged(wifiManager.getConnectionInfo());
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
        connect_net_change = view.findViewById(R.id.connect_net_change);
        connect_net_next = view.findViewById(R.id.connect_net_next);
    }

    @Override
    protected void initData() {
        mWifiHelper = new WifiHelper(getContext());
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
    }

    @Override
    protected void initEvent() {
        connect_net_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSystemWifiSettings();
            }
        });

        connect_net_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectNetViewModel.getData().setSsid(mSsid);
                mConnectNetViewModel.getData().setGateway(mWifiHelper.getGatewayMacAddress());
                mConnectNetViewModel.getData().setPassword(getPassword());
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .add(R.id.adddevice_fl, new ConfigNetFragment())
                             .addToBackStack("")
                             .commit();
            }
        });
    }

    private String getPassword() {
        return connect_net_password.getText().toString();
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

    private void onWiFiChanged(WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return;
        }
        String ssid = wifiInfo.getSSID();
        Log.e(TAG, "onWiFiChanged: " + ssid + "  " + wifiInfo.getBSSID());
        if (ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        connect_net_router.setText(ssid);
        boolean connected = !TextUtils.isEmpty(wifiInfo.getBSSID());
        if (connected) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && wifiInfo.getFrequency() > 4900 && wifiInfo.getFrequency() < 5900) {
                connect_net_tl1.setError(getString(R.string.warn_device_donot_support_5g));
                connect_net_next.setEnabled(false);
            } else {
                mSsid = ssid;
                connect_net_tl1.setError(null);
                connect_net_password.requestFocus();
                connect_net_next.setEnabled(true);
            }
        } else {
            connect_net_tl1.setError("Please connect router.");
            connect_net_next.setEnabled(false);
        }
    }

    private void onLocationChanged() {

    }

    private void gotoSystemWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
