package com.liruya.exoterra.device.socket;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.AppConstants;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOSocket;

public class SocketControlFragment extends BaseFragment {

    private LinearLayout socket_ll_sensor;
    private ImageView socket_iv_sensor;
    private TextView socket_tv_s1value;
    private TextView socket_tv_s2value;
    private ImageButton socket_ib_settings;
    private CheckableImageButton socket_cib_power;

    private SocketViewModel mSocketViewModel;
    private EXOSocket mSocket;

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
        return R.layout.fragment_socket_control;
    }

    @Override
    protected void initView(View view) {
        socket_ll_sensor = view.findViewById(R.id.socket_ll_sensor);
        socket_iv_sensor = view.findViewById(R.id.socket_iv_sensor);
        socket_tv_s1value = view.findViewById(R.id.socket_tv_s1value);
        socket_tv_s2value = view.findViewById(R.id.socket_tv_s2value);
        socket_ib_settings = view.findViewById(R.id.socket_ib_setting);
        socket_cib_power = view.findViewById(R.id.socket_cib_power);
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<EXOSocket>() {
            @Override
            public void onChanged(@Nullable EXOSocket exoSocket) {
                refreshData();
            }
        });
    }

    @Override
    protected void initEvent() {
        socket_ib_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        socket_cib_power.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                mSocketViewModel.setPower(!socket_cib_power.isChecked());
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    private void refreshData() {
        if (mSocket == null) {
            return;
        }
        Log.e(TAG, "refreshData: " + mSocket.getS1Available());
        if (mSocket.getS1Available()) {
            socket_ll_sensor.setVisibility(View.VISIBLE);
            socket_iv_sensor.setImageResource(getSensorIcon(mSocket.getS1Type(), mSocket.getS2Type()));
            socket_tv_s1value.setText(getSensorValueText(mSocket.getS1Type(), mSocket.getS1Value()));
            if (mSocket.getS2Available()) {
                socket_tv_s2value.setVisibility(View.VISIBLE);
                socket_tv_s2value.setText(getSensorValueText(mSocket.getS2Type(), mSocket.getS2Value()));
            } else {
                socket_tv_s2value.setVisibility(View.GONE);
            }
        } else {
            socket_ll_sensor.setVisibility(View.INVISIBLE);
        }

        socket_cib_power.setChecked(mSocket.getPower());
    }

    private String getSensorValueText(int type, int value) {
        String text = "";
        switch (type) {
            case AppConstants.SENSOR_TYPE_REPTILE_TEMPERATURE:
                text = "" + (value/10) + "." + (value%10) + " ℃";
                break;
            case AppConstants.SENSOR_TYPE_REPTILE_HUMIDITY:
                text = "" + (value/10) + "." + (value%10) + " %RH";
                break;
        }
        return text;
    }

    private @DrawableRes int getSensorIcon(int type1, int type2) {
        if (type2 == AppConstants.SENSOR_TYPE_NONE) {
            if (type1 == AppConstants.SENSOR_TYPE_REPTILE_TEMPERATURE) {
                return R.drawable.ic_temperature;
            }
            if (type1 == AppConstants.SENSOR_TYPE_REPTILE_HUMIDITY) {
                return R.drawable.ic_humidity;
            }
        }
        if (type1 == AppConstants.SENSOR_TYPE_REPTILE_TEMPERATURE && type2 == AppConstants.SENSOR_TYPE_REPTILE_HUMIDITY) {
            return R.drawable.ic_temperature_humidity;
        }
        return R.drawable.ic_temperature;
    }
}
