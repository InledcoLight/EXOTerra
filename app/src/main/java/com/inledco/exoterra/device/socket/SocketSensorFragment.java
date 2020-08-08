package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;

public class SocketSensorFragment extends BaseFragment {

    private TextView socket_sensor_title;
//    private TextView socket_sensor_daytime;
//    private ImageButton socket_sensor_daytime_edit;
    private ImageView socket_sensor_icon;
    private TextView socket_sensor_day;
    private ImageButton socket_sensor_day_edit;
    private TextView socket_sensor_night;
    private ImageButton socket_sensor_night_edit;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;

//    private DateFormat mTimeFormat;

//    private int mSunrise;
//    private int mSunset;
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
//        socket_sensor_daytime = view.findViewById(R.id.socket_sensor_daytime);
//        socket_sensor_daytime_edit = view.findViewById(R.id.socket_sensor_daytime_edit);
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
//        mTimeFormat = GlobalSettings.getTimeFormat();
//        mTimeFormat.setTimeZone(new SimpleTimeZone(0, ""));
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<ExoSocket>() {
            @Override
            public void onChanged(@Nullable ExoSocket exoSocket) {
                refreshData();
            }
        });

        refreshData();
    }

    @Override
    protected void initEvent() {
//        socket_sensor_daytime_edit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDaytimeDialog();
//            }
//        });

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

//    private String getTimeText(int time) {
//        return mTimeFormat.format(time*60000);
//    }

    private void refreshData() {
        if (mSocket == null) {
            return;
        }
//        socket_sensor_daytime.setText(getTimeText(mSocket.getSunrise()) + " ~ " + getTimeText(mSocket.getSunset()));
        ExoSocket.SensorConfig[] configs = mSocket.getSensorConfig();
        ExoSocket.SensorConfig config = null;
        if (configs == null || configs.length == 0 || configs.length > ExoSocket.SENSOR_COUNT_MAX) {
            return;
        }
        if (mSocket.getMode() == ExoSocket.MODE_SENSOR1) {
            config = configs[0];
        } else if (mSocket.getMode() == ExoSocket.MODE_SENSOR2) {
            config = configs[1];
        }
        if (config == null) {
            return;
        }
        int type = config.getType();
        String unit = getSensorUnit(type);
        socket_sensor_title.setText(getSensorName(type));
        socket_sensor_icon.setImageResource(getSensorDrawable(type));
        mDayThrd = config.getDay();
        mNightThrd = config.getNight();
        socket_sensor_day.setText("" + mDayThrd + " " + unit);
        socket_sensor_night.setText("" + mNightThrd + " " + unit);
    }

//    private void showDaytimeDialog() {
//        final Group group = GroupManager.getInstance().getDeviceGroup(mSocket.getProductKey(), mSocket.getDeviceName());
//        mSunrise = mSocket.getSunrise();
//        mSunset = mSocket.getSunset();
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_daytime, null, false);
//        CheckBox cb = view.findViewById(R.id.dialog_daytime_cb);
//        final RadioGroup rg = view.findViewById(R.id.dialog_daytime_rg);
//        final RadioButton start = view.findViewById(R.id.dialog_daytime_start);
//        final RadioButton end = view.findViewById(R.id.dialog_daytime_end);
//        final TimePicker tp = view.findViewById(R.id.dialog_daytime_tp);
//        cb.setVisibility(group != null ? View.VISIBLE : View.GONE);
//        rg.check(R.id.dialog_daytime_start);
//        start.setText(getTimeText(mSunrise));
//        end.setText(getTimeText(mSunset));
//        tp.setIs24HourView(GlobalSettings.is24HourFormat());
//        tp.setCurrentHour(mSunrise / 60);
//        tp.setCurrentMinute(mSunrise % 60);
//        final TimePicker.OnTimeChangedListener listener = new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                int value = hourOfDay*60+minute;
//                if (start.isChecked()) {
//                    mSunrise = value;
//                    start.setText(getTimeText(mSunrise));
//                } else if (end.isChecked()) {
//                    mSunset = value;
//                    start.setText(getTimeText(mSunset));
//                }
//            }
//        };
//        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (group != null) {
//                    if (isChecked) {
//                        mSunrise = group.getSunrise();
//                        mSunset = group.getSunset();
//                        rg.clearCheck();
//                    } else {
//                        mSunrise = mSocket.getSunrise();
//                        mSunset = mSocket.getSunset();
//                        rg.check(R.id.dialog_daytime_start);
//                    }
//                    start.setEnabled(!isChecked);
//                    end.setEnabled(!isChecked);
//                    start.setText(getTimeText(mSunrise));
//                    end.setText(getTimeText(mSunset));
//                    tp.setVisibility(isChecked ? View.GONE : View.VISIBLE);
//                }
//            }
//        });
//        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.dialog_daytime_start:
//                        tp.setOnTimeChangedListener(null);
//                        tp.setCurrentHour(mSunrise / 60);
//                        tp.setCurrentMinute(mSunrise % 60);
//                        tp.setOnTimeChangedListener(listener);
//                        break;
//                    case R.id.dialog_daytime_end:
//                        tp.setOnTimeChangedListener(null);
//                        tp.setCurrentHour(mSunset / 60);
//                        tp.setCurrentMinute(mSunset % 60);
//                        tp.setOnTimeChangedListener(listener);
//                        break;
//                }
//            }
//        });
//        tp.setOnTimeChangedListener(listener);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle(R.string.daytime);
//        builder.setView(view);
//        builder.setNegativeButton(R.string.cancel, null);
//        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mSocketViewModel.setDaytime(mSunrise, mSunset);
//            }
//        });
//        builder.show();
//    }

    private void showThresholdDialog(final boolean night) {
        if (mSocket.getSensorAvailable() == false) {
            return;
        }
        ExoSocket.Sensor[] sensors = mSocket.getSensor();
        final ExoSocket.SensorConfig[] configs = mSocket.getSensorConfig();
        if (sensors == null || sensors.length < 1 || sensors.length > ExoSocket.SENSOR_COUNT_MAX) {
            return;
        }
        if (configs == null || configs.length < 1 || configs.length > ExoSocket.SENSOR_COUNT_MAX) {
            return;
        }
        ExoSocket.Sensor sensor = null;
        ExoSocket.SensorConfig config = null;
        if (mSocket.getMode() == ExoSocket.MODE_SENSOR1) {
            sensor = sensors[0];
            config = configs[0];
        } else if (mSocket.getMode() == ExoSocket.MODE_SENSOR2) {
            sensor = sensors[1];
            config = configs[1];
        }
        if (sensor == null || config == null) {
            return;
        }
        int min = sensor.getMin();
        int max = sensor.getMax();
        String unit = getSensorUnit(sensor.getType());
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
        final ExoSocket.SensorConfig cfg = config;
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int val = np.getValue();
                if (night) {
                    cfg.setNight(val);
                } else {
                    cfg.setDay(val);
                }
                mSocketViewModel.setSensorConfig(configs);
            }
        });
        builder.show();
    }

    private String getSensorUnit(int type) {
        String text = "";
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                text = "℃";
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                text = "%";
                break;
        }
        return text;
    }

    private String getSensorName(int type) {
        String text = "";
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                text = getString(R.string.thermostat);
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                text = getString(R.string.hygrostat);
                break;
        }
        return text;
    }

    private @DrawableRes int getSensorDrawable(int type) {
        int res = 0;
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                res = R.drawable.ic_temperature_color_64dp;
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                res = R.drawable.ic_humidity_color_64dp;
                break;
        }
        return res;
    }
}
