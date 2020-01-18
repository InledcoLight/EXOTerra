package com.inledco.exoterra.main.pref;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;

public class PrefFragment extends BaseFragment {
    private Switch pref_timeformat;
    private Switch pref_tempunit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initView(view);
        initData();
        initEvent();
        return view;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_pref;
    }

    @Override
    protected void initView(View view) {
        pref_timeformat = view.findViewById(R.id.pref_timeformat);
        pref_tempunit = view.findViewById(R.id.pref_tempunit);
    }

    @Override
    protected void initData() {
        pref_timeformat.setChecked(!GlobalSettings.is24HourFormat());
        pref_tempunit.setChecked(!GlobalSettings.isCelsius());
    }

    @Override
    protected void initEvent() {
        pref_timeformat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalSettings.setIs24HourFormat(getContext(), !isChecked);
            }
        });

        pref_tempunit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalSettings.setIsCelsius(getContext(), !isChecked);
            }
        });
    }
}
