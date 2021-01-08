package com.inledco.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.LightSpectrum;
import com.inledco.exoterra.util.LightUtil;
import com.inledco.exoterra.util.SpectrumUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SimpleTimeZone;

public class LightAutoFragment extends BaseFragment {
    private ConstraintLayout light_auto_root;
    private LineChart auto_line_chart;
    private BarChart auto_spectrum;
//    private ImageView auto_iv_sunrise;
    private TextView auto_sunrise;
    private TextView auto_sunrise_ramp;
    private ImageButton auto_sunrise_edit;
    private TextView auto_sunset;
    private TextView auto_sunset_ramp;
    private ImageButton auto_sunset_edit;
    private TextView auto_turnoff;
    private ImageButton auto_turnoff_edit;
    private View auto_daylight;
    private ImageButton auto_daylight_edit;
    private View auto_nightlight;
    private ImageButton auto_nightlight_edit;
    private TextView[] daylight_tv;
    private TextView[] nightlight_tv;

    private LightViewModel mLightViewModel;
    private ExoLed mLight;
    private LightSpectrum mLightSpectrum;

    private int chnCount;
    private int sunriseStart;
    private int sunriseRamp;
    private int sunsetEnd;
    private int sunsetRamp;
    private boolean turnoffEnable;
    private int turnoffTime;
    private int[] dayBrights;
    private int[] nightBrights;

    private DateFormat mTimeFormat;
    private boolean mEditing;

