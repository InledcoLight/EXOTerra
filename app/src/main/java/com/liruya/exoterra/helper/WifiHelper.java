package com.liruya.exoterra.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public class WifiHelper {
    private WeakReference<Context> mContext;

    public WifiHelper(@NonNull Context context) {
        mContext = new WeakReference<>(context);
    }

    public String getIp(int ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(ip & 0xFF)
          .append('.');
        sb.append((ip >> 8) & 0xFF)
          .append(".");
        sb.append((ip >> 16) & 0xFF)
          .append(".");
        sb.append((ip >> 24) & 0xFF);
        String result = new String(sb);
        return result;
    }

    private NetworkInfo getWifiNetworkInfo() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivityManager == null) {
            return null;
        }
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWiFiNetworkInfo;
    }

    private WifiInfo getConnectedWiFiInfo() {
        WifiManager mWifiManager = (WifiManager) mContext.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager == null) {
            return null;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo;
    }

    public boolean isWiFiConnected() {
        NetworkInfo mWiFiNetworkInfo = getWifiNetworkInfo();
        boolean isWifiConnected = false;
        if (mWiFiNetworkInfo != null) {
            isWifiConnected = mWiFiNetworkInfo.isConnected();
        }
        return isWifiConnected;
    }

    public String getGatewayMacAddress() {
        WifiInfo wifiInfo = getConnectedWiFiInfo();
        String bssid = null;
        if (wifiInfo != null && isWiFiConnected()) {
            bssid = wifiInfo.getBSSID();
        }
        return bssid;
    }

    public String getGatewaySsid() {
        WifiInfo wifiInfo = getConnectedWiFiInfo();
        String ssid = null;
        if (wifiInfo != null && isWiFiConnected()) {
            ssid = wifiInfo.getSSID();
        }
        return ssid;
    }

    public String getGatewayIp() {
        WifiManager wifiManager = (WifiManager) mContext.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            return getIp(dhcpInfo.gateway);
        }
        return null;
    }

    public String getLocalMacAddress() {
        WifiInfo wifiInfo = getConnectedWiFiInfo();
        String mac = null;
        if (wifiInfo != null && isWiFiConnected()) {
            mac = wifiInfo.getMacAddress();
        }
        return mac;
    }

    public String getLocalIp() {
        WifiInfo wifiInfo = getConnectedWiFiInfo();
        if (wifiInfo != null && isWiFiConnected()) {
            int a = wifiInfo.getIpAddress();
            return getIp(a);
        }
        return null;
    }
}
