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
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOLedstrip;

public class LightPowerFragment extends BaseFragment {

    private CheckedTextView power_ctv;

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
        return R.layout.fragment_power;
    }

    @Override
    protected void initView(View view) {
        power_ctv = view.findViewById(R.id.power_ctv);
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
        if (mLight == null) {
            return;
        }

        refreshData();
    }

    @Override
    protected void initEvent() {
        power_ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLightViewModel.setPower(!power_ctv.isChecked());
            }
        });
    }

    private void refreshData() {
        if (mLight.getMode() == EXOLedstrip.MODE_MANUAL) {
            power_ctv.setVisibility(View.VISIBLE);
            boolean power = mLight.getPower();
            power_ctv.setChecked(power);
            power_ctv.setText(power ? R.string.on : R.string.off);
        } else {
            power_ctv.setVisibility(View.GONE);
        }
    }
}
