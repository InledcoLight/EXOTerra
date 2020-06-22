package com.inledco.exoterra.device.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.BaseProperty;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.DeviceInfo;
import com.inledco.exoterra.aliot.DeviceViewModel;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.ExoProduct;
import com.inledco.exoterra.event.DeviceChangedEvent;
import com.inledco.exoterra.event.DevicesRefreshedEvent;
import com.inledco.exoterra.event.GroupDeviceChangedEvent;
import com.inledco.exoterra.event.SimpleEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceDetailFragment extends BaseFragment {
    private final String DEVICE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final String KEY_DEVICETIME = "DeviceTime";

    private Toolbar device_detail_toolbar;
    private ImageView device_detail_icon;
    private TextView device_detail_name;
    private TextView device_detail_zone;
    private TextView device_detail_datetime;
    private TextView device_detail_user_list;
    private TextView device_detail_habitat;
    private TextView device_detail_upgrade;
    private TextView device_detail_delete;
    private TextView device_detail_router;
    private TextView device_detail_ipaddress;
    private TextView device_detail_rssi;
    private TextView device_detail_mac;
    private TextView device_detail_fwversion;
    private Button device_detail_back;

    private UpdateDialog mUpgradeDialog;

    private boolean mDeviceAdmin;

    private long mCurrentTime;
    private Timer mTimer;
    private TimerTask mTask;

    private DeviceViewModel mDeviceViewModel;
    private Device mDevice;
    private String mProductKey;
    private String mDeviceName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        EventBus.getDefault().register(this);
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mTask != null) {
            mTask.cancel();
        }
        mTimer = null;
        mTask = null;
        if (mUpgradeDialog != null) {
            mUpgradeDialog.dismiss();
            mUpgradeDialog = null;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_device_detail;
    }

    @Override
    protected void initView(View view) {
        device_detail_toolbar = view.findViewById(R.id.device_detail_toolbar);
        device_detail_icon = view.findViewById(R.id.device_detail_icon);
        device_detail_name = view.findViewById(R.id.device_detail_name);
        device_detail_habitat = view.findViewById(R.id.device_detail_habitat);
        device_detail_zone = view.findViewById(R.id.device_detail_zone);
        device_detail_datetime = view.findViewById(R.id.device_detail_datetime);
        device_detail_user_list = view.findViewById(R.id.device_detail_user_list);
        device_detail_upgrade = view.findViewById(R.id.device_detail_upgrade);
        device_detail_delete = view.findViewById(R.id.device_detail_delete);
        device_detail_router = view.findViewById(R.id.device_detail_router);
        device_detail_ipaddress = view.findViewById(R.id.device_detail_ipaddress);
        device_detail_rssi = view.findViewById(R.id.device_detail_rssi);
        device_detail_mac = view.findViewById(R.id.device_detail_mac);
        device_detail_fwversion = view.findViewById(R.id.device_detail_fwversion);
        device_detail_back = view.findViewById(R.id.device_detail_back);

        device_detail_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        device_detail_user_list.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        device_detail_upgrade.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
    }

    @Override
    protected void initData() {
        mDeviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceViewModel.class);
        mDevice = (Device) mDeviceViewModel.getData();
        mDeviceViewModel.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                checkDeviceDatetime();
            }
        });

        if (mDevice != null) {
            mDeviceAdmin = TextUtils.equals("管理员", mDevice.getRole());
            mProductKey = mDevice.getProductKey();
            mDeviceName = mDevice.getDeviceName();
            String name = mDevice.getName();
            ExoProduct product = ExoProduct.getExoProduct(mProductKey);
            if (product != null) {
                if (TextUtils.isEmpty(name)) {
                    name = product.getDefaultName();
                }
                device_detail_icon.setImageResource(product.getIconSmall());
            }
            Group group = GroupManager.getInstance().getDeviceGroup(mProductKey, mDeviceName);
            if (group != null) {
                device_detail_habitat.setText(group.name);
            } else {
                device_detail_habitat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
            }
            device_detail_delete.setEnabled(TextUtils.equals(mDevice.getRole(), "管理员"));
            getDeviceDatetime();

            device_detail_name.setText(name);
            device_detail_zone.setText(getTimezoneDesc(mDevice.getZone()));
            String mac = mDevice.getMac();
            device_detail_mac.setText(mac);
            DeviceInfo devInfo = mDevice.getDeviceInfo();
            if (devInfo != null) {
                device_detail_router.setText(devInfo.getSsid());
                device_detail_ipaddress.setText(devInfo.getIp());
                device_detail_mac.setText(devInfo.getMac());
                device_detail_rssi.setText("" + devInfo.getRssi() + " dB");
            }

            int fwversion = mDevice.getFirmwareVersion();
            if (fwversion > 0) {
                device_detail_fwversion.setText(String.valueOf(fwversion));
            }
        }
    }

    @Override
    protected void initEvent() {
        device_detail_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null || !mDeviceAdmin) {
                    return;
                }
                showRenameDialog();
            }
        });
        device_detail_habitat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDeviceAdmin) {
                    return;
                }
                Group group = GroupManager.getInstance().getDeviceGroup(mProductKey, mDeviceName);
                if (group == null) {
                    addFragmentToStack(R.id.device_root, SetHabitatFragment.newInstance(mProductKey, mDeviceName, mDevice.getName()));
                }
            }
        });
