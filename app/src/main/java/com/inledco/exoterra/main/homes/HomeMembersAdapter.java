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

import cn.xlink.restful.XLinkRestfulEnum;

public class HomeMembersAdapter extends SimpleAdapter<Home2.User, HomeMembersAdapter.HomeMemberViewHolder> {

    public HomeMembersAdapter(@NonNull Context context, List<Home2.User> data) {
        super(context, data);
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
        Home2.User user = mData.get(position);
        int suAdmin = XLinkRestfulEnum.HomeUserType.SUPER_ADMIN.getValue();
        int admin = XLinkRestfulEnum.HomeUserType.ADMIN.getValue();
        int usr = XLinkRestfulEnum.HomeUserType.USER.getValue();
        int visitor = XLinkRestfulEnum.HomeUserType.VISITOR.getValue();
        if (user.role == suAdmin || user.role == admin) {
            holder.icon.setImageResource(R.drawable.ic_admin_white_32dp);
            holder.role.setText(R.string.admin);
        } else {
            holder.icon.setImageResource(R.drawable.ic_person_white_32dp);
            holder.role.setText(R.string.user);
        }
        holder.nickname.setText(TextUtils.isEmpty(user.nickname) ? "" + user.user_id : user.nickname);

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
