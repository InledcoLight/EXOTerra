package com.inledco.exoterra.device.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.device.DeviceBaseViewModel;
import com.inledco.exoterra.event.DevicePropertyChangedEvent;
import com.inledco.exoterra.event.SubscribeChangedEvent;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.util.DeviceUtil;
import com.inledco.exoterra.view.MessageDialog;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.core.model.XLinkDataPoint;

public class DeviceDetailFragment extends BaseFragment {
    private final String DEVICE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Toolbar device_detail_toolbar;
    private LinearLayout device_detail_ll_name;
    private TextView device_detail_name;
    private TextView device_detail_zone;
    private LinearLayout device_detail_ll_location;
    private TextView device_detail_location;
    private TextView device_detail_datetime;
    private TextView device_detail_rssi;
    private TextView device_detail_user_list;
    private TextView device_detail_habitat;
    private TextView device_detail_devid;
    private TextView device_detail_mac;
    private TextView device_detail_upgrade;
    private TextView device_detail_fwversion;
    private TextView device_detail_cloudzone;
    private Button device_detail_delete;

    private UpdateDialog mUpgradeDialog;

    private boolean isDestroyed;

    private long mCurrentTime;
    private Timer mTimer = new Timer();
    private TimerTask mTask = new TimerTask() {
        @Override
        public void run() {
            mCurrentTime += 1000;
            final Date date = new Date(mCurrentTime);
            final DateFormat df = new SimpleDateFormat(DEVICE_DATE_FORMAT);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    device_detail_datetime.setText(df.format(date));
                }
            });
        }
    };

    private DeviceBaseViewModel mDeviceViewModel;
    private Device mDevice;

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
        isDestroyed = true;
        mTimer.cancel();
        mTask.cancel();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_device_detail;
    }

    @Override
    protected void initView(View view) {
        device_detail_toolbar = view.findViewById(R.id.device_detail_toolbar);
        device_detail_ll_name = view.findViewById(R.id.device_detail_ll_name);
        device_detail_name = view.findViewById(R.id.device_detail_name);
        device_detail_zone = view.findViewById(R.id.device_detail_zone);
        device_detail_ll_location = view.findViewById(R.id.device_detail_ll_location);
        device_detail_location = view.findViewById(R.id.device_detail_location);
        device_detail_datetime = view.findViewById(R.id.device_detail_datetime);
        device_detail_rssi = view.findViewById(R.id.device_detail_rssi);
        device_detail_user_list = view.findViewById(R.id.device_detail_user_list);
        device_detail_habitat = view.findViewById(R.id.device_detail_habitat);
        device_detail_devid = view.findViewById(R.id.device_detail_devid);
        device_detail_mac = view.findViewById(R.id.device_detail_mac);
        device_detail_fwversion = view.findViewById(R.id.device_detail_fwversion);
        device_detail_upgrade = view.findViewById(R.id.device_detail_upgrade);
        device_detail_cloudzone = view.findViewById(R.id.device_detail_cloudzone);
        device_detail_delete = view.findViewById(R.id.device_detail_delete);

        device_detail_user_list.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
        device_detail_habitat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
        device_detail_upgrade.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
    }

    @Override
    protected void initData() {
        mDeviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceBaseViewModel.class);
        mDevice = mDeviceViewModel.getData();
        mDeviceViewModel.observe(this, new Observer<Device>() {
            @Override
            public void onChanged(@Nullable Device device) {
                if (device == null) {
                    return;
                }
                byte state = device.getUpgradeState();
                if (state == 0) {
                    if (mUpgradeDialog != null) {
                        mUpgradeDialog.dismiss();
                    }
                }
                if (state == 1) {
                    showUpgradeProgress();
                    return;
                }
                if (state == 2) {
                    mUpgradeDialog.setMessage("Upgrade success.");
                    mUpgradeDialog.setResult(true);
                    return;
                }
                if (state < 0) {
                    mUpgradeDialog.setMessage("Upgrade failed, error code: " + state);
                    mUpgradeDialog.setResult(false);
                }
            }
        });

        if (mDevice != null) {
            @DrawableRes int[] wifi_signals = new int[] {R.drawable.ic_signal_wifi_0_bar_white_24dp,
                                                         R.drawable.ic_signal_wifi_1_bar_white_24dp,
                                                         R.drawable.ic_signal_wifi_2_bar_white_24dp,
                                                         R.drawable.ic_signal_wifi_3_bar_white_24dp,
                                                         R.drawable.ic_signal_wifi_4_bar_white_24dp};
            device_detail_delete.setEnabled(mDevice.getXDevice().getRole() == 0);
            getDeviceDatetime();
            String name = mDevice.getXDevice().getDeviceName();
            if (TextUtils.isEmpty(name)) {
                name = DeviceUtil.getDefaultName(mDevice.getXDevice().getProductId());
            }
            int devid = mDevice.getXDevice().getDeviceId();
            String mac = mDevice.getXDevice().getMacAddress();
            String fwversion = mDevice.getXDevice().getFirmwareVersion();
            device_detail_name.setText(name);
            device_detail_zone.setText(getTimezoneDesc(mDevice.getZone()));
            if (mDevice.getRssi() < 0) {
                device_detail_rssi.setText("" + mDevice.getRssi() + " dB");
                int level = WifiManager.calculateSignalLevel(mDevice.getRssi(), 5);
                device_detail_rssi.setCompoundDrawablesWithIntrinsicBounds(0, 0, wifi_signals[level], 0);
            }
            device_detail_cloudzone.setText("Cloud Zone: " + getTimezoneDesc(mDevice.getCloudZone()));
            device_detail_devid.setText(String.valueOf(devid));
            device_detail_mac.setText(mac);
            device_detail_fwversion.setText(fwversion);


            XlinkCloudManager.getInstance().getDeviceLocation(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceGeographyResponse>() {
                @Override
                public void onError(String error) {

                }

                @Override
                public void onSuccess(DeviceApi.DeviceGeographyResponse response) {
                    device_detail_location.setText(response.country + "/" + response.province + "/" + response.city);
                }
            });
        }
    }

    @Override
    protected void initEvent() {
        device_detail_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        device_detail_ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null) {
                    return;
                }
                showRenameDialog();
            }
        });
        device_detail_ll_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                addFragmentToStack(R.id.device_root, new LocationFragment());
            }
        });
        device_detail_habitat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.device_root, new DeviceZonesFragment());
            }
        });
        device_detail_user_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null) {
                    return;
                }
                addFragmentToStack(R.id.device_root, UserListFragment.newInstance(mDevice.getXDevice().getDeviceId()));
            }
        });
        device_detail_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null) {
                    return;
                }
                getNewestVersion();
            }
        });
        device_detail_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null) {
                    return;
                }
                showDeleteDialog();
            }
        });
    }

    private String getTimezoneDesc(int zone) {
        DecimalFormat df = new DecimalFormat("00");
        String zoneDesc = "GMT+";
        if (zone < 0) {
            zoneDesc = "GMT-";
            zone = -zone;
        }
        zoneDesc = zoneDesc + df.format(zone/100) + ":" + df.format(zone%100);
        return zoneDesc;
    }

    private void deleteDevice() {
        final String homeid = Home2Manager.getInstance().getCurrentHomeId();
        final String roomid = mDeviceViewModel.getRoomId();
        final int devid = mDevice.getXDevice().getDeviceId();
        XlinkCloudManager.getInstance().deleteDeviceFromHome(homeid, devid, new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                XlinkCloudManager.getInstance().deleteRoom(homeid, roomid, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        DeviceManager.getInstance().removeDevice(mDevice);
                        EventBus.getDefault().post(new SubscribeChangedEvent());
                        getActivity().finish();
                    }
                });
            }
        });
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

    private void showUpgradeProgress() {
        if (mUpgradeDialog == null) {
            mUpgradeDialog = new UpdateDialog(getContext());
        }
        mUpgradeDialog.show();
    }

    private void showUpgradeDialog(@NonNull final DeviceApi.DeviceNewestVersionResponse response) {
        Log.e(TAG, "showUpgradeDialog: " + response.current + " " + response.newest + " " + response.description);
        int current = Integer.parseInt(response.current);
        final int newest = Integer.parseInt(response.newest);
        if (current >= newest) {
            new MessageDialog(getContext(), true).setTitle(R.string.upgrade)
                                                 .setMessage(R.string.device_firmware_uptodate)
                                                 .setButton(R.string.ok, null)
                                                 .show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.upgrade)
                   .setMessage(getString(R.string.device_upgrade_msg, current, newest))
                   .setNegativeButton(R.string.cancel, null)
                   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           XlinkCloudManager.getInstance().upgradeDevice(mDevice.getXDevice(), new XlinkRequestCallback<String>() {
                               @Override
                               public void onError(String error) {
                                   Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                        .show();
                               }

                               @Override
                               public void onSuccess(String s) {
                                   showUpgradeProgress();
                               }
                           });
                       }
                   })
                   .setCancelable(false)
                   .show();
        }
    }

    private void getNewestVersion() {
        XlinkCloudManager.getInstance().getNewsetVersion(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceNewestVersionResponse>() {
            @Override
            public void onError(final String error) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }
                });
            }

            @Override
            public void onSuccess(DeviceApi.DeviceNewestVersionResponse response) {
                showUpgradeDialog(response);
            }
        });
    }

    private void getDeviceDatetime() {
        if (mDevice == null) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        ids.add(mDevice.getDeviceDatetimeIndex());
        XlinkCloudManager.getInstance().probeDevice(mDevice.getXDevice(), ids, new XlinkTaskCallback<List<XLinkDataPoint>>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onComplete(List<XLinkDataPoint> dataPoints) {
                if (isDestroyed) {
                    return;
                }
                for (XLinkDataPoint dp : dataPoints) {
                    mDevice.setDataPoint(dp);
                }
                String time = mDevice.getDeviceDatetime();
                DateFormat df = new SimpleDateFormat(DEVICE_DATE_FORMAT);
                try {
                    Date date = df.parse(time);
                    if (date != null) {
                        mCurrentTime = date.getTime();
                        mTimer.scheduleAtFixedRate(mTask, 0, 1000);
                    } else {
                        device_detail_datetime.setText(time);
                    }
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showRenameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.device_name));
        et_name.setText(mDevice.getXDevice().getDeviceName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(DeviceApi.DeviceResponse response) {
                        dialog.dismiss();
                        device_detail_name.setText(response.name);
                        EventBus.getDefault().post(new DevicePropertyChangedEvent(response.id, response.name));
                    }
                });
            }
        });
    }

    private final class UpdateDialog {
        private AlertDialog mDialog;
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