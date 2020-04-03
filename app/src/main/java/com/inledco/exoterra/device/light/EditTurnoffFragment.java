package com.inledco.exoterra.device.light;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TimePicker;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class EditTurnoffFragment extends BaseFragment {

    private Switch turnoff_enable;
    private TimePicker turnoff_time;
    private Button turnoff_cancel;
    private Button turnoff_save;

    private LightViewModel mLightViewModel;
    private ExoLed mLight;

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
        return R.layout.dialog_edit_turnoff;
    }

    @Override
    protected void initView(View view) {
        turnoff_enable = view.findViewById(R.id.dialog_turnoff_enable);
        turnoff_time = view.findViewById(R.id.dialog_turnoff_time);
        turnoff_cancel = view.findViewById(R.id.btn_cancel);
        turnoff_save = view.findViewById(R.id.btn_save);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        if (mLight == null) {
            return;
        }

        turnoff_enable.setChecked(mLight.getTurnoffEnable());
        turnoff_time.setIs24HourView(GlobalSettings.is24HourFormat());
        final int turnoffTime = mLight.getTurnoffTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            turnoff_time.setHour(turnoffTime / 60);
            turnoff_time.setMinute(turnoffTime % 60);
        }
        else {
            turnoff_time.setCurrentHour(turnoffTime / 60);
            turnoff_time.setCurrentMinute(turnoffTime % 60);
        }
    }

    @Override
    protected void initEvent() {
        turnoff_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        turnoff_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enable = turnoff_enable.isChecked();
                int time;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    time = turnoff_time.getHour() * 60 + turnoff_time.getMinute();
                }
                else {
                    time = turnoff_time.getCurrentHour() * 60 + turnoff_time.getCurrentMinute();
                }
                mLightViewModel.setTurnoff(enable, time);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
