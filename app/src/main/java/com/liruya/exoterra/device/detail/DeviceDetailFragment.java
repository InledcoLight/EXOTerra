package com.liruya.exoterra.device.detail;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.BaseViewModel;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.event.SubscribeChangedEvent;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.util.DeviceUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

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
import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;

public class DeviceDetailFragment extends BaseFragment {
    private final String DEVICE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Toolbar device_detail_toolbar;
    private LinearLayout device_detail_ll_name;
    private LinearLayout device_detail_ll_datetime;
    private TextView device_detail_name;
    private TextView device_detail_zone;
    private TextView device_detail_datetime;
    private TextView device_detail_user_list;
    private TextView device_detail_home;
    private TextView device_detail_devid;
    private TextView device_detail_mac;
    private TextView device_detail_upgrade;
    private TextView device_detail_fwversion;
    private Button device_detail_unsubscribe;

    private long mCurrentTime;
    private final Timer mTimer = new Timer();
    private final TimerTask mTask = new TimerTask() {
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

    private BaseViewModel<Device> mDeviceViewModel;
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
        device_detail_ll_datetime = view.findViewById(R.id.device_detail_ll_datetime);
        device_detail_name = view.findViewById(R.id.device_detail_name);
        device_detail_zone = view.findViewById(R.id.device_detail_zone);
        device_detail_datetime = view.findViewById(R.id.device_detail_datetime);
        device_detail_user_list = view.findViewById(R.id.device_detail_user_list);
        device_detail_home = view.findViewById(R.id.device_detail_home);
        device_detail_devid = view.findViewById(R.id.device_detail_devid);
        device_detail_mac = view.findViewById(R.id.device_detail_mac);
        device_detail_fwversion = view.findViewById(R.id.device_detail_fwversion);
        device_detail_upgrade = view.findViewById(R.id.device_detail_upgrade);
        device_detail_unsubscribe = view.findViewById(R.id.device_detail_unsubscribe);

        device_detail_user_list.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
        device_detail_upgrade.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_white_32dp, 0);
    }

    @Override
    protected void initData() {
        mDeviceViewModel = ViewModelProviders.of(getActivity()).get(BaseViewModel.class);
        mDevice = mDeviceViewModel.getData();
        if (mDevice != null) {
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
            device_detail_devid.setText(String.valueOf(devid));
            device_detail_mac.setText(mac);
            device_detail_fwversion.setText(fwversion);
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
        device_detail_ll_datetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null) {
                    return;
                }
            }
        });
        device_detail_user_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUserListFragment();
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
        device_detail_unsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice == null) {
                    return;
                }
                XlinkCloudManager.getInstance().unsubscribeDevice(mDevice.getXDevice(), new XLinkTaskListener<String>() {
                    @Override
                    public void onError(XLinkCoreException e) {
                        Toast.makeText(getContext(), "Unsubscribe failed", Toast.LENGTH_LONG)
                             .show();
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(String s) {
                        Toast.makeText(getContext(), "Unsubscribe success", Toast.LENGTH_LONG)
                             .show();
                        DeviceManager.getInstance().removeDevice(mDevice);
                        EventBus.getDefault().post(new SubscribeChangedEvent());
                        getActivity().finish();
                    }
                });
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

    private void getNewestVersion() {
        XlinkCloudManager.getInstance().getNewsetVersion(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceNewestVersionResponse>() {
            @Override
            public void onStart() {

            }

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
                int current = Integer.parseInt(response.current);
                int newest = Integer.parseInt(response.newest);
                if (current >= newest) {
                    Toast.makeText(getContext(), R.string.device_firmware_uptodate, Toast.LENGTH_SHORT)
                         .show();
                } else {
                    XlinkCloudManager.getInstance().upgradeDevice(mDevice.getXDevice(), new XlinkRequestCallback<String>() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                 .show();
                        }

                        @Override
                        public void onSuccess(String s) {
                            Toast.makeText(getContext(), "Upgrade start success.", Toast.LENGTH_SHORT)
                                 .show();
                        }
                    });
                }
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
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
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
                    public void onStart() {

                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
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

    private void gotoUserListFragment() {
        if (mDevice == null) {
            return;
        }
        getActivity().getSupportFragmentManager()
                     .beginTransaction()
                     .add(R.id.device_root, UserListFragment.newInstance(mDevice.getXDevice().getDeviceId()))
                     .addToBackStack("")
                     .commit();
    }
}
