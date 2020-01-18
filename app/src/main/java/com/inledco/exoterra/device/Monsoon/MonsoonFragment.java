package com.inledco.exoterra.device.Monsoon;

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

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOMonsoon;
import com.inledco.exoterra.view.CircleSeekbar;

public class MonsoonFragment extends BaseFragment {

    private CircleSeekbar monsoon_poweron_tmr;
    private CheckedTextView monsoon_power;
    private ImageButton monsoon_ib_back;
    private CheckedTextView monsoon_ctv_manual;
    private CheckedTextView monsoon_ctv_timer;

    private MonsoonViewModel mMonsoonViewModel;
    private EXOMonsoon mMonsoon;

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
        return R.layout.fragment_monsoon;
    }

    @Override
    protected void initView(View view) {
        monsoon_poweron_tmr = view.findViewById(R.id.monsoon_poweron_tmr);
        monsoon_power = view.findViewById(R.id.monsoon_power);
        monsoon_ib_back = view.findViewById(R.id.monsoon_ib_back);
        monsoon_ctv_manual = view.findViewById(R.id.monsoon_ctv_manual);
        monsoon_ctv_timer = view.findViewById(R.id.monsoon_ctv_timer);
    }

    @Override
    protected void initData() {
        mMonsoonViewModel = ViewModelProviders.of(getActivity()).get(MonsoonViewModel.class);
        mMonsoon = mMonsoonViewModel.getData();
        mMonsoonViewModel.observe(this, new Observer<EXOMonsoon>() {
            @Override
            public void onChanged(@Nullable EXOMonsoon exoMonsoon) {
                refreshData();
            }
        });

        refreshData();
        monsoon_ctv_manual.setChecked(true);
        replaceFragment(R.id.monsoon_fl, new MonsoonControlFragment());
    }

    @Override
    protected void initEvent() {
        monsoon_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte power = (monsoon_power.isChecked() ? AppConstants.MONSOON_POWEROFF : AppConstants.MONSOON_POWERON);      //128+120
                mMonsoonViewModel.setPower(power);
            }
        });

        monsoon_ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        monsoon_ctv_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!monsoon_ctv_manual.isChecked()) {
                    monsoon_ctv_manual.setChecked(true);
                    monsoon_ctv_timer.setChecked(false);
                    replaceFragment(R.id.monsoon_fl, new MonsoonControlFragment());
                }
            }
        });

        monsoon_ctv_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!monsoon_ctv_timer.isChecked()) {
                    monsoon_ctv_manual.setChecked(false);
                    monsoon_ctv_timer.setChecked(true);
                    replaceFragment(R.id.monsoon_fl, new MonsoonTimersFragment());
                }
            }
        });
    }

    private void refreshData() {
        if (mMonsoon == null) {
            return;
        }
        byte power = mMonsoon.getPower();
        if ((power&0x80) == 0) {
            monsoon_power.setChecked(false);
            monsoon_power.setText(R.string.off);
            monsoon_poweron_tmr.setProgress(0);
        } else {
            monsoon_power.setChecked(true);
            int total = power&0x7F;
            int tmr = mMonsoon.getPoweronTmr();
            monsoon_poweron_tmr.setMax(total);
            monsoon_poweron_tmr.setProgress(tmr);
            if (tmr <= 120) {
                monsoon_power.setText("" + tmr + "s");
            } else if (tmr <= 123) {
                monsoon_power.setText("" + (tmr-118)*60 + "s");
            } else {
                monsoon_power.setText(null);
            }
        }
    }
}
