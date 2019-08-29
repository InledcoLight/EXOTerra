package com.liruya.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.base.BaseFragment;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.util.LightUtil;
import com.liruya.exoterra.view.MultiCircleProgress;
import com.liruya.exoterra.view.VerticalSeekBar;

import java.text.DecimalFormat;
import java.util.Arrays;

public class LightManualFragment extends BaseFragment {

    private RecyclerView light_manual_rv;
    private MultiCircleProgress[] light_manual_custom;
    private AppCompatImageButton light_manual_power;
    private TextView light_manual_desc;
    private VerticalSeekBar light_manual_slider_all;
    private TextView light_manual_progress;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;
    private VerticalSliderAdapter mAdapter;

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
        light_manual_slider_all = view.findViewById(R.id.light_manual_slider_all);
        light_manual_progress = view.findViewById(R.id.light_manual_progress);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mAdapter = new VerticalSliderAdapter(getContext(), mLight) {
            @Override
            protected void setBright(int idx, int bright) {
                mLightViewModel.setBright(idx, bright);
            }
        };
        light_manual_rv.setAdapter(mAdapter);
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
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
                int[] prgs = light_manual_custom[0].getProgress();
                int[] progress = Arrays.copyOf(prgs, prgs.length);
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_custom[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] prgs = light_manual_custom[1].getProgress();
                int[] progress = Arrays.copyOf(prgs, prgs.length);
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_custom[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] prgs = light_manual_custom[2].getProgress();
                int[] progress = Arrays.copyOf(prgs, prgs.length);
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_custom[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] prgs = light_manual_custom[3].getProgress();
                int[] progress = Arrays.copyOf(prgs, prgs.length);
                for (int i = 0; i < progress.length; i++) {
                    progress[i] *= 10;
                }
                mLightViewModel.setAllBrights(progress);
            }
        });
        light_manual_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLightViewModel.setPower(!mLight.getPower());
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

        light_manual_slider_all.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int[] brights = new int[mLight.getChannelCount()];
                for (int i = 0; i < brights.length; i++) {
                    brights[i] = progress;
                }
                mLightViewModel.setAllBrights(brights);
                DecimalFormat df = new DecimalFormat("##0");
                light_manual_progress.setText(df.format(progress/10) + "%");
            }
        });
    }

    private void refreshData() {
        mAdapter.notifyDataSetChanged();
        light_manual_power.setImageResource(mLight.getPower() ? R.drawable.ic_power_white : R.drawable.ic_power_red);
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
