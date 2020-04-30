package com.inledco.exoterra.device.Monsoon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.common.SimpleAdapter;

import java.text.DecimalFormat;
import java.util.List;

public abstract class MonsoonTimerAdapter extends SimpleAdapter<ExoMonsoon.Timer, MonsoonTimerAdapter.MonsoonTimerViewHolder> {

    public MonsoonTimerAdapter(@NonNull Context context, List<ExoMonsoon.Timer> data) {
        super(context, data);
    }

    public void setTimers(List<ExoMonsoon.Timer> timers) {
        mData = timers;
        notifyDataSetChanged();
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_timer;
    }

    @NonNull
    @Override
    public MonsoonTimerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MonsoonTimerViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final MonsoonTimerViewHolder holder, int pos) {
        final int position = holder.getAdapterPosition();
        ExoMonsoon.Timer tmr = mData.get(position);
        holder.tv_ontime.setText(getTimeText(tmr.getTimer()));
        holder.tv_offtime.setText("" + tmr.getPeriod() + " s");
        holder.ctv_enable.setChecked(tmr.isEnable());
        int repeat = tmr.getRepeat();
        for (int i = 0; i < 7; i++) {
            holder.ctv_week[i].setChecked((repeat&(1<<i)) != 0);
        }

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
        holder.ctv_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.ctv_enable.isChecked()) {
                    onDisableTimer(position);
                } else {
                    onEnableTimer(position);
                }
            }
        });
    }

    private String getTimeText(int time) {
        DecimalFormat df = new DecimalFormat("00");
        return df.format(time/60) + ":" + df.format(time%60);
    }

    class MonsoonTimerViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_ontime;
        private TextView tv_offtime;
        private CheckedTextView ctv_enable;
        private CheckedTextView[] ctv_week;

        public MonsoonTimerViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_ontime = itemView.findViewById(R.id.item_timer_time);
            tv_offtime = itemView.findViewById(R.id.item_timer_offtime);
            ctv_enable = itemView.findViewById(R.id.item_timer_enable);
            ctv_week = new CheckedTextView[7];
            ctv_week[0] = itemView.findViewById(R.id.item_timer_sun);
            ctv_week[1] = itemView.findViewById(R.id.item_timer_mon);
            ctv_week[2] = itemView.findViewById(R.id.item_timer_tue);
            ctv_week[3] = itemView.findViewById(R.id.item_timer_wed);
            ctv_week[4] = itemView.findViewById(R.id.item_timer_thu);
            ctv_week[5] = itemView.findViewById(R.id.item_timer_fri);
            ctv_week[6] = itemView.findViewById(R.id.item_timer_sat);
        }
    }

    protected abstract void onEnableTimer(int position);

    protected abstract void onDisableTimer(int position);
}
