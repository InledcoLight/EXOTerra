package com.inledco.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.LightSpectrum;
import com.inledco.exoterra.device.LightPresets;
import com.inledco.exoterra.util.LightUtil;
import com.inledco.exoterra.util.SpectrumUtil;
import com.inledco.exoterra.view.CircleSeekbar;
import com.inledco.exoterra.view.MultiCircleProgress;
import com.inledco.exoterra.view.TurningWheel;

import java.util.ArrayList;
import java.util.List;

public class LightManualFragment extends BaseFragment {

    private View light_manual_show;

    private ToggleButton light_manual_presets;
    private View light_manual_presets_detail;
    private MultiCircleProgress[] mCustoms;
    private CheckedTextView[] mPresets;
    private BarChart light_manual_spectrum;
    private TurningWheel light_manual_csb;
    private CheckedTextView light_manual_power;
    private View[] includes;
    private CircleSeekbar[] mCircleSeekbars;
    private TextView[] mPercents;
    private TextView[] mColors;

    private LightViewModel mLightViewModel;
    private ExoLed mLight;

    private int mSelected = -1;

    private LightSpectrum mLightSpectrum;

    private static final int[][] mPresetBrights = new int[][] {
        LightPresets.PRESET_EXOSTRIP_PLANT,
        LightPresets.PRESET_EXOSTRIP_CLOUD,
        LightPresets.PRESET_EXOSTRIP_SUNSET,
        LightPresets.PRESET_EXOSTRIP_MOON,
        LightPresets.PRESET_EXOSTRIP_CACTUS,
        LightPresets.PRESET_EXOSTRIP_FROG,
        LightPresets.PRESET_EXOSTRIP_LIZARD,
        LightPresets.PRESET_EXOSTRIP_SNAKE
    };

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
        light_manual_show = view.findViewById(R.id.light_manual_show);
        light_manual_presets = view.findViewById(R.id.light_manual_presets);
        light_manual_presets_detail = view.findViewById(R.id.light_manual_presets_detail);
        mCustoms = new MultiCircleProgress[4];
        mPresets = new CheckedTextView[8];
        mCustoms[0] = view.findViewById(R.id.light_p1);
        mCustoms[1] = view.findViewById(R.id.light_p2);
        mCustoms[2] = view.findViewById(R.id.light_p3);
        mCustoms[3] = view.findViewById(R.id.light_p4);
        mPresets[0] = view.findViewById(R.id.light_p5);
        mPresets[1] = view.findViewById(R.id.light_p6);
        mPresets[2] = view.findViewById(R.id.light_p7);
        mPresets[3] = view.findViewById(R.id.light_p8);
        mPresets[4] = view.findViewById(R.id.light_p9);
        mPresets[5] = view.findViewById(R.id.light_p10);
        mPresets[6] = view.findViewById(R.id.light_p11);
        mPresets[7] = view.findViewById(R.id.light_p12);
        light_manual_power = view.findViewById(R.id.light_manual_power);
        light_manual_csb = view.findViewById(R.id.light_manual_csb);

        includes = new View[6];
        mCircleSeekbars = new CircleSeekbar[6];
        mPercents = new TextView[6];
        mColors = new TextView[6];
        light_manual_spectrum = light_manual_show.findViewById(R.id.light_manual_spectrum);
        includes[0] = light_manual_show.findViewById(R.id.light_manual_include1);
        includes[1] = light_manual_show.findViewById(R.id.light_manual_include2);
        includes[2] = light_manual_show.findViewById(R.id.light_manual_include3);
        includes[3] = light_manual_show.findViewById(R.id.light_manual_include4);
        includes[4] = light_manual_show.findViewById(R.id.light_manual_include5);
        includes[5] = light_manual_show.findViewById(R.id.light_manual_include6);
        for (int i = 0; i < 6; i++) {
            mCircleSeekbars[i] = includes[i].findViewById(R.id.item_progress_scb);
            mPercents[i] = includes[i].findViewById(R.id.item_progress_pct);
            mColors[i] = includes[i].findViewById(R.id.item_progress_color);
        }

        ChartHelper.initBarChart(light_manual_spectrum);
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

