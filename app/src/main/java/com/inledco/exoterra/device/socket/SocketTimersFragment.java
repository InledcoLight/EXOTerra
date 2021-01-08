package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.common.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

public class SocketTimersFragment extends BaseFragment {

    private RecyclerView socket_timers_rv;
    private ImageButton socket_timers_add;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;
    private final List<ExoSocket.Timer> mTimers = new ArrayList<>();
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
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        mSocketViewModel.observe(this, new Observer<ExoSocket>() {
            @Override
            public void onChanged(@Nullable ExoSocket exoSocket) {
                Log.e(TAG, "onChanged: " + mSocket.getTimers().size());
                refreshData();
            }
        });

        mAdapter = new SocketTimerAdapter(getContext(), mTimers) {
            @Override
            protected void onEnableTimer(int position) {
                if (position >= 0 && position < mTimers.size()) {
                    ExoSocket.Timer tmr = mTimers.get(position);
                    tmr.setEnable(true);
                    mSocketViewModel.setTimer(position, tmr);
                }
            }

            @Override
            protected void onDisableTimer(int position) {
                if (position >= 0 && position < mTimers.size()) {
                    ExoSocket.Timer tmr = mTimers.get(position);
                    tmr.setEnable(false);
                    mSocketViewModel.setTimer(position, tmr);
                }
            }
        };
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                addFragmentToStack(R.id.device_root, SocketAddTimerFragment.editTimerInstance(position));
            }
        });
        mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position) {
                showRemoveDialog(position);
                return true;
            }
        });
        socket_timers_rv.setAdapter(mAdapter);

        refreshData();
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
        mTimers.addAll(mSocket.getTimers());
        mAdapter.notifyDataSetChanged();
    }

    private void showRemoveDialog(final int position) {
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, null, false);
        Button btn_remove = view.findViewById(R.id.dialog_action_act2);
        Button btn_cancel = view.findViewById(R.id.dialog_action_cancel);
        btn_remove.setText(R.string.remove);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ExoSocket.Timer> timers = new ArrayList<>(mTimers);
                timers.remove(position);
                mSocketViewModel.setTimers(timers);
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showAddTimerDialog() {
        if (mTimers.size() >= ExoSocket.TIMER_COUNT_MAX) {
            Toast.makeText(getContext(), R.string.timer_count_over, Toast.LENGTH_SHORT)
                 .show();
            return;
        }

        final int ACTION_TURNOFF = 0;
        final int ACTION_TURNON = 1;
        final int ACTION_PERIOD = 2;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_timer_actions);
        Button btn_turnon = dialog.findViewById(R.id.dialog_action_turnon);
        Button btn_turnoff = dialog.findViewById(R.id.dialog_action_turnoff);
        Button btn_period = dialog.findViewById(R.id.dialog_action_period);
        Button btn_cancel = dialog.findViewById(R.id.dialog_action_cancel);
        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btn_turnon.setOnClickListener(v -> {
            addFragmentToStack(R.id.device_root, SocketAddTimerFragment.addTimerInstance(ACTION_TURNON));
            dialog.dismiss();
        });
        btn_turnoff.setOnClickListener(v -> {
            addFragmentToStack(R.id.device_root, SocketAddTimerFragment.addTimerInstance(ACTION_TURNOFF));
            dialog.dismiss();
        });
        btn_period.setOnClickListener(v -> {
            addFragmentToStack(R.id.device_root, SocketAddTimerFragment.addTimerInstance(ACTION_PERIOD));
            dialog.dismiss();
        });
        dialog.show();
    }

//    private void showEditTimerDialog(final int idx) {
//        if (idx >= 0 && idx < mTimers.size()) {
//            final ExoSocket.Timer timer = mTimers.get(idx);
//            final SocketTimerView view = new SocketTimerView(getContext());
//            view.init(timer);
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle(R.string.title_set_timer);
//            builder.setView(view);
//            builder.setNegativeButton(R.string.cancel, null);
//            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ExoSocket.Timer timer = view.getSocketTimer();
//                    mSocketViewModel.setTimer(idx, timer);
//                }
//            });
//            builder.setCancelable(false);
//            builder.show();
//        }
//    }

//    private void showAddTimerDialog() {
//        if (mTimers.size() >= ExoSocket.TIMER_COUNT_MAX) {
//            Toast.makeText(getContext(), R.string.timer_count_over, Toast.LENGTH_SHORT)
//                 .show();
//            return;
//        }
//        final SocketTimerView view = new SocketTimerView(getContext());
//        view.init(null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle(R.string.title_set_timer);
//        builder.setView(view);
//        builder.setNegativeButton(R.string.cancel, null);
//        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                ExoSocket.Timer timer = view.getSocketTimer();
//                mSocketViewModel.setTimer(mTimers.size(), timer);
//            }
//        });
//        builder.setCancelable(false);
//        builder.show();
//    }

