package com.inledco.exoterra.adddevice;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.xlink.XlinkConstants;

public class ConfigGuideFragment extends BaseFragment {
    private TextView config_guide_title;
    private TextView config_guide_confirm;
    private ImageView config_guide_icon;
    private TextView config_guide_step;
    private ImageView config_guide_led;
    private Button config_guide_next;

    private ConnectNetViewModel mConnectNetViewModel;
    private ConnectNetBean mConnectNetBean;

    private ValueAnimator mAnimator;

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
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_config_guide;
    }

    @Override
    protected void initView(View view) {
        config_guide_title = view.findViewById(R.id.config_guide_title);
        config_guide_confirm = view.findViewById(R.id.config_guide_confirm);
        config_guide_icon = view.findViewById(R.id.config_guide_icon);
        config_guide_step = view.findViewById(R.id.config_guide_step);
//        config_guide_led = view.findViewById(R.id.config_guide_led);
        config_guide_next = view.findViewById(R.id.config_guide_next);
    }

    @Override
    protected void initData() {
        mConnectNetViewModel = ViewModelProviders.of(getActivity()).get(ConnectNetViewModel.class);
        mConnectNetBean = mConnectNetViewModel.getData();
        if (mConnectNetBean == null) {
            return;
        }
        mConnectNetViewModel.observe(this, new Observer<ConnectNetBean>() {
            @Override
            public void onChanged(@Nullable ConnectNetBean connectNetBean) {
                refreshData();
            }
        });
//        config_guide_icon.setImageResource(DeviceUtil.getProductIcon(mConnectNetBean.getProductId()));

        refreshData();

//        int duration = 500;
//        if (mConnectNetBean.isCompatibleMode()) {
//            config_guide_title.setText(R.string.compatible_mode);
//            config_guide_confirm.setText(R.string.apconfig_confirm);
//            duration = 1500;
//            SpannableStringBuilder ssb = new SpannableStringBuilder();
//            String ssid;
//            if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, mConnectNetBean.getProductId())) {
//                ssid = XlinkConstants.LEDSTRIP_AP_SSID;
//                ssb.append(getString(R.string.apconfig_guide_strip, ssid));
//            } else if (TextUtils.equals(XlinkConstants.PRODUCT_ID_SOCKET, mConnectNetBean.getProductId())) {
//                ssid = XlinkConstants.LEDSOCKET_AP_SSID;
//                ssb.append(getString(R.string.apconfig_guide_default, ssid));
//            } else if (TextUtils.equals(XlinkConstants.PRODUCT_ID_MONSOON, mConnectNetBean.getProductId())) {
//                ssid = XlinkConstants.LEDMONSOON_AP_SSID;
//                ssb.append(getString(R.string.apconfig_guide_default, ssid));
//            } else {
//                return;
//            }
//            ClickableSpan clickableText = new ClickableSpan() {
//                @Override
//                public void onClick(@NonNull View widget) {
//                    gotoSystemWifiSettings();
//                }
//            };
//            int start = ssb.length() - ssid.length() - 1;
//            int end = ssb.length() - 1;
//            ssb.setSpan(clickableText, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#0000FF"));
//            ssb.setSpan(fcs, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            config_guide_step.setMovementMethod(LinkMovementMethod.getInstance());
//            config_guide_step.setText(ssb);
//
//            registerWifiReceiver();
//        } else {
//            config_guide_title.setText(R.string.smartconfig);
//            config_guide_confirm.setText(R.string.smartconfig_confirm);
//            if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, mConnectNetBean.getProductId())) {
//                config_guide_step.setText(R.string.smartconfig_guide_strip);
//            }
//            config_guide_next.setEnabled(true);
//        }
//
//        mAnimator = ObjectAnimator.ofInt(0-duration/2, duration/2);
//        mAnimator.setDuration(duration);
//        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
//        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int value = (int) animation.getAnimatedValue();
//                if (value > 0) {
//                    config_guide_led.setVisibility(View.VISIBLE);
//                } else {
//                    config_guide_led.setVisibility(View.GONE);
//                }
//            }
//        });
//        mAnimator.start();
    }

    @Override
    protected void initEvent() {
        config_guide_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment;
                if (mConnectNetBean.isCompatibleMode()) {
                    fragment = new CompatibleModeFragment();
                } else {
                    fragment = new SmartConfigFragment();
                }
                addFragmentToStack(R.id.adddevice_fl, fragment);
            }
        });
    }

    private void refreshData() {
        unregisterWifiReceiver();
        config_guide_next.setEnabled(!mConnectNetBean.isCompatibleMode());
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        int duration = 500;
        if (mConnectNetBean.isCompatibleMode()) {
            config_guide_title.setText(R.string.compatible_mode);
            config_guide_confirm.setText(R.string.apconfig_confirm);
            duration = 1500;
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            String ssid;
            if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, mConnectNetBean.getProductId())) {
                ssid = XlinkConstants.LEDSTRIP_AP_SSID;
                ssb.append(getString(R.string.apconfig_guide_strip, ssid));
            } else if (TextUtils.equals(XlinkConstants.PRODUCT_ID_SOCKET, mConnectNetBean.getProductId())) {
                ssid = XlinkConstants.LEDSOCKET_AP_SSID;
                ssb.append(getString(R.string.apconfig_guide_default, ssid));
            } else if (TextUtils.equals(XlinkConstants.PRODUCT_ID_MONSOON, mConnectNetBean.getProductId())) {
                ssid = XlinkConstants.LEDMONSOON_AP_SSID;
                ssb.append(getString(R.string.apconfig_guide_default, ssid));
            } else {
                return;
            }
            ClickableSpan clickableText = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    gotoSystemWifiSettings();
                }
            };
            int start = ssb.length() - ssid.length() - 1;
            int end = ssb.length() - 1;
            ssb.setSpan(clickableText, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan fcs = new ForegroundColorSpan(getContext().getResources().getColor(R.color.colorAccent));
            ssb.setSpan(fcs, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            config_guide_step.setMovementMethod(LinkMovementMethod.getInstance());
            config_guide_step.setText(ssb);

            registerWifiReceiver();
        } else {
            config_guide_title.setText(R.string.smartconfig);
            config_guide_confirm.setText(R.string.smartconfig_confirm);
            if (TextUtils.equals(XlinkConstants.PRODUCT_ID_LEDSTRIP, mConnectNetBean.getProductId())) {
                config_guide_step.setText(R.string.smartconfig_guide_strip);
            }

            unregisterWifiReceiver();
        }

        mAnimator = ObjectAnimator.ofInt(0-duration/2, duration/2);
        mAnimator.setDuration(duration);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (value > 0) {
                    config_guide_icon.setImageResource(DeviceUtil.getProductLedonIcon(mConnectNetBean.getProductId()));
                } else {
                    config_guide_icon.setImageResource(DeviceUtil.getProductLedoffIcon(mConnectNetBean.getProductId()));
                }
            }
        });
        mAnimator.start();
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
        if (dhcpInfo.gateway == DeviceUtil.ESP8266_GATEWAY) {
            String ssid = wifiInfo.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length()-1);
            }
            if (TextUtils.equals(mConnectNetBean.getProductId(), XlinkConstants.PRODUCT_ID_LEDSTRIP) &&
                ssid.matches(DeviceUtil.EXO_STRIP_REGEX)) {
                config_guide_next.setEnabled(true);
                return;
            } else if (TextUtils.equals(mConnectNetBean.getProductId(), XlinkConstants.PRODUCT_ID_SOCKET) &&
                       ssid.matches(DeviceUtil.EXO_SOCKET_REGEX)) {
                config_guide_next.setEnabled(true);
                return;
            } else if (TextUtils.equals(mConnectNetBean.getProductId(), XlinkConstants.PRODUCT_ID_MONSOON) &&
                       ssid.matches(DeviceUtil.EXO_MONSOON_REGEX)) {
                config_guide_next.setEnabled(true);
                return;
            }
        }
        config_guide_next.setEnabled(false);
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
