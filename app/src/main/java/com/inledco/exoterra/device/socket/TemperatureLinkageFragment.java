package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.base.BaseFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TemperatureLinkageFragment extends BaseFragment {
    private final int TEMPERATURE_SENSOR_TYP = 1;
    private final int TEMPERATURE_THRD_MIN = 10;
    private final int TEMPERATURE_THRD_MAX = 40;
    private final int TEMPERATURE_ARG_DEFAULT = 25;

    private Toolbar linkage_toolbar;
    private SwitchCompat linkage_enable;
    private TabLayout linkage_tab;
    private ViewPager linkage_vp;

    private final List<Fragment> mFragments = new ArrayList<>();
    private TemperatureAdapter mAdapter;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;

    private byte[][] mTempArgs;

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
        return R.layout.fragment_linkage;
    }

    @Override
    protected void initView(View view) {
        linkage_toolbar = view.findViewById(R.id.linkage_toolbar);
        linkage_enable = view.findViewById(R.id.linkage_enable);
        linkage_tab = view.findViewById(R.id.linkage_tab);
        linkage_vp = view.findViewById(R.id.linkage_vp);

        linkage_toolbar.inflateMenu(R.menu.menu_save);
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();

        refreshData();
    }

    @Override
    protected void initEvent() {
//        linkage_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().getSupportFragmentManager().popBackStack();
//            }
//        });
//
//        linkage_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                if (menuItem.getItemId() == R.id.menu_save) {
//                    boolean enable = linkage_enable.isChecked();
//                    byte[] args = new byte[256];
//                    for (int i = 0; i < 12; i++) {
//                        for (int j = 0; j < 12; j++) {
//                            args[i*12+j] = mTempArgs[i][j];
//                        }
//                    }
//                    mSocketViewModel.setSensorLinkage(enable, args);
//                    getActivity().getSupportFragmentManager().popBackStack();
//                }
//                return false;
//            }
//        });
    }

    private void refreshData() {
        if (mSocket != null) {
//            byte[] args = mSocket.getSV1LinkageArgs();
//            mTempArgs = new byte[12][12];
//            if (args != null && args.length == 256) {
//                for (int i = 0; i < 12; i++) {
//                    for (int j = 0; j < 12; j++) {
//                        mTempArgs[i][j] = args[i*12+j];
//                    }
//                }
//            }
//            for (int i = 0; i < 12; i++) {
//                for (int j = 0; j < 12; j++) {
//                    if (mTempArgs[i][j] < TEMPERATURE_THRD_MIN || mTempArgs[i][j] > TEMPERATURE_THRD_MAX) {
//                        mTempArgs[i][j] = TEMPERATURE_ARG_DEFAULT;
//                    }
//                }
//            }
//
//            linkage_enable.setChecked(mSocket.getSV1LinkageEnable());

            for (int i = 0; i < 12; i++) {
                mFragments.add(TemperatureFragment.newInstance(mTempArgs[i]));
            }
            mAdapter = new TemperatureAdapter(getActivity().getSupportFragmentManager(), getContext(), mFragments);
            linkage_vp.setAdapter(mAdapter);
            linkage_tab.setupWithViewPager(linkage_vp);

            int month = Calendar.getInstance().get(Calendar.MONTH);
            linkage_vp.setCurrentItem(month);
        }
    }
}
