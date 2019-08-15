package com.liruya.exoterra.device.light;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.util.LightUtil;
import com.liruya.exoterra.view.MultiCircleProgress;

public class LightManualFragment extends BaseFragment {

    private RecyclerView light_manual_rv;
    private MultiCircleProgress[] light_manual_custom;
    private CheckableImageButton light_manual_power;
    private TextView light_manual_desc;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;
    private SliderAdapter mAdapter;

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
        return R.layout.fragment_light_manual;
    }

    @Override
    protected void initView(View view) {
        light_manual_rv = view.findViewById(R.id.light_manual_rv);
        light_manual_desc = view.findViewById(R.id.light_manual_desc);
        light_manual_custom = new MultiCircleProgress[4];
        light_manual_custom[0] = view.findViewById(R.id.light_manual_custom1);
        light_manual_custom[1] = view.findViewById(R.id.light_manual_custom2);
        light_manual_custom[2] = view.findViewById(R.id.light_manual_custom3);
        light_manual_custom[3] = view.findViewById(R.id.light_manual_custom4);
        light_manual_power = view.findViewById(R.id.light_manual_power);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        Log.e(TAG, "initData: " + mLight.getPower());
        mAdapter = new SliderAdapter(getContext(), mLight) {
            @Override
            protected void setBright(int idx, int bright) {
                mLightViewModel.setBright(idx, bright);
            }
        };
        light_manual_rv.setAdapter(mAdapter);
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
                Log.e(TAG, "onChanged: " + exoLedstrip.getPower());
                refreshData();
            }
        });

        SpannableStringBuilder sp = new SpannableStringBuilder("");
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            String color = mLight.getChannelName(i);
            ImageSpan icon = new ImageSpan(getContext(), LightUtil.getIconRes(color));
            sp.append(" ");
            sp.setSpan(icon, sp.length()-1, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp.append(" " + color + " ");
        }
        light_manual_desc.setText(sp, TextView.BufferType.SPANNABLE);
    }

    @Override
    protected void initEvent() {
        light_manual_custom[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] progress = light_manual_custom[0].getProgress();
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_custom[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] progress = light_manual_custom[1].getProgress();
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_custom[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] progress = light_manual_custom[2].getProgress();
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_custom[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] progress = light_manual_custom[3].getProgress();
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_power.setOnClickListener(new View.OnClickListener() {
            @SuppressLint ("RestrictedApi")
            @Override
            public void onClick(View v) {
                mLightViewModel.setPower(!light_manual_power.isChecked());
            }
        });

        light_manual_custom[0].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLightViewModel.setCustomBrights(0, mAdapter.getBrights());
                return true;
            }
        });
        light_manual_custom[1].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLightViewModel.setCustomBrights(1, mAdapter.getBrights());
                return true;
            }
        });
        light_manual_custom[2].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLightViewModel.setCustomBrights(2, mAdapter.getBrights());
                return true;
            }
        });
        light_manual_custom[3].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLightViewModel.setCustomBrights(3, mAdapter.getBrights());
                return true;
            }
        });
    }

    @SuppressLint ("RestrictedApi")
    private void refreshData() {
        Log.e(TAG, "refreshData: " + mLight.getPower());
        mAdapter.notifyDataSetChanged();
        light_manual_power.setChecked(mLight.getPower());
        int count = mLight.getChannelCount();
        for (int i = 0; i < 4; i++) {
            light_manual_custom[i].setCircleCount(count);
            byte[] array = mLight.getCustomBrights(i);
            if (array != null && array.length == count) {
                for (int j = 0; j < count; j++) {
                    light_manual_custom[i].setProgress(j, array[j]);
                    String color = mLight.getChannelName(j);
                    light_manual_custom[i].setCircleColor(j, LightUtil.getColorValue(color));
                }
            }
            light_manual_custom[i].invalidate();
        }
    }
}
