package com.inledco.exoterra.manager;

import android.text.TextUtils;
import android.util.Log;

import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.xlink.HomesExtendApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class Home2Manager {
    private final String TAG = "Home2Manager";

//    private final List<HomeApi.HomesResponse.Home2> mHome2List;
    private final List<Home2> mHome2List;
    private String mCurrentHomeId;

//    private final XlinkRequestCallback<HomeApi.HomesResponse> mGetHomesCallback;
    private final XlinkRequestCallback<HomesExtendApi.HomesResponse> mGetHomesCallback;
    private final XlinkRequestCallback<String> mGetCurrentHomeidCallback;
    private final XlinkRequestCallback<HomeApi.HomeResponse> mCreateDefaultHomeCallback;
    private final XlinkRequestCallback<String> mSetCurrentHomeidCallback;

    private XlinkRequestCallback<String> mCheckHomeCallback;

    private Home2Manager() {
        mHome2List = new ArrayList<>();

        mGetHomesCallback = new XlinkRequestCallback<HomesExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                if (mCheckHomeCallback != null) {
                    mCheckHomeCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomesExtendApi.HomesResponse response) {
                mHome2List.clear();
                mHome2List.addAll(response.list);

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
                for (int i = 0; i < mHome2List.size(); i++) {
                    Home2 home2 = mHome2List.get(i);
                    if (TextUtils.equals(s, home2.id)) {
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

    public static Home2Manager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mHome2List.clear();
    }

    public void setCurrentHomeId(String currentHomeId) {
        mCurrentHomeId = currentHomeId;
    }

    public String getCurrentHomeId() {
        return mCurrentHomeId;
    }

    public List<Home2> getHome2List() {
        return mHome2List;
    }

    public void refreshHomeList() {
        XlinkCloudManager.getInstance().getHomes(new XlinkRequestCallback<HomesExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onSuccess(HomesExtendApi.HomesResponse response) {
                mHome2List.clear();
                mHome2List.addAll(response.list);
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
//                mHome2List.clear();
//                mHome2List.addAll(response.list);
//                if (callback != null) {
//                    callback.onSuccess(mHome2List);
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
        private static final Home2Manager INSTANCE = new Home2Manager();
    }
}
