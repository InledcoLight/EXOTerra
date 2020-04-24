package com.inledco.exoterra.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.common.SimpleAdapter;

import java.util.List;

public class HabitatMembersAdapter extends SimpleAdapter<Group.User, HabitatMembersAdapter.HomeMemberViewHolder> {

    private final String creator;

    public HabitatMembersAdapter(@NonNull Context context, final String creator, List<Group.User> data) {
        super(context, data);
        this.creator = creator;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_homember;
    }

    @NonNull
    @Override
    public HomeMemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HomeMemberViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMemberViewHolder holder, final int position) {
        Group.User user = mData.get(position);
        String nickname = user.nickname;
        if (TextUtils.isEmpty(nickname)) {
            nickname = "" + user.userid;
        }
        if (TextUtils.equals(creator, user.userid)) {
            holder.icon.setImageResource(R.drawable.ic_admin_white_32dp);
            holder.role.setText(R.string.admin);
        } else {
            holder.icon.setImageResource(R.drawable.ic_person_white_32dp);
            holder.role.setText(R.string.user);
        }
        holder.nickname.setText(nickname);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    class HomeMemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView nickname;
        private TextView role;
        public HomeMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_homember_icon);
            nickname = itemView.findViewById(R.id.item_homember_name);
            role = itemView.findViewById(R.id.item_homember_role);
        }
    }
}
