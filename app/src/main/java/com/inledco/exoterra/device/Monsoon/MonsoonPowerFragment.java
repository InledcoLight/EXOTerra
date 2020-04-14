package com.inledco.exoterra.device.Monsoon;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.MonsoonViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class MonsoonPowerFragment extends BaseFragment {

    private CheckedTextView power_ctv;
    private TextView power_status;

    private MonsoonViewModel mMonsoonViewModel;
    private ExoMonsoon mMonsoon;

    private CountDownTimer mTimer;

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
        power_status = view.findViewById(R.id.power_status);
    }

    @Override
    protected void initData() {
        mMonsoonViewModel = ViewModelProviders.of(getActivity()).get(MonsoonViewModel.class);
        mMonsoon = mMonsoonViewModel.getData();
        mMonsoonViewModel.observe(this, new Observer<ExoMonsoon>() {
            @Override
            public void onChanged(@Nullable ExoMonsoon exoMonsoon) {
                refreshData();
            }
        });
        if (mMonsoon == null) {
            return;
        }

        refreshData();
    }

    @Override
    protected void initEvent() {
        power_ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonsoonViewModel.setPower(power_ctv.isChecked() ? ExoMonsoon.SPRAY_OFF : ExoMonsoon.SPRAY_MAX);
            }
        });
    }

    private void refreshData() {
        int power = mMonsoon.getPower();
        if (power == 0) {
            power_ctv.setChecked(false);
            power_status.setText(null);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        } else {
            power_ctv.setChecked(true);
            if (mTimer == null) {
                long currTime = System.currentTimeMillis();
                long time = mMonsoon.getPowerTime();
                if (time > currTime) {
                    time = currTime;
                }
                long total = power*1000 + time - currTime;
                power_status.setText("" + total/1000 + "s");
                mTimer = new CountDownTimer(total+100, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        power_status.setText("" + millisUntilFinished/1000 + "s");
                    }

                    @Override
                    public void onFinish() {
                        power_status.setText(null);
                        mTimer = null;
                    }
                };
                mTimer.start();
            }
        }
    }
}
