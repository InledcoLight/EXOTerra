package com.inledco.exoterra.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.inledco.exoterra.R;

import java.util.Calendar;
import java.util.Locale;

public class TimePickerWithSecond extends LinearLayout {

    private final TimePickerDelegate mDelegate;

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         * @param second The current second.
         */
        void onTimeChanged(TimePickerWithSecond view, int hourOfDay, int minute, int second);
    }

    public TimePickerWithSecond(@NonNull Context context) {
        this(context, null);
    }

    public TimePickerWithSecond(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePickerWithSecond(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDelegate = new TimePickerDelegate(this, context);
    }

    /**
     * Sets the currently selected hour using 24-hour time.
     *
     * @param hour the hour to set, in the range (0-23)
     * @see #getHour()
     */
    public void setHour(@IntRange(from = 0, to = 23) int hour) {
        mDelegate.setHour(MathUtils.clamp(hour, 0, 23));
    }

    /**
     * Returns the currently selected hour using 24-hour time.
     *
     * @return the currently selected hour, in the range (0-23)
     * @see #setHour(int)
     */
    public int getHour() {
        return mDelegate.getHour();
    }

    /**
     * Sets the currently selected minute.
     *
     * @param minute the minute to set, in the range (0-59)
     * @see #getMinute()
     */
    public void setMinute(@IntRange(from = 0, to = 59) int minute) {
        mDelegate.setMinute(MathUtils.clamp(minute, 0, 59));
    }

    /**
     * Returns the currently selected minute.
     *
     * @return the currently selected minute, in the range (0-59)
     * @see #setMinute(int)
     */
    public int getMinute() {
        return mDelegate.getMinute();
    }

    /**
     * Sets the currently selected minute.
     *
     * @param second the minute to set, in the range (0-59)
     * @see #getSecond()
     */
    public void setSecond(@IntRange(from = 0, to = 59) int second) {
        mDelegate.setSecond(MathUtils.clamp(second, 0, 59));
    }

    /**
     * Returns the currently selected minute.
     *
     * @return the currently selected minute, in the range (0-59)
     * @see #setSecond (int)
     */
    public int getSecond() {
        return mDelegate.getSecond();
    }

    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        mDelegate.setOnTimeChangedListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mDelegate.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return mDelegate.isEnabled();
    }

    @Override
    public int getBaseline() {
        return mDelegate.getBaseline();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return mDelegate.onSaveInstanceState(superState);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
//        BaseSavedState st = (BaseSavedState) state;
//        super.onRestoreInstanceState(st.getSuperState());
        super.onRestoreInstanceState(state);
        mDelegate.onRestoreInstanceState(state);
    }

