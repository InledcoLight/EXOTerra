package com.inledco.exoterra.main.me;

import android.widget.Toast;

import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.HomeApi;

public class InviteMsgFragment extends MsgFragment<HomeApi.InviteeListResponse.Invitee> {

    private final XlinkRequestCallback<HomeApi.InviteeListResponse> mCallback = new XlinkRequestCallback<HomeApi.InviteeListResponse>() {
        @Override
        public void onStart() {

        }

        @Override
        public void onError(final String error) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                         .show();
                    msg_swipe_refresh.setRefreshing(false);
                }
            });
        }

        @Override
        public void onSuccess(HomeApi.InviteeListResponse response) {
            mMessages.clear();
            mMessages.addAll(response.list);
            Collections.sort(mMessages, mComparator);
            mAdapter.notifyDataSetChanged();
            msg_swipe_refresh.setRefreshing(false);
        }
    };

    @Override
    protected void initData() {
        mComparator = new Comparator<HomeApi.InviteeListResponse.Invitee>() {
            @Override
            public int compare(HomeApi.InviteeListResponse.Invitee o1, HomeApi.InviteeListResponse.Invitee o2) {
                String st1 = o1.createTime;
                String st2 = o2.createTime;
                long t1 = 0;
                long t2 = 0;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 2);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                try {
                    Date dt1 = df.parse(st1);
                    Date dt2 = df.parse(st2);
                    t1 = dt1.getTime();
                    t2 = dt2.getTime();
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                if (XLinkRestfulEnum.InvitationStatus.PENDING == o1.status) {
                    if (XLinkRestfulEnum.InvitationStatus.PENDING == o2.status) {

                    } else {
                        return -1;
                    }
                } else if (XLinkRestfulEnum.InvitationStatus.PENDING == o2.status) {
                    return 1;
                }
                if (t1 > t2) {
                    return -1;
                } else if (t1 < t2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        mAdapter = new InviteMsgAdapter(getContext(), mMessages) {
            @Override
            protected void showInviteMessage(final String msg) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT)
                             .show();
                    }
                });
            }
        };
        msg_rv_show.setAdapter(mAdapter);
        msg_swipe_refresh.setRefreshing(true);
        getMessages();
    }

    @Override
    protected void getMessages() {
        XlinkCloudManager.getInstance().getHomeInviteeList(mCallback);
    }
}
