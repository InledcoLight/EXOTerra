package com.inledco.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.bean.Profile;

import java.util.List;

public class LightProFragment extends BaseFragment {
    private LineChart light_pro_chart;
    private TextView light_pro_select;
    private ImageButton light_pro_list;

    private LineData mLineData;
    private List<ILineDataSet> mDataSets;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;

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
        return R.layout.fragment_light_pro;
    }

    @Override
    protected void initView(View view) {
        light_pro_chart = view.findViewById(R.id.light_pro_chart);
        light_pro_select = view.findViewById(R.id.light_pro_select);
        light_pro_list = view.findViewById(R.id.light_pro_list);

        LineChartHelper.init(light_pro_chart);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
                refreshData();
            }
        });

        refreshData();
    }

    @Override
    protected void initEvent() {
        light_pro_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectProfileFragment();
//                showSelectProfileDialog();
            }
        });

        light_pro_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfilesFragment();
            }
        });
    }

    private void refreshData() {
        if (mLight == null) {
            return;
        }
        int select = mLight.getSelectProfile();
        String name = mLight.getProfileName(select);
        light_pro_select.setText(name);

        int chnCount = mLight.getChannelCount();
        String[] colors = mLight.getChannelNames();
        Profile profile = mLight.getProfile(select);
        LineChartHelper.setProfile(light_pro_chart, chnCount, colors, profile);
    }

    private void showSelectProfileFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();

        if (manager.findFragmentByTag(SelectProfileFragment.class.getSimpleName()) == null) {
            addFragmentToStack(R.id.device_root, new SelectProfileFragment());
        }
    }

    private void showProfilesFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();

        if (manager.findFragmentByTag(ProfilesFragment.class.getSimpleName()) == null) {
            addFragmentToStack(R.id.device_root, new ProfilesFragment());
        }
    }
}
