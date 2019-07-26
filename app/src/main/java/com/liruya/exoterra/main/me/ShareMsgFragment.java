package com.liruya.exoterra.main.me;

import android.widget.Toast;

import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.DeviceApi;

public class ShareMsgFragment extends MsgFragment<DeviceApi.ShareDeviceItem> {

    private final XlinkRequestCallback<List<DeviceApi.ShareDeviceItem>> mCallback = new XlinkRequestCallback<List<DeviceApi.ShareDeviceItem>>() {
        @Override
        public void onSuccess(List<DeviceApi.ShareDeviceItem> shareDeviceItems) {
            mMessages.clear();
            mMessages.addAll(shareDeviceItems);
            Collections.sort(mMessages, mComparator);
            mAdapter.notifyDataSetChanged();
            msg_swipe_refresh.setRefreshing(false);
        }

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
    };

    @Override
    protected void initData() {
        mComparator = new Comparator<DeviceApi.ShareDeviceItem>() {
            @Override
            public int compare(DeviceApi.ShareDeviceItem o1, DeviceApi.ShareDeviceItem o2) {
                long t1 = Long.parseLong(o1.genDate);
                long t2 = Long.parseLong(o2.genDate);
                if (XLinkRestfulEnum.ShareStatus.PENDING == o1.state) {
                    if (XLinkRestfulEnum.ShareStatus.PENDING == o2.state) {

                    } else {
                        return -1;
                    }
                } else if (XLinkRestfulEnum.ShareStatus.PENDING == o2.state) {
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
        mAdapter = new ShareMsgAdapter(getContext(), mMessages) {
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
        getMessages();
        msg_swipe_refresh.setRefreshing(true);
    }

    @Override
    protected void getMessages() {
        XlinkCloudManager.getInstance().getDeviceShareList(mCallback);
    }
}
