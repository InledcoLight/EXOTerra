package com.inledco.exoterra.device.detail;

public class DeviceUserAdapter {
//public class DeviceUserAdapter extends SimpleAdapter<DeviceApi.DeviceSubscribeUsersResponse.UserBean, DeviceUserAdapter.DeviceUserViewHolder> {
//
//    public DeviceUserAdapter(@NonNull Context context, List<DeviceApi.DeviceSubscribeUsersResponse.UserBean> data) {
//        super(context, data);
//    }
//
//    @Override
//    protected int getItemLayoutResId() {
//        return R.layout.item_device_user;
//    }
//
//    @NonNull
//    @Override
//    public DeviceUserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        return new DeviceUserViewHolder(createView(viewGroup));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull DeviceUserViewHolder holder, int i) {
//        DeviceApi.DeviceSubscribeUsersResponse.UserBean user = mData.get(i);
//        Log.e("DeviceUserAdapter", "onBindViewHolder: " + user.fromId + "  " + user.userId);
//        if (user.role == XLinkRestfulEnum.DeviceSubscribeRole.ADMIN) {
//            holder.iv_role.setImageResource(R.drawable.ic_admin_white_64dp);
//        } else {
//            holder.iv_role.setImageResource(R.drawable.ic_person_white_64dp);
//        }
//        String name = user.nickname;
//        if (XLinkUserManager.getInstance().getUid() == user.userId) {
//            name = name + " (" + mContext.getString(R.string.me) + ")";
//        }
//        holder.tv_nickname.setText(name);
//        holder.tv_userid.setText(String.valueOf(user.userId));
//    }
//
//    class DeviceUserViewHolder extends RecyclerView.ViewHolder {
//        private ImageView iv_role;
//        private TextView tv_nickname;
//        private TextView tv_userid;
//        public DeviceUserViewHolder(@NonNull View itemView) {
//            super(itemView);
//            iv_role = itemView.findViewById(R.id.item_user_role);
//            tv_nickname = itemView.findViewById(R.id.item_user_nickname);
//            tv_userid = itemView.findViewById(R.id.item_user_userid);
//        }
//    }
}
