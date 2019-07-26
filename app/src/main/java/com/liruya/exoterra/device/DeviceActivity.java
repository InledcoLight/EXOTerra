package com.liruya.exoterra.device;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liruya.base.BaseActivity;
import com.liruya.exoterra.AppConstants;
import com.liruya.exoterra.BaseViewModel;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.bean.EXOMonsoon;
import com.liruya.exoterra.bean.EXOSocket;
import com.liruya.exoterra.device.Monsoon.MonsoonFragment;
import com.liruya.exoterra.device.Monsoon.MonsoonViewModel;
import com.liruya.exoterra.device.detail.DeviceDetailFragment;
import com.liruya.exoterra.device.light.LightFragment;
import com.liruya.exoterra.device.light.LightViewModel;
import com.liruya.exoterra.device.socket.SocketFragment;
import com.liruya.exoterra.device.socket.SocketViewModel;
import com.liruya.exoterra.event.DatapointChangedEvent;
import com.liruya.exoterra.event.DeviceStateChangedEvent;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.util.DeviceUtil;
import com.liruya.exoterra.util.LogUtil;
import com.liruya.exoterra.util.RegexUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkConstants;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.manager.XLinkDeviceManager;
import cn.xlink.sdk.v5.model.XDevice;

public class DeviceActivity extends BaseActivity {
    private Toolbar device_toolbar;
    private CheckableImageButton device_cib_cloud;
    private CheckableImageButton device_cib_wifi;

    private Device mDevice;
    private DeviceViewModel mDeviceViewModel;
    private BaseViewModel<Device> mDeviceBaseViewModel;

    private XlinkTaskCallback<XDevice> mSetCallback;
    private XlinkTaskCallback<List<XLinkDataPoint>> mGetCallback;

