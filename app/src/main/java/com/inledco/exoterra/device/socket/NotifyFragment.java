package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOSocket;

public class NotifyFragment extends BaseFragment {
    private Toolbar notify_toolbar;
    private Switch notify_sw_enable;
    private TextInputEditText notify_thrd_lower;
    private TextInputEditText notify_thrd_upper;

    private SocketViewModel mSocketViewModel;

    private String mName;
    private boolean mSensor2;
    private int mThrdMin;
    private int mThrdMax;
    private String mUnit;

    public static NotifyFragment newInstance(String name, boolean sensor2, int min, int max, String unit) {
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putBoolean("sensor2", sensor2);
        args.putInt("min", min);
        args.putInt("max", max);
        args.putString("unit", unit);
        NotifyFragment fragment = new NotifyFragment();
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
        return R.layout.fragment_notify;
    }

    @Override
    protected void initView(View view) {
        notify_toolbar = view.findViewById(R.id.notify_toolbar);
        notify_sw_enable = view.findViewById(R.id.notify_sw_enable);
        notify_thrd_lower = view.findViewById(R.id.notify_thrd_lower);
        notify_thrd_upper = view.findViewById(R.id.notify_thrd_upper);

        notify_toolbar.inflateMenu(R.menu.menu_save);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mName = args.getString("name", "");
            mSensor2 = args.getBoolean("sensor2", false);
            mThrdMin = args.getInt("min", 0);
            mThrdMax = args.getInt("max", 0);
            mUnit = args.getString("unit", "");

            notify_toolbar.setTitle(mName + " " + getString(R.string.notification));
        }

        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        EXOSocket socket = mSocketViewModel.getData();
        if (socket != null) {
            notify_sw_enable.setChecked(mSensor2 ? socket.getSV2NotifyEnable() : socket.getSV1NotifyEnable());
            int min = mSensor2 ? socket.getSV2ThrdLower() : socket.getSV1ThrdLower();
            int max = mSensor2 ? socket.getSV2ThrdUpper() : socket.getSV1ThrdUpper();
            if (min >= mThrdMin && max <= mThrdMax && min <= max ) {

            } else {
                min = mThrdMin;
                max = mThrdMax;
            }
            notify_thrd_lower.setText(String.valueOf(min));
            notify_thrd_upper.setText(String.valueOf(max));
        }
    }

    @Override
    protected void initEvent() {
        notify_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        notify_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_save) {
                    boolean enable = notify_sw_enable.isChecked();
                    int lower = Integer.parseInt(notify_thrd_lower.getText().toString());
                    int upper = Integer.parseInt(notify_thrd_upper.getText().toString());
                    if (lower >= mThrdMin && upper <= mThrdMax && lower <= upper) {
                        if (mSensor2) {
                            mSocketViewModel.setSensor2Notify(enable, lower, upper);
                        } else {
                            mSocketViewModel.setSensor1Notify(enable, lower, upper);
                        }
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Invalid params", Toast.LENGTH_SHORT)
                             .show();
                    }
                }
                return false;
            }
        });
    }
}
