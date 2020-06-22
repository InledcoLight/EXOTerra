package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.util.SensorUtil;

public class SocketFragment extends BaseFragment {
    private LinearLayout socket_sensor_group;
    private TextView socket_sensor1;
    private TextView socket_sensor2;
//    private ImageButton socket_power;
//    private ImageButton socket_ib_back;
//    private CheckedTextView socket_ctv_timer;
//    private CheckedTextView socket_ctv_thermostat;
//    private CheckedTextView socket_ctv_hygrostat;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;

    private int currMode = -1;

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
        return R.layout.fragment_socket;
    }

    @Override
    protected void initView(View view) {
        socket_sensor_group = view.findViewById(R.id.socket_sensor_group);
        socket_sensor1 = view.findViewById(R.id.socket_sensor1);
        socket_sensor2 = view.findViewById(R.id.socket_sensor2);
//        socket_power = view.findViewById(R.id.socket_power);
//        socket_ib_back = view.findViewById(R.id.socket_ib_back);
//        socket_ctv_timer = view.findViewById(R.id.socket_ctv_timer);
//        socket_ctv_thermostat = view.findViewById(R.id.socket_ctv_thermostat);
//        socket_ctv_hygrostat = view.findViewById(R.id.socket_ctv_hygrostat);
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

        refreshData();
    }

    @Override
    protected void initEvent() {
        socket_sensor_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToStack(R.id.device_root, new SensorHistoryFragment());
            }
        });

//        socket_ctv_timer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!socket_ctv_timer.isChecked()) {
//                    mSocketViewModel.setMode(ExoSocket.MODE_TIMER);
//                }
//            }
//        });
//
//        socket_ctv_thermostat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!socket_ctv_thermostat.isChecked()) {
//                    mSocketViewModel.setMode(ExoSocket.MODE_SENSOR1);
//                }
//            }
//        });
//
//        socket_ctv_hygrostat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!socket_ctv_hygrostat.isChecked()) {
//                    mSocketViewModel.setMode(ExoSocket.MODE_SENSOR2);
//                }
//            }
//        });
    }

    private void refreshData() {
        if (mSocket == null) {
            return;
        }

        ExoSocket.Sensor[] sensors = mSocket.getSensor();
        if (mSocket.getSensorAvailable() && sensors != null && sensors.length > 0 && sensors.length <= ExoSocket.SENSOR_COUNT_MAX) {
            ExoSocket.Sensor sensor1 = sensors[0];
            int s1type = sensor1.getType();
            int s1value = sensor1.getValue();
            socket_sensor1.setText(SensorUtil.getSensorValueText(s1value, s1type) + "\n" + SensorUtil.getSensorUnit(s1type));
            if (sensors.length == ExoSocket.SENSOR_COUNT_MAX) {
                ExoSocket.Sensor sensor2 = sensors[1];
                int s2type = sensor2.getType();
                int s2value = sensor2.getValue();
                socket_sensor2.setText(SensorUtil.getSensorValueText(s2value, s2type) + "\n" + SensorUtil.getSensorUnit(s2type));
            } else {
                socket_sensor2.setText(null);
            }
        } else {
            socket_sensor1.setText(null);
            socket_sensor2.setText(null);
        }
//        socket_power.setImageResource(mSocket.getPower() ? R.drawable.ic_power_blue_48dp : R.drawable.ic_power_red_48dp);

//        socket_ctv_thermostat.setEnabled(mSocket.getSensorAvailable() && sensors != null && sensors.length > 0);
//        socket_ctv_hygrostat.setEnabled(mSocket.getSensorAvailable() && sensors != null && sensors.length > 1);
//        if (!socket_ctv_timer.isChecked() && mSocket.getMode() == ExoSocket.MODE_TIMER) {
//            socket_ctv_timer.setChecked(true);
//            socket_ctv_thermostat.setChecked(false);
//            socket_ctv_hygrostat.setChecked(false);
//            replaceFragment(R.id.socket_fl, new SocketTimersFragment());
//        } else if (!socket_ctv_thermostat.isChecked() && mSocket.getMode() == ExoSocket.MODE_SENSOR1) {
//            socket_ctv_timer.setChecked(false);
//            socket_ctv_thermostat.setChecked(true);
//            socket_ctv_hygrostat.setChecked(false);
//            replaceFragment(R.id.socket_fl, new SocketSensorFragment());
//        } else if (!socket_ctv_hygrostat.isChecked() && mSocket.getMode() == ExoSocket.MODE_SENSOR2) {
//            socket_ctv_timer.setChecked(false);
//            socket_ctv_thermostat.setChecked(false);
//            socket_ctv_hygrostat.setChecked(true);
//            replaceFragment(R.id.socket_fl, new SocketSensorFragment());
//        }
        int newMode = mSocket.getMode();
        switch (newMode) {
            case ExoSocket.MODE_TIMER:
                if (currMode != newMode) {
                    replaceFragment(R.id.socket_fl, new SocketTimersFragment());
                    currMode = newMode;
                }
                break;
            case ExoSocket.MODE_SENSOR1:
            case ExoSocket.MODE_SENSOR2:
                if (currMode != newMode) {
                    replaceFragment(R.id.socket_fl, new SocketSensorFragment());
                    currMode = newMode;
                }
                break;
        }
    }
}
