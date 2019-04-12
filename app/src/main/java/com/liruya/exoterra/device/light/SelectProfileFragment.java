package com.liruya.exoterra.device.light;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.github.mikephil.charting.charts.LineChart;
import com.liruya.base.BaseFragment;
import com.liruya.exoterra.R;
import com.liruya.exoterra.bean.Profile;

public class SelectProfileFragment extends BaseFragment {
    private Toolbar select_profile_toolbar;
    private RecyclerView select_profile_rv;

    private LightViewModel mLightViewModel;
    private ProfileAdapter mAdapter;

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
        return R.layout.fragment_select_profile;
    }

    @Override
    protected void initView(View view) {
        select_profile_toolbar = view.findViewById(R.id.select_profile_toolbar);
        select_profile_rv = view.findViewById(R.id.select_profile_rv);

        select_profile_toolbar.inflateMenu(R.menu.menu_select_profile);
        select_profile_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mLightViewModel = ViewModelProviders.of(getActivity()).get(LightViewModel.class);

        mAdapter = new ProfileAdapter();
        select_profile_rv.setAdapter(mAdapter);
        select_profile_rv.smoothScrollToPosition(mLightViewModel.getData().getSelectProfile());
    }

    @Override
    protected void initEvent() {
        select_profile_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        select_profile_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_select_choose) {
                    mLightViewModel.setSelectProfile(mAdapter.getSelect());
                    close();
                }
                return false;
            }
        });
    }

    private void close() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {
        private int select;
        private int chnCount;
        private String[] colors;

        public ProfileAdapter() {
            select = mLightViewModel.getData().getSelectProfile();
            chnCount = mLightViewModel.getData().getChannelCount();
            colors = mLightViewModel.getData().getChannelNames();
        }

        public int getSelect() {
            return select;
        }

        @NonNull
        @Override
        public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_profile, viewGroup, false);
            return new ProfileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ProfileViewHolder holder, final int i) {
            final int position = holder.getAdapterPosition();
            final Profile profile = mLightViewModel.getData().getProfile(position);
            //避免触发onCheckedChanged监听事件
            holder.radioButton.setOnCheckedChangeListener(null);
            holder.radioButton.setChecked(select == position);
            holder.radioButton.setText(mLightViewModel.getData().getProfileName(position));
//            LineChartHelper.setProfile(holder.lineChart, chnCount, colors, profile);
//            holder.lineChart.setVisibility(View.VISIBLE);
            if (select == position) {
                LineChartHelper.setProfile(holder.lineChart, chnCount, colors, profile);
                holder.lineChart.setVisibility(View.VISIBLE);
            } else {
                LineChartHelper.setProfile(holder.lineChart, chnCount, colors, null);
                holder.lineChart.setVisibility(View.GONE);
            }
            holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        int old = select;
                        notifyItemChanged(old);
                        select = position;
                        LineChartHelper.setProfile(holder.lineChart, chnCount, colors, profile);
                        holder.lineChart.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return 13;
        }
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private RadioButton radioButton;
        private LineChart lineChart;
        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.item_profile_rb);
            lineChart = itemView.findViewById(R.id.item_profile_chart);
            LineChartHelper.init(lineChart);
        }
    }
}