    private final BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_TICK)) {
                refresSpectrum();
                showTimeLine();
            }
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mTimeTickReceiver);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_light_auto;
    }

    @Override
    protected void initView(View view) {
        light_auto_root = view.findViewById(R.id.light_auto_root);
        auto_line_chart = view.findViewById(R.id.auto_line_chart);
        auto_spectrum = view.findViewById(R.id.light_auto_spectrum);
        auto_sunrise = view.findViewById(R.id.light_auto_sunrise);
//        auto_sunrise_ramp = view.findViewById(R.id.light_auto_sunrise_ramp);
        auto_sunrise_edit = view.findViewById(R.id.light_auto_sunrise_edit);
        auto_sunset = view.findViewById(R.id.light_auto_sunset);
//        auto_sunset_ramp = view.findViewById(R.id.light_auto_sunset_ramp);
        auto_sunset_edit = view.findViewById(R.id.light_auto_sunset_edit);
        auto_turnoff = view.findViewById(R.id.light_auto_turnoff);
        auto_turnoff_edit =view.findViewById(R.id.light_auto_turnoff_edit);
        auto_daylight = view.findViewById(R.id.light_auto_daylight);
        auto_daylight_edit = view.findViewById(R.id.light_auto_daylight_edit);
        auto_nightlight = view.findViewById(R.id.light_auto_nightlight);
        auto_nightlight_edit = view.findViewById(R.id.light_auto_nightlight_edit);
        daylight_tv = new TextView[6];
        nightlight_tv = new TextView[6];
        daylight_tv[0] = auto_daylight.findViewById(R.id.item_percent_tv1);
        daylight_tv[1] = auto_daylight.findViewById(R.id.item_percent_tv2);
        daylight_tv[2] = auto_daylight.findViewById(R.id.item_percent_tv3);
        daylight_tv[3] = auto_daylight.findViewById(R.id.item_percent_tv4);
        daylight_tv[4] = auto_daylight.findViewById(R.id.item_percent_tv5);
        daylight_tv[5] = auto_daylight.findViewById(R.id.item_percent_tv6);
        nightlight_tv[0] = auto_nightlight.findViewById(R.id.item_percent_tv1);
        nightlight_tv[1] = auto_nightlight.findViewById(R.id.item_percent_tv2);
        nightlight_tv[2] = auto_nightlight.findViewById(R.id.item_percent_tv3);
        nightlight_tv[3] = auto_nightlight.findViewById(R.id.item_percent_tv4);
        nightlight_tv[4] = auto_nightlight.findViewById(R.id.item_percent_tv5);
        nightlight_tv[5] = auto_nightlight.findViewById(R.id.item_percent_tv6);

        ChartHelper.initLineChart(auto_line_chart);
        ChartHelper.initBarChart(auto_spectrum);
    }

    @Override
    protected void initData() {
        mTimeFormat = GlobalSettings.getTimeFormat();
        mTimeFormat.setTimeZone(new SimpleTimeZone(0, ""));
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<ExoLed>() {
            @Override
            public void onChanged(@Nullable ExoLed exoLed) {
                if (!mEditing) {
                    initParam();
                    refreshData();
                }
            }
        });

        mLightSpectrum = SpectrumUtil.loadDataFromAssets(getContext().getAssets(), "exoterrastrip_spectrum_450.txt");

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeTickReceiver, filter);

        refreshData();
        auto_line_chart.animateXY(1000, 1000);
        auto_spectrum.animateXY(1000, 1000);
        showTimeLine();
    }

    @Override
    protected void initEvent() {
        auto_sunrise_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showEditSunriseDialog(false);
                addFragmentToStack(R.id.device_fl_btm, EditSunriseSusetFragment.newInstance(false));
            }
        });
        auto_sunset_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showEditSunriseDialog(true);
                addFragmentToStack(R.id.device_fl_btm, EditSunriseSusetFragment.newInstance(true));
            }
        });
        auto_turnoff_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showEditTurnoffDialog();
                addFragmentToStack(R.id.device_fl_btm, new EditTurnoffFragment());
            }
        });
        auto_daylight_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showEditDayNightDialog(false);
                addFragmentToStack(R.id.device_fl_btm, EditDayNightFragment.newInstance(false));
            }
        });
        auto_nightlight_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showEditDayNightDialog(true);
                addFragmentToStack(R.id.device_fl_btm, EditDayNightFragment.newInstance(true));
            }
        });
    }

    private void initParam() {
        chnCount = mLight.getChannelCount();
        sunriseStart = mLight.getSunrise();
        sunriseRamp = mLight.getSunriseRamp();
        sunsetEnd = mLight.getSunset();
        sunsetRamp = mLight.getSunsetRamp();
        turnoffEnable = mLight.getTurnoffEnable();
        turnoffTime = mLight.getTurnoffTime();
        int[] brights = mLight.getDayBrights();
        if (brights == null) {
            return;
        }
        dayBrights = Arrays.copyOf(brights, brights.length);
        brights = mLight.getNightBrights();
        if (brights == null) {
            return;
        }
        nightBrights = Arrays.copyOf(brights, brights.length);
    }

    private void showTimeLine() {
        int zone = mLight.getZone();
        long tm = System.currentTimeMillis()/60000 + zone;
        int minutes = (int) (tm%1440);
        LimitLine line = new LimitLine(minutes);
        line.setLineColor(Color.WHITE);
        line.setLineWidth(1);
        line.enableDashedLine(10, 20, 0);
        auto_line_chart.getXAxis().removeAllLimitLines();
        auto_line_chart.getXAxis().addLimitLine(line);
        auto_line_chart.invalidate();
    }

    private void refresSpectrum() {
        int zone = mLight.getZone();
        long tm = System.currentTimeMillis()/60000 + zone;
        int minutes = (int) (tm%1440);

        int cnt = mLight.getChannelCount();
        int t1 = mLight.getSunrise();
        int d1 = mLight.getSunriseRamp();
        int t2 = mLight.getSunset();
        int d2 = mLight.getSunsetRamp();
        boolean enable = mLight.getTurnoffEnable();
        int t3 = mLight.getTurnoffTime();
        int[] dbrts = mLight.getDayBrights();
        int[] nbrts = mLight.getNightBrights();
        int sunriseEnd = (t1 + d1) % 1440;
        int sunsetStart = (1440 + t2 - d2) % 1440;
        int[] time;
        int[][] brights;

        if (enable) {
            time = new int[]{t1, sunriseEnd, sunsetStart, t2, t3, t3};
            brights = new int[6][cnt];
            for (int i = 0; i < cnt; i++) {
                brights[0][i] = 0;
                brights[1][i] = dbrts[i];
                brights[2][i] = dbrts[i];
                brights[3][i] = nbrts[i];
                brights[4][i] = nbrts[i];
                brights[5][i] = 0;
            }
        }
        else {
            time = new int[]{t1, sunriseEnd, sunsetStart, t2};
            brights = new int[4][cnt];
            for (int i = 0; i < cnt; i++) {
                brights[0][i] = nbrts[i];
                brights[1][i] = dbrts[i];
                brights[2][i] = dbrts[i];
                brights[3][i] = nbrts[i];
            }
        }
        int duration = 0;
        int span = 0;
        for (int i = 0; i < time.length; i++) {
            int j = (i+1)%time.length;
            if (time[i] < time[j]) {
                if (minutes >= time[i] && minutes < time[j]) {
                    duration = time[j] - time[i];
                    span = minutes - time[i];
                    for (int k = 0; k < cnt; k++) {
                        int db = brights[j][k] - brights[i][k];
                        int val = brights[i][k] + db * span / duration;
                        mLightSpectrum.setGain(k, ((float) val)/100);
                    }
                    break;
                }
            } else if (time[i] > time[j]) {
                if (minutes >= time[i] || minutes < time[j]) {
                    duration = 1440 - time[i] + time[j];
                    span = 1440 - time[i] + minutes;
                    for (int k = 0; k < cnt; k++) {
                        int db = brights[j][k] - brights[i][k];
                        int val = brights[i][k] + db * span / duration;
                        mLightSpectrum.setGain(k, ((float) val)/100);
                    }
                    break;
                }
            } else {
                if (minutes == time[j]) {
                    for (int k = 0; k < cnt; k++) {
                        mLightSpectrum.setGain(k, ((float) brights[j][k])/100);
                    }
                    break;
                }
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
        auto_spectrum.setData(barData);
        auto_spectrum.invalidate();
    }

    private String getPercentText(int val) {
        if (val >= 100) {
            return "100%";
        }
        if (val >= 10) {
            return "  " + val + "%";
        }
        if (val >= 0) {
            return "    " + val + "%";
        }
        return "    0%";
    }

    private String getTimeText(int time) {
        return mTimeFormat.format(time*60000);
    }

    private void refreshData() {
        if (mLight == null) {
            return;
        }
        auto_sunrise.setError(null);
        auto_sunset.setError(null);
        auto_turnoff.setError(null);
        List<ILineDataSet> dataSets = new ArrayList<>();

        int sunriseEnd = (sunriseStart + sunriseRamp) % 1440;
        int sunsetStart = (1440 + sunsetEnd - sunsetRamp) % 1440;
        int[] time;
        int[][] brights;

        if (turnoffEnable) {
            time = new int[]{sunriseStart, sunriseEnd, sunsetStart, sunsetEnd, turnoffTime, turnoffTime};
            brights = new int[6][chnCount];
            for (int i = 0; i < chnCount; i++) {
                brights[0][i] = 0;
                brights[1][i] = dayBrights[i];
                brights[2][i] = dayBrights[i];
                brights[3][i] = nightBrights[i];
                brights[4][i] = nightBrights[i];
                brights[5][i] = 0;
            }
        }
        else {
            time = new int[]{sunriseStart, sunriseEnd, sunsetStart, sunsetEnd};
            brights = new int[4][chnCount];
            for (int i = 0; i < chnCount; i++) {
                brights[0][i] = nightBrights[i];
                brights[1][i] = dayBrights[i];
                brights[2][i] = dayBrights[i];
                brights[3][i] = nightBrights[i];
            }
        }

        //sort time index
        int[] index = new int[time.length];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }
        for (int i = index.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (time[index[j]] > time[index[j + 1]]) {
                    int tmp = index[j];
                    index[j] = index[j + 1];
                    index[j + 1] = tmp;
                }
            }
        }
        //check time valid
        boolean b = true;
        for (int i = 0; i < index.length; i++) {
            if ((index[i] + 1) % index.length != index[(i + 1) % index.length]) {
                b = false;
                break;
            }
        }
        if (!b) {
            auto_sunrise.setError("");
            auto_sunset.setError("");
            auto_turnoff.setError("");
        }

        //chart
        for (int i = 0; i < chnCount; i++) {
            List<Entry> entries = new ArrayList<>();
            if (b) {
                int ts = time[index[0]];
                int te = time[index[index.length - 1]];
                int bs = brights[index[0]][i];
                int be = brights[index[index.length - 1]][i];
                int duration = 1440 - te + ts;
                int dbrt = bs - be;
                float b0 = be + dbrt * (1440 - te) / (float) duration;
                entries.add(new Entry(0, b0));
                int idx;
                for (int j = 0; j < index.length; j++) {
                    idx = index[j];
                    entries.add(new Entry(time[idx], brights[idx][i]));
                }
                entries.add(new Entry(1440, b0));
            }

            String color = mLight.getChannelName(i);
            if (color.endsWith("\0")) {
                color = color.substring(0, color.length() - 1);
            }
            LineDataSet lineDataSet = new LineDataSet(entries, color);
            lineDataSet.setColor(LightUtil.getColorValue(color));
            lineDataSet.setCircleRadius(3.0f);
            lineDataSet.setCircleColor(LightUtil.getColorValue(color));
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setLineWidth(2.0f);
            dataSets.add(lineDataSet);
        }
        LineData lineData = new LineData(dataSets);
        auto_line_chart.setData(lineData);
        auto_line_chart.invalidate();

        auto_sunrise.setText(getTimeText(sunriseStart) + " ~ " + getTimeText(sunriseEnd));
        auto_sunset.setText(getTimeText(sunsetStart) + " ~ " + getTimeText(sunsetEnd));
        for (int i = 0; i < chnCount; i++) {
            String name = mLight.getChannelName(i);
            int color = LightUtil.getColorValue(name);
            daylight_tv[i].setVisibility(View.VISIBLE);
            daylight_tv[i].setText(getPercentText(dayBrights[i]));
            daylight_tv[i].setBackgroundColor(color);
            nightlight_tv[i].setVisibility(View.VISIBLE);
            nightlight_tv[i].setText(getPercentText(nightBrights[i]));
            nightlight_tv[i].setBackgroundColor(color);
        }
        for (int i = chnCount; i < 6; i++) {
            daylight_tv[i].setVisibility(View.GONE);
            nightlight_tv[i].setVisibility(View.GONE);
        }
        if (mLight.getTurnoffEnable()) {
            auto_turnoff.setText(getTimeText(turnoffTime));
        }
        else {
            auto_turnoff.setText(R.string.disabled);
        }
        refresSpectrum();
    }

