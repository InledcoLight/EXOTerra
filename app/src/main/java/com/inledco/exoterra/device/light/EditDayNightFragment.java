package com.inledco.exoterra.device.light;

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
import android.widget.ImageView;
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
import com.inledco.exoterra.view.GradientCornerButton;
import com.inledco.exoterra.view.MultiCircleProgress;
import com.inledco.exoterra.view.TurningWheel;

import java.util.ArrayList;
import java.util.List;

public class EditDayNightFragment extends BaseFragment {

    private View daynight_show;
    private ImageView daynight_icon;
    private View[] includes;
    private CircleSeekbar[] mCircleSeekbars;
    private TextView[] mPercents;
    private TextView[] mColors;
    private BarChart daynight_spectrum;
    private ToggleButton daynight_presets;
    private TurningWheel daynight_csb;
    private View daynight_presets_detail;
    private MultiCircleProgress[] mCustoms;
    private CheckedTextView[] mPresets;
    private GradientCornerButton daynight_cancel;
    private GradientCornerButton daynight_save;

    private LightViewModel mLightViewModel;
    private ExoLed mLight;

    private boolean mNight;

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

    public static EditDayNightFragment newInstance(final boolean night) {
        Bundle args = new Bundle();
        args.putBoolean("daynight", night);
        EditDayNightFragment fragment = new EditDayNightFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        EventBus.getDefault().post(new FragmentShowEvent("LightAutoFragment", false));
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        EventBus.getDefault().post(new FragmentShowEvent("LightAutoFragment", true));
//    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_edit_daynight;
    }

    @Override
    protected void initView(View view) {
        daynight_show = view.findViewById(R.id.edit_daynight_show);
        daynight_icon = view.findViewById(R.id.edit_daynight_icon);
        daynight_csb = view.findViewById(R.id.edit_daynight_csb);
        daynight_presets = view.findViewById(R.id.edit_daynight_presets);
        daynight_presets_detail = view.findViewById(R.id.edit_daynight_presets_detail);
        daynight_cancel = view.findViewById(R.id.btn_cancel);
        daynight_save = view.findViewById(R.id.btn_save);

        daynight_csb.setMax(100);
        includes = new View[6];
        mCircleSeekbars = new CircleSeekbar[6];
        mPercents = new TextView[6];
        mColors = new TextView[6];
        daynight_spectrum = daynight_show.findViewById(R.id.light_manual_spectrum);
        includes[0] = daynight_show.findViewById(R.id.light_manual_include1);
        includes[1] = daynight_show.findViewById(R.id.light_manual_include2);
        includes[2] = daynight_show.findViewById(R.id.light_manual_include3);
        includes[3] = daynight_show.findViewById(R.id.light_manual_include4);
        includes[4] = daynight_show.findViewById(R.id.light_manual_include5);
        includes[5] = daynight_show.findViewById(R.id.light_manual_include6);
        for (int i = 0; i < 6; i++) {
            mCircleSeekbars[i] = includes[i].findViewById(R.id.item_progress_scb);
            mCircleSeekbars[i].setMax(100);
            mPercents[i] = includes[i].findViewById(R.id.item_progress_pct);
            mColors[i] = includes[i].findViewById(R.id.item_progress_color);
        }

        mCustoms = new MultiCircleProgress[4];
        mPresets = new CheckedTextView[8];
        mCustoms[0] = daynight_presets_detail.findViewById(R.id.light_p1);
        mCustoms[1] = daynight_presets_detail.findViewById(R.id.light_p2);
        mCustoms[2] = daynight_presets_detail.findViewById(R.id.light_p3);
        mCustoms[3] = daynight_presets_detail.findViewById(R.id.light_p4);
        mPresets[0] = daynight_presets_detail.findViewById(R.id.light_p5);
        mPresets[1] = daynight_presets_detail.findViewById(R.id.light_p6);
        mPresets[2] = daynight_presets_detail.findViewById(R.id.light_p7);
        mPresets[3] = daynight_presets_detail.findViewById(R.id.light_p8);
        mPresets[4] = daynight_presets_detail.findViewById(R.id.light_p9);
        mPresets[5] = daynight_presets_detail.findViewById(R.id.light_p10);
        mPresets[6] = daynight_presets_detail.findViewById(R.id.light_p11);
        mPresets[7] = daynight_presets_detail.findViewById(R.id.light_p12);

        ChartHelper.initBarChart(daynight_spectrum);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        Bundle args = getArguments();
        if (mLight == null || args == null) {
            return;
        }

        mNight = args.getBoolean("daynight", false);
        mLightSpectrum = SpectrumUtil.loadDataFromAssets(getContext().getAssets(), "exoterrastrip_spectrum_450.txt");

        daynight_icon.setImageResource(mNight ? R.drawable.ic_moon : R.drawable.ic_sun);
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            int brt = (mNight ? mLight.getNightBrights()[i] : mLight.getDayBrights()[i]);
            mCircleSeekbars[i].setProgress(brt);
        }

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

