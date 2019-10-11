package com.inledco.exoterra.device.socket;

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
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.EXOSocketTimer;

import java.util.ArrayList;
import java.util.List;

public class SocketTimersFragment extends BaseFragment {

    private RecyclerView socket_timers_rv;
    private FloatingActionButton socket_timers_add;

    private SocketViewModel mSocketViewModel;
    private EXOSocket mSocket;
    private final List<EXOSocketTimer> mTimers = new ArrayList<>();
    private SocketTimerAdapter mAdapter;

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
        return R.layout.fragment_socket_timers;
    }

    @Override
    protected void initView(View view) {
        socket_timers_rv = view.findViewById(R.id.socket_timers_rv);
        socket_timers_add = view.findViewById(R.id.socket_timers_add);
        socket_timers_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<EXOSocket>() {
            @Override
            public void onChanged(@Nullable EXOSocket exoSocket) {
                refreshData();
            }
        });

        mAdapter = new SocketTimerAdapter(getContext(), mTimers) {
            @Override
            protected void onClickItem(int position) {
                showEditTimerDialog(position);
            }

            @Override
            protected void onLongClickItem(int position) {
                showRemoveDialog(position);
            }

            @Override
            protected void onEnableTimer(int position) {
                if (position >= 0 && position < mTimers.size()) {
                    EXOSocketTimer tmr = new EXOSocketTimer(mTimers.get(position).getValue());
                    tmr.setEnable(true);
                    mSocketViewModel.setTimer(position, tmr);
                }
            }

            @Override
            protected void onDisableTimer(int position) {
                if (position >= 0 && position < mTimers.size()) {
                    EXOSocketTimer tmr = new EXOSocketTimer(mTimers.get(position).getValue());
                    tmr.setEnable(false);
                    mSocketViewModel.setTimer(position, tmr);
                }
            }
        };
        socket_timers_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        socket_timers_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTimerDialog();
            }
        });
    }

    private void refreshData() {
        if (mSocket == null) {
            return;
        }
        mTimers.clear();
        mTimers.addAll(mSocket.getAllTimers());
        mAdapter.notifyDataSetChanged();
    }

    private void showRemoveDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.remove_timer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSocketViewModel.removeTimer(position);
            }
        });
        builder.show();
    }

    private void showEditTimerDialog(final int idx) {
        if (idx >= 0 && idx < mTimers.size()) {
            final EXOSocketTimer timer = new EXOSocketTimer(mTimers.get(idx).getValue());
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.title_set_timer);
            View view = LayoutInflater.from(getContext())
                                      .inflate(R.layout.dialog_socket_timer, null);
            final CheckBox[] cb_week = new CheckBox[7];
            final TimePicker tp_tmr = view.findViewById(R.id.dialog_socket_timer);
            final ToggleButton tb_power = view.findViewById(R.id.dialog_socket_power);
            cb_week[0] = view.findViewById(R.id.dialog_socket_sun);
            cb_week[1] = view.findViewById(R.id.dialog_socket_mon);
            cb_week[2] = view.findViewById(R.id.dialog_socket_tue);
            cb_week[3] = view.findViewById(R.id.dialog_socket_wed);
            cb_week[4] = view.findViewById(R.id.dialog_socket_thu);
            cb_week[5] = view.findViewById(R.id.dialog_socket_fri);
            cb_week[6] = view.findViewById(R.id.dialog_socket_sat);
            tp_tmr.setIs24HourView(true);
            tp_tmr.setCurrentHour(timer.getTimer() / 60);
            tp_tmr.setCurrentMinute(timer.getTimer() % 60);
            tb_power.setChecked(timer.getAction());
            for (int i = 0; i < 7; i++) {
                cb_week[i].setChecked(timer.getWeek(i));
            }
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timer.setTimer(tp_tmr.getCurrentHour() * 60 + tp_tmr.getCurrentMinute());
                    timer.setAction(tb_power.isChecked());
                    for (int i = 0; i < 7; i++) {
                        timer.setWeek(i, cb_week[i].isChecked());
                    }
                    mSocketViewModel.setTimer(idx, timer);
                }
            });
            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void showAddTimerDialog() {
        if (mTimers.size() >= EXOSocket.TIMER_COUNT_MAX) {
            Toast.makeText(getContext(), R.string.timer_count_over, Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_set_timer);
        View view = LayoutInflater.from(getContext())
                                  .inflate(R.layout.dialog_socket_timer, null);
        final CheckBox[] cb_week = new CheckBox[7];
        final TimePicker tp_tmr = view.findViewById(R.id.dialog_socket_timer);
        final ToggleButton tb_power = view.findViewById(R.id.dialog_socket_power);
        cb_week[0] = view.findViewById(R.id.dialog_socket_sun);
        cb_week[1] = view.findViewById(R.id.dialog_socket_mon);
        cb_week[2] = view.findViewById(R.id.dialog_socket_tue);
        cb_week[3] = view.findViewById(R.id.dialog_socket_wed);
        cb_week[4] = view.findViewById(R.id.dialog_socket_thu);
        cb_week[5] = view.findViewById(R.id.dialog_socket_fri);
        cb_week[6] = view.findViewById(R.id.dialog_socket_sat);
        tp_tmr.setIs24HourView(true);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EXOSocketTimer timer = new EXOSocketTimer();
                timer.setTimer(tp_tmr.getCurrentHour() * 60 + tp_tmr.getCurrentMinute());
                timer.setAction(tb_power.isChecked());
                for (int i = 0; i < 7; i++) {
                    timer.setWeek(i, cb_week[i].isChecked());
                }
                timer.setEnable(true);
                mSocketViewModel.addTimer(timer);
            }
        });
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
