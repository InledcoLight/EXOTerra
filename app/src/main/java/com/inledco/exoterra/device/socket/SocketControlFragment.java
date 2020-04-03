package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class SocketControlFragment extends BaseFragment {

    private LinearLayout socket_control_ll;
    private LinearLayout socket_control_ll2;
    private View socket_control_div;
    private TextView socket_control_s1value;
    private TextView socket_control_s2value;
    private ImageButton socket_control_s1notify;
    private ImageButton socket_control_s1linkage;
    private ImageButton socket_control_s2notify;
    private AppCompatImageButton socket_control_power;
    private TextView socket_control_status;
    private TextView socket_control_life;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;

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
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_socket_control;
    }

    @Override
    protected void initView(View view) {
        socket_control_ll = view.findViewById(R.id.socket_control_ll);
        socket_control_ll2 = view.findViewById(R.id.socket_control_ll2);
        socket_control_div = view.findViewById(R.id.socket_control_div);
        socket_control_s1value = view.findViewById(R.id.socket_control_s1value);
        socket_control_s1notify = view.findViewById(R.id.socket_control_s1notify);
        socket_control_s1linkage = view.findViewById(R.id.socket_control_s1linkage);
        socket_control_s2value = view.findViewById(R.id.socket_control_s2value);
        socket_control_s2notify = view.findViewById(R.id.socket_control_s2notify);
        socket_control_power = view.findViewById(R.id.socket_control_power);
        socket_control_status = view.findViewById(R.id.socket_control_status);
        socket_control_life = view.findViewById(R.id.socket_control_life);
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<ExoSocket>() {
            @Override
            public void onChanged(@Nullable ExoSocket exoSocket) {
                refreshData();
            }
        });
    }

    @Override
    protected void initEvent() {
//        socket_control_ll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        socket_control_s1linkage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addFragmentToStack(R.id.device_root, new TemperatureLinkageFragment());
//            }
//        });
//
//        socket_control_s1notify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (mSocket.getS1Type()) {
//                    case AppConstants.SENSOR_TYPE_REPTILE_TEMPERATURE:
//                        addFragmentToStack(R.id.device_root, NotifyFragment.newInstance(getString(R.string.temperature), false, 10, 40, "℃"));
//                        break;
//                }
//            }
//        });
//
//        socket_control_s2notify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (mSocket.getS2Type()) {
//                    case AppConstants.SENSOR_TYPE_REPTILE_HUMIDITY:
//                        addFragmentToStack(R.id.device_root, NotifyFragment.newInstance(getString(R.string.humidity), true, 20, 80, "%RH"));
//                        break;
//                }
//            }
//        });
//
//        socket_control_power.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mSocketViewModel.setPower(!mSocket.getPower());
//            }
//        });
    }

    private void refreshData() {
//        if (mSocket == null) {
//            return;
//        }
//        if (mSocket.getS1Available()) {
//            socket_control_ll.setVisibility(View.VISIBLE);
//            socket_control_s1value.setCompoundDrawablesRelativeWithIntrinsicBounds(getSensorIcon(mSocket.getS1Type()), 0, 0, 0);
//            socket_control_s1value.setText(getSensorValueText(mSocket.getS1Type(), mSocket.getS1Value()));
//            boolean available2 = mSocket.getS2Available();
//            if (available2) {
//                socket_control_s2value.setCompoundDrawablesRelativeWithIntrinsicBounds(getSensorIcon(mSocket.getS2Type()), 0, 0, 0);
//                socket_control_s2value.setText(getSensorValueText(mSocket.getS2Type(), mSocket.getS2Value()));
//            }
//            socket_control_div.setVisibility(available2 ? View.VISIBLE : View.GONE);
//            socket_control_ll2.setVisibility(available2 ? View.VISIBLE : View.GONE);
//        } else {
//            socket_control_ll.setVisibility(View.INVISIBLE);
//        }
//
//        Log.e(TAG, "refreshData: " + mSocket.getPower());
//        socket_control_power.setImageResource(mSocket.getPower() ? R.drawable.ic_power_blue : R.drawable.ic_power_red);
//        socket_control_status.setText(getString(R.string.current_status) + (mSocket.getPower() ? getString(R.string.on) : getString(R.string.off)));
//        socket_control_life.setText("" + mSocket.getSwitchCount() + "/" + mSocket.getSwitchCountMax());
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

    private @DrawableRes int getSensorIcon(int type) {
        if (type == AppConstants.SENSOR_TYPE_REPTILE_TEMPERATURE) {
            return R.drawable.ic_temperature;
        }
        if (type == AppConstants.SENSOR_TYPE_REPTILE_HUMIDITY) {
            return R.drawable.ic_humidity;
        }
        return R.drawable.ic_temperature;
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
