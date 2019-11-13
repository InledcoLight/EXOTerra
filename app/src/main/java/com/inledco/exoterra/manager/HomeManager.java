package com.inledco.exoterra.manager;

import android.text.TextUtils;
import android.util.Log;

import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.xlink.HomesExtendApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class HomeManager {
    private final String TAG = "HomeManager";

//    private final List<HomeApi.HomesResponse.Home> mHomeList;
    private final List<Home> mHomeList;
    private String mCurrentHomeId;

//    private final XlinkRequestCallback<HomeApi.HomesResponse> mGetHomesCallback;
    private final XlinkRequestCallback<HomesExtendApi.HomesResponse> mGetHomesCallback;
    private final XlinkRequestCallback<String> mGetCurrentHomeidCallback;
    private final XlinkRequestCallback<HomeApi.HomeResponse> mCreateDefaultHomeCallback;
    private final XlinkRequestCallback<String> mSetCurrentHomeidCallback;

    private XlinkRequestCallback<String> mCheckHomeCallback;

    private HomeManager() {
        mHomeList = new ArrayList<>();

        mGetHomesCallback = new XlinkRequestCallback<HomesExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                if (mCheckHomeCallback != null) {
                    mCheckHomeCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomesExtendApi.HomesResponse response) {
                mHomeList.clear();
                mHomeList.addAll(response.list);

                XlinkCloudManager.getInstance().getCurrentHomeId(mGetCurrentHomeidCallback);
            }
        };

        mGetCurrentHomeidCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                if (mCheckHomeCallback != null) {
                    mCheckHomeCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "onSuccess: gethomeid " + s);
                for (int i = 0; i < mHomeList.size(); i++) {
                    Home home = mHomeList.get(i);
                    if (TextUtils.equals(s, home.id)) {
                        mCurrentHomeId = s;

                        if (mCheckHomeCallback != null) {
                            mCheckHomeCallback.onSuccess(s);
                        }
                        return;
                    }
                }

                XlinkCloudManager.getInstance().createDefaultHome(mCreateDefaultHomeCallback);
            }
        };

        mCreateDefaultHomeCallback = new XlinkRequestCallback<HomeApi.HomeResponse>() {
            @Override
            public void onError(String error) {
                if (mCheckHomeCallback != null) {
                    mCheckHomeCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomeApi.HomeResponse response) {
                XlinkCloudManager.getInstance().setCurrentHomeId(response.id, mSetCurrentHomeidCallback);

                Log.e(TAG, "onSuccess: createhome" + response.id + " " + response.name);
            }
        };

        mSetCurrentHomeidCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                if (mCheckHomeCallback != null) {
                    mCheckHomeCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(String s) {
                mCurrentHomeId = s;

                if (mCheckHomeCallback != null) {
                    mCheckHomeCallback.onSuccess(s);
                }
            }
        };
    }

    public static HomeManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mHomeList.clear();
    }

    public void setCurrentHomeId(String currentHomeId) {
        mCurrentHomeId = currentHomeId;
    }

    public String getCurrentHomeId() {
        return mCurrentHomeId;
    }

    public List<Home> getHomeList() {
        return mHomeList;
    }

    public void refreshHomeList() {
        XlinkCloudManager.getInstance().getHomes(new XlinkRequestCallback<HomesExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onSuccess(HomesExtendApi.HomesResponse response) {
                mHomeList.clear();
                mHomeList.addAll(response.list);
                EventBus.getDefault().post(new HomeChangedEvent());
            }
        });
    }

    public void syncHomeList(final XlinkRequestCallback<List<HomeApi.HomesResponse.Home>> callback) {
        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomeApi.HomesResponse response) {
//                mHomeList.clear();
//                mHomeList.addAll(response.list);
//                if (callback != null) {
//                    callback.onSuccess(mHomeList);
//                }
            }
        });
    }

    /**
     * 1. 获取homes列表
     * 2. 获取currentHomeId
     * 3. 检查currentHomeId是否在homes列表中, 如果不在则创建默认Home并设置为currentHomeId
     */
    public void checkHome(final XlinkRequestCallback<String> callback) {
        if (XLinkUserManager.getInstance().isUserAuthorized()) {
            mCheckHomeCallback = callback;
            XlinkCloudManager.getInstance().getHomes(mGetHomesCallback);
        }
    }

    private static class LazyHolder {
        private static final HomeManager INSTANCE = new HomeManager();
    }
}
