package com.liruya.exoterra.device.Monsoon;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOMonsoon;
import com.liruya.exoterra.bean.EXOMonsoonTimer;

import java.util.List;

public class MonsoonTimersFragment extends BaseFragment {

    private RecyclerView monsoon_timers_rv;
    private FloatingActionButton monsoon_timers_add;

    private MonsoonViewModel mMonsoonViewModel;
    private EXOMonsoon mMonsoon;
    private List<EXOMonsoonTimer> mTimers;
    private MonsoonTimerAdapter mAdapter;

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
        return R.layout.fragment_monsoon_timers;
    }

    @Override
    protected void initView(View view) {
        monsoon_timers_rv = view.findViewById(R.id.monsoon_timers_rv);
        monsoon_timers_add = view.findViewById(R.id.monsoon_timers_add);
        monsoon_timers_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mMonsoonViewModel = ViewModelProviders.of(getActivity()).get(MonsoonViewModel.class);
        mMonsoon = mMonsoonViewModel.getData();
        mMonsoonViewModel.observe(this, new Observer<EXOMonsoon>() {
            @Override
            public void onChanged(@Nullable EXOMonsoon exoMonsoon) {
                mTimers.clear();
                mTimers.addAll(mMonsoon.getAllTimers());
                mAdapter.notifyDataSetChanged();
            }
        });

        mTimers = mMonsoon.getAllTimers();
        mAdapter = new MonsoonTimerAdapter(getContext(), mTimers) {
            @Override
            protected void onClickItem(int position) {
                showEditTimerDialog(position);
            }

            @Override
            protected void onLongClickItem(int position) {
                showRemoveDialog(position);
            }
        };
        monsoon_timers_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        monsoon_timers_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTimerDialog();
            }
        });
    }

    private void showRemoveDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.remove_timer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMonsoonViewModel.removeTimer(position);
            }
        });
        builder.show();
    }

    private void showEditTimerDialog(final int idx) {
        if (idx >= 0 && idx < mTimers.size()) {
            final EXOMonsoonTimer timer = new EXOMonsoonTimer(mTimers.get(idx).getValue());
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.title_set_timer);
            View view = LayoutInflater.from(getContext())
                                      .inflate(R.layout.dialog_monsoon_timer, null);
            final CheckBox[] cb_week = new CheckBox[7];
            final TimePicker tp_tmr = view.findViewById(R.id.dialog_monsoon_timer);
            final NumberPicker np = view.findViewById(R.id.dialog_monsoon_duration);
            String[] values = new String[127];
            for (int i = 0; i < 59; i++) {
                values[i] = "" + (i + 1) + " Sec";
            }
            for (int i = 59; i < 119; i++) {
                values[i] = "1 Min " + (i - 59) + " Sec";
            }
            values[119] = "2 Min";
            values[120] = "3 Min";
            values[121] = "4 Min";
            values[122] = "5 Min";
            values[123] = "6 Min";
            values[124] = "8 Min";
            values[125] = "10 Min";
            values[126] = "15 Min";
            np.setMinValue(1);
            np.setMaxValue(127);
            np.setDisplayedValues(values);
            cb_week[0] = view.findViewById(R.id.dialog_monsoon_sun);
            cb_week[1] = view.findViewById(R.id.dialog_monsoon_mon);
            cb_week[2] = view.findViewById(R.id.dialog_monsoon_tue);
            cb_week[3] = view.findViewById(R.id.dialog_monsoon_wed);
            cb_week[4] = view.findViewById(R.id.dialog_monsoon_thu);
            cb_week[5] = view.findViewById(R.id.dialog_monsoon_fri);
            cb_week[6] = view.findViewById(R.id.dialog_monsoon_sat);
            final Switch sw_enable = view.findViewById(R.id.dialog_monsoon_enable);
            tp_tmr.setIs24HourView(true);
            tp_tmr.setCurrentHour(timer.getTimer() / 60);
            tp_tmr.setCurrentMinute(timer.getTimer() % 60);
            np.setValue(timer.getDuration());
            for (int i = 0; i < 7; i++) {
                cb_week[i].setChecked(timer.getWeek(i));
            }
            sw_enable.setChecked(timer.isEnable());
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timer.setTimer(tp_tmr.getCurrentHour() * 60 + tp_tmr.getCurrentMinute());
                    timer.setDuration(np.getValue());
                    for (int i = 0; i < 7; i++) {
                        timer.setWeek(i, cb_week[i].isChecked());
                    }
                    timer.setEnable(sw_enable.isChecked());
                    mMonsoonViewModel.setTimer(idx, timer);
                }
            });
            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void showAddTimerDialog() {
        if (mTimers.size() >= EXOMonsoon.TIMER_COUNT_MAX) {
            Toast.makeText(getContext(), R.string.timer_count_over, Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_set_timer);
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.dialog_monsoon_timer, null);
        final CheckBox[] cb_week = new CheckBox[7];
        final TimePicker tp_tmr = view.findViewById(R.id.dialog_monsoon_timer);
        final NumberPicker np = view.findViewById(R.id.dialog_monsoon_duration);
        String[] values = new String[127];
        for (int i = 0; i < 59; i++) {
            values[i] = "" + (i + 1) + " Sec";
        }
        for (int i = 59; i < 119; i++) {
            values[i] = "1 Min " + (i - 59) + " Sec";
        }
        values[119] = "2 Min";
        values[120] = "3 Min";
        values[121] = "4 Min";
        values[122] = "5 Min";
        values[123] = "6 Min";
        values[124] = "8 Min";
        values[125] = "10 Min";
        values[126] = "15 Min";
        np.setMinValue(1);
        np.setMaxValue(127);
        np.setDisplayedValues(values);
        cb_week[0] = view.findViewById(R.id.dialog_monsoon_sun);
        cb_week[1] = view.findViewById(R.id.dialog_monsoon_mon);
        cb_week[2] = view.findViewById(R.id.dialog_monsoon_tue);
        cb_week[3] = view.findViewById(R.id.dialog_monsoon_wed);
        cb_week[4] = view.findViewById(R.id.dialog_monsoon_thu);
        cb_week[5] = view.findViewById(R.id.dialog_monsoon_fri);
        cb_week[6] = view.findViewById(R.id.dialog_monsoon_sat);
        final Switch sw_enable = view.findViewById(R.id.dialog_monsoon_enable);
        tp_tmr.setIs24HourView(true);
        np.setValue(5);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EXOMonsoonTimer tmr = new EXOMonsoonTimer();
                tmr.setTimer(tp_tmr.getCurrentHour() * 60 + tp_tmr.getCurrentMinute());
                tmr.setDuration(np.getValue());
                for (int i = 0; i < 7; i++) {
                    tmr.setWeek(i, cb_week[i].isChecked());
                }
                tmr.setEnable(sw_enable.isChecked());
                mMonsoonViewModel.addTimer(tmr);
            }
        });
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
