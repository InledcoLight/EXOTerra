package com.inledco.exoterra.main.groups;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.view.AdvancedTextInputEditText;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.xlink.restful.api.app.HomeApi;

public class AddHabitatFragment extends BaseFragment {
    private Toolbar add_habitat_toolbar;
    private TextInputLayout add_habitat_til;
    private AdvancedTextInputEditText add_habitat_name;
    private AdvancedTextInputEditText add_habitat_time;
    private AdvancedTextInputEditText add_habitat_sunrise;
    private AdvancedTextInputEditText add_habitat_sunset;
    private Switch add_habitat_favourite;

    private final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    private boolean is24Hour = true;
    private int mZone;
    private int mSunrise = 360;
    private int mSunset = 1080;

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
        return R.layout.fragment_add_habitat;
    }

    @Override
    protected void initView(View view) {
        add_habitat_toolbar = view.findViewById(R.id.add_habitat_toolbar);
        add_habitat_til = view.findViewById(R.id.add_habitat_til);
        add_habitat_name = view.findViewById(R.id.add_habitat_name);
        add_habitat_time = view.findViewById(R.id.add_habitat_time);
        add_habitat_sunrise = view.findViewById(R.id.add_habitat_sunrise);
        add_habitat_sunset = view.findViewById(R.id.add_habitat_sunset);
        add_habitat_favourite = view.findViewById(R.id.add_habitat_favourite);

        add_habitat_toolbar.inflateMenu(R.menu.menu_save);
        add_habitat_name.requestFocus();
        add_habitat_name.bindTextInputLayout(add_habitat_til);
        add_habitat_time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        add_habitat_sunrise.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        add_habitat_sunset.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
    }

    @Override
    protected void initData() {
        mZone = TimeZone.getDefault().getRawOffset()/60000;
        long time = System.currentTimeMillis();
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        add_habitat_time.setText(df.format(new Date(time)));
    }

    @Override
    protected void initEvent() {
        add_habitat_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        add_habitat_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_save:
                        String name = add_habitat_name.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            add_habitat_til.setError(getString(R.string.input_empty));
                            return true;
                        }
                        createHome(name, mZone, mSunrise, mSunset);
                        break;
                }
                return true;
            }
        });

        add_habitat_time.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showZoneDialog();
            }
        });

        add_habitat_sunrise.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showTimePickerDialog(mSunrise, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mSunrise = hourOfDay*60+minute;
                        DecimalFormat df = new DecimalFormat("00");
                        add_habitat_sunrise.setText(df.format(mSunrise/60) + ":" + df.format(mSunrise%60));
                    }
                });
            }
        });

        add_habitat_sunset.setDrawableRightClickListener(new AdvancedTextInputEditText.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClick() {
                showTimePickerDialog(mSunset, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mSunset = hourOfDay*60+minute;
                        DecimalFormat df = new DecimalFormat("00");
                        add_habitat_sunset.setText(df.format(mSunset/60) + ":" + df.format(mSunset%60));
                    }
                });
            }
        });
    }

    private void showTimePickerDialog(int time, final TimePickerDialog.OnTimeSetListener listener) {
        TimePickerDialog  dialog = new TimePickerDialog(getContext(), listener, time/60, time%60, is24Hour);
        dialog.show();
    }

    private void showZoneDialog() {
        DecimalFormat format = new DecimalFormat("00");
        final String[] hours = new String[26];
        final String[] minutes = new String[60];
        for (int i = 0; i < 13; i++) {
            hours[i] = "-" + format.format(i-12);
            hours[i+13] = "+" + format.format(i);
        }
        for (int i = 0; i < 60; i++) {
            minutes[i] = format.format(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final int rawZone = TimeZone.getDefault().getRawOffset() / 60000;
        long time = System.currentTimeMillis() + (mZone - rawZone)*60000;
        final DateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_zone, null, false);
        final SeekBar sb = view.findViewById(R.id.dialog_zone_sb);
        final TextView tv_time = view.findViewById(R.id.dialog_zone_time);
        sb.setProgress(720+mZone);
        tv_time.setText(df.format(new Date(time)));

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int zone = progress - 720;
                tv_time.setText(df.format(new Date(System.currentTimeMillis() + (zone-rawZone)*60000)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setTitle(R.string.habitat_time)
               .setView(view)
               .show();
    }

    private void createHome(final String name, final int zone, final int sunrise, final int sunset) {
        XlinkCloudManager.getInstance().createHome(name, new XlinkRequestCallback<HomeApi.HomeResponse>() {
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(HomeApi.HomeResponse response) {
                final String homeid = response.id;
                XlinkCloudManager.getInstance().setHomeProperty(homeid, zone, sunrise, sunset, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        EventBus.getDefault().post(new HomeChangedEvent());
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
    }
}
