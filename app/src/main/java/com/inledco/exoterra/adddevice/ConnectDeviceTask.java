package com.inledco.exoterra.adddevice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.List;

public class ConnectDeviceTask extends AsyncTask<ScanResult, Void, Boolean> {

    private final WeakReference<AppCompatActivity> mActivity;

    public ConnectDeviceTask(@NonNull final AppCompatActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    private boolean validateDevice(DhcpInfo info) {
        if (info == null) {
            return false;
        }
        //192.168.4.1
        if (info.gateway == 0x0104A8C0) {
            return true;
        }
        return false;
    }

    @Override
    protected Boolean doInBackground(ScanResult... scanResults) {
        if (scanResults.length != 1) {
            return false;
        }
        ScanResult result = scanResults[0];
        String ssid = result.SSID;
        String bssid = result.BSSID;
        WifiManager manager = (WifiManager) mActivity.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = null;
        List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
        for (WifiConfiguration cfg : configurations) {
            if (ssid.equals(cfg.SSID) && bssid.equals(cfg.BSSID)) {
                config = cfg;
                break;
            }
        }
        if (config != null) {
            config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();
            config.SSID = ssid;
            config.BSSID = bssid;
            manager.addNetwork(config);
        }
        if (manager.isWifiEnabled() == false) {
            manager.setWifiEnabled(true);
        }
        manager.enableNetwork(config.networkId, true);
        ConnectivityManager connectivityManager = (ConnectivityManager) mActivity.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        int timeout = 100;
        while (timeout > 0) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            NetworkInfo ni = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                WifiInfo info = manager.getConnectionInfo();
                if (info != null && ssid.equals(info.getSSID()) && bssid.equals(info.getBSSID())) {
                    return true;
                }
            }
            timeout--;
        }
        return false;
    }
}