//        device_detail_user_list.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mDevice == null) {
//                    return;
//                }
//                addFragmentToStack(R.id.device_root, UserListFragment.newInstance(mDevice.getXDevice().getDeviceId()));
//            }
//        });
        device_detail_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null || !mDeviceAdmin) {
                    return;
                }
                getNewestVersion();
            }
        });
        device_detail_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null || !mDeviceAdmin) {
                    return;
                }
                showDeleteDialog();
            }
        });
        device_detail_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void checkDeviceDatetime() {
        BaseProperty prop = mDevice.getItems().get(KEY_DEVICETIME);
        if (prop != null && prop.isUpdated()) {
            Log.e(TAG, "checkDeviceDatetime: ");
            String datetime = mDevice.getDeviceTime();
            if (!TextUtils.isEmpty(datetime)) {
                try {
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    if (mTask != null) {
                        mTask.cancel();
                    }
                    mTimer = new Timer();
                    mTask = new TimerTask() {
                        @Override
                        public void run() {
                            mCurrentTime += 1000;
                            EventBus.getDefault().post(new SimpleEvent(mCurrentTime));
                        }
                    };

                    int offset = TimeZone.getDefault().getRawOffset();
                    int zone = mDevice.getZone();
                    long currentTime = Long.parseLong(datetime);
                    mCurrentTime = currentTime + zone * 60000 - offset;
                    EventBus.getDefault().post(new SimpleEvent(mCurrentTime));

                    mTimer.scheduleAtFixedRate(mTask, 0, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDeviceChangedEvent(DeviceChangedEvent event) {
        if (event != null && mDevice != null
            && TextUtils.equals(event.getTag(), mDevice.getTag())) {
            Group group = GroupManager.getInstance().getDeviceGroup(mProductKey, mDeviceName);
            if (group != null) {
                device_detail_habitat.setText(group.name);
            } else {
                device_detail_habitat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onGroupDeviceChangedEvent(GroupDeviceChangedEvent event) {
        if (mDevice == null || event == null) {
            return;
        }
        Group group = GroupManager.getInstance().getDeviceGroup(mProductKey, mDeviceName);
        if (group != null) {
            device_detail_habitat.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            device_detail_habitat.setText(group.name);
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onFotaProgressEvent(UserApi.FotaProgress event) {
        if (event == null) {
            return;
        }
        if (mDeviceAdmin && TextUtils.equals(mProductKey, event.productKey) && TextUtils.equals(mDeviceName, event.deviceName)) {
            if (mUpgradeDialog == null) {
                mUpgradeDialog = new UpdateDialog(getContext());
            }
//            if (event.params.step == 0) {
//                mUpgradeDialog.show();
//                return;
//            }
            if (!mUpgradeDialog.isShowing()) {
                mUpgradeDialog.show();
            }
            if (event.params.step == 0) {
                return;
            }
            if (event.params.step == 100) {
                mUpgradeDialog.setMessage("Upgrade success.");
                mUpgradeDialog.setResult(true);
            } else {
                mUpgradeDialog.setMessage("Upgrade failed: " + event.params.desc);
                mUpgradeDialog.setResult(false);
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void updateDatetime(SimpleEvent event) {
        final Date date = new Date(mCurrentTime);
        final DateFormat df = new SimpleDateFormat(DEVICE_DATE_FORMAT);
        device_detail_datetime.setText(df.format(date));
    }

    private String getTimezoneDesc(int zone) {
        DecimalFormat df = new DecimalFormat("00");
        String zoneDesc = "GMT+";
        if (zone < 0) {
            zoneDesc = "GMT-";
            zone = -zone;
        }
        zoneDesc = zoneDesc + df.format(zone/60) + ":" + df.format(zone%60);
        return zoneDesc;
    }

    private void deleteDevice() {
        AliotServer.getInstance().unsubscribeDevice(mProductKey, mDeviceName, new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                Group group = GroupManager.getInstance().getDeviceGroup(mProductKey, mDeviceName);
                if (group != null) {
                    group.removeDevice(mProductKey, mDeviceName);
                    EventBus.getDefault().post(new GroupDeviceChangedEvent(group.groupid));
                }
                DeviceManager.getInstance().removeDevice(mDevice);
                EventBus.getDefault().post(new DevicesRefreshedEvent());
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                });
            }
        });
        showLoadDialog();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_device)
               .setMessage(R.string.msg_delete_device)
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteDevice();
                   }
               })
               .setCancelable(false)
               .show();
    }

    private void getNewestVersion() {
        final int version = mDevice.getFirmwareVersion();
        if (version <= 0) {
            return;
        }
        AliotServer.getInstance().getFirmwareList(mProductKey, new HttpCallback<UserApi.FirmwaresResponse>() {
            @Override
            public void onError(String error) {
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.FirmwaresResponse result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                dismissLoadDialog();
                if (TextUtils.equals(result.data.product_key, mProductKey)) {
                    int newest_version = version;
                    String url = null;
                    for (UserApi.Firmware fw : result.data.firmware_list) {
                        if (fw.version > version && (fw.version-version)%2 == 1 && newest_version < fw.version) {
                            newest_version = fw.version;
                            url = fw.url;
                        }
                    }
                    if (newest_version <= version || TextUtils.isEmpty(url)) {
                        showToast("The device is up to date.");
                    } else {
                        showUpgradeDialog(newest_version, url);
                    }
                }
            }
        });
        showLoadDialog();
    }

    private void showUpgradeDialog(final int version, final String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.upgradable)
               .setMessage(getString(R.string.upgrade_msg, version))
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       AliotServer.getInstance().upgradeFirmware(mProductKey, mDeviceName, version, url, new HttpCallback<UserApi.PublishTopicResponse>() {
                           @Override
                           public void onError(String error) {
                               showToast(error);
                           }

                           @Override
                           public void onSuccess(UserApi.PublishTopicResponse result) {

                           }
                       });
                   }
               })
               .setCancelable(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    private void getDeviceDatetime() {
        mDeviceViewModel.getProperty("DeviceTime");
    }

    private void showRenameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.device_name));
        et_name.setText(mDevice.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.rename_device)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        et_name.requestFocus();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                final String pkey = mDevice.getProductKey();
                final String dname = mDevice.getDeviceName();
                AliotServer.getInstance().modifyDeviceName(pkey, dname, name, new HttpCallback<UserApi.Response>() {
                    @Override
                    public void onError(String error) {
                        dismissLoadDialog();
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.Response result) {
                        mDevice.setName(name);
                        EventBus.getDefault().post(new DeviceChangedEvent(pkey, dname));
                        dismissLoadDialog();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                device_detail_name.setText(name);
                            }
                        });
                    }
                });
                showLoadDialog();
            }
        });
    }

    private final class UpdateDialog {
        private final AlertDialog mDialog;
        private ProgressBar mProgressBar;
        private TextView mMessageText;
        private ImageView mResultIcon;

        protected UpdateDialog(@NonNull Context context) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_upgrade, null, false);
            mProgressBar = view.findViewById(R.id.dialog_upgrade_pb);
            mMessageText = view.findViewById(R.id.dialog_upgrade_msg);
            mResultIcon = view.findViewById(R.id.dialog_upgrade_result);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.upgrading);
            builder.setCancelable(false);
            builder.setView(view);
            builder.setPositiveButton(R.string.close, null);
            mDialog = builder.create();
        }

        public void setMessage(String msg) {
            mMessageText.setText(msg);
        }

        public void setMessage(@StringRes int res) {
            mMessageText.setText(getString(res));
        }

        public void show() {
            if (!mDialog.isShowing()) {
                mProgressBar.setVisibility(View.VISIBLE);
                mResultIcon.setVisibility(View.INVISIBLE);
                mDialog.show();
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
            }
        }

        public void dismiss() {
            mDialog.dismiss();
        }

        public boolean isShowing() {
            return mDialog.isShowing();
        }

        public void setResult(final boolean result) {
            if (mDialog.isShowing()) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mResultIcon.setImageResource(result ? R.drawable.ic_done_green_64dp : R.drawable.ic_fail_red_64dp);
                mResultIcon.setVisibility(View.VISIBLE);
                Button btn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (result) {
                            getActivity().finish();
                        } else {
                            mDialog.dismiss();
                        }
                    }
                });
                btn.setVisibility(View.VISIBLE);
            }
        }
    }
}
