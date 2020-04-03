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
import android.widget.ImageButton;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class LightFragment extends BaseFragment {

    private ImageButton light_ib_back;
    private CheckedTextView light_ctv_manual;
    private CheckedTextView light_ctv_auto;
    private CheckedTextView light_ctv_pro;

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
        return R.layout.fragment_light;
    }

    @Override
    protected void initView(View view) {
        light_ib_back = view.findViewById(R.id.light_ib_back);
        light_ctv_manual = view.findViewById(R.id.light_ctv_manual);
        light_ctv_auto = view.findViewById(R.id.light_ctv_auto);
        light_ctv_pro = view.findViewById(R.id.light_ctv_pro);
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
        light_ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        light_ctv_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_ctv_manual.isChecked()) {
                    mLightViewModel.setMode(ExoLed.MODE_MANUAL);
                }
            }
        });

        light_ctv_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_ctv_auto.isChecked()) {
                    mLightViewModel.setMode(ExoLed.MODE_AUTO);
                }
            }
        });

        light_ctv_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_ctv_pro.isChecked()) {
                    mLightViewModel.setMode(ExoLed.MODE_PRO);
                }
            }
        });
    }

    private void refreshData() {
        if (mLight == null) {
            return;
        }
        if (!light_ctv_auto.isChecked() && mLight.getMode() == ExoLed.MODE_AUTO) {
            light_ctv_manual.setChecked(false);
            light_ctv_auto.setChecked(true);
            light_ctv_pro.setChecked(false);
            replaceFragment(R.id.light_fl_show, new LightAutoFragment());
        } else if (!light_ctv_pro.isChecked() && mLight.getMode() == ExoLed.MODE_PRO) {
            light_ctv_manual.setChecked(false);
            light_ctv_auto.setChecked(false);
            light_ctv_pro.setChecked(true);
            replaceFragment(R.id.light_fl_show, new LightProFragment());
        } else if (!light_ctv_manual.isChecked() && mLight.getMode() == ExoLed.MODE_MANUAL){
            light_ctv_manual.setChecked(true);
            light_ctv_auto.setChecked(false);
            light_ctv_pro.setChecked(false);
            replaceFragment(R.id.light_fl_show, new LightManualFragment());
        }
    }
}
