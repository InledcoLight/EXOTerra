package com.liruya.exoterra.device.light;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.bean.Profile;
import com.liruya.exoterra.bean.TimePoint;
import com.liruya.exoterra.util.LightUtil;

import java.text.DecimalFormat;

public class ProfileFragment extends BaseFragment {
    private LineChart profile_chart;
    private RecyclerView profile_rv;
    private FloatingActionButton profile_fab_edit;

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;
    private Profile mProfile;
    private int mIndex = -1;
    private TimePointsAdapter mAdapter;

    public static ProfileFragment newInstance(int idx) {
        Bundle args = new Bundle();
        args.putInt("idx", idx);
        ProfileFragment fragment = new ProfileFragment();
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
        return R.layout.fragment_profile;
    }

    @Override
    protected void initView(View view) {
        profile_chart = view.findViewById(R.id.profile_chart);
        profile_rv = view.findViewById(R.id.profile_rv);
        profile_fab_edit = view.findViewById(R.id.profile_fab_edit);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        mIndex = args.getInt("idx");
        mLightViewModel = ViewModelProviders.of(getActivity())
                                            .get(LightViewModel.class);
        mLight = mLightViewModel.getData();
        mLightViewModel.observe(this, new Observer<EXOLedstrip>() {
            @Override
            public void onChanged(@Nullable EXOLedstrip exoLedstrip) {
                mProfile = mLight.getProfile(mIndex);
                LineChartHelper.setProfile(profile_chart, mLight.getChannelCount(), mLight.getChannelNames(), mProfile);
                mAdapter.notifyDataSetChanged();
            }
        });

        LineChartHelper.init(profile_chart);
        mProfile = mLight.getProfile(mIndex);
        LineChartHelper.setProfile(profile_chart, mLight.getChannelCount(), mLight.getChannelNames(), mProfile);
        mAdapter = new TimePointsAdapter();
        profile_rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        profile_fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                             .beginTransaction()
                             .add(R.id.device_root, EditProFragment.newInstance(mIndex))
                             .addToBackStack("")
                             .commit();
            }
        });
    }

    class TimePointsAdapter extends RecyclerView.Adapter<TimerPointsViewHolder> {
        @NonNull
        @Override
        public TimerPointsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TimerPointsViewHolder holder = new TimerPointsViewHolder(LayoutInflater.from(getContext())
                                                                                   .inflate(R.layout.item_timepoint, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TimerPointsViewHolder holder, int position) {
            int chns = mLight.getChannelCount();
            if (chns <= 0 || chns > 6) {
                return;
            }
            TimePoint tp = mProfile.getPoints().get(position);
            DecimalFormat df = new DecimalFormat("00");
            holder.tv_num.setText("" + df.format(position + 1));
            holder.tv_tmr.setText(df.format(tp.getTimer()/60) + ":" + df.format(tp.getTimer()%60));
            for (int i = 0; i < chns; i++) {
                holder.tv_brts[i].setVisibility(View.VISIBLE);
                holder.tv_brts[i].setText(" " + tp.getBrights()[i] + "%");
                holder.tv_brts[i].setCompoundDrawablesWithIntrinsicBounds(LightUtil.getIconRes(mLight.getChannelName(i)), 0, 0, 0);
            }
            for (int i = chns; i < 6; i++) {
                holder.tv_brts[i].setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mProfile == null ? 0 : mProfile.getPointCount();
        }
    }

    class TimerPointsViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_num;
        private TextView tv_tmr;
        private TextView[] tv_brts;

        public TimerPointsViewHolder(View itemView) {
            super(itemView);
            tv_num = itemView.findViewById(R.id.item_tbp_num);
            tv_tmr = itemView.findViewById(R.id.item_tbp_tmr);
            tv_brts = new TextView[6];
            tv_brts[0] = itemView.findViewById(R.id.item_tp_chn1);
            tv_brts[1] = itemView.findViewById(R.id.item_tp_chn2);
            tv_brts[2] = itemView.findViewById(R.id.item_tp_chn3);
            tv_brts[3] = itemView.findViewById(R.id.item_tp_chn4);
            tv_brts[4] = itemView.findViewById(R.id.item_tp_chn5);
            tv_brts[5] = itemView.findViewById(R.id.item_tp_chn6);
        }
    }
}
