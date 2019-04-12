package com.liruya.exoterra.device.light;

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
import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.bean.Profile;

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
//        if (profile == null || !profile.isValid()) {
//            return;
//        }
//        int pointCount = profile.getPointCount();
//        profile.sort();
//        int[] time = profile.getTimes();
//        int[][] brights = profile.getBrights();
//
//        if (mDataSets == null) {
//            mDataSets = new ArrayList<>();
//        }
//        mDataSets.clear();
//
//        for (int i = 0; i < chnCount; i++) {
//            List<Entry> entries = new ArrayList<>();
//            int ts = time[0];
//            int te = time[pointCount - 1];
//            int bs = brights[0][i];
//            int be = brights[pointCount - 1][i];
//            int duration = 1440 - te + ts;
//            int dbrt = bs - be;
//            float b0 = be + dbrt * (1440 - te) / (float) duration;
//            entries.add(new Entry(0, b0));
//            for (int j = 0; j < pointCount; j++) {
//                entries.add(new Entry(time[j], brights[j][i]));
//            }
//            entries.add(new Entry(1440, b0));
//
//            String color = mLight.getChannelName(i);
//            if (color.endsWith("\0")) {
//                color = color.substring(0, color.length() - 1);
//            }
//            LineDataSet lineDataSet = new LineDataSet(entries, color);
//            lineDataSet.setColor(LightUtil.getColorValue(color));
//            lineDataSet.setCircleRadius(3.0f);
//            lineDataSet.setCircleColor(LightUtil.getColorValue(color));
//            lineDataSet.setDrawCircleHole(false);
//            lineDataSet.setLineWidth(2.0f);
//            mDataSets.add(lineDataSet);
//        }
//        mLineData = new LineData(mDataSets);
//        light_pro_chart.setData(mLineData);
//        light_pro_chart.invalidate();
    }

    private void showSelectProfileFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();

        if (manager.findFragmentByTag("select_profile") == null) {
            manager.beginTransaction()
                   .add(R.id.device_root, new SelectProfileFragment(), "select_profile")
                   .addToBackStack("")
                   .commit();
        }
    }

    private void showProfilesFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();

        if (manager.findFragmentByTag("profiles") == null) {
            manager.beginTransaction()
                   .add(R.id.device_root, new ProfilesFragment(), "profiles")
                   .addToBackStack("")
                   .commit();
        }
    }

//    private void showSelectProfileDialog() {
//        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_select_profile, null, false);
//        RecyclerView select_profile_rv = view.findViewById(R.id.select_profile_rv);
//        Button select_profile_cancel = view.findViewById(R.id.select_profile_cancel);
//        Button select_profile_choose = view.findViewById(R.id.select_profile_choose);
//        select_profile_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
//        final ProfileAdapter adapter = new ProfileAdapter();
//        select_profile_rv.setAdapter(adapter);
//        select_profile_rv.smoothScrollToPosition(mLight.getSelectProfile());
//        select_profile_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        select_profile_choose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLightViewModel.setSelectProfile(adapter.getSelect());
//                dialog.dismiss();
//            }
//        });
//        dialog.setContentView(view);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
//        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//    }
//
//    class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {
//        private int select;
//        private int chnCount;
//        private String[] colors;
//
//        public ProfileAdapter() {
//            select = mLightViewModel.getData().getSelectProfile();
//            chnCount = mLightViewModel.getData().getChannelCount();
//            colors = mLightViewModel.getData().getChannelNames();
//        }
//
//        public int getSelect() {
//            return select;
//        }
//
//        @NonNull
//        @Override
//        public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_profile, viewGroup, false);
//            return new ProfileViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull final ProfileViewHolder holder, final int i) {
//            final int position = holder.getAdapterPosition();
//            final Profile profile = mLightViewModel.getData().getProfile(position);
//            //避免触发onCheckedChanged监听事件
//            holder.radioButton.setOnCheckedChangeListener(null);
//            holder.radioButton.setChecked(select == position);
//            holder.radioButton.setText(mLightViewModel.getData().getProfileName(position));
//            //            LineChartHelper.setProfile(holder.lineChart, chnCount, colors, profile);
//            //            holder.lineChart.setVisibility(View.VISIBLE);
//            if (select == position) {
//                LineChartHelper.setProfile(holder.lineChart, chnCount, colors, profile);
//                holder.lineChart.setVisibility(View.VISIBLE);
//            } else {
//                LineChartHelper.setProfile(holder.lineChart, chnCount, colors, null);
//                holder.lineChart.setVisibility(View.GONE);
//            }
//            holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        int old = select;
//                        notifyItemChanged(old);
//                        select = position;
//                        LineChartHelper.setProfile(holder.lineChart, chnCount, colors, profile);
//                        holder.lineChart.setVisibility(View.VISIBLE);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return 13;
//        }
//    }
//
//    class ProfileViewHolder extends RecyclerView.ViewHolder {
//        private RadioButton radioButton;
//        private LineChart lineChart;
//        public ProfileViewHolder(@NonNull View itemView) {
//            super(itemView);
//            radioButton = itemView.findViewById(R.id.item_profile_rb);
//            lineChart = itemView.findViewById(R.id.item_profile_chart);
//            LineChartHelper.init(lineChart);
//        }
//    }
}
