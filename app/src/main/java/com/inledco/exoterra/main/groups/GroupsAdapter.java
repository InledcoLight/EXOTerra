package com.inledco.exoterra.main.groups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.xlink.HomeExtendApi;

import java.util.List;

public abstract class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
    private Context mContext;
    private List<HomeExtendApi.HomesResponse.Home> mHomes;

    public GroupsAdapter(@NonNull Context context, final List<HomeExtendApi.HomesResponse.Home> homes) {
        mContext = context;
        mHomes = homes;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        GroupViewHolder holder = new GroupViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_group, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupViewHolder holder, int i) {
        HomeExtendApi.HomesResponse.Home home = mHomes.get(i);
        holder.home_name.setText(home.name);
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

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView home_name;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            home_name = itemView.findViewById(R.id.item_home_name);
        }
    }
}
