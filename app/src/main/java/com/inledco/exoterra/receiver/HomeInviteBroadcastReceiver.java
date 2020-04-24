package com.inledco.exoterra.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.inledco.exoterra.AppConstants;

public class HomeInviteBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), AppConstants.HOME_INVITE)) {
            final int notification_id = intent.getIntExtra(AppConstants.NOTIFICATION_ID, 0);
            final String action = intent.getStringExtra(AppConstants.ACTION);
            final String homeid = intent.getStringExtra(AppConstants.HOME_ID);
            final String inviteid = intent.getStringExtra(AppConstants.INVITE_ID);
            if (notification_id == 0 || TextUtils.isEmpty(action) || TextUtils.isEmpty(homeid) || TextUtils.isEmpty(inviteid)) {
                return;
            }
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            switch (action) {
//                case AppConstants.ACCEPT:
//
//                    notificationManager.cancel(inviteid, notification_id);
//                    break;
//                case AppConstants.DENY:
//
//                    notificationManager.cancel(inviteid, notification_id);
//                    break;
//            }
        }
    }
}
