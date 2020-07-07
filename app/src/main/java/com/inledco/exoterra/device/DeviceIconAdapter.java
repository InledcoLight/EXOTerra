package com.inledco.exoterra.device;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.DeviceIconUtil;

public class DeviceIconAdapter extends SimpleAdapter<Integer, DeviceIconAdapter.DeviceIconViewHolder> {

    private int mSelected;
    private DeviceIconViewHolder mSelectedHolder;

    public DeviceIconAdapter(@NonNull Context context, int resid) {
        super(context, DeviceIconUtil.getDeviceIcons());
        mSelected = resid;
    }

    @NonNull
    @Override
    public DeviceIconViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DeviceIconViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceIconViewHolder holder, int i) {
        final int postion = holder.getAdapterPosition();
        final int res = mData.get(postion);

        holder.icon.setImageResource(res);

        if (mSelected == res) {
            mSelectedHolder = holder;
            holder.icon.setBackgroundResource(R.drawable.shape_roundrect_gradient_horiz);
        } else {
            holder.icon.setBackgroundColor(0);
        }

        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedHolder != null) {
                    mSelectedHolder.icon.setBackgroundColor(0);
                }
                mSelected = res;
                mSelectedHolder = holder;
                holder.icon.setBackgroundResource(R.drawable.shape_roundrect_gradient_horiz);

                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(postion);
                }
            }
        });
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_devicon;
    }

    public int getSelectedPostion() {
        for (int i = 0; i < mData.size(); i++) {
            if (mSelected == mData.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getSelected() {
        return mSelected;
    }

    class DeviceIconViewHolder extends RecyclerView.ViewHolder {
        private ImageButton icon;
        public DeviceIconViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_devicon_src);
        }
    }
}
