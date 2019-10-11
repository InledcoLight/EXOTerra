package com.inledco.exoterra.device.socket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.EXOSocketTimer;

import java.text.DecimalFormat;
import java.util.List;

public abstract class SocketTimerAdapter extends RecyclerView.Adapter<SocketTimerAdapter.SocketTimerViewHolder> {

    private Context mContext;
    private List<EXOSocketTimer> mTimers;

    public SocketTimerAdapter(Context context, List<EXOSocketTimer> timers) {
        mContext = context;
        mTimers = timers;
    }

    @NonNull
    @Override
    public SocketTimerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        SocketTimerViewHolder holder = new SocketTimerViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_socket_timer, viewGroup, false));
        return holder;
    }

    @SuppressLint ("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull SocketTimerViewHolder holder, final int position) {
        EXOSocketTimer tmr = mTimers.get(position);
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
        holder.cib_power.setChecked(tmr.getAction());
        holder.tv_time.setText(df.format(tmr.getTimer()/60) + ":" + df.format(tmr.getTimer()%60));
        holder.tv_week.setText(wk);
        holder.sw_enable.setChecked(tmr.isEnable());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItem(position);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClickItem(position);
                return true;
            }
        });
        holder.sw_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    buttonView.setChecked(!isChecked);
                    if (isChecked) {
                        onEnableTimer(position);
                    } else {
                        onDisableTimer(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTimers == null ? 0 : mTimers.size();
    }

    class SocketTimerViewHolder extends RecyclerView.ViewHolder {
        private CheckableImageButton cib_power;
        private TextView tv_time;
        private TextView tv_week;
        private Switch sw_enable;

        public SocketTimerViewHolder(@NonNull View itemView) {
            super(itemView);
            cib_power = itemView.findViewById(R.id.item_socket_timer_power);
            tv_time = itemView.findViewById(R.id.item_socket_timer_time);
            tv_week = itemView.findViewById(R.id.item_socket_timer_week);
            sw_enable = itemView.findViewById(R.id.item_socket_timer_enable);
        }
    }

    protected abstract void onClickItem(int position);

    protected abstract void onLongClickItem(int position);

    protected abstract void onEnableTimer(int position);

    protected abstract void onDisableTimer(int position);
}
