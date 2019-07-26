package com.liruya.exoterra.manager;

import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkRequestCallback;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class HomeManager {

    private final List<HomeApi.HomesResponse.Home> mHomeList;

    private HomeManager() {
        mHomeList = new ArrayList<>();
    }

    public static HomeManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mHomeList.clear();
    }

    public List<HomeApi.HomesResponse.Home> getHomeList() {
        return mHomeList;
    }

    public void syncHomeList(final XlinkRequestCallback<List<HomeApi.HomesResponse.Home>> callback) {
        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeApi.HomesResponse>() {
            @Override
            public void onStart() {
                if (callback != null) {
                    callback.onStart();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomeApi.HomesResponse response) {
                mHomeList.clear();
                mHomeList.addAll(response.list);
                if (callback != null) {
                    callback.onSuccess(mHomeList);
                }
            }
        });
    }

    private static class LazyHolder {
        private static final HomeManager INSTANCE = new HomeManager();
    }
}
