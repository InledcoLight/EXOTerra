package com.liruya.exoterra.main.me;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.liruya.exoterra.R;
import com.liruya.exoterra.base.SimpleAdapter;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import java.util.List;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.DeviceApi;

public abstract class ShareMsgAdapter extends SimpleAdapter<DeviceApi.ShareDeviceItem, ShareMsgAdapter.MsgViewHolder> {

    private final XlinkTaskCallback<String> mAcceptCallback;
    private final XlinkTaskCallback<String> mDenyCallback;

    public ShareMsgAdapter(@NonNull Context context, List<DeviceApi.ShareDeviceItem> data) {
        super(context, data);
        mAcceptCallback = new XlinkTaskCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String s) {
                showInviteMessage("Accept device share sucess.");
            }

            @Override
            public void onError(String error) {
                showInviteMessage(error);
            }
        };
        mDenyCallback = new XlinkTaskCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String s) {
                showInviteMessage("Deny device share sucess.");
            }

            @Override
            public void onError(String error) {
                showInviteMessage(error);
            }
        };
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_msg;
    }

    @Override
    protected void onItemClick(int position) {

    }

    @Override
    protected boolean onItemLongClick(int position) {
        return false;
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MsgViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int i) {
        final DeviceApi.ShareDeviceItem msg = mData.get(i);
        String state = "";
        if (msg.state == XLinkRestfulEnum.ShareStatus.PENDING) {
            holder.btn_accept.setVisibility(View.VISIBLE);
            holder.btn_deny.setVisibility(View.VISIBLE);
            holder.tv_status.setVisibility(View.GONE);
        } else {
            holder.btn_accept.setVisibility(View.GONE);
            holder.btn_deny.setVisibility(View.GONE);
            holder.tv_status.setVisibility(View.VISIBLE);
        }
        switch (msg.state) {
            case DENY:
                state = mContext.getString(R.string.denied);
                break;
            case ACCEPT:
                state = mContext.getString(R.string.accepted);
                break;
            case CANCEL:
                state = mContext.getString(R.string.canceled);
                break;
            case OVERTIME:
                state = mContext.getString(R.string.overtime);
                break;
            case UNSUBSCRIBED:
                state = mContext.getString(R.string.unsubscribed);
                break;
            case INVALID:
                state = mContext.getString(R.string.invalid);
                break;
        }
        holder.tv_status.setText(state);
        String from = msg.fromName;
        if (TextUtils.isEmpty(from)) {
            from = String.valueOf(msg.fromId);
        }
        holder.tv_msg.setText("User " + from + " share to you device " + msg.extend + ".\n" + msg.genDate);
        holder.btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().acceptShareDevice(msg.inviteCode, mAcceptCallback);
            }
        });
        holder.btn_deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XlinkCloudManager.getInstance().denyShareDevice(msg.inviteCode, mDenyCallback);
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