//    private class SocketTimerView extends LinearLayout {
//        final Spinner sp_action;
//        final RadioGroup rg;
//        final RadioButton start;
//        final RadioButton end;
//        final TextView exec;
//        final NumberPicker np_hour;
//        final NumberPicker np_minute;
//        final NumberPicker np_second;
//        final CheckBox[] cb_week;
//
//        private int action;
//        private int repeat;
//        private int hour;
//        private int minute;
//        private int second;
//        private int end_hour;
//        private int end_minute;
//        private int end_second;
//
//        private final DecimalFormat df = new DecimalFormat("00");
//        private final String[] values1 = new String[24];
//        private final String[] values2 = new String[60];
//
//        public SocketTimerView(Context context) {
//            this(context, null, 0);
//        }
//
//        public SocketTimerView(Context context, @Nullable AttributeSet attrs) {
//            this(context, attrs, 0);
//        }
//
//        public SocketTimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//            super(context, attrs, defStyleAttr);
//            View view = inflate(context, R.layout.dialog_socket_timer, this);
//
//            sp_action = view.findViewById(R.id.dialog_socket_action);
//            rg = view.findViewById(R.id.dialog_socket_rg);
//            start = view.findViewById(R.id.dialog_socket_start);
//            end = view.findViewById(R.id.dialog_socket_end);
//            exec = view.findViewById(R.id.dialog_socket_exec);
//            np_hour = view.findViewById(R.id.dialog_socket_hour);
//            np_minute = view.findViewById(R.id.dialog_socket_minute);
//            np_second = view.findViewById(R.id.dialog_socket_second);
//            cb_week = new CheckBox[7];
//            cb_week[0] = view.findViewById(R.id.dialog_socket_sun);
//            cb_week[1] = view.findViewById(R.id.dialog_socket_mon);
//            cb_week[2] = view.findViewById(R.id.dialog_socket_tue);
//            cb_week[3] = view.findViewById(R.id.dialog_socket_wed);
//            cb_week[4] = view.findViewById(R.id.dialog_socket_thu);
//            cb_week[5] = view.findViewById(R.id.dialog_socket_fri);
//            cb_week[6] = view.findViewById(R.id.dialog_socket_sat);
//
//            for (int i = 0; i < 24; i++) {
//                values1[i] = df.format(i);
//            }
//            for (int i = 0; i < 60; i++) {
//                values2[i] = df.format(i);
//            }
//
//            np_hour.setMinValue(0);
//            np_hour.setMaxValue(23);
//            np_hour.setDisplayedValues(values1);
//            np_minute.setMinValue(0);
//            np_minute.setMaxValue(59);
//            np_minute.setDisplayedValues(values2);
//            np_second.setMinValue(0);
//            np_second.setMaxValue(59);
//            np_second.setDisplayedValues(values2);
//
//            initEvent();
//        }
//
//        private void init(ExoSocket.Timer timer) {
//            if (timer != null) {
//                action = timer.getAction();
//                repeat = timer.getRepeat();
//                hour = timer.getHour();
//                minute = timer.getMinute();
//                second = timer.getSecond();
//                end_hour = timer.getEndHour();
//                end_minute = timer.getEndMinute();
//                end_second = timer.getEndSecond();
//            } else {
//                action = 2;
//                Calendar calendar = Calendar.getInstance();
//                hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
//                minute = (byte) calendar.get(Calendar.MINUTE);
//                second = (byte) calendar.get(Calendar.SECOND);
//                end_hour = (hour + 1)%24;
//                end_minute = minute;
//                end_second = second;
//            }
//            sp_action.setSelection(action, true);
//            showStartText();
//            showEndText();
//            rg.check(R.id.dialog_socket_start);
//            for (int i = 0; i < 7; i++) {
//                if ((repeat&(1<<i)) != 0) {
//                    cb_week[i].setChecked(true);
//                }
//            }
//        }
//
//        private void initEvent() {
//            sp_action.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    action = (byte) position;
//                    if (position < 2) {
//                        end.setVisibility(INVISIBLE);
//                        rg.check(R.id.dialog_socket_start);
//                    } else {
//                        end.setVisibility(VISIBLE);
//                    }
//                    showStartText();
//                    showEndText();
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//
//                }
//            });
//            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    switch (checkedId) {
//                        case R.id.dialog_socket_start:
//                            np_hour.setValue(hour);
//                            np_minute.setValue(minute);
//                            np_second.setValue(second);
//                            break;
//                        case R.id.dialog_socket_end:
//                            np_hour.setValue(end_hour);
//                            np_minute.setValue(end_minute);
//                            np_second.setValue(end_second);
//                            break;
//                    }
//                }
//            });
//            np_second.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//                @Override
//                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                    Log.e(TAG, "onValueChange: " + oldVal + " " + newVal);
//                    if (oldVal == np_second.getMinValue() && newVal == np_second.getMaxValue()) {
//                        if (np_minute.getValue() == np_minute.getMinValue()) {
//                            np_minute.setValue(np_minute.getMaxValue());
//                            np_hour.setValue((np_hour.getValue()+np_hour.getMaxValue())%(np_hour.getMaxValue()+1));
//                        } else {
//                            np_minute.setValue(np_minute.getValue()-1);
//                        }
//                    } else if (oldVal == np_second.getMaxValue() && newVal == np_second.getMinValue()) {
//                        if (np_minute.getValue() == np_minute.getMaxValue()) {
//                            np_minute.setValue(np_minute.getMinValue());
//                            np_hour.setValue((np_hour.getValue()+1)%(np_hour.getMaxValue()+1));
//                        } else {
//                            np_minute.setValue(np_minute.getValue()+1);
//                        }
//                    }
//                    switch (rg.getCheckedRadioButtonId()) {
//                        case R.id.dialog_socket_start:
//                            setStart(np_hour.getValue(), np_minute.getValue(), np_second.getValue());
//                            break;
//                        case R.id.dialog_socket_end:
//                            setEnd(np_hour.getValue(), np_minute.getValue(), np_second.getValue());
//                            break;
//                    }
//                }
//            });
//            np_minute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//                @Override
//                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                    if (oldVal == np_minute.getMinValue() && newVal == np_minute.getMaxValue()) {
//                        np_hour.setValue((np_hour.getValue()+np_hour.getMaxValue())%(np_hour.getMaxValue()+1));
//                    } else if (oldVal == np_minute.getMaxValue() && newVal == np_minute.getMinValue()) {
//                        np_hour.setValue((np_hour.getValue()+1)%(np_hour.getMaxValue()+1));
//                    }
//                    switch (rg.getCheckedRadioButtonId()) {
//                        case R.id.dialog_socket_start:
//                            setStart(np_hour.getValue(), np_minute.getValue(), np_second.getValue());
//                            break;
//                        case R.id.dialog_socket_end:
//                            setEnd(np_hour.getValue(), np_minute.getValue(), np_second.getValue());
//                            break;
//                    }
//                }
//            });
//            np_hour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//                @Override
//                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                    switch (rg.getCheckedRadioButtonId()) {
//                        case R.id.dialog_socket_start:
//                            setStart(np_hour.getValue(), np_minute.getValue(), np_second.getValue());
//                            break;
//                        case R.id.dialog_socket_end:
//                            setEnd(np_hour.getValue(), np_minute.getValue(), np_second.getValue());
//                            break;
//                    }
//                }
//            });
//
//            for (int i = 0; i < 7; i++) {
//                final int wk = i;
//                cb_week[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (isChecked) {
//                            repeat |= (1<<wk);
//                        } else {
//                            repeat &= ~(1<<wk);
//                        }
//                        showExecText();
//                    }
//                });
//            }
//        }
//
//        private void setStart(int hour, int minute, int second) {
//            this.hour = (byte) hour;
//            this.minute = (byte) minute;
//            this.second = (byte) second;
//            showStartText();
//        }
//
//        private void setEnd(int hour, int minute, int second) {
//            end_hour = (byte) hour;
//            end_minute = (byte) minute;
//            end_second = (byte) second;
//            showEndText();
//        }
//
//        private void showStartText() {
//            String act = action > 0 ? getString(R.string.turnon) : getString(R.string.turnoff);
//            String text = df.format(hour) + ":" + df.format(minute) + ":" + df.format(second) + " " + act;
//            start.setText(text);
//        }
//
//        private void showEndText() {
//            String text = df.format(end_hour) + ":" + df.format(end_minute) + ":" + df.format(end_second) + " " + getString(R.string.turnoff);
//            end.setText(text);
//        }
//
//        private void showExecText() {
//            String text = null;
//            if (repeat == 0) {
//                text = getString(R.string.execute_once);
//            } else if (repeat == 0x7F) {
//                text = getString(R.string.everyday);
//            } else {
////                StringBuilder sb = new StringBuilder();
////                final String[] array = getContext().getResources().getStringArray(R.array.weeks);
////                for (int i = 0; i < 7; i++) {
////                    if ((repeat&(1<<i)) != 0) {
////                        sb.append(array[i]).append(" ");
////                    }
////                }
////                text = new String(sb).trim();
//            }
//            exec.setText(text);
//        }
//
//        private ExoSocket.Timer getSocketTimer() {
//            ExoSocket.Timer timer = new ExoSocket.Timer();
//            timer.setEnable(true);
//            timer.setAction(action);
//            timer.setHour(hour);
//            timer.setMinute(minute);
//            timer.setSecond(second);
//            timer.setEndHour(end_hour);
//            timer.setEndMinute(end_minute);
//            timer.setEndSecond(end_second);
//            timer.setRepeat(repeat);
//            return timer;
//        }
//    }
}
