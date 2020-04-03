package com.inledco.exoterra.adddevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.common.SimpleAdapter;

import java.util.List;

public class AssignHabitatAdapter extends SimpleAdapter<Home, AssignHabitatAdapter.ChooseHabitatViewHolder> {
    private String mSelectedHomeid;
    private ChooseHabitatViewHolder mSelectedHolder;

    public AssignHabitatAdapter(@NonNull Context context, List<Home> data) {
        super(context, data);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_choose_habitat;
    }

    @NonNull
    @Override
    public ChooseHabitatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChooseHabitatViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final ChooseHabitatViewHolder holder, int i) {
//        final int position = holder.getAdapterPosition();
//        final HomeApi.HomesResponse.Home home = mData.get(position).getHome();
//        boolean selected = TextUtils.equals(mSelectedHomeid, home.id);
//        holder.name.setText(home.name);
//        holder.name.setChecked(selected);
//        if (selected) {
//            mSelectedHolder = holder;
//        }
//
//        holder.name.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!holder.name.isChecked()) {
//                    if (mSelectedHolder != null) {
//                        mSelectedHolder.name.setChecked(false);
//                    }
//                    mSelectedHomeid = home.id;
//                    mSelectedHolder = holder;
//                    holder.name.setChecked(true);
//                }
//            }
//        });
    }

    public String getSelectedHomeid() {
        return mSelectedHomeid;
    }

    class ChooseHabitatViewHolder extends RecyclerView.ViewHolder {
        private CheckedTextView name;
        public ChooseHabitatViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_choose_habitat_name);
        }
    }
}
