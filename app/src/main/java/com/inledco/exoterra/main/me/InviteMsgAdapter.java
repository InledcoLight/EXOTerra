package com.inledco.exoterra.main.me;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.util.List;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.HomeApi;

public abstract class InviteMsgAdapter extends SimpleAdapter<HomeApi.InviteeListResponse.Invitee, InviteMsgAdapter.MsgViewHolder> {

    private final XlinkRequestCallback<String> mAcceptCallback;
    private final XlinkRequestCallback<String> mDenyCallback;

    public InviteMsgAdapter(@NonNull Context context, List<HomeApi.InviteeListResponse.Invitee> data) {
        super(context, data);
        mAcceptCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                showInviteMessage(error);
            }

            @Override
            public void onSuccess(String s) {
                showInviteMessage("Accept home invite sucess.");
            }
        };
        mDenyCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(String error) {
                showInviteMessage(error);
            }

            @Override
            public void onSuccess(String s) {
                showInviteMessage("Deny home invite sucess.");
            }
        };
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
        final HomeApi.InviteeListResponse.Invitee msg = mData.get(i);
        String state = "";
        if (msg.status == XLinkRestfulEnum.InvitationStatus.PENDING) {
            holder.btn_accept.setVisibility(View.VISIBLE);
            holder.btn_deny.setVisibility(View.VISIBLE);
            holder.tv_status.setVisibility(View.GONE);
        } else {
            holder.btn_accept.setVisibility(View.GONE);
            holder.btn_deny.setVisibility(View.GONE);
            holder.tv_status.setVisibility(View.VISIBLE);
        }
        switch (msg.status) {
            case DENY:
                state = mContext.getString(R.string.denied);
                break;
            case ACCEPT:
                state = mContext.getString(R.string.accepted);
                break;
            case CANCEL:
                state = mContext.getString(R.string.canceled);
                break;
            case INVALID:
                state = mContext.getString(R.string.invalid);
                break;
        }
        holder.tv_status.setText(state);
        String from = String.valueOf(msg.inviter);
        holder.tv_msg.setText("User " + from + " invite you join home " + msg.homeName + ".\n" + msg.createTime);
        holder.btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().acceptHomeInvite(msg.homeId, String.valueOf(msg.invitee), mAcceptCallback);
            }
        });
        holder.btn_deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().denyHomeInvite(msg.homeId, String.valueOf(msg.invitee), mDenyCallback);
            }
        });
    }

    protected abstract void showInviteMessage(String msg);

    public class MsgViewHolder extends RecyclerView.ViewHolder {
        TextView tv_msg;
        Button btn_accept;
        Button btn_deny;
        TextView tv_status;
        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_msg = itemView.findViewById(R.id.item_share_msg);
            btn_accept = itemView.findViewById(R.id.item_msg_accept);
            btn_deny = itemView.findViewById(R.id.item_msg_deny);
            tv_status = itemView.findViewById(R.id.item_msg_status);
        }
    }
}
