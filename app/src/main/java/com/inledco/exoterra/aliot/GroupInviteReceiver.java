package com.inledco.exoterra.aliot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.aliot.bean.InviteAction;
import com.inledco.exoterra.aliot.bean.InviteMessage;

import org.greenrobot.eventbus.EventBus;

public class GroupInviteReceiver extends BroadcastReceiver {
    private static final String TAG = "GroupInviteReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: " + intent.getAction());
        if (TextUtils.equals(intent.getAction(), AppConstants.GROUP_INVITE)) {
            final String message = intent.getStringExtra("InviteMessage");
            Log.e(TAG, "onReceive: " + message);
            try {
                final InviteMessage inviteMessage = JSON.parseObject(message, InviteMessage.class);
                if (inviteMessage == null) {
                    return;
                }
                final InviteAction action = InviteAction.getInviteAction(inviteMessage.getAction());
                if (action == null) {
                    return;
                }
                final String inviter = inviteMessage.getInviter();
                final String inviteid = inviteMessage.getInvite_id();
                final String groupid = inviteMessage.getGroupid();
                final String groupname = inviteMessage.getGroupname();
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                switch (action) {
                    case ACCEPT:
                        AliotServer.getInstance().acceptInvite(groupid, inviteid, new HttpCallback<UserApi.Response>() {
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "onError: " + error);
                            }

                            @Override
                            public void onSuccess(UserApi.Response result) {
                                EventBus.getDefault().post(inviteMessage);
                                AliotServer.getInstance().inviteAccept(inviter, inviteid, groupid, groupname);
                            }
                        });
                        manager.cancel(inviteid, 0);
                        break;
                    case DENY:
                        AliotServer.getInstance().denyInvite(groupid, inviteid, new HttpCallback<UserApi.Response>() {
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "onError: " + error);
                            }

                            @Override
                            public void onSuccess(UserApi.Response result) {
                                AliotServer.getInstance().inviteDeny(inviter, inviteid, groupid, groupname);
                            }
                        });
                        manager.cancel(inviteid, 0);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
