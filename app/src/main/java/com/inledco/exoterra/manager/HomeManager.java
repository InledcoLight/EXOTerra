package com.inledco.exoterra.manager;

import android.text.TextUtils;
import android.util.Log;

import com.inledco.exoterra.xlink.HomeExtendApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class HomeManager {
    private final String TAG = "HomeManager";

//    private final List<HomeApi.HomesResponse.Home> mHomeList;
    private final List<HomeExtendApi.HomesResponse.Home> mHomeList;
    private String mCurrentHomeId;

    private final XlinkRequestCallback<HomeExtendApi.HomesResponse> mGetHomesCallback;
    private final XlinkRequestCallback<String> mGetCurrentHomeidCallback;
    private final XlinkRequestCallback<HomeApi.HomeResponse> mCreateDefaultHomeCallback;
    private final XlinkRequestCallback<String> mSetCurrentHomeidCallback;

    private HomeManager() {
        mHomeList = new ArrayList<>();

        mGetHomesCallback = new XlinkRequestCallback<HomeExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: gethomes " + error);
            }

            @Override
            public void onSuccess(HomeExtendApi.HomesResponse response) {
                Log.e(TAG, "onSuccess: gethomes " + response.list.size());
                mHomeList.clear();
                mHomeList.addAll(response.list);

                XlinkCloudManager.getInstance().getCurrentHomeId(mGetCurrentHomeidCallback);
            }
        };

        mGetCurrentHomeidCallback = new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: gethomeid " + error);
            }

            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "onSuccess: gethomeid " + s);
                for (int i = 0; i < mHomeList.size(); i++) {
                    if (TextUtils.equals(s, mHomeList.get(i).id)) {
                        mCurrentHomeId = s;
                        return;
                    }
                }

                XlinkCloudManager.getInstance().createDefaultHome(mCreateDefaultHomeCallback);
            }
        };

        mCreateDefaultHomeCallback = new XlinkRequestCallback<HomeApi.HomeResponse>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: createhome " + error);
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
                Log.e(TAG, "onError: sethomeid " + error);
            }

            @Override
            public void onSuccess(String s) {
                mCurrentHomeId = s;

                Log.e(TAG, "onSuccess: sethomeid " + s);
            }
        };
    }

    public static HomeManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mHomeList.clear();
    }

    public String getCurrentHomeId() {
        return mCurrentHomeId;
    }

    //    public List<HomeApi.HomesResponse.Home> getHomeList() {
    public List<HomeExtendApi.HomesResponse.Home> getHomeList() {
        return mHomeList;
    }

    public void syncHomeList(final XlinkRequestCallback<List<HomeExtendApi.HomesResponse.Home>> callback) {
        //        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeApi.HomesResponse>() {
        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomeExtendApi.HomesResponse response) {
                mHomeList.clear();
                mHomeList.addAll(response.list);
                if (callback != null) {
                    callback.onSuccess(mHomeList);
                }
            }
        });
    }

    public void checkHome() {
        XlinkCloudManager.getInstance().getHomeList(mGetHomesCallback);
    }

    private static class LazyHolder {
        private static final HomeManager INSTANCE = new HomeManager();
    }
}
