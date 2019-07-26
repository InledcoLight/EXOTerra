package com.liruya.exoterra.adddevice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.util.DeviceUtil;
import com.liruya.exoterra.xlink.XlinkConstants;

public class APConfigGuideFragment extends BaseFragment {
    private TextView apconfig_guide_step3;
    private Button apconfig_guide_next;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

    private boolean mRegistered;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                TextUtils.equals(intent.getAction(), LocationManager.PROVIDERS_CHANGED_ACTION)) {
                WifiManager manager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                onWiFiChanged(manager.getConnectionInfo(), manager.getDhcpInfo());
            }
        }
    };

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
        unregisterWifiReceiver();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_apconfig_guide;
    }

    @Override
    protected void initView(View view) {
        apconfig_guide_step3 = view.findViewById(R.id.apconfig_guide_step3);
        apconfig_guide_next = view.findViewById(R.id.apconfig_guide_next);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String ssid = "";
        if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, mConnectNetBean.getProductId())) {
            ssid = XlinkConstants.LEDSTRIP_AP_SSID;
        } else if (TextUtils.equals(XlinkConstants.PRODUCT_ID_SOCKET, mConnectNetBean.getProductId())) {
            ssid = XlinkConstants.LEDSOCKET_AP_SSID;
        } else if (TextUtils.equals(XlinkConstants.PRODUCT_ID_MONSOON, mConnectNetBean.getProductId())) {
            ssid = XlinkConstants.LEDMONSOON_AP_SSID;
        }
        ssb.append(getString(R.string.phone_connect_to_device, ssid));
        ClickableSpan clickableText = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                gotoSystemWifiSettings();
            }
        };
        int start = ssb.length()-ssid.length()-1;
        int end = ssb.length()-1;
        ssb.setSpan(clickableText, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#0000FF"));
        ssb.setSpan(fcs, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        apconfig_guide_step3.setMovementMethod(LinkMovementMethod.getInstance());
        apconfig_guide_step3.setText(ssb);

        registerWifiReceiver();
    }

    @Override
    protected void initEvent() {
        apconfig_guide_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .add(R.id.adddevice_fl, new CompatibleModeFragment())
                             .addToBackStack("")
                             .commit();
            }
        });
    }

    private void gotoSystemWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void onWiFiChanged(WifiInfo wifiInfo, DhcpInfo dhcpInfo) {
        if (wifiInfo == null || dhcpInfo == null) {
            return;
        }
        Log.e(TAG, "onWiFiChanged: " + wifiInfo.getSSID() + " " + wifiInfo.getBSSID());
        if (dhcpInfo.gateway == DeviceUtil.ESP8266_GATEWAY) {
            String ssid = wifiInfo.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length()-1);
            }
            if (TextUtils.equals(mConnectNetBean.getProductId(), XlinkConstants.PRODUCT_ID_LEDSTRIP) &&
                ssid.matches(DeviceUtil.EXO_STRIP_REGEX)) {
                apconfig_guide_next.setEnabled(true);
                return;
            } else if (TextUtils.equals(mConnectNetBean.getProductId(), XlinkConstants.PRODUCT_ID_SOCKET) &&
                       ssid.matches(DeviceUtil.EXO_SOCKET_REGEX)) {
                apconfig_guide_next.setEnabled(true);
                return;
            } else if (TextUtils.equals(mConnectNetBean.getProductId(), XlinkConstants.PRODUCT_ID_MONSOON) &&
                       ssid.matches(DeviceUtil.EXO_MONSOON_REGEX)) {
                apconfig_guide_next.setEnabled(true);
                return;
            }
        }
        apconfig_guide_next.setEnabled(false);
    }

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        getActivity().registerReceiver(mReceiver, filter);
        mRegistered = true;
    }

    private void unregisterWifiReceiver() {
        if (mRegistered) {
            getActivity().unregisterReceiver(mReceiver);
            mRegistered = false;
        }
    }
}
