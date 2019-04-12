package com.liruya.exoterra.device.light;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOLedstrip;

public class LightFragment extends BaseFragment {

    private CheckableImageButton light_cib_manual;
    private CheckableImageButton light_cib_auto;
    private CheckedTextView light_ctv_pro;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;

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
        light_cib_manual = view.findViewById(R.id.light_cib_manual);
        light_cib_auto = view.findViewById(R.id.light_cib_auto);
        light_ctv_pro = view.findViewById(R.id.light_ctv_pro);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
                refreshData();
            }
        });
        refreshData();
    }

    @Override
    protected void initEvent() {
        light_cib_manual.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (!light_cib_manual.isChecked()) {
                    mLightViewModel.setMode(EXOLedstrip.MODE_MANUAL);
                }
            }
        });

        light_cib_auto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (!light_cib_auto.isChecked()) {
                    mLightViewModel.setMode(EXOLedstrip.MODE_AUTO);
                }
            }
        });

        light_ctv_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light_ctv_pro.isChecked()) {
                    mLightViewModel.setMode(EXOLedstrip.MODE_PRO);
                }
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    private void refreshData() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                                                       .beginTransaction();
        if (mLight.getMode() == EXOLedstrip.MODE_AUTO) {
            light_cib_manual.setChecked(false);
            light_cib_auto.setChecked(true);
            light_ctv_pro.setChecked(false);
            transaction.replace(R.id.light_fl_show, new LightAutoFragment()).commit();
        } else if (mLight.getMode() == EXOLedstrip.MODE_PRO) {
            light_cib_manual.setChecked(false);
            light_cib_auto.setChecked(false);
            light_ctv_pro.setChecked(true);
            transaction.replace(R.id.light_fl_show, new LightProFragment()).commit();
        } else {
            light_cib_manual.setChecked(true);
            light_cib_auto.setChecked(false);
            light_ctv_pro.setChecked(false);
            transaction.replace(R.id.light_fl_show, new LightManualFragment()).commit();
        }
    }
}
