package com.inledco.exoterra.main.groups;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.util.FavouriteUtil;
import com.inledco.exoterra.util.GroupUtil;
import com.inledco.exoterra.view.AdvancedTextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class AddHabitatFragment extends BaseFragment {
    private TextView add_habitat_systime;
    private TextInputLayout add_habitat_til;
    private AdvancedTextInputEditText add_habitat_name;
    private AdvancedTextInputEditText add_habitat_time;
    private AdvancedTextInputEditText add_habitat_sunrise;
    private AdvancedTextInputEditText add_habitat_sunset;
    private Switch add_habitat_favourite;
    private Button add_habitat_back;
    private Button add_habitat_save;

    private String mIconName = GroupUtil.getDefaultIconName();

    private final int mOffset = TimeZone.getDefault().getRawOffset() / 60000;
    private final BroadcastReceiver mTimeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    refreshTime();
                    break;
            }
        }
    };

    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;

    private int mZone = mOffset;
    private int mSunrise = 360;
    private int mSunset = 1080;

    private static final String KEY_FAVOURITE = "favourite";

    public static AddHabitatFragment newInstance(final boolean favourite) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_FAVOURITE, favourite);
        AddHabitatFragment fragment = new AddHabitatFragment();
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
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mTimeChangeReceiver);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_add_habitat;
    }

    @Override
    protected void initView(View view) {
        add_habitat_systime = view.findViewById(R.id.add_habitat_systime);
        add_habitat_til = view.findViewById(R.id.add_habitat_til);
        add_habitat_name = view.findViewById(R.id.add_habitat_name);
        add_habitat_time = view.findViewById(R.id.add_habitat_time);
        add_habitat_sunrise = view.findViewById(R.id.add_habitat_sunrise);
        add_habitat_sunset = view.findViewById(R.id.add_habitat_sunset);
        add_habitat_favourite = view.findViewById(R.id.add_habitat_favourite);
        add_habitat_back = view.findViewById(R.id.add_habitat_back);
        add_habitat_save = view.findViewById(R.id.add_habitat_save);

        add_habitat_name.requestFocus();
        add_habitat_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_type_1, 0, R.drawable.ic_edit_white_24dp, 0);
        add_habitat_time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        add_habitat_sunrise.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        add_habitat_sunset.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            add_habitat_favourite.setChecked(args.getBoolean(KEY_FAVOURITE));
        }
        mDateFormat = GlobalSettings.getDateTimeFormat();
        mTimeFormat = GlobalSettings.getTimeFormat();
        mTimeFormat.setTimeZone(new SimpleTimeZone(0, ""));
        add_habitat_sunrise.setText(mTimeFormat.format(mSunrise*60000));
        add_habitat_sunset.setText(mTimeFormat.format(mSunset*60000));
        refreshTime();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeChangeReceiver, filter);
    }

    @Override
    protected void initEvent() {
        add_habitat_name.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showGroupIconDialog();
            }
        });
        add_habitat_time.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showDateTimePickerDialog();
            }
        });

        add_habitat_sunrise.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showTimePickerDialog(mSunrise/60, mSunrise%60, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mSunrise = hourOfDay*60+minute;
                        add_habitat_sunrise.setText(mTimeFormat.format(mSunrise*60000));
                    }
                });
            }
        });

        add_habitat_sunset.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showTimePickerDialog(mSunset/60, mSunset%60, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mSunset = hourOfDay*60+minute;
                        add_habitat_sunset.setText(mTimeFormat.format(mSunset*60000));
                    }
                });
            }
        });

        add_habitat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        add_habitat_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = add_habitat_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    add_habitat_til.setError(getString(R.string.input_empty));
                    return;
                }
                createHabitat(name, mIconName, mZone, mSunrise, mSunset);
            }
        });
    }

    private void refreshTime() {
        long time = System.currentTimeMillis();
        mDateFormat.setTimeZone(new SimpleTimeZone(mOffset*60000, ""));
        add_habitat_systime.setText(mDateFormat.format(time));

        mDateFormat.setTimeZone(new SimpleTimeZone(mZone*60000, ""));
        add_habitat_time.setText(mDateFormat.format(time));
    }

    private void showDateTimePickerDialog() {
        long time = System.currentTimeMillis() + (mZone - mOffset) * 60000;
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        final int yr = calendar.get(Calendar.YEAR);
        final int mon = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);
        showTimePickerDialog(hour, min, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                showDatePickerDialog(yr, mon, day, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mZone = (int) (calendar.getTimeInMillis() / 60000 - System.currentTimeMillis() / 60000 + mOffset);
                        refreshTime();
                    }
                });
            }
        });

//        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
//                TimePickerDialog timeDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        calendar.set(Calendar.YEAR, year);
//                        calendar.set(Calendar.MONTH, month);
//                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                        calendar.set(Calendar.MINUTE, minute);
//                        mZone = (int) (calendar.getTimeInMillis() / 60000 - System.currentTimeMillis() / 60000 + mOffset);
//                        refreshTime();
//                    }
//                }, hour, min, GlobalSettings.is24HourFormat());
//                timeDialog.setCancelable(false);
//                timeDialog.show();
//            }
//        }, yr, mon, day);
//        dateDialog.setCancelable(false);
//        dateDialog.show();
    }

    private void showTimePickerDialog(int hour, int min, final TimePickerDialog.OnTimeSetListener listener) {
//        TimePickerDialog timeDialog = new TimePickerDialog(getContext(), R.style.TimePickerDialogStyle, listener, time/60, time%60, GlobalSettings.is24HourFormat());
//        timeDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_time_picker, null, false);
        final TimePicker tp = view.findViewById(R.id.dialog_time_picker);
        tp.setCurrentHour(hour);
        tp.setCurrentMinute(min);
        tp.setIs24HourView(GlobalSettings.is24HourFormat());
        final AlertDialog dialog = builder.setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setCancelable(false)
                                          .show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTimeSet(tp, tp.getCurrentHour(), tp.getCurrentMinute());
                }
                dialog.dismiss();
            }
        });
    }

    private void showDatePickerDialog(int year, int month, int day, final DatePickerDialog.OnDateSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_date_picker, null, false);
        final DatePicker dp = view.findViewById(R.id.dialog_date_picker);
        dp.init(year, month, day, null);
        final AlertDialog dialog = builder.setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setCancelable(false)
                                          .show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDateSet(dp, dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                }
                dialog.dismiss();
            }
        });
    }

    private void createHabitat(final String name, final String iconName, final int zone, final int sunrise, final int sunset) {
        Map<String, Object> map = new HashMap<>();
        map.put("zone", zone);
        map.put("sunrise", sunrise);
        map.put("sunset", sunset);
        final UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.name = name;
        request.remark1 = JSON.toJSONString(map);
        request.remark2 = iconName;
        AliotServer.getInstance().createGroup(request, new HttpCallback<UserApi.GroupResponse>() {
            @Override
            public void onError(String error) {
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.GroupResponse result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                if (add_habitat_favourite.isChecked()) {
                    FavouriteUtil.addFavourite(getContext(), result.data.groupid);
                }
                GroupManager.getInstance().getGroups();
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
        showLoadDialog();
    }

    private void showGroupIconDialog() {
        GroupIconDialog dialog = new GroupIconDialog(getContext()) {
            @Override
            public void onChoose(String name, int res) {
                mIconName = name;
                add_habitat_name.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, R.drawable.ic_edit_white_24dp, 0);
            }
        };
        dialog.init(mIconName);
        dialog.show();
    }
}
