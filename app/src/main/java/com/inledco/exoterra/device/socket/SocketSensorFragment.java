package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.util.TimeFormatUtil;

import java.text.DateFormat;

public class SocketSensorFragment extends BaseFragment {

    private TextView socket_sensor_title;
    private TextView socket_sensor_daytime;
    private ImageButton socket_sensor_daytime_edit;
    private ImageView socket_sensor_icon;
    private TextView socket_sensor_day;
    private ImageButton socket_sensor_day_edit;
    private TextView socket_sensor_night;
    private ImageButton socket_sensor_night_edit;

    private SocketViewModel mSocketViewModel;
    private EXOSocket mSocket;

    private DateFormat mTimeFormat;

    private int mDaytimeStart;
    private int mDaytimeEnd;
    private int mDayThrd;
    private int mNightThrd;

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
        return R.layout.fragment_socket_sensor;
    }

    @Override
    protected void initView(View view) {
        socket_sensor_title = view.findViewById(R.id.socket_sensor_title);
        socket_sensor_daytime = view.findViewById(R.id.socket_sensor_daytime);
        socket_sensor_daytime_edit = view.findViewById(R.id.socket_sensor_daytime_edit);
        socket_sensor_icon = view.findViewById(R.id.socket_sensor_icon);
        socket_sensor_day = view.findViewById(R.id.socket_sensor_day);
        socket_sensor_day_edit = view.findViewById(R.id.socket_sensor_day_edit);
        socket_sensor_night = view.findViewById(R.id.socket_sensor_night);
        socket_sensor_night_edit = view.findViewById(R.id.socket_sensor_night_edit);

        socket_sensor_day.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sun, 0, 0, 0);
        socket_sensor_night.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_moon, 0, 0, 0);
    }

    @Override
    protected void initData() {
        mTimeFormat = GlobalSettings.getTimeFormat();
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<EXOSocket>() {
            @Override
            public void onChanged(@Nullable EXOSocket exoSocket) {
                refreshData();
            }
        });

        refreshData();
    }

    @Override
    protected void initEvent() {
        socket_sensor_daytime_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDaytimeDialog();
            }
        });

        socket_sensor_day_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThresholdDialog(false);
            }
        });

        socket_sensor_night_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThresholdDialog(true);
            }
        });
    }

    private String getTimeText(int time) {
        return TimeFormatUtil.formatMinutesTime(mTimeFormat, time);
    }

    private void refreshData() {
        if (mSocket == null) {
            return;
        }
        String tempunit = GlobalSettings.getTemperatureUnit();
        socket_sensor_daytime.setText(getTimeText(mSocket.getDaytimeStart()) + " ~ " + getTimeText(mSocket.getDaytimeEnd()));
        if (mSocket.getMode() == EXOSocket.MODE_SENSOR1) {
            socket_sensor_title.setText(R.string.thermostat);
            socket_sensor_icon.setImageResource(R.drawable.ic_temperature_color_64dp);
            mDayThrd = mSocket.getSV1DayThreshold();
            mNightThrd = mSocket.getSV1NightThreshold();
//            if (mSocket.getSV1Type() != mSocket.getS1Type() || mSocket.getSV2Type() != mSocket.getS2Type()) {
//                mDayThrd = 10;
//                mNightThrd = 40;
//            }
            socket_sensor_day.setText("" + mDayThrd + " " + tempunit);
            socket_sensor_night.setText("" + mNightThrd + " " + tempunit);
        } else if (mSocket.getMode() == EXOSocket.MODE_SENSOR2) {
            socket_sensor_title.setText(R.string.hygrostat);
            socket_sensor_icon.setImageResource(R.drawable.ic_humidity_color_64dp);
            mDayThrd = mSocket.getSV2DayThreshold();
            mNightThrd = mSocket.getSV2NightThreshold();
//            if (mSocket.getSV1Type() != mSocket.getS1Type() || mSocket.getSV2Type() != mSocket.getS2Type()) {
//                mDayThrd = 20;
//                mNightThrd = 80;
//            }
            socket_sensor_day.setText("" + mDayThrd + " %");
            socket_sensor_night.setText("" + mNightThrd + " %");
        }
    }

    private void showDaytimeDialog() {
        final Home home = HomeManager.getInstance().getDeviceHome(mSocket);
        mDaytimeStart = mSocket.getDaytimeStart();
        mDaytimeEnd = mSocket.getDaytimeEnd();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_daytime, null, false);
        CheckBox cb = view.findViewById(R.id.dialog_daytime_cb);
        final RadioGroup rg = view.findViewById(R.id.dialog_daytime_rg);
        final RadioButton start = view.findViewById(R.id.dialog_daytime_start);
        final RadioButton end = view.findViewById(R.id.dialog_daytime_end);
        final TimePicker tp = view.findViewById(R.id.dialog_daytime_tp);
        cb.setVisibility(home != null ? View.VISIBLE : View.GONE);
        rg.check(R.id.dialog_daytime_start);
        start.setText(getTimeText(mDaytimeStart));
        end.setText(getTimeText(mDaytimeEnd));
        tp.setIs24HourView(GlobalSettings.is24HourFormat());
        tp.setCurrentHour(mDaytimeStart/60);
        tp.setCurrentMinute(mDaytimeStart%60);
        final TimePicker.OnTimeChangedListener listener = new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                int value = hourOfDay*60+minute;
                if (start.isChecked()) {
                    mDaytimeStart = value;
                    start.setText(getTimeText(mDaytimeStart));
                } else if (end.isChecked()) {
                    mDaytimeEnd = value;
                    start.setText(getTimeText(mDaytimeEnd));
                }
            }
        };
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (home != null) {
                    if (isChecked) {
                        mDaytimeStart = home.getSunrise();
                        mDaytimeEnd = home.getSunset();
                        rg.clearCheck();
                    } else {
                        mDaytimeStart = mSocket.getDaytimeStart();
                        mDaytimeEnd = mSocket.getDaytimeEnd();
                        rg.check(R.id.dialog_daytime_start);
                    }
                    start.setEnabled(!isChecked);
                    end.setEnabled(!isChecked);
                    start.setText(getTimeText(mDaytimeStart));
                    end.setText(getTimeText(mDaytimeEnd));
                    tp.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                }
            }
        });
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.dialog_daytime_start:
                        tp.setOnTimeChangedListener(null);
                        tp.setCurrentHour(mDaytimeStart/60);
                        tp.setCurrentMinute(mDaytimeStart%60);
                        tp.setOnTimeChangedListener(listener);
                        break;
                    case R.id.dialog_daytime_end:
                        tp.setOnTimeChangedListener(null);
                        tp.setCurrentHour(mDaytimeEnd/60);
                        tp.setCurrentMinute(mDaytimeEnd%60);
                        tp.setOnTimeChangedListener(listener);
                        break;
                }
            }
        });
        tp.setOnTimeChangedListener(listener);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.daytime);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSocketViewModel.setDaytime(mDaytimeStart, mDaytimeEnd);
            }
        });
        builder.show();
    }

    private void showThresholdDialog(final boolean night) {
        int min;
        int max;
        String unit;
        if (mSocket.getMode() == EXOSocket.MODE_SENSOR1) {
            min = 10;
            max = 40;
            unit = "℃";
        } else if (mSocket.getMode() == EXOSocket.MODE_SENSOR2) {
            min = 20;
            max = 80;
            unit = "%";
        } else {
            return;
        }
        String[] values = new String[max-min+1];
        for (int i = 0; i < values.length; i++) {
            values[i] = "" + (min+i) + " " + unit;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_threshold, null, false);
        final NumberPicker np = view.findViewById(R.id.dialog_threshold_np);
        np.setWrapSelectorWheel(false);
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setDisplayedValues(values);
        np.setValue(night ? mNightThrd : mDayThrd);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(night ? "Night Threshold" : "Daytime Threshold");
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int val = np.getValue();
                if (mSocket.getMode() == EXOSocket.MODE_SENSOR1) {
                    if (night) {
                        mSocketViewModel.setSensor1NightThreshold(val);
                    } else {
                        mSocketViewModel.setSensor1DayThreshold(val);
                    }
                } else if (mSocket.getMode() == EXOSocket.MODE_SENSOR2) {
                    if (night) {
                        mSocketViewModel.setSensor2NightThreshold(val);
                    } else {
                        mSocketViewModel.setSensor2DayThreshold(val);
                    }
                }
            }
        });
        builder.show();
    }

