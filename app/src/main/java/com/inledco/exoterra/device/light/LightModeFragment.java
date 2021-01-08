package com.inledco.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class LightModeFragment extends BaseFragment {
    private CheckedTextView light_mode_manual;
    private CheckedTextView light_mode_auto;
    private CheckedTextView light_mode_pro;
    private CheckedTextView light_mode_power;

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
        return R.layout.fragment_light_mode;
    }

    @Override
    protected void initView(View view) {
        light_mode_manual = view.findViewById(R.id.light_mode_manual);
        light_mode_auto = view.findViewById(R.id.light_mode_auto);
        light_mode_pro = view.findViewById(R.id.light_mode_pro);
        light_mode_power = view.findViewById(R.id.light_mode_power);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<ExoLed>() {
            @Override
            public void onChanged(@Nullable ExoLed exoLed) {
                refreshData();
            }
        });
        refreshData();
    }

    @Override
    protected void initEvent() {
        light_mode_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_mode_manual.isChecked()) {
                    mLightViewModel.setMode(ExoLed.MODE_MANUAL);
                }
            }
        });

        light_mode_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_mode_auto.isChecked()) {
                    mLightViewModel.setMode(ExoLed.MODE_AUTO);
                }
            }
        });

        light_mode_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_mode_pro.isChecked()) {
                    mLightViewModel.setMode(ExoLed.MODE_PRO);
                }
            }
        });

        light_mode_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLightViewModel.setPower(!light_mode_power.isChecked());
            }
        });
    }

    private void refreshData() {
        if (mLight == null) {
            return;
        }
        if (!light_mode_auto.isChecked() && mLight.getMode() == ExoLed.MODE_AUTO) {
            light_mode_manual.setChecked(false);
            light_mode_auto.setChecked(true);
            light_mode_pro.setChecked(false);

            replaceFragment(R.id.device_fl_show, new LightAutoFragment());
        } else if (!light_mode_pro.isChecked() && mLight.getMode() == ExoLed.MODE_PRO) {
            light_mode_manual.setChecked(false);
            light_mode_auto.setChecked(false);
            light_mode_pro.setChecked(true);

            replaceFragment(R.id.device_fl_show, new LightProFragment());
        } else if (!light_mode_manual.isChecked() && mLight.getMode() == ExoLed.MODE_MANUAL){
            light_mode_manual.setChecked(true);
            light_mode_auto.setChecked(false);
            light_mode_pro.setChecked(false);

            replaceFragment(R.id.device_fl_show, new LightManualFragment());
        }

        if (mLight.getMode() == ExoLed.MODE_MANUAL) {
            light_mode_power.setVisibility(View.VISIBLE);
            light_mode_power.setChecked(mLight.getPower());
            light_mode_power.setText(mLight.getPower() ? R.string.on : R.string.off);
        } else {
            light_mode_power.setVisibility(View.INVISIBLE);
        }
    }
}
