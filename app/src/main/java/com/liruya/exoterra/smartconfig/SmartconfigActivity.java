package com.liruya.exoterra.smartconfig;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.liruya.base.BaseImmersiveActivity;
import com.liruya.esptouch.EsptouchLinker;
import com.liruya.esptouch.EsptouchResult;
import com.liruya.esptouch.IEsptouchLinkListener;
import com.liruya.exoterra.R;
import com.liruya.exoterra.helper.WifiHelper;
import com.liruya.exoterra.scan.ScanActivity;

import java.util.List;

public class SmartconfigActivity extends BaseImmersiveActivity {
    private Toolbar smartconfig_toolbar;
    private TextInputEditText smartconfig_et_ssid;
    private TextInputEditText smartconfig_et_password;
    private TextView smartconfig_tv_msg;
    private ImageButton smartconfig_ib_change;
    private ProgressBar smartconfig_progress;
    private ToggleButton smartconfig_tb_start;

    private EsptouchLinker mEsptouchLinker;
    private IEsptouchLinkListener mLinkListener;
    private CountDownTimer mTimer;

    private WifiHelper mWifiHelper;

    @Override
    protected void onStart() {
        super.onStart();

        initData();
        initEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mEsptouchLinker != null) {
            mEsptouchLinker.unregisterWiFiStateChangedListener();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_smartconfig;
    }

    @Override
    protected void initView() {
        smartconfig_toolbar = findViewById(R.id.smartconfig_toolbar);
        smartconfig_et_ssid = findViewById(R.id.smartconfig_et_ssid);
        smartconfig_et_password = findViewById(R.id.smartconfig_et_password);
        smartconfig_tv_msg = findViewById(R.id.smartconfig_tv_msg);
        smartconfig_ib_change = findViewById(R.id.smartconfig_ib_change);
        smartconfig_progress = findViewById(R.id.smartconfig_progress);
        smartconfig_tb_start = findViewById(R.id.smartconfig_tb_start);
    }

    @Override
    protected void initData() {
        smartconfig_et_password.requestFocus();
        mWifiHelper = new WifiHelper(this);
        mEsptouchLinker = new EsptouchLinker(this);
        mLinkListener = new IEsptouchLinkListener() {
            @Override
            public void onWifiChanged(WifiInfo wifiInfo) {
                if (wifiInfo == null) {
                    return;
                }
                String ssid = wifiInfo.getSSID();
                if (ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
                boolean connected = !TextUtils.isEmpty(wifiInfo.getBSSID());
                boolean is5G = false;
                if (connected && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && wifiInfo.getFrequency() > 4900 && wifiInfo.getFrequency() < 5900) {
                    is5G = true;
                }
                smartconfig_et_ssid.setText(ssid);
                smartconfig_tb_start.setEnabled(connected && !is5G);
                if (connected) {
                    if (is5G) {
                        smartconfig_tv_msg.setText(R.string.warn_device_donot_support_5g);
                    } else {
                        smartconfig_tv_msg.setText("");
                        smartconfig_et_password.requestFocus();
                    }
                } else {
                    smartconfig_tv_msg.setText("Please connect router.");
                }
                smartconfig_tb_start.setChecked(false);
                stopEsptouch();
            }

            @Override
            public void onLocationChanged() {
                Log.e(TAG, "onLocationChanged: ");
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailed() {
                showSmartconfigFailedDialog();
            }

            @Override
            public void onLinked(final EsptouchResult result) {
                Log.e(TAG, "onLinked: " + result.getAddress() + " " + result.getInetAddress().getHostAddress());

//                smartconfig_tv_msg.append("<" + result.getBssid() + "> is connected to router. IP: " + result.getInetAddress().getHostAddress() + "\n");
            }

            @Override
            public void onCompleted(List<EsptouchResult> results) {
                smartconfig_tb_start.setChecked(false);
                stopEsptouch();
                if (results == null || results.size() == 0) {
                    showNodeviceConnecteDialog();
                } else {
                    showDeviceConnectDialog(results.get(0));
                }
            }
        };
        mEsptouchLinker.setEsptouchLinkListener(mLinkListener);
        mEsptouchLinker.registerWiFiStateChangedListener();

        mTimer = new CountDownTimer(mEsptouchLinker.getPeriod(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((mEsptouchLinker.getPeriod() - millisUntilFinished) / 1000);
                smartconfig_progress.setProgress(progress);
            }

            @Override
            public void onFinish() {
                smartconfig_progress.setProgress(smartconfig_progress.getMax());
            }
        };
    }

    @Override
    protected void initEvent() {
        smartconfig_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        smartconfig_ib_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSystemWifiSettings();
            }
        });

        smartconfig_tb_start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startEsptouch();
                } else {
                    stopEsptouch();
                }
            }
        });
    }

    private void startEsptouch() {
        String ssid = getSsidText();
        String mac = mWifiHelper.getGatewayMacAddress();
        String psw = getPasswordText();
        smartconfig_et_password.setEnabled(false);
        smartconfig_ib_change.setEnabled(false);
        smartconfig_tv_msg.setText("");
        smartconfig_progress.setProgress(0);
        smartconfig_progress.setMax(mEsptouchLinker.getPeriod()/1000);
        smartconfig_progress.setVisibility(View.VISIBLE);
        mEsptouchLinker.start(ssid, mac, psw, 1, true);
        mTimer.start();
    }

    private void stopEsptouch() {
        mTimer.cancel();
        mEsptouchLinker.stop();
        smartconfig_et_password.setEnabled(true);
        smartconfig_ib_change.setEnabled(true);
        smartconfig_tv_msg.setText("");
        smartconfig_progress.setProgress(0);
        smartconfig_progress.setVisibility(View.GONE);
    }

    private String getSsidText() {
        return smartconfig_et_ssid.getText().toString();
    }

    private String getPasswordText() {
        return smartconfig_et_password.getText().toString();
    }

    private String getNodeviceConnectMessage() {
        String ssid = getSsidText();
        return getString(R.string.msg_smartconfig_nodevice_connected).replace(getString(R.string.placeholder_ssid), ssid);
    }

    private String getDeviceConnectMessage(EsptouchResult result) {
        String mac = result.getAddress();
        String ssid = getSsidText();
        String ip = result.getInetAddress().getHostAddress();
        return getString(R.string.msg_smartconfig_device_connect_router).replace(getString(R.string.placeholder_mac), mac)
                                                                        .replace(getString(R.string.placeholder_ssid), ssid)
                                                                        .replace(getString(R.string.placeholder_ip), ip);
    }

    private void showSmartconfigFailedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_smartconfig_failed)
               .setMessage(R.string.msg_smartconfig_failed)
               .show();
    }

    private void showNodeviceConnecteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_smartconfig_completed)
               .setMessage(getNodeviceConnectMessage())
               .show();
    }

    private void showDeviceConnectDialog(final EsptouchResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_smartconfig_completed)
               .setMessage(getDeviceConnectMessage(result))
               .setNegativeButton(R.string.no, null)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       gotoScanActivity();
                       finish();
                   }
               })
               .show();
    }

    private void gotoScanActivity() {
        Intent intent = new Intent(SmartconfigActivity.this, ScanActivity.class);;
        startActivity(intent);
    }

    private void gotoSystemWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
