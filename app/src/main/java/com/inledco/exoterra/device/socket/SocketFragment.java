package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOSocket;

public class SocketFragment extends BaseFragment {
    private TextView socket_sensor1;
    private TextView socket_sensor2;
    private ImageButton socket_power;
    private ImageButton socket_ib_back;
    private CheckedTextView socket_ctv_timer;
    private CheckedTextView socket_ctv_thermostat;
    private CheckedTextView socket_ctv_hygrostat;

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
        return R.layout.fragment_socket;
    }

    @Override
    protected void initView(View view) {
        socket_sensor1 = view.findViewById(R.id.socket_sensor1);
        socket_sensor2 = view.findViewById(R.id.socket_sensor2);
        socket_power = view.findViewById(R.id.socket_power);
        socket_ib_back = view.findViewById(R.id.socket_ib_back);
        socket_ctv_timer = view.findViewById(R.id.socket_ctv_timer);
        socket_ctv_thermostat = view.findViewById(R.id.socket_ctv_thermostat);
        socket_ctv_hygrostat = view.findViewById(R.id.socket_ctv_hygrostat);
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

        refreshData();
    }

    @Override
    protected void initEvent() {
        socket_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocketViewModel.setPower(!mSocket.getPower());
            }
        });

        socket_ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        socket_ctv_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!socket_ctv_timer.isChecked()) {
                    mSocketViewModel.setMode(EXOSocket.MODE_TIMER);
                }
            }
        });

        socket_ctv_thermostat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!socket_ctv_thermostat.isChecked()) {
                    mSocketViewModel.setMode(EXOSocket.MODE_SENSOR1);
                }
            }
        });

        socket_ctv_hygrostat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!socket_ctv_hygrostat.isChecked()) {
                    mSocketViewModel.setMode(EXOSocket.MODE_SENSOR2);
                }
            }
        });
    }

    private void refreshData() {
        if (mSocket == null) {
            return;
        }
        socket_sensor1.setText(mSocket.getS1Available() ? ((float)mSocket.getS1Value()/10) + "\n℃" : null);
        socket_sensor2.setText(mSocket.getS2Available() ? ((float)mSocket.getS2Value()/10) + "\n%" : null);
        socket_power.setImageResource(mSocket.getPower() ? R.drawable.ic_power_blue_48dp : R.drawable.ic_power_red_48dp);

        socket_ctv_thermostat.setEnabled(mSocket.getS1Available());
        socket_ctv_hygrostat.setEnabled(mSocket.getS2Available());
        if (!socket_ctv_timer.isChecked() && mSocket.getMode() == EXOSocket.MODE_TIMER) {
            socket_ctv_timer.setChecked(true);
            socket_ctv_thermostat.setChecked(false);
            socket_ctv_hygrostat.setChecked(false);
            replaceFragment(R.id.socket_fl, new SocketTimersFragment());
        } else if (!socket_ctv_thermostat.isChecked() && mSocket.getMode() == EXOSocket.MODE_SENSOR1) {
            socket_ctv_timer.setChecked(false);
            socket_ctv_thermostat.setChecked(true);
            socket_ctv_hygrostat.setChecked(false);
            replaceFragment(R.id.socket_fl, new SocketSensorFragment());
        } else if (!socket_ctv_hygrostat.isChecked() && mSocket.getMode() == EXOSocket.MODE_SENSOR2) {
            socket_ctv_timer.setChecked(false);
            socket_ctv_thermostat.setChecked(false);
            socket_ctv_hygrostat.setChecked(true);
            replaceFragment(R.id.socket_fl, new SocketSensorFragment());
        }
    }
}
