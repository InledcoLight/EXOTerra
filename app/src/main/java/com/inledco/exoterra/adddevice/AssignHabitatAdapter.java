package com.inledco.exoterra.adddevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.UserManager;

import java.util.List;

public class AssignHabitatAdapter extends SimpleAdapter<Group, AssignHabitatAdapter.ChooseHabitatViewHolder> {
    private String mSelectedGroupid;
    private Group mSelectedGroup;
    private ChooseHabitatViewHolder mSelectedHolder;

    public AssignHabitatAdapter(@NonNull Context context, List<Group> data) {
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
        final int position = holder.getAdapterPosition();
        final Group group = mData.get(position);
        boolean selected = TextUtils.equals(mSelectedGroupid, group.groupid);
        holder.name.setEnabled(TextUtils.equals(group.creator, UserManager.getInstance().getUserid()));
        holder.name.setText(group.name);
        holder.name.setChecked(selected);
        if (selected) {
            mSelectedGroup = group;
            mSelectedHolder = holder;
        }
        
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.name.isChecked()) {
                    if (mSelectedHolder != null) {
                        mSelectedHolder.name.setChecked(false);
                    }
                    mSelectedGroupid = group.groupid;
                    mSelectedGroup = group;
                    mSelectedHolder = holder;
                    holder.name.setChecked(true);
                }
            }
        });
    }

    public Group getSelectedGroup() {
        return mSelectedGroup;
    }

    class ChooseHabitatViewHolder extends RecyclerView.ViewHolder {
        private CheckedTextView name;
        public ChooseHabitatViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_choose_habitat_name);
        }
    }
}
