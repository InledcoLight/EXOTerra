package com.liruya.exoterra.main.homes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liruya.exoterra.R;

import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public abstract class HomesAdapter extends RecyclerView.Adapter<HomesAdapter.HomeViewHolder> {
    private Context mContext;
    private List<HomeApi.HomesResponse.Home> mHomes;

    public HomesAdapter(@NonNull Context context, final List<HomeApi.HomesResponse.Home> homes) {
        mContext = context;
        mHomes = homes;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        HomeViewHolder holder = new HomeViewHolder(LayoutInflater.from(mContext)
                                                                 .inflate(R.layout.item_home, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeViewHolder holder, int i) {
        HomeApi.HomesResponse.Home home = mHomes.get(i);
        holder.home_name.setText(home.name);
        holder.home_id.setText(home.id);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onItemLongClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mHomes == null ? 0 : mHomes.size();
    }

    protected abstract void onItemClick(int position);

    protected abstract boolean onItemLongClick(int position);

    class HomeViewHolder extends RecyclerView.ViewHolder {
        private TextView home_name;
        private TextView home_id;
        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            home_name = itemView.findViewById(R.id.item_home_name);
            home_id = itemView.findViewById(R.id.item_home_id);
        }
    }
}
