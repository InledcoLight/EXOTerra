package com.inledco.exoterra.device.light;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.common.OnItemClickListener;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private Context mContext;
    private EXOLedstrip mLight;
    private OnItemClickListener mItemClickListener;

    public SliderAdapter(Context context, EXOLedstrip light) {
        mContext = context;
        mLight = light;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        SliderViewHolder holder = new SliderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_progress, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SliderViewHolder holder, final int pos) {
        final int position = holder.getAdapterPosition();
        int progress = mLight.getBright(position);
        String name = mLight.getChannelName(position);

//        holder.progress.setFinishedStrokeColor(LightUtil.getColorValue(name));
//        holder.progress.setProgress(progress);
//        holder.progress.setText("" + progress/10 + " %");
//        holder.color.setText(name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });

//        //动态设置SeekBar progressDrawable
//        holder.sb_progress.setProgressDrawable(LightUtil.getProgressDrawable(mContext, name));
//        //动态设置SeekBar thumb
//        GradientDrawable thumb = (GradientDrawable) holder.sb_progress.getThumb();
//        thumb.setColor(LightUtil.getColorValue(name));
//
//        holder.sb_progress.setProgress(progress);
//        holder.tv_percent.setText(df.format(progress / 10) + "%");
//        holder.sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                setBright(holder.getAdapterPosition(), seekBar.getProgress());
//            }
//        });
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

    class SliderViewHolder extends RecyclerView.ViewHolder {
//        private DonutProgress progress;
//        private TextView color;
//        private SeekBar sb_progress;
//        private TextView tv_percent;

        public SliderViewHolder(View itemView) {
            super(itemView);
//            progress = itemView.findViewById(R.id.item_progress_percent);
//            color = itemView.findViewById(R.id.item_progress_color);
//            sb_progress = itemView.findViewById(R.id.item_slider_progress);
//            tv_percent = itemView.findViewById(R.id.item_slider_percent);
        }
    }

//    protected abstract void setBright(int idx, int bright);
}
