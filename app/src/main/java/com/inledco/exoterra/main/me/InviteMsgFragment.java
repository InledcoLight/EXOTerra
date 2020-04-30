package com.inledco.exoterra.main.me;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.InviteStatus;
import com.inledco.exoterra.common.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;

public class InviteMsgFragment extends MsgFragment<UserApi.InviteRecord> {

    private boolean isSent;

    public static InviteMsgFragment newInstance(boolean isSent) {
        Bundle args = new Bundle();
        args.putBoolean("is_sent", isSent);
        InviteMsgFragment fragment = new InviteMsgFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
        Bundle args = getArguments();
        if (args != null) {
            isSent = args.getBoolean("is_sent");
        }

        mComparator = new Comparator<UserApi.InviteRecord>() {
            @Override
            public int compare(UserApi.InviteRecord o1, UserApi.InviteRecord o2) {
                int pending = InviteStatus.PENDING.getStatus();
                if (o1.status == pending && o2.status != pending) {
                    return -1;
                } else if (o2.status == pending && o1.status != pending) {
                    return 1;
                } else if (o1.status == pending && o2.status == pending) {
                    return (int) (o2.create_time - o1.create_time);
                }
                return (int) (o2.create_time - o1.create_time);
            }
        };
        mAdapter = new InviteMsgAdapter(getContext(), mMessages, isSent);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                UserApi.InviteRecord record = mMessages.get(position);
                if (record.status == InviteStatus.PENDING.getStatus()) {
                    if (isSent) {
                        showInviterActionDialog(record);
                    } else {
                        showInviteeActionDialog(record);
                    }
                }
            }
        });
        msg_rv_show.setAdapter(mAdapter);
        msg_swipe_refresh.setRefreshing(true);
        getMessages();
    }

    @Override
    protected void getMessages() {
        HttpCallback<UserApi.InviteListResponse> callback = new HttpCallback<UserApi.InviteListResponse>() {
            @Override
            public void onError(String error) {
                showToast(error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msg_swipe_refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onSuccess(UserApi.InviteListResponse result) {
                mMessages.clear();
                mMessages.addAll(result.data);
                Collections.sort(mMessages, mComparator);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        msg_swipe_refresh.setRefreshing(false);
                    }
                });
            }
        };
        if (isSent) {
            AliotServer.getInstance().getInviterList(callback);
        } else {
            AliotServer.getInstance().getInviteeList(callback);
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onInviteStatusChangedEvent(UserApi.InviteRecord record) {
        if (record == null || mAdapter == null) {
            return;
        }
        for (int i = 0; i < mMessages.size(); i++) {
            if (TextUtils.equals(mMessages.get(i).invite_id, record.invite_id)) {
                mAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    private void showInviterActionDialog(final UserApi.InviteRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.habitat_invitation)
               .setMessage("Invite " + record.invitee + " to join habitat \"" +record.group_name + "\".")
               .setNegativeButton(R.string.cancel_invitation, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       showLoadDialog();
                       AliotServer.getInstance().cancelInvite(record.groupid, record.invite_id, new HttpCallback<UserApi.Response>() {
                           @Override
                           public void onError(String error) {
                               showToast(error);
                               dismissLoadDialog();
                           }

                           @Override
                           public void onSuccess(UserApi.Response result) {
                               record.status = InviteStatus.CANCELLED.getStatus();
                               EventBus.getDefault().post(record);
                               dismissLoadDialog();
                           }
                       });
                   }
               })
               .setPositiveButton(R.string.close, null)
               .setCancelable(false)
               .show();
    }

    private void showInviteeActionDialog(final UserApi.InviteRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.habitat_invitation)
               .setMessage("Join habitat \"" + record.group_name + "\"?")
               .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       showLoadDialog();
                       AliotServer.getInstance().denyInvite(record.groupid, record.invite_id, new HttpCallback<UserApi.Response>() {
                           @Override
                           public void onError(String error) {
                               showToast(error);
                               dismissLoadDialog();
                           }

                           @Override
                           public void onSuccess(UserApi.Response result) {
                               record.status = InviteStatus.DENIED.getStatus();
                               EventBus.getDefault().post(record);
                               dismissLoadDialog();
                           }
                       });
                   }
               })
               .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       showLoadDialog();
                       AliotServer.getInstance().acceptInvite(record.groupid, record.invite_id, new HttpCallback<UserApi.Response>() {
                           @Override
                           public void onError(String error) {
                               showToast(error);
                               dismissLoadDialog();
                           }

                           @Override
                           public void onSuccess(UserApi.Response result) {
                               record.status = InviteStatus.ACCEPTED.getStatus();
                               EventBus.getDefault().post(record);
                               dismissLoadDialog();
                           }
                       });
                   }
               })
               .setNeutralButton(R.string.later, null)
               .setCancelable(false)
               .show();
    }
}
