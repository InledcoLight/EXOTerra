package com.inledco.exoterra.device.socket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
    public void onBindViewHolder(@NonNull final SocketTimerViewHolder holder, final int pos) {
        final int position = holder.getAdapterPosition();
        EXOSocketTimer tmr = mData.get(position);
        DecimalFormat df = new DecimalFormat("00");
        final String[] weeks = mContext.getResources().getStringArray(R.array.weeks);
        final int repeat = tmr.getRepeat()&0x7F;
        String wk = "";
        if (repeat == 0) {
            wk = mContext.getString(R.string.execute_once);
        } else if (repeat == 0x7F) {
            wk = mContext.getString(R.string.everyday);
        } else {
            for (int i = 0; i < 7; i++) {
                if ((repeat&(1<<i)) != 0) {
                    wk += weeks[i] + " ";
                }
            }
        }
        wk = wk.trim();
        switch (tmr.getAction()) {
            case EXOSocketTimer.ACTION_TURNOFF:
                holder.tv_ontime.setVisibility(View.INVISIBLE);
                holder.tv_offtime.setVisibility(View.VISIBLE);
//                holder.tv_offtime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_power_red_24dp, 0, 0, 0);
                holder.tv_offtime.setText(df.format(tmr.getHour()) + ":" + df.format(tmr.getMinute()) + ":" + df.format(tmr.getSecond()));
                break;
            case EXOSocketTimer.ACTION_TURNON:
                holder.tv_ontime.setVisibility(View.VISIBLE);
                holder.tv_offtime.setVisibility(View.INVISIBLE);
//                holder.tv_ontime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_power_blue_24dp, 0, 0, 0);
                holder.tv_ontime.setText(df.format(tmr.getHour()) + ":" + df.format(tmr.getMinute()) + ":" + df.format(tmr.getSecond()));
                break;
            case EXOSocketTimer.ACTION_TURNON_PERIOD:
                holder.tv_ontime.setVisibility(View.VISIBLE);
                holder.tv_offtime.setVisibility(View.VISIBLE);
//                holder.tv_ontime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_power_blue_24dp, 0, 0, 0);
//                holder.tv_offtime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_power_red_24dp, 0, 0, 0);
                holder.tv_ontime.setText(df.format(tmr.getHour()) + ":" + df.format(tmr.getMinute()) + ":" + df.format(tmr.getSecond()));
                holder.tv_offtime.setText(df.format(tmr.getEndHour()) + ":" + df.format(tmr.getEndMinute()) + ":" + df.format(tmr.getEndSecond()));
                break;
        }
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
        private TextView tv_ontime;
        private TextView tv_offtime;
        private TextView tv_week;
        private FrameLayout sw_fl;
        private Switch sw_enable;

        public SocketTimerViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_ontime = itemView.findViewById(R.id.item_socket_timer_ontime);
            tv_offtime = itemView.findViewById(R.id.item_socket_timer_offtime);
            tv_week = itemView.findViewById(R.id.item_socket_timer_week);
            sw_fl = itemView.findViewById(R.id.item_socket_timer_fl);
            sw_enable = itemView.findViewById(R.id.item_socket_timer_enable);
        }
    }

    protected abstract void onEnableTimer(int position);

    protected abstract void onDisableTimer(int position);
}