//    interface TimePickerDelegate {
//        void setHour(@IntRange (from = 0, to = 23) int hour);
//        int getHour();
//
//        void setMinute(@IntRange(from = 0, to = 59) int minute);
//        int getMinute();
//
//        void setSecond(@IntRange(from = 0, to = 59) int second);
//        int getSecond();
//
//        void setTime(@IntRange(from = 0, to = 23) int hour,
//                     @IntRange(from = 0, to = 59) int minute,
//                     @IntRange(from = 0, to = 59) int second);
//
//        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);
//
//        void setEnabled(boolean enabled);
//        boolean isEnabled();
//
//        int getBaseline();
//
//        Parcelable onSaveInstanceState(Parcelable superState);
//        void onRestoreInstanceState(Parcelable state);
//    }

    /**
     * An abstract class which can be used as a start for TimePicker implementations
     */
    static class TimePickerDelegate {
        private final TimePickerWithSecond mDelegator;
        private final Locale mLocale;

        private boolean isEnabled = true;

        private final NumberPicker np_hour;
        private final NumberPicker np_minute;
        private final NumberPicker np_second;

        protected OnTimeChangedListener mOnTimeChangedListener;

        private Calendar mTempCalendar;

        public TimePickerDelegate(@NonNull TimePickerWithSecond delegator, @NonNull Context context) {
            mDelegator = delegator;
            mLocale = context.getResources().getConfiguration().locale;

            View view = LayoutInflater.from(context).inflate(R.layout.timepicker_with_second, delegator, true);
            view.setSaveFromParentEnabled(false);

            np_hour = view.findViewById(R.id.timepicker_hour);
            np_minute = view.findViewById(R.id.timepicker_minute);
            np_second = view.findViewById(R.id.timepicker_second);

            np_hour.setMinValue(0);
            np_hour.setMaxValue(23);
            np_hour.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());

            np_minute.setMinValue(0);
            np_minute.setMaxValue(59);
            np_minute.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());

            np_second.setMaxValue(0);
            np_second.setMaxValue(59);
            np_second.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());

            mTempCalendar = Calendar.getInstance(mLocale);
            setHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
            setMinute(mTempCalendar.get(Calendar.MINUTE));
            setSecond(mTempCalendar.get(Calendar.SECOND));

            setEnabled(isEnabled);

            final NumberPicker.Formatter formatter = value -> String.format("%02d", value);
            np_hour.setFormatter(formatter);
            np_minute.setFormatter(formatter);
            np_second.setFormatter(formatter);
        }

        public void setOnTimeChangedListener(OnTimeChangedListener listener) {
            mOnTimeChangedListener = listener;
        }

        public void setHour(@IntRange (from = 0, to = 23)int hour) {
            if (hour == getHour()) {
                return;
            }
            np_hour.setValue(hour);
            onTimeChanged();
        }

        public int getHour() {
            return np_hour.getValue();
        }

        public void setMinute(@IntRange (from = 0, to = 59)int minute) {
            if (minute == getMinute()) {
                return;
            }
            np_minute.setValue(minute);
            onTimeChanged();
        }

        public int getMinute() {
            return np_minute.getValue();
        }

        public void setSecond(@IntRange (from = 0, to = 59)int second) {
            if (second == getSecond()) {
                return;
            }
            np_second.setValue(second);
            onTimeChanged();
        }

        public int getSecond() {
            return np_second.getValue();
        }

        public void setTime(@IntRange (from = 0, to = 23) int hour,
                            @IntRange (from = 0, to = 59) int minute,
                            @IntRange (from = 0, to = 59) int second) {
            setHour(hour);
            setMinute(minute);
            setSecond(second);
            onTimeChanged();
        }

        public void setEnabled(boolean enabled) {
            np_hour.setEnabled(enabled);
            np_minute.setEnabled(enabled);
            np_second.setEnabled(enabled);
            isEnabled = enabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public int getBaseline() {
            return np_hour.getBaseline();
        }

        public Parcelable onSaveInstanceState(Parcelable superState) {
            return new SavedState(superState, getHour(), getMinute(), getSecond());
        }

        public void onRestoreInstanceState(Parcelable state) {
            if (state instanceof SavedState) {
                final SavedState st = (SavedState) state;
                setHour(st.getHour());
                setMinute(st.getMinute());
                setSecond(st.getSecond());
            }
        }

        private void onTimeChanged() {
            if (mOnTimeChangedListener != null) {
                mOnTimeChangedListener.onTimeChanged(mDelegator, getHour(), getMinute(), getSecond());
            }
        }

        protected static class SavedState extends View.BaseSavedState {
            private final int mHour;
            private final int mMinute;
            private final int mSecond;

            public SavedState(Parcelable superState, int hour, int minute, int second) {
                super(superState);
                mHour = hour;
                mMinute = minute;
                mSecond = second;
            }

            private SavedState(Parcel in) {
                super(in);
                mHour = in.readInt();
                mMinute = in.readInt();
                mSecond = in.readInt();
            }

            public int getHour() {
                return mHour;
            }

            public int getMinute() {
                return mMinute;
            }

            public int getSecond() {
                return mSecond;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(mHour);
                dest.writeInt(mMinute);
                dest.writeInt(mSecond);
            }

            public static final @NonNull Creator<SavedState> CREATOR = new Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
        }
    }
}
