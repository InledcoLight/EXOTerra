package com.inledco.exoterra.device.socket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.EXOSocketTimer;
import com.inledco.exoterra.common.SimpleAdapter;

import java.text.DecimalFormat;
import java.util.List;

public abstract class SocketTimerAdapter extends SimpleAdapter<EXOSocketTimer, SocketTimerAdapter.SocketTimerViewHolder> {

    public SocketTimerAdapter(@NonNull Context context, List<EXOSocketTimer> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_socket_timer;
    }

    @NonNull
    @Override
    public SocketTimerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SocketTimerViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final SocketTimerViewHolder holder, final int position) {
        EXOSocketTimer tmr = mData.get(position);
        DecimalFormat df = new DecimalFormat("00");
        @StringRes int[] week = new int[]{R.string.week_sun, R.string.week_mon, R.string.week_tue, R.string.week_wed,
                                          R.string.week_thu, R.string.week_fri, R.string.week_sat};
        String wk = "";
        for (int i = 0; i < 7; i++) {
            if (tmr.getWeeks()[i]) {
                wk += mContext.getString(week[i]) + "  ";
            }
        }
        wk = wk.trim();
        holder.iv_power.setImageResource(tmr.getAction() ? R.drawable.ic_power_blue : R.drawable.ic_power_red);
        holder.tv_time.setText(df.format(tmr.getTimer()/60) + ":" + df.format(tmr.getTimer()%60));
        holder.tv_week.setText(wk);
        holder.sw_enable.setChecked(tmr.isEnable());

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

    class SocketTimerViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_power;
        private TextView tv_time;
        private TextView tv_week;
        private FrameLayout sw_fl;
        private Switch sw_enable;

        public SocketTimerViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_power = itemView.findViewById(R.id.item_socket_timer_power);
            tv_time = itemView.findViewById(R.id.item_socket_timer_time);
            tv_week = itemView.findViewById(R.id.item_socket_timer_week);
            sw_fl = itemView.findViewById(R.id.item_socket_timer_fl);
            sw_enable = itemView.findViewById(R.id.item_socket_timer_enable);
        }
    }

    protected abstract void onEnableTimer(int position);

    protected abstract void onDisableTimer(int position);
}
