package com.inledco.exoterra.device.Monsoon;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.MonsoonViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.common.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

public class MonsoonTimersFragment extends BaseFragment {

    private RecyclerView monsoon_timers_rv;
    private ImageButton monsoon_timers_add;

    private MonsoonViewModel mMonsoonViewModel;
    private ExoMonsoon mMonsoon;
    private List<ExoMonsoon.Timer> mTimers = new ArrayList<>();
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
    }

    @Override
    protected void initData() {
        mMonsoonViewModel = ViewModelProviders.of(getActivity()).get(MonsoonViewModel.class);
        mMonsoon = mMonsoonViewModel.getData();
        mMonsoonViewModel.observe(this, new Observer<ExoMonsoon>() {
            @Override
            public void onChanged(@Nullable ExoMonsoon exoMonsoon) {
                mTimers.clear();
                mTimers.addAll(mMonsoon.getTimers());
                mAdapter.notifyDataSetChanged();
            }
        });

        mTimers.addAll(mMonsoon.getTimers());
        mAdapter = new MonsoonTimerAdapter(getContext(), mTimers) {
            @Override
            protected void onEnableTimer(int position) {
                if (position >= 0 && position < mTimers.size()) {
                    ExoMonsoon.Timer tmr = mTimers.get(position);
                    tmr.setEnable(true);
                    mMonsoonViewModel.setTimers(mTimers);
                }
            }

            @Override
            protected void onDisableTimer(int position) {
                if (position >= 0 && position < mTimers.size()) {
                    ExoMonsoon.Timer tmr = mTimers.get(position);
                    tmr.setEnable(false);
                    mMonsoonViewModel.setTimers(mTimers);
                }
            }
        };
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showEditTimerDialog(position);
            }
        });
        mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position) {
                showRemoveDialog(position);
                return true;
            }
        });
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
        if (position < 0 || position >= mTimers.size()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.remove_timer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTimers.remove(position);
                mMonsoonViewModel.setTimers(mTimers);
            }
        });
        builder.show();
    }

    private void showEditTimerDialog(final int idx) {
        if (idx >= 0 && idx < mTimers.size()) {
            final ExoMonsoon.Timer timer = mTimers.get(idx);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.title_set_timer);
            View view = LayoutInflater.from(getContext())
                                      .inflate(R.layout.dialog_monsoon_timer, null);
            final CheckBox[] cb_week = new CheckBox[7];
            final TimePicker tp_tmr = view.findViewById(R.id.dialog_monsoon_timer);
            final NumberPicker np = view.findViewById(R.id.dialog_monsoon_duration);
            String[] values = new String[120];
            for (int i = 0; i < 59; i++) {
                values[i] = "" + (i + 1) + " Sec";
            }
            values[59] = "1 Min";
            for (int i = 60; i < 119; i++) {
                values[i] = "1 Min " + (i - 59) + " Sec";
            }
            values[119] = "2 Min";
            np.setMinValue(1);
            np.setMaxValue(120);
            np.setDisplayedValues(values);
            cb_week[0] = view.findViewById(R.id.dialog_monsoon_sun);
            cb_week[1] = view.findViewById(R.id.dialog_monsoon_mon);
            cb_week[2] = view.findViewById(R.id.dialog_monsoon_tue);
            cb_week[3] = view.findViewById(R.id.dialog_monsoon_wed);
            cb_week[4] = view.findViewById(R.id.dialog_monsoon_thu);
            cb_week[5] = view.findViewById(R.id.dialog_monsoon_fri);
            cb_week[6] = view.findViewById(R.id.dialog_monsoon_sat);
            tp_tmr.setIs24HourView(GlobalSettings.is24HourFormat());
            tp_tmr.setCurrentHour(timer.getTimer() / 60);
            tp_tmr.setCurrentMinute(timer.getTimer() % 60);
            np.setValue(timer.getPeriod());
            int repeat = timer.getRepeat();
            for (int i = 0; i < 7; i++) {
                cb_week[i].setChecked((repeat&(1<<i)) != 0);
            }
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timer.setTimer(tp_tmr.getCurrentHour() * 60 + tp_tmr.getCurrentMinute());
                    timer.setPeriod(np.getValue());
                    int rpt = 0;
                    for (int i = 0; i < 7; i++) {
                        if (cb_week[i].isChecked()) {
                            rpt |= (1<<i);
                        }
                        timer.setRepeat(rpt);
                    }
                    mMonsoonViewModel.setTimers(mTimers);
                }
            });
            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void showAddTimerDialog() {
        if (mTimers.size() >= ExoMonsoon.TIMER_COUNT_MAX) {
            Toast.makeText(getContext(), R.string.timer_count_over, Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_add_timer);
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.dialog_monsoon_timer, null);
        final CheckBox[] cb_week = new CheckBox[7];
        final TimePicker tp_tmr = view.findViewById(R.id.dialog_monsoon_timer);
        final NumberPicker np = view.findViewById(R.id.dialog_monsoon_duration);
        String[] values = new String[120];
        for (int i = 0; i < 59; i++) {
            values[i] = "" + (i + 1) + " Sec";
        }
        values[59] = "1 Min";
        for (int i = 60; i < 119; i++) {
            values[i] = "1 Min " + (i - 59) + " Sec";
        }
        values[119] = "2 Min";
        np.setMinValue(ExoMonsoon.SPRAY_MIN);
        np.setMaxValue(ExoMonsoon.SPRAY_MAX);
        np.setDisplayedValues(values);
        cb_week[0] = view.findViewById(R.id.dialog_monsoon_sun);
        cb_week[1] = view.findViewById(R.id.dialog_monsoon_mon);
        cb_week[2] = view.findViewById(R.id.dialog_monsoon_tue);
        cb_week[3] = view.findViewById(R.id.dialog_monsoon_wed);
        cb_week[4] = view.findViewById(R.id.dialog_monsoon_thu);
        cb_week[5] = view.findViewById(R.id.dialog_monsoon_fri);
        cb_week[6] = view.findViewById(R.id.dialog_monsoon_sat);
        tp_tmr.setIs24HourView(GlobalSettings.is24HourFormat());
        np.setValue(5);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExoMonsoon.Timer tmr = new ExoMonsoon.Timer();
                tmr.setTimer(tp_tmr.getCurrentHour() * 60 + tp_tmr.getCurrentMinute());
                tmr.setPeriod(np.getValue());
                int rpt = 0;
                for (int i = 0; i < 7; i++) {
                    if (cb_week[i].isChecked()) {
                        rpt |= (1<<i);
                    }
                }
                tmr.setRepeat(rpt);
                tmr.setEnable(true);
                mTimers.add(tmr);
                mMonsoonViewModel.setTimers(mTimers);
            }
        });
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
