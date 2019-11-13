package com.inledco.exoterra.device.Monsoon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.EXOMonsoonTimer;
import com.inledco.exoterra.common.SimpleAdapter;

import java.text.DecimalFormat;
import java.util.List;

public abstract class MonsoonTimerAdapter extends SimpleAdapter<EXOMonsoonTimer, MonsoonTimerAdapter.MonsoonTimerViewHolder> {

    public MonsoonTimerAdapter(@NonNull Context context, List<EXOMonsoonTimer> data) {
        super(context, data);
    }

    public void setTimers(List<EXOMonsoonTimer> timers) {
        mData = timers;
        notifyDataSetChanged();
    }

    private String getDurationDesc(int value) {
        if (value >= 0 && value <= 59) {
            return "" + value + " Sec";
        }
        if (value >= 60 && value <= 119) {
            return "1 Min " + value%60 + " Sec";
        }
        if (value == 120) {
            return "2 Min";
        }
        if (value == 121) {
            return "3 Min";
        }
        if (value == 122) {
            return "4 Min";
        }
        if (value == 123) {
            return "5 Min";
        }
        if (value == 124) {
            return "6 Min";
        }
        if (value == 125) {
            return "8 Min";
        }
        if (value == 126) {
            return "10 Min";
        }
        if (value == 127) {
            return "15 Min";
        }
        return "Invalid";
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_monsoon_timer;
    }

    @NonNull
    @Override
    public MonsoonTimerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MonsoonTimerViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final MonsoonTimerViewHolder holder, final int position) {
        final EXOMonsoonTimer timer = mData.get(holder.getAdapterPosition());
        DecimalFormat df = new DecimalFormat("00");
        holder.tv_action.setText(getDurationDesc(timer.getDuration()));
        holder.tv_time.setText(df.format(timer.getTimer()/60) + ":" + df.format(timer.getTimer()%60));
        @StringRes int[] week = new int[]{R.string.week_sun, R.string.week_mon, R.string.week_tue, R.string.week_wed,
                                          R.string.week_thu, R.string.week_fri, R.string.week_sat};
        String wk = "";
        for (int i = 0; i < 7; i++) {
            if (timer.getWeeks()[i]) {
                wk += mContext.getString(week[i]) + "  ";
            }
        }
        wk = wk.trim();
        holder.tv_week.setText(wk);
        holder.sw_enable.setChecked(timer.isEnable());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(position);
                }
                return false;
            }
        });

        holder.sw_fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.sw_enable.isChecked()) {
                    onDisableTimer(position);
                } else {
                    onEnableTimer(position);
                }
            }
        });
    }

    class MonsoonTimerViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_action;
        private TextView tv_time;
        private TextView tv_week;
        private FrameLayout sw_fl;
        private Switch sw_enable;

        public MonsoonTimerViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_action = itemView.findViewById(R.id.item_monsoon_timer_action);
            tv_time = itemView.findViewById(R.id.item_monsoon_timer_time);
            tv_week = itemView.findViewById(R.id.item_monsoon_timer_week);
            sw_fl = itemView.findViewById(R.id.item_monsoon_timer_fl);
            sw_enable = itemView.findViewById(R.id.item_monsoon_timer_enable);
        }
    }

    protected abstract void onEnableTimer(int position);

    protected abstract void onDisableTimer(int position);
}
