package com.inledco.exoterra.main.me;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.InviteStatus;
import com.inledco.exoterra.common.SimpleAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InviteMsgAdapter extends SimpleAdapter<UserApi.InviteRecord, InviteMsgAdapter.MsgViewHolder> {
    private boolean mInviter;

    public InviteMsgAdapter(@NonNull Context context, List<UserApi.InviteRecord> data, boolean inviter) {
        super(context, data);
        mInviter = inviter;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_msg;
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MsgViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int i) {
        final int position = holder.getAdapterPosition();
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final UserApi.InviteRecord msg = mData.get(position);
        InviteStatus status = InviteStatus.getInviteStatus(msg.status);
        String state = "";
        if (status != null) {
            switch (status) {
                case PENDING:
                    state = mContext.getString(R.string.pending);
                    break;
                case CANCELLED:
                    state = mContext.getString(R.string.cancelled);
                    break;
                case ACCEPTED:
                    state = mContext.getString(R.string.accepted);
                    break;
                case DENIED:
                    state = mContext.getString(R.string.denied);
                    break;
                case EXPIRED:
                    state = mContext.getString(R.string.expired);
                    break;
            }
        }
        holder.create_time.setText(df.format(new Date(msg.create_time)));
        holder.habitat_name.setText(msg.group_name);
        holder.habitat_id.setText(msg.groupid);
        holder.inviter_invitee_tag.setText(mInviter ? R.string.invitee : R.string.inviter);
        holder.inviter_invitee.setText(mInviter ? msg.invitee : msg.inviter);
        holder.expire_time.setText(df.format(new Date(msg.end_time)));
        holder.status.setText(state);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {
        private TextView create_time;
        private TextView habitat_name;
        private TextView habitat_id;
        private TextView inviter_invitee_tag;
        private TextView inviter_invitee;
        private TextView expire_time;
        private TextView status;
        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            create_time = itemView.findViewById(R.id.item_invite_create_time);
            habitat_name = itemView.findViewById(R.id.item_invite_habitat_name);
            habitat_id = itemView.findViewById(R.id.item_invite_habitat_id);
            inviter_invitee_tag = itemView.findViewById(R.id.item_inviter_invitee_tag);
            inviter_invitee = itemView.findViewById(R.id.item_inviter_invitee);
            expire_time = itemView.findViewById(R.id.item_invite_expire_time);
            status = itemView.findViewById(R.id.item_invite_status);
        }
    }
}
