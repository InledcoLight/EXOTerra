package com.liruya.exoterra.device.light;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.liruya.exoterra.R;
import com.liruya.exoterra.base.BaseFragment;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.util.LightUtil;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.xlink.restful.api.app.DeviceApi;

public class LightAutoFragment extends BaseFragment {
    private LineChart auto_line_chart;
    private ImageView auto_iv_sunrise;
    private TextView auto_tv_sunrise;
    private TextView auto_tv_sunset;
    private TextView auto_tv_turnoff;
    private TextView auto_tv_day;
    private TextView auto_tv_night;
    private CheckedTextView auto_ctv_gis;
    private LinearLayout auto_ll;
    private SwitchCompat auto_sw_gis;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;

    private int chnCount;
    private boolean gisValid;
    private int sunriseStart;
    private int sunriseRamp;
    private int sunsetEnd;
    private int sunsetRamp;
    private boolean turnoffEnable;
    private int turnoffTime;
    private byte[] dayBrights;
    private byte[] nightBrights;

    private boolean mEditing;

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
        return R.layout.fragment_light_auto;
    }

    @Override
    protected void initView(View view) {
        auto_line_chart = view.findViewById(R.id.auto_line_chart);
        auto_iv_sunrise = view.findViewById(R.id.auto_iv_sunrise);
        auto_tv_sunrise = view.findViewById(R.id.auto_tv_sunrise);
        auto_tv_sunset = view.findViewById(R.id.auto_tv_sunset);
        auto_tv_turnoff = view.findViewById(R.id.auto_tv_turnoff);
        auto_tv_day = view.findViewById(R.id.auto_tv_day);
        auto_tv_night = view.findViewById(R.id.auto_tv_night);
        auto_ll = view.findViewById(R.id.auto_ll);
        auto_sw_gis = view.findViewById(R.id.auto_sw_gis);
//        auto_ctv_gis = view.findViewById(R.id.auto_ctv_gis);

        LineChartHelper.init(auto_line_chart);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity())
                                            .get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
                if (!mEditing) {
                    initParam();
                    refreshData();
                }
            }
        });
    }

    @Override
    protected void initEvent() {
//        auto_ctv_gis.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (auto_ctv_gis.isChecked()) {
//                    showDisableGisDialog();
//                }
//                else {
//                    showEnableGisDialog();
//                }
//            }
//        });
        auto_tv_sunrise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditSunriseDialog(false);
            }
        });
        auto_tv_sunset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditSunriseDialog(true);
            }
        });
        auto_tv_turnoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTurnoffDialog();
            }
        });
        auto_tv_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDayNightDialog(false);
            }
        });
        auto_tv_night.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDayNightDialog(true);
            }
        });
        auto_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auto_sw_gis.isChecked()) {
                    showDisableGisDialog();
                }
                else {
                    showEnableGisDialog();
                }
            }
        });
    }

    private void initParam() {
        chnCount = mLight.getChannelCount();
        gisValid = mLight.getGisEnable() && mLight.getGisValid();
        sunriseStart = (gisValid ? mLight.getGisSunrise() : mLight.getSunrise());
        sunriseRamp = (gisValid ? ((1440 + mLight.getGisSunset() - mLight.getGisSunrise()) % 1440) / 10 : mLight.getSunriseRamp());
        sunsetEnd = (gisValid ? mLight.getGisSunset() : mLight.getSunset());
        sunsetRamp = (gisValid ? ((1440 + mLight.getGisSunset() - mLight.getGisSunrise()) % 1440) / 10 : mLight.getSunsetRamp());
        turnoffEnable = mLight.getTurnoffEnable();
        turnoffTime = mLight.getTurnoffTime();
        byte[] brights = mLight.getDayBrights();
        dayBrights = Arrays.copyOf(brights, brights.length);
        brights = mLight.getNightBrights();
        nightBrights = Arrays.copyOf(brights, brights.length);
    }

    private void refreshData() {
        if (mLight == null) {
            return;
        }
        auto_tv_sunrise.setError(null);
        auto_tv_sunset.setError(null);
        auto_tv_turnoff.setError(null);
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
            if (!gisValid) {
                auto_tv_sunrise.setError("");
                auto_tv_sunset.setError("");
            }
            auto_tv_turnoff.setError("");
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

        DecimalFormat df = new DecimalFormat("00");
        //        auto_tv_sunrise.setText(df.format(sunriseStart/60) + ":" + df.format(sunriseStart%60) +
        //                                "\n~\n" + sunriseRamp + " min");
        //        auto_tv_sunset.setText(df.format(sunsetEnd/60) + ":" + df.format(sunsetEnd%60) +
        //                               "\n~\n" + sunsetRamp + " min");
        auto_tv_sunrise.setText(df.format(sunriseStart / 60) + ":" + df.format(sunriseStart % 60) + "\n~\n" + df.format(sunriseEnd / 60) + ":" + df.format(sunriseEnd % 60));
        auto_tv_sunset.setText(df.format(sunsetStart / 60) + ":" + df.format(sunsetStart % 60) + "\n~\n" + df.format(sunsetEnd / 60) + ":" + df.format(sunsetEnd % 60));
        SpannableStringBuilder sp1 = new SpannableStringBuilder();
        SpannableStringBuilder sp2 = new SpannableStringBuilder();
        for (int i = 0; i < chnCount; i++) {
            String color = mLight.getChannelName(i);
            ImageSpan icon = new ImageSpan(getContext(), LightUtil.getIconRes(color));
            sp1.append(" ");
            sp1.setSpan(icon, sp1.length() - 1, sp1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp1.append("  " + dayBrights[i] + " %\n");
            sp2.append(" ");
            sp2.setSpan(icon, sp2.length() - 1, sp2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp2.append("  " + nightBrights[i] + " %\n");
        }
        auto_tv_day.setText(sp1, TextView.BufferType.SPANNABLE);
        auto_tv_night.setText(sp2, TextView.BufferType.SPANNABLE);
        if (mLight.getTurnoffEnable()) {
            auto_tv_turnoff.setText(df.format(turnoffTime / 60) + ":" + df.format(turnoffTime % 60));
        }
        else {
            auto_tv_turnoff.setText(R.string.disabled);
        }
        auto_sw_gis.setChecked(mLight.getGisEnable());
        auto_sw_gis.setText(mLight.getGisEnable() ? R.string.sync_func_enabled : R.string.sync_func_disabled);
//        auto_ctv_gis.setChecked(mLight.getGisEnable());
//        auto_ctv_gis.setText(mLight.getGisEnable() ? "Gis Enabled" : "Gis Disabled");
    }

    private void showDisableGisDialog() {
        if (mLight == null) {
            return;
        }
        DecimalFormat df = new DecimalFormat("00");
        int sunrise = mLight.getSunrise();
        int sunset = mLight.getSunset();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Disable Synchro Function")
               .setMessage("Once disable, device will run the sunrise and sunset settings of manual settings. ")
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       mLightViewModel.setGisEnable(false);
                   }
               })
               .show();
    }

    private void showEnableGisDialog() {
        if (mLight == null) {
            return;
        }
        DecimalFormat df = new DecimalFormat("00");
//        int sunrise = mLight.getGisSunrise();
//        int sunset = mLight.getGisSunset();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enable Synchro Function")
               .setMessage("Once enable, device will run according to local sunrise and sunset time.")
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       XlinkCloudManager.getInstance().getDeviceLocation(mLight.getXDevice(), new XlinkRequestCallback<DeviceApi.DeviceGeographyResponse>() {
                           @Override
                           public void onError(String error) {
                               Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                    .show();
                           }

                           @Override
                           public void onSuccess(DeviceApi.DeviceGeographyResponse response) {
                               mLightViewModel.setGisEnable(true, (float) response.lon, (float) response.lat);
                           }
                       });
                   }
               })
               .show();
    }

    @SuppressLint ("RestrictedApi")
    private void showEditSunriseDialog(final boolean sunset) {
        if (mLight == null) {
            return;
        }
        if (gisValid) {
            Toast.makeText(getContext(), "已开启定位功能，自动获取日出日落时间.", Toast.LENGTH_LONG)
                 .show();
            return;
        }
        mEditing = true;
        int height = auto_iv_sunrise.getHeight();
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.dialog_edit_sunrise_sunset, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        CheckableImageButton cib_bg = view.findViewById(R.id.dialog_sunrs_bg);
        TextView tv_text = view.findViewById(R.id.dialog_sunrs_text);
        final TimePicker tp_time = view.findViewById(R.id.dialog_sunrs_time);
        final NumberPicker np_ramp = view.findViewById(R.id.dialog_sunrs_ramp);
        Button btn_cancel = view.findViewById(R.id.dialog_sunrs_cancel);
        Button btn_save = view.findViewById(R.id.dialog_sunrs_save);
        String[] displayValues = new String[25];
        for (int i = 0; i < 25; i++) {
            displayValues[i] = "" + i * 10 + " min";
        }
        np_ramp.setMaxValue(24);
        np_ramp.setMinValue(0);
        np_ramp.setDisplayedValues(displayValues);
        cib_bg.setChecked(sunset);
        int time;
        if (sunset) {
            tv_text.setText(R.string.sunset);
            time = sunsetEnd;
            np_ramp.setValue(sunsetRamp / 10);
        }
        else {
            time = sunriseStart;
            np_ramp.setValue(sunriseRamp / 10);
        }
        tp_time.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tp_time.setHour(time / 60);
            tp_time.setMinute(time % 60);
        }
        else {
            tp_time.setCurrentHour(time / 60);
            tp_time.setCurrentMinute(time % 60);
        }
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initParam();
                refreshData();
                dialog.dismiss();
                mEditing = false;
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int t;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    t = tp_time.getHour() * 60 + tp_time.getMinute();
                }
                else {
                    t = tp_time.getCurrentHour() * 60 + tp_time.getCurrentMinute();
                }
                int ramp = np_ramp.getValue() * 10;
                if (sunset) {
                    mLightViewModel.setSunsetAndRamp(t, ramp);
                }
                else {
                    mLightViewModel.setSunriseAndRamp(t, ramp);
                }
                dialog.dismiss();
                mEditing = false;
            }
        });
        tp_time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                int t = hourOfDay * 60 + minute;
                if (sunset) {
                    sunsetEnd = t;
                }
                else {
                    sunriseStart = t;
                }
                refreshData();
            }
        });
        np_ramp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (sunset) {
                    sunsetRamp = newVal * 10;
                }
                else {
                    sunriseRamp = newVal * 10;
                }
                refreshData();
            }
        });
        dialog.show();
    }

    private void showEditTurnoffDialog() {
        if (mLight == null) {
            return;
        }
        mEditing = true;
        int height = auto_iv_sunrise.getHeight();
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.dialog_edit_turnoff, null, false);
        final Switch sw_enable = view.findViewById(R.id.dialog_turnoff_enable);
        final TimePicker tp_turnoff = view.findViewById(R.id.dialog_turnoff_time);
        Button btn_cancel = view.findViewById(R.id.dialog_turnoff_cancel);
        Button btn_save = view.findViewById(R.id.dialog_turnoff_save);
        sw_enable.setChecked(turnoffEnable);
        tp_turnoff.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tp_turnoff.setHour(turnoffTime / 60);
            tp_turnoff.setMinute(turnoffTime % 60);
        }
        else {
            tp_turnoff.setCurrentHour(turnoffTime / 60);
            tp_turnoff.setCurrentMinute(turnoffTime % 60);
        }
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        sw_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                turnoffEnable = isChecked;
                refreshData();
            }
        });
        tp_turnoff.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                turnoffTime = hourOfDay * 60 + minute;
                refreshData();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initParam();
                refreshData();
                dialog.dismiss();
                mEditing = false;
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enable = sw_enable.isChecked();
                int time;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    time = tp_turnoff.getHour() * 60 + tp_turnoff.getMinute();
                }
                else {
                    time = tp_turnoff.getCurrentHour() * 60 + tp_turnoff.getCurrentMinute();
                }
                mLightViewModel.setTurnoff(enable, time);
                dialog.dismiss();
                mEditing = false;
            }
        });
        dialog.show();
    }

    @SuppressLint ("RestrictedApi")
    private void showEditDayNightDialog(final boolean night) {
        if (mLight == null) {
            return;
        }
        mEditing = true;
        int height = auto_iv_sunrise.getHeight();
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.dialog_edit_day_night, null, false);
        CheckableImageButton cib_bg = view.findViewById(R.id.dialog_daynight_bg);
        ListView listView = view.findViewById(R.id.dialog_daynight_lv);
        Button btn_cancel = view.findViewById(R.id.dialog_daynight_cancel);
        Button btn_save = view.findViewById(R.id.dialog_daynight_save);
        cib_bg.setChecked(night);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        DialogSliderAdeapter adapter = new DialogSliderAdeapter(night ? nightBrights : dayBrights);
        listView.setAdapter(adapter);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initParam();
                refreshData();
                dialog.dismiss();
                mEditing = false;
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (night) {
                    mLightViewModel.setNightBrights(nightBrights);
                }
                else {
                    mLightViewModel.setDayBrights(dayBrights);
                }
                dialog.dismiss();
                mEditing = false;
            }
        });
        dialog.show();
    }

    class DialogSliderAdeapter extends BaseAdapter {
        private byte[] mBrights;

        public DialogSliderAdeapter(byte[] brights) {
            mBrights = brights;
        }

        @Override
        public int getCount() {
            return mBrights == null ? 0 : mBrights.length;
        }

        @Override
        public Object getItem(int i) {
            return mBrights[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int postion, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(getContext())
                                     .inflate(R.layout.dialog_item_slider, viewGroup, false);
                holder = new ViewHolder();
                holder.iv_icon = view.findViewById(R.id.dialog_item_slider_color);
                holder.sb_progress = view.findViewById(R.id.dialog_item_slider_progress);
                holder.tv_percent = view.findViewById(R.id.dialog_item_slider_percent);
                view.setTag(holder);
            }
            else {
                holder = (ViewHolder) view.getTag();
            }
            String color = mLight.getChannelName(postion);
            holder.iv_icon.setImageResource(LightUtil.getIconRes(color));
            Drawable progressDraw = getResources().getDrawable(LightUtil.getProgressRes(color));
            Drawable thumbDraw = getResources().getDrawable(LightUtil.getThumbRes(color));
            holder.sb_progress.setProgressDrawable(progressDraw);
            holder.sb_progress.setThumb(thumbDraw);
            holder.sb_progress.setProgress(mBrights[postion]);
            holder.tv_percent.setText("" + mBrights[postion] + "%");
            final ViewHolder finalHolder = holder;
            holder.sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        mBrights[postion] = (byte) i;
                        finalHolder.tv_percent.setText("" + i + "%");
                        refreshData();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            return view;
        }
    }

    class ViewHolder {
        private ImageView iv_icon;
        private SeekBar sb_progress;
        private TextView tv_percent;
    }
}
