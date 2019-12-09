package com.inledco.exoterra.main.homes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.common.SimpleAdapter;

import java.util.List;

public class HomesAdapter extends SimpleAdapter<Home2, HomesAdapter.HomesViewHolder> {

    private String mCurrentHomeId;
    private boolean mShowEnter;

    public HomesAdapter(@NonNull Context context, List<Home2> data, String currentHomeId) {
        super(context, data);
        mCurrentHomeId = currentHomeId;
        mShowEnter = true;
    }

    public HomesAdapter(@NonNull Context context, List<Home2> data, String currentHomeId, boolean showEnter) {
        super(context, data);
        mCurrentHomeId = currentHomeId;
        mShowEnter = showEnter;
    }

    public void setCurrentHomeId(String currentHomeId) {
        mCurrentHomeId = currentHomeId;
        notifyDataSetChanged();
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_home;
    }

    @NonNull
    @Override
    public HomesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HomesViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull HomesViewHolder holder, final int position) {
        Home2 home2 = mData.get(position);
        if (TextUtils.equals(mCurrentHomeId, home2.id)) {
            holder.item_current.setVisibility(View.VISIBLE);
        } else {
            holder.item_current.setVisibility(View.INVISIBLE);
        }
        holder.item_name.setText(home2.name);
        holder.item_enter.setVisibility(mShowEnter ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    class HomesViewHolder extends RecyclerView.ViewHolder {
        private ImageView item_current;
        private TextView item_name;
        private ImageView item_enter;
        public HomesViewHolder(@NonNull View itemView) {
            super(itemView);
            item_current = itemView.findViewById(R.id.item_home_current);
            item_name = itemView.findViewById(R.id.item_home_name);
            item_enter = itemView.findViewById(R.id.item_home_enter);
        }
    }
}
