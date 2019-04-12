package com.liruya.exoterra.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.liruya.exoterra.R;

import java.text.DecimalFormat;

public class CustomTimePicker extends TimePicker {
    private NumberPicker mMinuteNumberPicker;
    private boolean mIs24Hour;
    private int mInterval;

    public CustomTimePicker(Context context) {
        this(context, null, 0);
    }

    public CustomTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMinuteNumberPicker = findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTimePicker);
        mIs24Hour = a.getBoolean(R.styleable.CustomTimePicker_is24Hour, true);
        mInterval = a.getInt(R.styleable.CustomTimePicker_interval, 5);
        if (mInterval < 1 || mInterval >= 60 || 60%mInterval != 0) {
            mInterval = 1;
        }
        setIs24HourView(mIs24Hour);
        if (mMinuteNumberPicker != null) {
            int count = 60/mInterval;
            int tmr;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tmr = super.getHour()*60 + super.getMinute();
            } else {
                tmr = super.getCurrentHour()*60 + super.getCurrentMinute();
            }
            if (tmr%mInterval != 0) {
                tmr = (tmr+mInterval)%1440;
            }
            mMinuteNumberPicker.setMinValue(0);
            mMinuteNumberPicker.setMaxValue(count - 1);
            setHour(tmr/60);
            setMinute((tmr%60)/mInterval);
            String[] values = new String[count];
            DecimalFormat df = new DecimalFormat("00");
            for (int i = 0; i < count; i++) {
                values[i] = df.format(i*mInterval);
            }
            mMinuteNumberPicker.setDisplayedValues(values);
        }
    }

    @Override
    public int getHour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return super.getHour();
        } else {
            return super.getCurrentHour();
        }
    }

    @Override
    public int getMinute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return super.getMinute()*mInterval;
        } else {
            return super.getCurrentMinute()*mInterval;
        }
    }

    @Override
    public void setHour(int hour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.setHour(hour);
        } else {
            super.setCurrentHour(hour);
        }
    }

    @Override
    public void setMinute(int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.setMinute(minute);
        } else {
            super.setCurrentMinute(minute);
        }
    }

    public int getInterval() {
        return mInterval;
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }
}
