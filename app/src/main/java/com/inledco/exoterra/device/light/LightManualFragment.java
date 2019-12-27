package com.inledco.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.bean.LightSpectrum;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.util.LightUtil;
import com.inledco.exoterra.util.SpectrumUtil;
import com.inledco.exoterra.view.MultiCircleProgress;
import com.inledco.exoterra.view.VerticalSeekBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LightManualFragment extends BaseFragment {

    private RecyclerView light_manual_rv;
    private MultiCircleProgress[] light_manual_custom;
    private AppCompatImageButton light_manual_power;
    private TextView light_manual_desc;
    private VerticalSeekBar light_manual_slider_all;
    private TextView light_manual_progress;
    private BarChart light_manual_spectrum;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;
//    private VerticalSliderAdapter mAdapter;
    private SliderAdapter mAdapter;

    private int mSelected = -1;

    private LightSpectrum mLightSpectrum;

    private final int mSpectrumWaveStart = 360;
    private final int mSpectrumWaveEnd = 800;

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
        light_manual_spectrum = view.findViewById(R.id.light_manual_spectrum);
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

        //动态设置SeekBar progressDrawable
        light_manual_slider_all.setProgressDrawable(LightUtil.getProgressDrawable(getContext(), "white"));
        //动态设置SeekBar thumb
        GradientDrawable thumb = (GradientDrawable) light_manual_slider_all.getThumb();
        thumb.setColor(Color.WHITE);

        XAxis xAxis = light_manual_spectrum.getXAxis();
        YAxis axisLeft = light_manual_spectrum.getAxisLeft();
        YAxis axisRight = light_manual_spectrum.getAxisRight();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAxisMaximum(getResources().getInteger(R.integer.spectrum_wavelength_max));
        xAxis.setAxisMinimum(getResources().getInteger(R.integer.spectrum_wavelength_min));
        xAxis.setLabelCount(5, false);
        xAxis.setDrawLabels(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(((int) value));
            }
        });
        xAxis.setEnabled(true);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat df = new DecimalFormat("0.0");
                return df.format(value);
            }
        };
        axisLeft.setAxisMaximum(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(6, false);
        axisLeft.setDrawLabels(false);
        axisLeft.setValueFormatter(formatter);
        axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisLeft.setTextColor(Color.WHITE);
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisLineColor(Color.WHITE);
        axisLeft.setGranularity(0.1f);
        axisLeft.setGranularityEnabled(true);
        axisLeft.setSpaceTop(0);
        axisLeft.setSpaceBottom(0);
        axisLeft.setEnabled(true);
        
        axisRight.setEnabled(false);

        light_manual_spectrum.setTouchEnabled(false);
        light_manual_spectrum.setDragEnabled(false);
        light_manual_spectrum.setScaleEnabled(false);
        light_manual_spectrum.setPinchZoom(false);
        light_manual_spectrum.setDoubleTapToZoomEnabled(false);
        light_manual_spectrum.setDrawValueAboveBar(true);
        light_manual_spectrum.setBorderWidth(1);
        light_manual_spectrum.setDrawBorders(false);
        light_manual_spectrum.setDrawGridBackground(true);
        light_manual_spectrum.setGridBackgroundColor(Color.TRANSPARENT);
        light_manual_spectrum.setDescription(null);
        light_manual_spectrum.setFitBars(true);
//        light_manual_spectrum.getLegend().setTextColor(Color.WHITE);
//        light_manual_spectrum.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        light_manual_spectrum.getLegend().setEnabled(false);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mAdapter = new SliderAdapter(getContext(), mLight);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mSelected == position) {
                    mSelected = -1;
                }
            }
        });
        light_manual_rv.setAdapter(mAdapter);
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
                refreshData();
            }
        });

        mLightSpectrum = SpectrumUtil.loadDataFromAssets(getContext().getAssets(), "exoterrastrip_spectrum_450.txt");
        refreshData();
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

        SpannableStringBuilder sp = new SpannableStringBuilder("");
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            String name = mLight.getChannelName(i);
            ImageSpan icon = new ImageSpan(getContext(), LightUtil.getIconRes(name));

            sp.append(" ");
            sp.setSpan(icon, sp.length()-1, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp.append(" " + name + " ");
        }
        light_manual_desc.setText(sp, TextView.BufferType.SPANNABLE);

        if (mLightSpectrum == null) {
            return;
        }
        if (mLight.getPower()) {
            for (int i = 0; i < mLight.getChannelCount(); i++) {
                mLightSpectrum.setGain(i, ((float) mLight.getBright(i)) / 1000);
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