//    private void showEditSunriseDialog(final boolean sunset) {
//        if (mLight == null) {
//            return;
//        }
//        mEditing = true;
//        View view = LayoutInflater.from(getContext())
//                                  .inflate(R.layout.dialog_edit_sunrise_sunset, null, false);
//        int height = (int) (auto_line_chart.getHeight() * 1.2f);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
//        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
//        dialog.setContentView(view);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);
//        TextView tv_text = view.findViewById(R.id.dialog_sunrs_text);
//        final TimePicker tp_time = view.findViewById(R.id.dialog_sunrs_time);
//        final NumberPicker np_ramp = view.findViewById(R.id.dialog_sunrs_ramp);
//        Button btn_cancel = view.findViewById(R.id.btn_cancel);
//        Button btn_save = view.findViewById(R.id.btn_save);
//        String[] displayValues = new String[25];
//        for (int i = 0; i < 25; i++) {
//            displayValues[i] = "" + i * 10 + " min";
//        }
//        np_ramp.setMaxValue(24);
//        np_ramp.setMinValue(0);
//        np_ramp.setDisplayedValues(displayValues);
//        int time;
//        if (sunset) {
//            tv_text.setText(R.string.sunset);
//            time = sunsetEnd;
//            np_ramp.setValue(sunsetRamp / 10);
//        }
//        else {
//            time = sunriseStart;
//            np_ramp.setValue(sunriseRamp / 10);
//        }
//        tp_time.setIs24HourView(GlobalSettings.is24HourFormat());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            tp_time.setHour(time / 60);
//            tp_time.setMinute(time % 60);
//        }
//        else {
//            tp_time.setCurrentHour(time / 60);
//            tp_time.setCurrentMinute(time % 60);
//        }
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                initParam();
//                refreshData();
//                dialog.dismiss();
//                mEditing = false;
//            }
//        });
//        btn_save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int t;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                    t = tp_time.getHour() * 60 + tp_time.getMinute();
//                }
//                else {
//                    t = tp_time.getCurrentHour() * 60 + tp_time.getCurrentMinute();
//                }
//                int ramp = np_ramp.getValue() * 10;
//                if (sunset) {
//                    mLightViewModel.setSunsetAndRamp(t, ramp);
//                }
//                else {
//                    mLightViewModel.setSunriseAndRamp(t, ramp);
//                }
//                dialog.dismiss();
//                mEditing = false;
//            }
//        });
//        tp_time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                int t = hourOfDay * 60 + minute;
//                if (sunset) {
//                    sunsetEnd = t;
//                }
//                else {
//                    sunriseStart = t;
//                }
//                refreshData();
//            }
//        });
//        np_ramp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                if (sunset) {
//                    sunsetRamp = newVal * 10;
//                }
//                else {
//                    sunriseRamp = newVal * 10;
//                }
//                refreshData();
//            }
//        });
//        dialog.show();
//    }