//    public interface ISensor {
//        int getMin();
//        int getMax();
//        String getUnit();
//        void setDayThreshold(int thrd);
//        void setNightThreshold(int thrd);
//    }
//
//    public enum Sensor implements ISensor {
//        Temperature {
//            @Override
//            public int getMin() {
//                return 10;
//            }
//
//            @Override
//            public int getMax() {
//                return 40;
//            }
//
//            @Override
//            public String getUnit() {
//                return "℃";
//            }
//
//            @Override
//            public void setDayThreshold(int thrd) {
//                mSocketViewModel.setSensor1DayThreshold(thrd);
//            }
//
//            @Override
//            public void setNightThreshold(int thrd) {
//                mSocketViewModel.setSensor1NightThreshold(thrd);
//            }
//        },
//        Humidity {
//            @Override
//            public int getMin() {
//                return 20;
//            }
//
//            @Override
//            public int getMax() {
//                return 80;
//            }
//
//            @Override
//            public String getUnit() {
//                return "%";
//            }
//
//            @Override
//            public void setDayThreshold(int thrd) {
//                mSocketViewModel.setSensor2DayThreshold(thrd);
//            }
//
//            @Override
//            public void setNightThreshold(int thrd) {
//                mSocketViewModel.setSensor2NightThreshold(thrd);
//            }
//        }
//    }
}
