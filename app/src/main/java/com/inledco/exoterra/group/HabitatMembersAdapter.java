package com.inledco.exoterra.group;

public class HabitatMembersAdapter  {
//public class HabitatMembersAdapter extends SimpleAdapter<HomeApi.HomesResponse.Home.User, HabitatMembersAdapter.HomeMemberViewHolder> {
//
//    public HabitatMembersAdapter(@NonNull Context context, List<HomeApi.HomesResponse.Home.User> data) {
//        super(context, data);
//    }
//
//    @Override
//    protected int getItemLayoutResId() {
//        return R.layout.item_homember;
//    }
//
//    @NonNull
//    @Override
//    public HomeMemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        return new HomeMemberViewHolder(createView(viewGroup));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull HomeMemberViewHolder holder, final int position) {
//        HomeApi.HomesResponse.Home.User user = mData.get(position);
//        String nickname = user.nicketName;
//        if (TextUtils.isEmpty(nickname)) {
//            nickname = "" + user.userId;
//        }
//        switch (user.role) {
//            case SUPER_ADMIN:
//                holder.icon.setImageResource(R.drawable.ic_admin_white_32dp);
//                holder.role.setText(R.string.creator);
//                break;
//            case ADMIN:
//                holder.icon.setImageResource(R.drawable.ic_admin_white_32dp);
//                holder.role.setText(R.string.admin);
//                break;
//            case USER:
//                holder.icon.setImageResource(R.drawable.ic_person_white_32dp);
//                holder.role.setText(R.string.user);
//                break;
//            case VISITOR:
//                holder.icon.setImageResource(R.drawable.ic_person_white_32dp);
//                holder.role.setText(R.string.visitor);
//                break;
//        }
//        holder.nickname.setText(nickname);
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mItemClickListener != null) {
//                    mItemClickListener.onItemClick(position);
//                }
//            }
//        });
//    }
//
//    class HomeMemberViewHolder extends RecyclerView.ViewHolder {
//        private ImageView icon;
//        private TextView nickname;
//        private TextView role;
//        public HomeMemberViewHolder(@NonNull View itemView) {
//            super(itemView);
//            icon = itemView.findViewById(R.id.item_homember_icon);
//            nickname = itemView.findViewById(R.id.item_homember_name);
//            role = itemView.findViewById(R.id.item_homember_role);
//        }
//    }
}