//    private void showEditTurnoffDialog() {
//        if (mLight == null) {
//            return;
//        }
//        mEditing = true;
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_turnoff, null, false);
//        int height = (int) (auto_line_chart.getHeight() * 1.2f);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
//        final Switch sw_enable = view.findViewById(R.id.dialog_turnoff_enable);
//        final TimePicker tp_turnoff = view.findViewById(R.id.dialog_turnoff_time);
//        Button btn_cancel = view.findViewById(R.id.btn_cancel);
//        Button btn_save = view.findViewById(R.id.btn_save);
//        sw_enable.setChecked(turnoffEnable);
//        tp_turnoff.setIs24HourView(GlobalSettings.is24HourFormat());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            tp_turnoff.setHour(turnoffTime / 60);
//            tp_turnoff.setMinute(turnoffTime % 60);
//        }
//        else {
//            tp_turnoff.setCurrentHour(turnoffTime / 60);
//            tp_turnoff.setCurrentMinute(turnoffTime % 60);
//        }
//        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
//        dialog.setContentView(view);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);
//        sw_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                turnoffEnable = isChecked;
//                refreshData();
//            }
//        });
//        tp_turnoff.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                turnoffTime = hourOfDay * 60 + minute;
//                refreshData();
//            }
//        });
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                initParam();
//                refreshData();
//                dialog.dismiss();
//                mEditing = false;
//            }
//        });
//        btn_save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                boolean enable = sw_enable.isChecked();
//                int time;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                    time = tp_turnoff.getHour() * 60 + tp_turnoff.getMinute();
//                }
//                else {
//                    time = tp_turnoff.getCurrentHour() * 60 + tp_turnoff.getCurrentMinute();
//                }
//                mLightViewModel.setTurnoff(enable, time);
//                dialog.dismiss();
//                mEditing = false;
//            }
//        });
//        dialog.show();
//    }

