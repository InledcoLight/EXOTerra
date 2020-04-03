package com.inledco.exoterra.group;

public class GroupDevicesAdapter {
//public class GroupDevicesAdapter extends SimpleAdapter<HomeApi.HomeDevicesResponse.Device, GroupDevicesAdapter.HomeDevicesViewHolder> {
//    public GroupDevicesAdapter(@NonNull Context context, List<HomeApi.HomeDevicesResponse.Device> data) {
//        super(context, data);
//    }
//
//    @Override
//    protected int getItemLayoutResId() {
//        return R.layout.item_device_content;
//    }
//
//    @NonNull
//    @Override
//    public HomeDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        return new HomeDevicesViewHolder(createView(viewGroup));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull HomeDevicesViewHolder holder, int i) {
//        final int position = holder.getAdapterPosition();
//        HomeApi.HomeDevicesResponse.Device device = mData.get(position);
//        String pid = device.productId;
//        String name = TextUtils.isEmpty(device.name) ? DeviceUtil.getDefaultName(pid) : device.name;
//        holder.iv_icon.setImageResource(DeviceUtil.getProductIconSmall(pid));
//        holder.tv_name.setText(name);
//        boolean state = device.isOnline;
//        holder.ctv_state.setChecked(state);
//        holder.ctv_state.setText(state ? R.string.cloud_online : R.string.cloud_offline);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mItemClickListener != null) {
//                    mItemClickListener.onItemClick(position);
//                }
//            }
//        });
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (mItemLongClickListener != null) {
//                    return mItemLongClickListener.onItemLongClick(position);
//                }
//                return false;
//            }
//        });
//    }
//
//    class HomeDevicesViewHolder extends RecyclerView.ViewHolder {
//        private ImageView iv_icon;
//        private TextView tv_name;
//        private CheckedTextView ctv_state;
////        private TextView ctv_habitat;
//        public HomeDevicesViewHolder(@NonNull View itemView) {
//            super(itemView);
//            iv_icon = itemView.findViewById(R.id.item_device_icon);
//            tv_name = itemView.findViewById(R.id.item_device_name);
//            ctv_state = itemView.findViewById(R.id.item_device_state);
////            ctv_habitat = itemView.findViewById(R.id.item_device_desc);
//        }
//    }
}
