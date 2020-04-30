package com.inledco.exoterra.device.Monsoon;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.MonsoonViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class MonsoonFragment extends BaseFragment {

//    private CircleSeekbar monsoon_poweron_tmr;
//    private CheckedTextView monsoon_power;
//    private ImageButton monsoon_ib_back;
//    private CheckedTextView monsoon_ctv_manual;
//    private CheckedTextView monsoon_ctv_timer;

    private MonsoonViewModel mMonsoonViewModel;
    private ExoMonsoon mMonsoon;

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
//        monsoon_poweron_tmr = view.findViewById(R.id.monsoon_poweron_tmr);
//        monsoon_power = view.findViewById(R.id.monsoon_power);
//        monsoon_ib_back = view.findViewById(R.id.monsoon_ib_back);
//        monsoon_ctv_manual = view.findViewById(R.id.monsoon_ctv_manual);
//        monsoon_ctv_timer = view.findViewById(R.id.monsoon_ctv_timer);
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
//
//        refreshData();
//        monsoon_ctv_manual.setChecked(true);
//        replaceFragment(R.id.monsoon_fl, new MonsoonControlFragment());
    }

    @Override
    protected void initEvent() {
//        monsoon_power.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int power = (monsoon_power.isChecked() ? ExoMonsoon.SPRAY_OFF : ExoMonsoon.SPRAY_MAX);
//                mMonsoonViewModel.setPower(power);
//            }
//        });
//
//        monsoon_ib_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().finish();
//            }
//        });
//
//        monsoon_ctv_manual.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!monsoon_ctv_manual.isChecked()) {
//                    monsoon_ctv_manual.setChecked(true);
//                    monsoon_ctv_timer.setChecked(false);
//                    replaceFragment(R.id.monsoon_fl, new MonsoonControlFragment());
//                }
//            }
//        });
//
//        monsoon_ctv_timer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!monsoon_ctv_timer.isChecked()) {
//                    monsoon_ctv_manual.setChecked(false);
//                    monsoon_ctv_timer.setChecked(true);
//                    replaceFragment(R.id.monsoon_fl, new MonsoonTimersFragment());
//                }
//            }
//        });
    }

    private void refreshData() {
//        if (mMonsoon == null) {
//            return;
//        }
//        int power = mMonsoon.getPower();
//        if (power == 0) {
//            monsoon_power.setChecked(false);
//            monsoon_power.setText(R.string.off);
//            monsoon_poweron_tmr.setProgress(0);
//        } else {
//            monsoon_power.setChecked(true);
//            int total = power;
//            int tmr = mMonsoon.getCountdown();
//            monsoon_poweron_tmr.setMax(total);
//            monsoon_poweron_tmr.setProgress(tmr);
//            monsoon_power.setText("" + tmr + "s");
//        }
    }
}