//    private void showEditDayNightDialog(final boolean night) {
//        if (mLight == null) {
//            return;
//        }
//        mEditing = true;
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_day_night, null, false);
//        int height = (int) (auto_line_chart.getHeight() * 1.2f);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
//        TextView title = view.findViewById(R.id.dialog_daynight_title);
//        ListView listView = view.findViewById(R.id.dialog_daynight_lv);
//        Button btn_cancel = view.findViewById(R.id.dialog_daynight_cancel);
//        Button btn_save = view.findViewById(R.id.dialog_daynight_save);
//        title.setText(night ? R.string.night_light : R.string.day_light);
//        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
//        dialog.setContentView(view);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);
//        DialogSliderAdeapter adapter = new DialogSliderAdeapter(night ? nightBrights : dayBrights);
//        listView.setAdapter(adapter);
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                initParam();
//                refreshData();
//                dialog.dismiss();
//                mEditing = false;
//            }
//        });
//        btn_save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (night) {
//                    mLightViewModel.setNightBrights(nightBrights);
//                }
//                else {
//                    mLightViewModel.setDayBrights(dayBrights);
//                }
//                dialog.dismiss();
//                mEditing = false;
//            }
//        });
//        dialog.show();
//    }

//    class DialogSliderAdeapter extends BaseAdapter {
//        private byte[] mBrights;
//
//        public DialogSliderAdeapter(byte[] brights) {
//            mBrights = brights;
//        }
//
//        @Override
//        public int getCount() {
//            return mBrights == null ? 0 : mBrights.length;
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return mBrights[i];
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return i;
//        }
//
//        @Override
//        public View getView(final int postion, View view, ViewGroup viewGroup) {
//            ViewHolder holder;
//            if (view == null) {
//                view = LayoutInflater.from(getContext())
//                                     .inflate(R.layout.dialog_item_slider, viewGroup, false);
//                holder = new ViewHolder();
////                holder.iv_icon = view.findViewById(R.id.dialog_item_slider_color);
//                holder.sb_progress = view.findViewById(R.id.dialog_item_slider_progress);
//                holder.tv_percent = view.findViewById(R.id.dialog_item_slider_percent);
//                view.setTag(holder);
//            }
//            else {
//                holder = (ViewHolder) view.getTag();
//            }
//            String name = mLight.getChannelName(postion);
//            int color = LightUtil.getColorValue(name);
//
////            GradientDrawable icon = (GradientDrawable) holder.iv_icon.getDrawable();
////            icon.setColor(color);
//            //动态设置SeekBar progressDrawable
//            holder.sb_progress.setProgressDrawable(LightUtil.getProgressDrawable(getContext(), name));
//            //动态设置SeekBar thumb
//            GradientDrawable thumb = (GradientDrawable) holder.sb_progress.getThumb();
//            thumb.setColor(color);
//
//            holder.sb_progress.setProgress(mBrights[postion]);
//            holder.tv_percent.setText("" + mBrights[postion] + "%");
//            final ViewHolder finalHolder = holder;
//            holder.sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                    if (b) {
//                        mBrights[postion] = (byte) i;
//                        finalHolder.tv_percent.setText("" + i + "%");
//                        refreshData();
//                    }
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) {
//
//                }
//
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) {
//
//                }
//            });
//            return view;
//        }
//    }

    class ViewHolder {
//        private ImageView iv_icon;
        private SeekBar sb_progress;
        private TextView tv_percent;
    }
}