        mLightSpectrum = SpectrumUtil.loadDataFromAssets(getContext().getAssets(), "exoterrastrip_spectrum_450.txt");
        refreshData();
        light_manual_spectrum.animateXY(1000, 1000);
    }

    @Override
    protected void initEvent() {
        light_manual_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLightViewModel.setPower(!mLight.getPower());
            }
        });

        light_manual_presets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                light_manual_csb.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
                light_manual_presets_detail.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        for (int i = 0; i < 4; i++) {
            final int pos = i;
            mCustoms[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] progress = mCustoms[pos].getProgress();
                    mLightViewModel.setChannelBrights(progress);
                    for (int j = 0; j < 8; j++) {
                        mPresets[j].setChecked(false);
                    }
                }
            });
            mCustoms[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mLightViewModel.setCustomBrights(pos, mLight.getChannelBrights());
                    return true;
                }
            });
        }

        for (int i = 0; i < 8; i++) {
            final int pos = i;
            mPresets[pos].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j = 0; j < 8; j++) {
                        mPresets[j].setChecked(pos == j ? true : false);
                    }
                    mLightViewModel.setChannelBrights(mPresetBrights[pos]);
                }
            });
        }

        for (int i = 0; i < 6; i++) {
            final int pos = i;
            includes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelected == pos) {
                        mSelected = -1;
                        light_manual_csb.setProgressColor(Color.WHITE);
                        light_manual_csb.setProgress(0);
                    } else {
                        mSelected = pos;
                        String name = mLight.getChannelName(pos);
                        int color = LightUtil.getColorValue(name);
                        light_manual_csb.setProgressColor(color);
                        light_manual_csb.setProgress(mLight.getChannelBright(pos));
                    }
                    for (int j = 0; j < 6; j++) {
                        if (mSelected == j) {
                            Drawable drawable = getContext().getResources().getDrawable(R.drawable.shape_round_white);
                            mCircleSeekbars[j].setBackground(drawable);
                        } else {
                            mCircleSeekbars[j].setBackground(null);
                        }
                    }
                }
            });
        }

        light_manual_csb.setListener(new TurningWheel.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {

            }

            @Override
            public void onSeekStop() {
                int progress = light_manual_csb.getProgress();
                if (mSelected >= 0 && mSelected < mLight.getChannelCount()) {
                    mLightViewModel.setChannelBright(mSelected, progress);
                } else {
//                    int[] brights = new int[mLight.getChannelCount()];
//                    for (int i = 0; i < brights.length; i++) {
//                        brights[i] = progress;
//                    }
//                    mLightViewModel.setChannelBrights(brights);
                }
            }
        });
    }

    private void refreshData() {
        if (mLight == null || mLight.getChannelCount() > 6) {
            return;
        }
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            includes[i].setVisibility(View.VISIBLE);
            int brt = mLight.getChannelBright(i);
            String color = mLight.getChannelName(i);
            mCircleSeekbars[i].setProgressColor(LightUtil.getColorValue(color));
            mCircleSeekbars[i].setProgress(brt);
            if (brt > 0 && brt < 10) {
                mPercents[i].setText("0." + brt + " %");
            } else {
                mPercents[i].setText("" + brt / 10 + " %");
            }
            mColors[i].setText(color);
            if (mSelected == i) {
                light_manual_csb.setProgress(brt);
            }
        }
        for (int i = mLight.getChannelCount(); i < 6; i++) {
            includes[i].setVisibility(View.GONE);
        }
        light_manual_power.setChecked(mLight.getPower());
        light_manual_power.setText(mLight.getPower() ? R.string.on : R.string.off);
        int count = mLight.getChannelCount();
        for (int i = 0; i < 4; i++) {
            mCustoms[i].setCircleCount(count);
            int[] array = mLight.getCustomBrights(i);
            if (array != null && array.length == count) {
                for (int j = 0; j < count; j++) {
                    mCustoms[i].setProgress(j, array[j]);
                    String color = mLight.getChannelName(j);
                    mCustoms[i].setCircleColor(j, LightUtil.getColorValue(color));
                }
            }
            mCustoms[i].invalidate();
        }

        if (mLightSpectrum == null) {
            return;
        }
        if (mLight.getPower()) {
            for (int i = 0; i < mLight.getChannelCount(); i++) {
                mLightSpectrum.setGain(i, ((float) mLight.getChannelBright(i)) / 1000);
            }
        } else {
            for (int i = 0; i < mLight.getChannelCount(); i++) {
                mLightSpectrum.setGain(i, 0);
            }
        }

        int min = getResources().getInteger(R.integer.spectrum_wavelength_min);
        int max = getResources().getInteger(R.integer.spectrum_wavelength_max);
        int[] waveColors = getResources().getIntArray(R.array.spectrum);
        if (waveColors == null || waveColors.length != max-min+1) {
            try {
                throw new Exception("Invalid wave-color table.");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        int wave_min = mLightSpectrum.getStart();
        float spectrumMax = mLightSpectrum.getMax();
        List<IBarDataSet> dataSets = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(i, mLightSpectrum.get(i-wave_min)/spectrumMax));
            BarDataSet barDataSet = new BarDataSet(entries, null);
            barDataSet.setDrawValues(false);
            int color = waveColors[i - min];
            barDataSet.setColor(color);
            barDataSet.setBarBorderColor(color);
            barDataSet.setBarShadowColor(color);
            barDataSet.setHighlightEnabled(false);
            dataSets.add(barDataSet);
        }
        BarData barData = new BarData(dataSets);
        barData.setBarWidth(2);
        light_manual_spectrum.setData(barData);
        light_manual_spectrum.invalidate();
    }
}
