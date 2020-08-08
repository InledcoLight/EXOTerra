package com.inledco.exoterra.uvbbuddy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;

import java.util.List;

public class DistanceAdapter extends SimpleAdapter<DistanceUvbLight, DistanceAdapter.DistanceViewHolder> {

    private DistanceViewHolder selectedHolder;

    public DistanceAdapter(@NonNull Context context, List<DistanceUvbLight> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_distance;
    }

    @NonNull
    @Override
    public DistanceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DistanceViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final DistanceViewHolder holder, int i) {
        final int postion = holder.getAdapterPosition();
        DistanceUvbLight distanceUvbLight = mData.get(postion);
        String distance = distanceUvbLight.getDistance();
        if (distance.endsWith("cm")) {
            distance = distance.replace("cm", "\ncm");
        }
        holder.text.setText(distance);
        holder.text.setChecked(holder == selectedHolder);

        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder != selectedHolder) {
                    if (selectedHolder != null) {
                        selectedHolder.text.setChecked(false);
                    }
                    selectedHolder = holder;
                    selectedHolder.text.setChecked(true);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(postion);
                }
            }
        });
    }

    public DistanceUvbLight getSelectedItem() {
        if (selectedHolder != null) {
            int position = selectedHolder.getAdapterPosition();
            return mData.get(position);
        }
        return null;
    }

    class DistanceViewHolder extends RecyclerView.ViewHolder {
        private CheckedTextView text;
        public DistanceViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_distance_text);
        }
    }
}
