package com.inledco.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.LightViewModel;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.LightSpectrum;
import com.inledco.exoterra.util.SpectrumUtil;

public class LightProFragment extends BaseFragment {
    private LineChart light_pro_chart;
    private BarChart light_pro_spectrum;
    private TextView light_pro_select;
    private ImageButton light_pro_list;

    private LightViewModel mLightViewModel;
    private ExoLed mLight;
    private LightSpectrum mLightSpectrum;

    private final BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_TICK)) {
                refresSpectrum();
                showTimeLine();
            }
        }
    };

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
        getActivity().unregisterReceiver(mTimeTickReceiver);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_light_pro;
    }

    @Override
    protected void initView(View view) {
        light_pro_chart = view.findViewById(R.id.light_pro_chart);
        light_pro_spectrum = view.findViewById(R.id.light_pro_spectrum);
        light_pro_select = view.findViewById(R.id.light_pro_select);
        light_pro_list = view.findViewById(R.id.light_pro_list);

        ChartHelper.initLineChart(light_pro_chart);
        ChartHelper.initBarChart(light_pro_spectrum);
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<ExoLed>() {
            @Override
            public void onChanged(@Nullable ExoLed exoLed) {
                refreshData();
            }
        });

        mLightSpectrum = SpectrumUtil.loadDataFromAssets(getContext().getAssets(), "exoterrastrip_spectrum_450.txt");

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getActivity().registerReceiver(mTimeTickReceiver, filter);

        refreshData();
        showTimeLine();
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

    private void showTimeLine() {
//        int zone = mLight.getZone();
//        zone = (zone/100)*60 + (zone%100);
//        long tm = System.currentTimeMillis()/60000 + zone;
//        int minutes = (int) (tm%1440);
//        LimitLine line = new LimitLine(minutes);
//        line.setLineColor(Color.WHITE);
//        line.setLineWidth(1);
//        line.enableDashedLine(10, 20, 0);
//        light_pro_chart.getXAxis().removeAllLimitLines();
//        light_pro_chart.getXAxis().addLimitLine(line);
//        light_pro_chart.invalidate();
    }

    private void refresSpectrum() {
//        int zone = mLight.getZone();
//        zone = (zone/100)*60 + (zone%100);
//        long tm = System.currentTimeMillis()/60000 + zone;
//        int minutes = (int) (tm%1440);
//
//        int cnt = mLight.getChannelCount();
//        int select = mLight.getSelectProfile();
//        Profile profile = mLight.getProfile(select);
//        List<TimePoint> tps = profile.getPoints();
//        int[][] brights = profile.getBrights();
//        int duration = 0;
//        int span = 0;
//        int t1 = tps.get(tps.size()-1).getTimer();
//        int t2 = tps.get(0).getTimer();
//        if (minutes >= t1 || minutes < t2) {
//            duration = 1440 - t1 + t2;
//            span = (1440 + minutes - t1) % 1440;
//            for (int i = 0; i < cnt; i++) {
//                int db = brights[0][i] - brights[tps.size()-1][i];
//                int val = brights[tps.size()-1][i] + span * db / duration;
//                Log.e(TAG, "refresSpectrum1: " + val);
//                mLightSpectrum.setGain(i, ((float) val)/100);
//            }
//        } else {
//            for (int i = 0; i < tps.size() - 1; i++) {
//                t1 = tps.get(i).getTimer();
//                t2 = tps.get(i + 1).getTimer();
//                if (minutes >= t1 && minutes < t2) {
//                    duration = 1440 - t1 + t2;
//                    span = (1440 + minutes - t1) % 1440;
//                    for (int j = 0; j < cnt; j++) {
//                        int db = brights[i+1][j] - brights[i][j];
//                        int val = brights[i][j] + span * db / duration;
//                        Log.e(TAG, "refresSpectrum2: " + val);
//                        mLightSpectrum.setGain(j, ((float) val) / 100);
//                    }
//                    break;
//                }
//            }
//        }
//
//        int min = getResources().getInteger(R.integer.spectrum_wavelength_min);
//        int max = getResources().getInteger(R.integer.spectrum_wavelength_max);
//        int[] waveColors = getResources().getIntArray(R.array.spectrum);
//        if (waveColors == null || waveColors.length != max-min+1) {
//            try {
//                throw new Exception("Invalid wave-color table.");
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        int wave_min = mLightSpectrum.getStart();
//        float spectrumMax = mLightSpectrum.getMax();
//        List<IBarDataSet> dataSets = new ArrayList<>();
//        for (int i = min; i <= max; i++) {
//            List<BarEntry> entries = new ArrayList<>();
//            entries.add(new BarEntry(i, mLightSpectrum.get(i-wave_min)/spectrumMax));
//            BarDataSet barDataSet = new BarDataSet(entries, null);
//            barDataSet.setDrawValues(false);
//            int color = waveColors[i - min];
//            barDataSet.setColor(color);
//            barDataSet.setBarBorderColor(color);
//            barDataSet.setBarShadowColor(color);
//            barDataSet.setHighlightEnabled(false);
//            dataSets.add(barDataSet);
//        }
//        BarData barData = new BarData(dataSets);
//        barData.setBarWidth(2);
//        light_pro_spectrum.setData(barData);
//        light_pro_spectrum.invalidate();
    }

    private void refreshData() {
//        if (mLight == null) {
//            return;
//        }
//        int select = mLight.getSelectProfile();
//        String name = mLight.getProfileName(select);
//        light_pro_select.setText(name);
//
//        int chnCount = mLight.getChannelCount();
//        String[] colors = mLight.getChannelNames();
//        Profile profile = mLight.getProfile(select);
//        ChartHelper.setProfile(light_pro_chart, chnCount, colors, profile);
//        refresSpectrum();
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