    private final Handler mHandler = new Handler();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mDevice != null) {
            XLinkDeviceManager.getInstance().removeDeviceConnectionFlags(mDevice.getXDevice().getDeviceTag(), 1);
            XLinkDeviceManager.getInstance().disconnectDeviceLocal(mDevice.getXDevice().getDeviceTag());
        }
    }

    @SuppressLint ("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        View view = menu.findItem(R.id.menu_device_status).getActionView();
        device_cib_cloud = view.findViewById(R.id.device_cib_cloud);
        device_cib_wifi = view.findViewById(R.id.device_cib_wifi);
        if (mDevice != null) {
            device_cib_cloud.setChecked(mDevice.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? true : false);
            device_cib_wifi.setChecked(mDevice.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? true : false);
        }
        MenuItem device_edit = menu.findItem(R.id.menu_device_edit);
        MenuItem device_rename = menu.findItem(R.id.menu_device_rename);
        MenuItem device_datetime = menu.findItem(R.id.menu_device_datetime);
        MenuItem device_share = menu.findItem(R.id.menu_device_share);
        MenuItem device_upgrade = menu.findItem(R.id.menu_device_upgrade);
        MenuItem device_detail = menu.findItem(R.id.menu_device_detail);
        device_edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return gotoDeviceSetFragment();
            }
        });
        device_rename.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showRenameDialog();
                return true;
            }
        });
        device_datetime.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showDeviceDatetimeDialog();
                return true;
            }
        });
        device_share.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showShareDeviceDialog();
                return true;
            }
        });
        device_upgrade.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                upgrade();
                return true;
            }
        });
        device_detail.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.device_root, new DeviceDetailFragment())
                                           .addToBackStack("")
                                           .commit();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_device;
    }

    @Override
    protected void initView() {
        device_toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(device_toolbar);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String deviceTag = intent.getStringExtra(AppConstants.DEVICE_TAG);
        final Device device = DeviceManager.getInstance().getDevice(deviceTag);
        if (device == null) {
            return;
        }
        mDeviceBaseViewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        mDeviceBaseViewModel.setData(device);

        String pid = device.getXDevice().getProductId();
        String name = device.getXDevice().getDeviceName();
        device_toolbar.setTitle(TextUtils.isEmpty(name) ? DeviceUtil.getDefaultName(pid) : name);
//            device_toolbar.setLogo(DeviceUtil.getProductIcon(pid));
        setSupportActionBar(device_toolbar);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_LEDSTRIP)) {
            mDevice = new EXOLedstrip(device);
            mDeviceViewModel = ViewModelProviders.of(this).get(LightViewModel.class);
            mDeviceViewModel.setData(mDevice);
            transaction.replace(R.id.device_fl_show, new LightFragment()).commit();
        } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_MONSOON)) {
            mDevice = new EXOMonsoon(device);
            mDeviceViewModel = ViewModelProviders.of(this).get(MonsoonViewModel.class);
            mDeviceViewModel.setData(mDevice);
            transaction.replace(R.id.device_fl_show, new MonsoonFragment()).commit();
        } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_SOCKET)) {
            mDevice = new EXOSocket(device);
            mDeviceViewModel = ViewModelProviders.of(this).get(SocketViewModel.class);
            mDeviceViewModel.setData(mDevice);
            transaction.replace(R.id.device_fl_show, new SocketFragment()).commit();
        } else {
            throw new RuntimeException("Invalid ProductID.");
        }
        XLinkDeviceManager.getInstance().addDeviceConnectionFlags(mDevice.getXDevice().getDeviceTag(), 1);
        XLinkDeviceManager.getInstance().connectDeviceLocal(mDevice.getXDevice().getDeviceTag());
        EventBus.getDefault().register(this);

        mSetCallback = new XlinkTaskCallback<XDevice>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: setDataPointError - " + error);
            }

            @Override
            public void onStart() {
                Log.e(TAG, "onStart: setDataPointStart");
            }

            @Override
            public void onComplete(XDevice xDevice) {
                Log.e(TAG, "onComplete: setDataPointComplete");
            }
        };
        mGetCallback = new XlinkTaskCallback<List<XLinkDataPoint>>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: getDataPoints- " + error );
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<XLinkDataPoint> dataPoints) {
                Log.e(TAG, "onComplete: getDataPoints");
                Collections.sort(dataPoints, new Comparator<XLinkDataPoint>() {
                    @Override
                    public int compare(XLinkDataPoint o1, XLinkDataPoint o2) {
                        return o1.getIndex() - o2.getIndex();
                    }
                });
                StringBuilder sb = new StringBuilder();
                for (XLinkDataPoint dp : dataPoints) {
                    device.setDataPoint(dp);
                    mDevice.setDataPoint(dp);
                    sb.append(dp.getIndex()).append(" ")
                      .append(dp.getName()).append(" ")
                      .append(dp.getType()).append(" ")
                      .append(dp.getValue()).append("\n");
                }
                mDeviceViewModel.postValue();
                LogUtil.e(TAG, "onComplete: " + dataPoints.size() + "\n" + sb);
            }
        };
        mDeviceViewModel.setGetCallback(mGetCallback);
        mDeviceViewModel.setSetCallback(mSetCallback);

        XlinkCloudManager.getInstance().getDeviceMetaDatapoints(mDevice.getXDevice(), new XlinkTaskCallback<List<XLinkDataPoint>>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: getMetaDatapoints- " + error );
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<XLinkDataPoint> dataPoints) {
                Log.e(TAG, "onComplete: getMetaDatapoints");
                device.setDataPointList(dataPoints);
                mDevice.setDataPointList(dataPoints);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDeviceViewModel.getDatapoints();
                    }
                }, 1000);
            }
        });
    }

    @Override
    protected void initEvent() {
        device_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceStateChangedEvent(DeviceStateChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getDeviceTag(), mDevice.getXDevice().getDeviceTag())) {
            device_cib_cloud.setChecked(mDevice.getXDevice().getCloudConnectionState() == XDevice.State.CONNECTED ? true : false);
            device_cib_wifi.setChecked(mDevice.getXDevice().getLocalConnectionState() == XDevice.State.CONNECTED ? true : false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatapointChangedEvent(DatapointChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getDeviceTag(), mDevice.getXDevice().getDeviceTag())) {
            LogUtil.e(TAG, "onDatapointChangedEvent: ");
            mDeviceViewModel.postValue();
        }
    }

    private void showDeviceDatetimeDialog() {
        if (mDevice == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        final AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.device_datetime);
        dialog.setMessage(mDevice.getDeviceDatetime() + "\n\n" + getString(R.string.device_datetime_not_correct));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.set_timezone), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoDeviceSetFragment();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mDeviceViewModel.probeDeviceDatetime(new XlinkTaskCallback<List<XLinkDataPoint>>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<XLinkDataPoint> dataPoints) {
                for (XLinkDataPoint dp : dataPoints) {
                    mDevice.setDataPoint(dp);
                }
                dialog.setMessage(mDevice.getDeviceDatetime() + "\n\n" + getString(R.string.device_datetime_not_correct));
            }
        });
        dialog.show();
    }

    private void showRenameDialog() {
        View view = LayoutInflater.from(DeviceActivity.this).inflate(R.layout.dialog_rename, null, false);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        et_name.setText(mDevice.getXDevice().getDeviceName());
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        final AlertDialog dialog = builder.setTitle(R.string.rename_device)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                String pid = mDevice.getXDevice().getProductId();
                int devid = mDevice.getXDevice().getDeviceId();
                XlinkCloudManager.getInstance().renameDevice(pid, devid, name, new XlinkRequestCallback<DeviceApi.DeviceResponse>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(DeviceActivity.this, error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(DeviceApi.DeviceResponse deviceResponse) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private boolean gotoDeviceSetFragment() {
        if (getSupportFragmentManager().findFragmentByTag("device_set") == null) {
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.device_root,
                                            DeviceSetFragment.newInstance(mDevice.getDeviceTag()), "device_set")
                                       .addToBackStack("")
                                       .commit();
            return true;
        }
        return false;
    }

    private void checkUpdate() {
        XlinkCloudManager.getInstance().getNewsetVersion(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceNewestVersionResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Toast.makeText(DeviceActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(DeviceApi.DeviceNewestVersionResponse response) {
            }
        });
    }

    private void upgrade() {
        XlinkCloudManager.getInstance().upgradeDevice(mDevice.getXDevice(), new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                Toast.makeText(DeviceActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {

            }
        });
    }

    private void shareDevice(@NonNull String email) {
        XlinkCloudManager.getInstance().shareDevice(mDevice.getXDevice(), email, new XlinkTaskCallback<DeviceApi.ShareDeviceResponse>() {
            @Override
            public void onError(String error) {
                Toast.makeText(DeviceActivity.this, error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(DeviceApi.ShareDeviceResponse response) {
                Toast.makeText(DeviceActivity.this, "Share Success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    private void showShareDeviceDialog() {
        View view = LayoutInflater.from(DeviceActivity.this).inflate(R.layout.dialog_share, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_share_til);
        final TextInputEditText et_email = view.findViewById(R.id.dialog_share_email);
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        final AlertDialog dialog = builder.setTitle(R.string.share_device)
                                          .setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.share, null)
                                          .show();
        Button btn_share = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                if (RegexUtil.isEmail(email)) {
                    shareDevice(email);
                    dialog.dismiss();
                } else {
                    til.setError(getString(R.string.error_email));
                }
            }
        });
    }
}
