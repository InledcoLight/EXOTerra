package com.inledco.exoterra.device.Monsoon;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;

public class MonsoonModeFragment extends BaseFragment {
    private CheckedTextView monsoon_mode_manual;
    private CheckedTextView monsoon_mode_timer;
//    private CheckedTextView monsoon_mode_power;

//    private MonsoonViewModel mMonsoonViewModel;
//    private ExoMonsoon mMonsoon;

//    private CountDownTimer mTimer;

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
        return R.layout.fragment_monsoon_mode;
    }

    @Override
    protected void initView(View view) {
        monsoon_mode_manual = view.findViewById(R.id.monsoon_mode_manual);
        monsoon_mode_timer = view.findViewById(R.id.monsoon_mode_timer);
//        monsoon_mode_power = view.findViewById(R.id.monsoon_mode_power);
    }

    @Override
    protected void initData() {
//        mMonsoonViewModel = ViewModelProviders.of(getActivity()).get(MonsoonViewModel.class);
//        mMonsoon = mMonsoonViewModel.getData();
//        mMonsoonViewModel.observe(this, new Observer<ExoMonsoon>() {
//            @Override
//            public void onChanged(@Nullable ExoMonsoon exoMonsoon) {
//                refreshData();
//            }
//        });

//        refreshData();

        monsoon_mode_timer.setChecked(true);
        replaceFragment(R.id.device_fl_show, new MonsoonTimersFragment());
    }

    @Override
    protected void initEvent() {
        monsoon_mode_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!monsoon_mode_manual.isChecked()) {
                    monsoon_mode_manual.setChecked(true);
                    monsoon_mode_timer.setChecked(false);
                    replaceFragment(R.id.device_fl_show, new MonsoonControlFragment());
                }
            }
        });

        monsoon_mode_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!monsoon_mode_timer.isChecked()) {
                    monsoon_mode_manual.setChecked(false);
                    monsoon_mode_timer.setChecked(true);
                    replaceFragment(R.id.device_fl_show, new MonsoonTimersFragment());
                }
            }
        });

//        monsoon_mode_power.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mMonsoonViewModel.setPower(monsoon_mode_power.isChecked() ? ExoMonsoon.SPRAY_OFF : ExoMonsoon.SPRAY_MAX);
//            }
//        });
    }

//    private void refreshData() {
//        if (mMonsoon == null) {
//            return;
//        }
//        int power = mMonsoon.getPower();
//        if (power == 0) {
//            monsoon_mode_power.setChecked(false);
//            monsoon_mode_power.setText(null);
//            if (mTimer != null) {
//                mTimer.cancel();
//                mTimer = null;
//            }
//        } else {
//            monsoon_mode_power.setChecked(true);
//            if (mTimer == null) {
//                long currTime = System.currentTimeMillis() + AliotClient.getInstance().getTimeOffset();
//                long time = mMonsoon.getPowerTime();
//                if (time > currTime) {
//                    time = currTime;
//                }
//                long total = power*1000 + time - currTime;
//                monsoon_mode_power.setText("" + total/1000 + "s");
//                mTimer = new CountDownTimer(total+100, 1000) {
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//                        monsoon_mode_power.setText("" + millisUntilFinished/1000 + "s");
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        monsoon_mode_power.setText(null);
//                        mTimer = null;
//                    }
//                };
//                mTimer.start();
//            }
//        }
//    }
}
