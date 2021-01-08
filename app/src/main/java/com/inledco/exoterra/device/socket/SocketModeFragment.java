package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class SocketModeFragment extends BaseFragment {
    private CheckedTextView socket_mode_timer;
    private CheckedTextView socket_mode_thermostat;
    private CheckedTextView socket_mode_hygrostat;
    private CheckedTextView socket_mode_power;

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
    protected int getLayoutRes() {
        return R.layout.fragment_socket_mode;
    }

    @Override
    protected void initView(View view) {
        socket_mode_timer = view.findViewById(R.id.socket_mode_timer);
        socket_mode_thermostat = view.findViewById(R.id.socket_mode_thermostat);
        socket_mode_hygrostat = view.findViewById(R.id.socket_mode_hygrostat);
        socket_mode_power = view.findViewById(R.id.socket_mode_power);
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, exoSocket -> refreshData());
        refreshData();
    }

    @Override
    protected void initEvent() {
        socket_mode_timer.setOnClickListener(v -> {
            if (!socket_mode_timer.isChecked()) {
                mSocketViewModel.setMode(ExoSocket.MODE_TIMER);
            }
        });

        socket_mode_thermostat.setOnClickListener(v -> {
            if (!socket_mode_thermostat.isChecked()) {
                mSocketViewModel.setMode(ExoSocket.MODE_SENSOR1);
            }
        });

        socket_mode_hygrostat.setOnClickListener(v -> {
            if (!socket_mode_hygrostat.isChecked()) {
                mSocketViewModel.setMode(ExoSocket.MODE_SENSOR2);
            }
        });

        socket_mode_power.setOnClickListener(v -> mSocketViewModel.setPower(!socket_mode_power.isChecked()));
    }

    private void refreshData() {
        if (mSocket == null) {
            return;
        }

        ExoSocket.Sensor[] sensors = mSocket.getSensor();
        socket_mode_thermostat.setEnabled(mSocket.getSensorAvailable() && sensors != null && sensors.length > 0);
        socket_mode_hygrostat.setEnabled(mSocket.getSensorAvailable() && sensors != null && sensors.length > 1);
        if (!socket_mode_timer.isChecked() && mSocket.getMode() == ExoSocket.MODE_TIMER) {
            socket_mode_timer.setChecked(true);
            socket_mode_thermostat.setChecked(false);
            socket_mode_hygrostat.setChecked(false);
        } else if (!socket_mode_thermostat.isChecked() && mSocket.getMode() == ExoSocket.MODE_SENSOR1) {
            socket_mode_timer.setChecked(false);
            socket_mode_thermostat.setChecked(true);
            socket_mode_hygrostat.setChecked(false);
        } else if (!socket_mode_hygrostat.isChecked() && mSocket.getMode() == ExoSocket.MODE_SENSOR2) {
            socket_mode_timer.setChecked(false);
            socket_mode_thermostat.setChecked(false);
            socket_mode_hygrostat.setChecked(true);
        }

        socket_mode_power.setChecked(mSocket.getPower());
        socket_mode_power.setText(mSocket.getPower() ? R.string.on : R.string.off);
    }
}
