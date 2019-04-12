package com.liruya.exoterra.device;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.AppConstants;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.manager.DeviceManager;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.restful.api.app.DeviceApi;
import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;

public class DeviceSetFragment extends BaseFragment {
    private Toolbar device_set_toolbar;
    private LinearLayout device_set_ll;
    private TextView device_set_timezone;
    private TextInputEditText device_set_longitude;
    private TextInputEditText device_set_latitude;
    private ImageButton device_set_position;

    private Device mDevice;
    private int mZone;
    private float mLongitude;
    private float mLatitude;

    public static DeviceSetFragment newInstance(String deviceTag) {
        Bundle args = new Bundle();
        args.putString(AppConstants.DEVICE_TAG, deviceTag);
        DeviceSetFragment fragment = new DeviceSetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_device_set;
    }

    @Override
    protected void initView(View view) {
        device_set_toolbar = view.findViewById(R.id.device_set_toolbar);
        device_set_ll = view.findViewById(R.id.device_set_ll);
        device_set_timezone = view.findViewById(R.id.device_set_timezone);
        device_set_longitude = view.findViewById(R.id.device_set_longitude);
        device_set_latitude = view.findViewById(R.id.device_set_latitude);
        device_set_position = view.findViewById(R.id.device_set_position);

        device_set_toolbar.inflateMenu(R.menu.menu_deviceset);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            String deviceTag = args.getString(AppConstants.DEVICE_TAG);
            mDevice = DeviceManager.getInstance().getDevice(deviceTag);
            if (mDevice != null) {
                int zone = mDevice.getZone();
                mZone = zone/100*60+zone%100;
                mLongitude = mDevice.getLongitude();
                mLatitude = mDevice.getLatitude();
                refreshData();
            }
        }
    }

    @Override
    protected void initEvent() {
        device_set_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        device_set_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_deviceset_save) {
                    saveDeviceSet();
                }
                return false;
            }
        });
        device_set_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                TimeZoneFragment frag = new TimeZoneFragment();
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                             .add(R.id.device_root, frag)
                             .addToBackStack("")
                             .commit();
            }
        });
        device_set_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mZone = TimeZone.getDefault().getRawOffset()/60000;
                if (mDevice != null) {
                    XlinkCloudManager.getInstance().getDeviceLocation(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceGeographyResponse>() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onError(String error) {

                        }

                        @Override
                        public void onSuccess(DeviceApi.DeviceGeographyResponse response) {
                            mLongitude = (float) response.lon;
                            mLatitude = (float) response.lat;
                            refreshData();
                        }
                    });
                }
            }
        });
    }

    private void refreshData() {
        if (mDevice == null) {
            return;
        }
        device_set_timezone.setText(getTimezoneDesc(mZone));
        device_set_longitude.setText("" + mLongitude);
        device_set_latitude.setText("" + mLatitude);
    }

    private void hideSoftKeyboard() {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && manager.isActive()) {
            manager.hideSoftInputFromWindow(device_set_longitude.getApplicationWindowToken(), 0);
        }
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

    private float getLongitudeValue() {
        String s = device_set_longitude.getText().toString();
        return Float.parseFloat(s);
    }

    private float getLatitudeValue() {
        String s = device_set_latitude.getText().toString();
        return Float.parseFloat(s);
    }

    private void saveDeviceSet() {
        if (mDevice != null) {
            float lon = getLongitudeValue();
            if (lon < -180 || lon > 180) {
                device_set_longitude.setError(getString(R.string.error_longitude));
                return;
            }
            float lat = getLatitudeValue();
            if (lat < -60 || lat > 60) {
                device_set_latitude.setError(getString(R.string.error_latitude));
                return;
            }
            mLongitude = lon;
            mLatitude = lat;
            int zone = (mZone/60)*100 + (mZone%60);
            XLinkDataPoint dp1 = mDevice.setZone((short) zone);
            XLinkDataPoint dp2 = mDevice.setLongitude(mLongitude);
            XLinkDataPoint dp3 = mDevice.setLatitude(mLatitude);
            if (dp1 != null && dp2 != null && dp3 != null) {
                List<XLinkDataPoint> dps = new ArrayList<>();
                dps.add(dp1);
                dps.add(dp2);
                dps.add(dp3);
                XlinkCloudManager.getInstance().setDeviceDatapoints(mDevice.getXDevice(), dps, new XLinkTaskListener<XDevice>() {
                    @Override
                    public void onError(XLinkCoreException e) {
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(XDevice device) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        }
    }
}