        refreshData();
        daynight_spectrum.animateXY(1000, 1000);
    }

    @Override
    protected void initEvent() {
        for (int i = 0; i < 6; i++) {
            final int pos = i;
            includes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelected == pos) {
                        mSelected = -1;
                        daynight_csb.setProgressColor(Color.WHITE);
                        daynight_csb.setProgress(0);
                    } else {
                        mSelected = pos;
                        String name = mLight.getChannelName(pos);
                        int color = LightUtil.getColorValue(name);
                        daynight_csb.setProgressColor(color);
                        daynight_csb.setProgress(mCircleSeekbars[pos].getProgress());
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

        daynight_presets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                daynight_csb.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
                daynight_presets_detail.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        for (int i = 0; i < 4; i++) {
            final int pos = i;
            mCustoms[pos].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j = 0; j < mLight.getChannelCount(); j++) {
                        mCircleSeekbars[j].setProgress(mLight.getCustomBrights(pos)[j]/10);
                    }
                    for (int j = 0; j < 8; j++) {
                        mPresets[j].setChecked(false);
                    }
                    refreshData();
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
                    for (int j = 0; j < mLight.getChannelCount(); j++) {
                        mCircleSeekbars[j].setProgress(mPresetBrights[pos][j]);
                    }
                    refreshData();
                }
            });
        }

        daynight_csb.setListener(new TurningWheel.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {

            }

            @Override
            public void onSeekStop() {
                int progress = daynight_csb.getProgress();
                if (mSelected >= 0 && mSelected < mLight.getChannelCount()) {
                    mCircleSeekbars[mSelected].setProgress(progress);
                } else {
                    for (int i = 0; i < mLight.getChannelCount(); i++) {
                        mCircleSeekbars[i].setProgress(progress);
                    }
                }
                refreshData();
            }
        });

        daynight_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        daynight_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] brts = new int[mLight.getChannelCount()];
                for (int i = 0; i < brts.length; i++) {
                    brts[i] = mCircleSeekbars[i].getProgress();
                }
                if (mNight) {
                    mLightViewModel.setNightBrights(brts);
                } else {
                    mLightViewModel.setDayBrights(brts);
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void refreshData() {
        if (mLight == null || mLight.getChannelCount() > 6) {
            return;
        }
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            includes[i].setVisibility(View.VISIBLE);
            int brt = mCircleSeekbars[i].getProgress();
            String color = mLight.getChannelName(i);
            mCircleSeekbars[i].setProgressColor(LightUtil.getColorValue(color));
            mCircleSeekbars[i].setProgress(brt);
            mPercents[i].setText("" + brt + " %");
            mColors[i].setText(color);
            if (mSelected == i) {
                daynight_csb.setProgress(brt);
            }
        }
        for (int i = mLight.getChannelCount(); i < 6; i++) {
            includes[i].setVisibility(View.GONE);
        }

        if (mLightSpectrum == null) {
            return;
        }
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            mLightSpectrum.setGain(i, ((float) mCircleSeekbars[i].getProgress()) / 100);
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
        daynight_spectrum.setData(barData);
        daynight_spectrum.invalidate();
    }
}
