package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.view.TimePickerWithSecond;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketAddTimerFragment extends BaseFragment {

    private Toolbar socket_addtimer_toolbar;
    private TextView socket_addtimer_title;
    private View socket_addtimer_repeat;
    private View socket_addtimer_start;
    private View socket_addtimer_end;
    private TextView socket_addtimer_text_repeat;
    private TextView socket_addtimer_desc_repeat;
    private TextView socket_addtimer_text_start;
    private TextView socket_addtimer_desc_start;
    private TextView socket_addtimer_text_end;
    private TextView socket_addtimer_desc_end;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;

    @IntDef({SocketAction.ACTION_TURNOFF, SocketAction.ACTION_TURNON, SocketAction.ACTION_PERIOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SocketAction {
        int ACTION_TURNOFF = 0;
        int ACTION_TURNON = 1;
        int ACTION_PERIOD = 2;
    }

    private int mIndex = -1;

    @SocketAction
    private int mAction = SocketAction.ACTION_PERIOD;

    private int mRepeat = -1;
    private int mHour = -1;
    private int mMinute = -1;
    private int mSecond = -1;
    private int mEndHour = -1;
    private int mEndMinute = -1;
    private int mEndSecond = -1;

    public static SocketAddTimerFragment editTimerInstance(int index) {
        Bundle args = new Bundle();
        args.putInt("index", index);
        SocketAddTimerFragment fragment = new SocketAddTimerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SocketAddTimerFragment addTimerInstance(int act) {
        Bundle args = new Bundle();
        args.putInt("action", act);
        SocketAddTimerFragment fragment = new SocketAddTimerFragment();
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

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_timer;
    }

    @Override
    protected void initView(View view) {
        socket_addtimer_toolbar = view.findViewById(R.id.socket_addtimer_toolbar);
        socket_addtimer_toolbar.inflateMenu(R.menu.menu_confirm);

        socket_addtimer_title = view.findViewById(R.id.socket_addtimer_title);
        socket_addtimer_repeat = view.findViewById(R.id.socket_addtimer_repeat);
        socket_addtimer_start = view.findViewById(R.id.socket_addtimer_start);
        socket_addtimer_end = view.findViewById(R.id.socket_addtimer_end);

        socket_addtimer_text_repeat = socket_addtimer_repeat.findViewById(R.id.item_simple_text);
        socket_addtimer_desc_repeat = socket_addtimer_repeat.findViewById(R.id.item_simple_desc);
        socket_addtimer_text_repeat.setText(R.string.repeat);

        socket_addtimer_text_start = socket_addtimer_start.findViewById(R.id.item_simple_text);
        socket_addtimer_desc_start = socket_addtimer_start.findViewById(R.id.item_simple_desc);

        socket_addtimer_text_end = socket_addtimer_end.findViewById(R.id.item_simple_text);
        socket_addtimer_desc_end = socket_addtimer_end.findViewById(R.id.item_simple_desc);
        socket_addtimer_text_end.setText(R.string.turnoff_time);
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();

        Bundle args = getArguments();
        if (args != null) {
            mIndex = args.getInt("index", -1);
            mAction = args.getInt("action", SocketAction.ACTION_PERIOD);
        }
        if (mIndex >= 0 && mIndex < mSocket.getTimers().size()) {
            ExoSocket.Timer timer = mSocket.getTimer(mIndex);
            mRepeat = timer.getRepeat();
            mAction = timer.getAction();
            mHour = timer.getHour();
            mMinute = timer.getMinute();
            mSecond = timer.getSecond();
            mEndHour = timer.getEndHour();
            mEndMinute = timer.getEndMinute();
            mEndSecond = timer.getEndSecond();
        }

        switch (mAction) {
            case SocketAction.ACTION_TURNOFF:
                mEndHour = 0;
                mEndMinute = 0;
                mEndSecond = 0;
                socket_addtimer_title.setText(R.string.turnoff);
                socket_addtimer_text_start.setText(R.string.turnoff_time);
                socket_addtimer_end.setVisibility(View.GONE);
                break;
            case SocketAction.ACTION_TURNON:
                mEndHour = 0;
                mEndMinute = 0;
                mEndSecond = 0;
                socket_addtimer_title.setText(R.string.turnon);
                socket_addtimer_text_start.setText(R.string.turnon_time);
                socket_addtimer_end.setVisibility(View.GONE);
                break;
            case SocketAction.ACTION_PERIOD:
                socket_addtimer_title.setText(R.string.time_period);
                socket_addtimer_text_start.setText(R.string.turnon_time);
                socket_addtimer_end.setVisibility(View.VISIBLE);
                break;
        }

        refreshRepeat();
        refreshTime();
    }

    @Override
    protected void initEvent() {
        socket_addtimer_toolbar.setNavigationOnClickListener(v -> getFragmentManager().popBackStack());

        socket_addtimer_toolbar.getMenu().findItem(R.id.menu_confirm).setOnMenuItemClickListener(item -> {
            if (mRepeat < 0 || mRepeat > 0x7F) {
                showToast(R.string.invalid);
                showRepeatDialog();
                return false;
            }
            if (!isValidTime()) {
                showToast(R.string.invalid);
                showTimeDialog();
                return false;
            }
            if (!isValidEndTime()) {
                showToast(R.string.invalid);
                showEndTimeDialog();
                return false;
            }
            ExoSocket.Timer timer = new ExoSocket.Timer();
            timer.setEnable(true);
            timer.setRepeat(mRepeat);
            timer.setAction(mAction);
            timer.setTime(mHour, mMinute, mSecond);
            timer.setEndTime(mEndHour, mEndMinute, mEndSecond);
            int size = mSocket.getTimers().size();
            if (mIndex >= 0 && mIndex < size) {
                mSocketViewModel.setTimer(mIndex, timer);
                getFragmentManager().popBackStack();
            } else if (size < ExoSocket.TIMER_COUNT_MAX) {
                mSocketViewModel.setTimer(size, timer);
                getFragmentManager().popBackStack();
            } else {
                showToast(R.string.timer_count_over);
            }
            return false;
        });

        socket_addtimer_repeat.setOnClickListener(v -> showRepeatDialog());

        socket_addtimer_start.setOnClickListener(v -> showTimeDialog());

        socket_addtimer_end.setOnClickListener(v -> showEndTimeDialog());
    }

    private void refreshRepeat() {
        String weektext = "";
        if (mRepeat == 0) {
            weektext = getString(R.string.execute_once);
        } else if (mRepeat > 0 && mRepeat < 0x7F) {
            String[] weeks = getResources().getStringArray(R.array.weeks);
            for (int i = 0; i < 7; i++) {
                if ((mRepeat & (1 << i)) != 0) {
                    weektext += weeks[i] + " ";
                }
            }
        } else if (mRepeat == 0x7F) {
            weektext = getString(R.string.everyday);
        }
        socket_addtimer_desc_repeat.setText(weektext.trim());
    }

    private void refreshTime() {
        long secs = mHour * 3600 + mMinute * 60 + mSecond;
        if (isValidTime()) {
            String text = String.format("%02d:%02d:%02d", mHour, mMinute, mSecond);
            if (mRepeat == 0) {
                long cursecs = ((System.currentTimeMillis() + TimeZone.getDefault().getRawOffset())/1000)%86400;
                if (cursecs > secs) {
                    text = getString(R.string.tomorrow) + " " + text;
                }
            }
            socket_addtimer_desc_start.setText(text);
        }
        String endtext = "";
        if (isValidEndTime() && mAction == SocketAction.ACTION_PERIOD) {
            endtext = String.format("%02d:%02d:%02d", mEndHour, mEndMinute, mEndSecond);
            if (isValidTime()) {
                long endsecs = mEndHour * 3600 + mEndMinute * 60 + mEndSecond;
                if (endsecs <= secs) {
                    endtext = getString(R.string.next_day) + " " + endtext;
                }
            }
        }
        socket_addtimer_desc_end.setText(endtext);
    }

    private boolean isValidTime(int hour, int min, int sec) {
        if (hour < 0 || hour > 23 || min < 0 || min > 59 || sec < 0 || sec > 59) {
            return false;
        }
        return true;
    }

    private boolean isValidTime() {
        return isValidTime(mHour, mMinute, mSecond);
    }

    private boolean isValidEndTime() {
        return isValidTime(mEndHour, mEndMinute, mEndSecond);
    }

    private boolean isValid() {
        if (mRepeat < 0 || mRepeat > 0x7F) {
            return false;
        }
        if (!isValidTime()) {
            return false;
        }
        if (!isValidEndTime()) {
            return false;
        }
        return true;
    }

    private void showRepeatDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_repeat);
        RadioGroup rg = dialog.findViewById(R.id.dialog_repeat_rg);
        CheckBox[] cb_weeks = new CheckBox[7];
        cb_weeks[0] = dialog.findViewById(R.id.dialog_repeat_sun);
        cb_weeks[1] = dialog.findViewById(R.id.dialog_repeat_mon);
        cb_weeks[2] = dialog.findViewById(R.id.dialog_repeat_tue);
        cb_weeks[3] = dialog.findViewById(R.id.dialog_repeat_wed);
        cb_weeks[4] = dialog.findViewById(R.id.dialog_repeat_thu);
        cb_weeks[5] = dialog.findViewById(R.id.dialog_repeat_fri);
        cb_weeks[6] = dialog.findViewById(R.id.dialog_repeat_sat);
        Button btn_cancel = dialog.findViewById(R.id.dialog_repeat_cancel);
        Button btn_ok = dialog.findViewById(R.id.dialog_repeat_ok);
        int wk = mRepeat;
        if (mRepeat == 0x7F) {
            rg.check(R.id.dialog_repeat_everyday);
        } else if (mRepeat > 0 && mRepeat < 0x7F) {
            rg.check(R.id.dialog_repeat_custom);
        } else {
            wk = 0;
            rg.check(R.id.dialog_repeat_execonce);
        }
        for (int i = 0; i < 7; i++) {
            cb_weeks[i].setChecked((wk&(1<<i)) != 0);
        }

        AtomicBoolean fromCode = new AtomicBoolean(false);
        final CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (fromCode.get()) {
                return;
            }
            int rpt = 0;
            for (int i = 0; i < 7; i++) {
                if (cb_weeks[i].isChecked()) {
                    rpt |= (1<<i);
                }
            }

            fromCode.set(true);
            if (rpt == 0) {
                rg.check(R.id.dialog_repeat_execonce);
            } else if (rpt == 0x7F) {
                rg.check(R.id.dialog_repeat_everyday);
            } else {
                rg.check(R.id.dialog_repeat_custom);
            }
            fromCode.set(false);
        };
        for (int i = 0; i < 7; i++) {
            cb_weeks[i].setOnCheckedChangeListener(listener);
        }
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (fromCode.get()) {
                return;
            }
            fromCode.set(true);
            switch (checkedId) {
                case R.id.dialog_repeat_execonce:
                    for (int i = 0; i < 7; i++) {
                        cb_weeks[i].setChecked(false);
                    }
                    break;
                case R.id.dialog_repeat_everyday:
                    for (int i = 0; i < 7; i++) {
                        cb_weeks[i].setChecked(true);
                    }
                    break;
            }
            fromCode.set(false);
        });
        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btn_ok.setOnClickListener(v -> {
            int rpt = 0;
            for (int i = 0; i < 7; i++) {
                if (cb_weeks[i].isChecked()) {
                    rpt |= (1<<i);
                }
            }
            mRepeat = rpt;
            refreshRepeat();
            refreshTime();
            dialog.dismiss();
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showTimePickerDialog(int hour, int min, int sec, TimePickerWithSecond.OnTimeChangedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_timepicker);
        TimePickerWithSecond timepicker = dialog.findViewById(R.id.dialog_timepicker);
        Button btn_cancel = dialog.findViewById(R.id.dialog_timepicker_cancel);
        Button btn_ok = dialog.findViewById(R.id.dialog_timepicker_ok);
        if (isValidTime(hour, min, sec)) {
            timepicker.setHour(hour);
            timepicker.setMinute(min);
            timepicker.setSecond(sec);
        }
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_ok.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTimeChanged(timepicker, timepicker.getHour(), timepicker.getMinute(), timepicker.getSecond());
            }
            dialog.dismiss();
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showTimeDialog() {
        showTimePickerDialog(mHour, mMinute, mSecond, (view, hourOfDay, min, sec) -> {
            mHour = hourOfDay;
            mMinute = min;
            mSecond = sec;
            Log.e(TAG, "showTimeDialog: " + mHour +" " + mMinute + " " + mSecond);
            refreshTime();
        });
    }

    private void showEndTimeDialog() {
        showTimePickerDialog(mEndHour, mEndMinute, mEndSecond, (view, hourOfDay, min, sec) -> {
            mEndHour = hourOfDay;
            mEndMinute = min;
            mEndSecond = sec;
            Log.e(TAG, "showEndTimeDialog: " + mHour +" " + mMinute + " " + mSecond);
            refreshTime();
        });
    }
}
