package com.inledco.exoterra.device;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.DeviceBaseViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class LocationFragment extends BaseFragment {
    private Toolbar location_toolbar;
    private TextInputEditText location_longitude;
    private TextInputEditText location_latitude;
    private ImageButton location_position;

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
        hideSoftKeyboard();
        super.onDestroyView();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_location;
    }

    @Override
    protected void initView(View view) {
        location_toolbar = view.findViewById(R.id.location_toolbar);
        location_longitude = view.findViewById(R.id.location_longitude);
        location_latitude = view.findViewById(R.id.location_latitude);
        location_position = view.findViewById(R.id.location_position);

        location_toolbar.inflateMenu(R.menu.menu_save);
    }

    @Override
    protected void initData() {
        mDeviceViewModel = ViewModelProviders.of(getActivity()).get(DeviceBaseViewModel.class);
        mDevice = mDeviceViewModel.getData();
//        if (mDevice != null) {
//            location_longitude.setText("" + mDevice.getLongitude());
//            location_latitude.setText("" + mDevice.getLatitude());
//        }
    }

    @Override
    protected void initEvent() {
        location_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        location_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_save) {
                    saveLocation();
                }
                return false;
            }
        });
        location_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mDevice != null) {
//                    XlinkCloudManager.getInstance().getDeviceLocation(mDevice.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceGeographyResponse>() {
//                        @Override
//                        public void onError(String error) {
//
//                        }
//
//                        @Override
//                        public void onSuccess(DeviceApi.DeviceGeographyResponse response) {
//                            location_longitude.setText("" + (float) response.lon);
//                            location_latitude.setText("" + (float) response.lat);
//                        }
//                    });
//                }
            }
        });
    }

    private void hideSoftKeyboard() {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && manager.isActive()) {
            manager.hideSoftInputFromWindow(location_longitude.getApplicationWindowToken(), 0);
        }
    }

    private float getLongitudeValue() {
        String s = location_longitude.getText().toString();
        return Float.parseFloat(s);
    }

    private float getLatitudeValue() {
        String s = location_latitude.getText().toString();
        return Float.parseFloat(s);
    }

    private void saveLocation() {
//        if (mDevice != null) {
//            float lon = getLongitudeValue();
//            if (lon < -180 || lon > 180) {
//                location_longitude.setError(getString(R.string.error_longitude));
//                return;
//            }
//            float lat = getLatitudeValue();
//            if (lat < -60 || lat > 60) {
//                location_latitude.setError(getString(R.string.error_latitude));
//                return;
//            }
//            XLinkDataPoint dp1 = mDevice.setLongitude(lon);
//            XLinkDataPoint dp2 = mDevice.setLatitude(lat);
//            if (dp1 != null && dp2 != null) {
//                List<XLinkDataPoint> dps = new ArrayList<>();
//                dps.add(dp1);
//                dps.add(dp2);
//                XlinkCloudManager.getInstance().setDeviceDatapoints(mDevice.getXDevice(), dps, new XlinkTaskCallback<XDevice>() {
//                    @Override
//                    public void onError(String error) {
//                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                             .show();
//                    }
//
//                    @Override
//                    public void onComplete(XDevice device) {
//                        getActivity().getSupportFragmentManager().popBackStack();
//                    }
//                });
//            }
//        }
    }
}
