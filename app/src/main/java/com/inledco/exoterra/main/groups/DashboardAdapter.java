package com.inledco.exoterra.main.groups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.util.GroupUtil;

import java.util.List;

public class DashboardAdapter extends SimpleAdapter<Group, DashboardAdapter.GroupViewHolder> {

    public DashboardAdapter(@NonNull Context context, List<Group> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_dashboard_group;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GroupViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupViewHolder holder, int i) {
        final int positon = holder.getAdapterPosition();
        final Group group = mData.get(positon);
        holder.name.setText(group.name);
        holder.icon.setImageResource(GroupUtil.getGroupIcon(group.remark2));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(positon);
                }
            }
        });
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_dashgroup_icon);
            name = itemView.findViewById(R.id.item_dashgroup_name);
        }
    }
}
