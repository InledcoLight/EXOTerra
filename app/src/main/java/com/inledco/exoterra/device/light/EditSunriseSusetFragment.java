package com.inledco.exoterra.device.light;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.view.GradientCornerButton;

public class EditSunriseSusetFragment extends BaseFragment {
    private TextView sunrs_title;
    private TimePicker sunrs_time;
    private NumberPicker sunrs_ramp;
    private View sunrs_action;
    private GradientCornerButton sunrs_cancel;
    private GradientCornerButton sunrs_save;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;

    private boolean mSunset;

    public static EditSunriseSusetFragment newInstance(final boolean sunset) {
        Bundle args = new Bundle();
        args.putBoolean("sunrs", sunset);
        EditSunriseSusetFragment fragment = new EditSunriseSusetFragment();
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
        return R.layout.dialog_edit_sunrise_sunset;
    }

    @Override
    protected void initView(View view) {
        sunrs_title = view.findViewById(R.id.dialog_sunrs_text);
        sunrs_time = view.findViewById(R.id.dialog_sunrs_time);
        sunrs_ramp = view.findViewById(R.id.dialog_sunrs_ramp);
//        sunrs_action = view.findViewById(R.id.dialog_sunrs_action);
        sunrs_cancel = view.findViewById(R.id.btn_cancel);
        sunrs_save = view.findViewById(R.id.btn_save);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        if (mLight == null || args == null) {
            return;
        }

        mSunset = args.getBoolean("sunrs", false);
        String[] displayValues = new String[25];
        for (int i = 0; i < 25; i++) {
            displayValues[i] = "" + i * 10 + " min";
        }
        sunrs_ramp.setMaxValue(24);
        sunrs_ramp.setMinValue(0);
        sunrs_ramp.setDisplayedValues(displayValues);
        int time;
        if (mSunset) {
            sunrs_title.setText(R.string.sunset);
            time = mLight.getDaytimeEnd();
            sunrs_ramp.setValue(mLight.getSunsetRamp() / 10);
        }
        else {
            time = mLight.getDaytimeStart();
            sunrs_ramp.setValue(mLight.getSunriseRamp() / 10);
        }
        sunrs_time.setIs24HourView(GlobalSettings.is24HourFormat());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sunrs_time.setHour(time / 60);
            sunrs_time.setMinute(time % 60);
        }
        else {
            sunrs_time.setCurrentHour(time / 60);
            sunrs_time.setCurrentMinute(time % 60);
        }
    }

    @Override
    protected void initEvent() {
        sunrs_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        sunrs_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int t;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    t = sunrs_time.getHour() * 60 + sunrs_time.getMinute();
                }
                else {
                    t = sunrs_time.getCurrentHour() * 60 + sunrs_time.getCurrentMinute();
                }
                int ramp = sunrs_ramp.getValue() * 10;
                if (mSunset) {
                    mLightViewModel.setSunsetAndRamp(t, ramp);
                } else {
                    mLightViewModel.setSunriseAndRamp(t, ramp);
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
