package com.inledco.exoterra.device.light;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.util.LightUtil;
import com.inledco.exoterra.view.VerticalSeekBar;

import java.text.DecimalFormat;

public abstract class VerticalSliderAdapter extends RecyclerView.Adapter<VerticalSliderAdapter.VerticalSliderViewHolder> {

    private Context mContext;
    private EXOLedstrip mLight;

    public VerticalSliderAdapter(Context context, EXOLedstrip light) {
        mContext = context;
        mLight = light;
    }

    @NonNull
    @Override
    public VerticalSliderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        VerticalSliderViewHolder holder = new VerticalSliderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_slider_vertical, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final VerticalSliderViewHolder holder, int i) {
        int progress = mLight.getBright(holder.getAdapterPosition());
        String name = mLight.getChannelName(holder.getAdapterPosition());
        int color = LightUtil.getColorValue(name);
        DecimalFormat df = new DecimalFormat("##0");

        //动态设置SeekBar progressDrawable
        LayerDrawable layer = (LayerDrawable) holder.sb_progress.getProgressDrawable();
        ClipDrawable clipDrawable = (ClipDrawable) layer.findDrawableByLayerId(android.R.id.progress);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            GradientDrawable progressDraw = (GradientDrawable) clipDrawable.getDrawable();
            progressDraw.setColor(color);
        } else {
            clipDrawable.getCurrent();
            holder.sb_progress.setProgressDrawable(LightUtil.getProgressDrawable(mContext, name));
        }
        //动态设置SeekBar thumb
        GradientDrawable thumb = (GradientDrawable) holder.sb_progress.getThumb();
        thumb.setColor(LightUtil.getColorValue(name));

        holder.sb_progress.setProgress(progress);
        holder.tv_percent.setText(df.format(progress / 10) + "%");
        holder.sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBright(holder.getAdapterPosition(), seekBar.getProgress());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLight.getChannelCount();
    }

    public byte[] getBrights() {
        byte[] brights = new byte[mLight.getChannelCount()];
        for (int i = 0; i < mLight.getChannelCount(); i++) {
            brights[i] = (byte) (mLight.getBright(i) / 10);
        }
        return brights;
    }

    class VerticalSliderViewHolder extends RecyclerView.ViewHolder {
        private VerticalSeekBar sb_progress;
        private TextView tv_percent;

        public VerticalSliderViewHolder(View itemView) {
            super(itemView);
            sb_progress = itemView.findViewById(R.id.item_slider_vertical_progress);
            tv_percent = itemView.findViewById(R.id.item_slider_vertical_percent);
        }
    }

    protected abstract void setBright(int idx, int bright);
}
