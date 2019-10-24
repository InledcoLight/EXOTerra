package com.inledco.exoterra.manager;

import com.inledco.exoterra.xlink.HomeExtendApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.util.ArrayList;
import java.util.List;

public class HomeManager {

//    private final List<HomeApi.HomesResponse.Home> mHomeList;
    private final List<HomeExtendApi.HomesResponse.Home> mHomeList;
    private String mCurrentHomeId;

    private HomeManager() {
        mHomeList = new ArrayList<>();
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

    public void setCurrentHomeId(String currentHomeId) {
        mCurrentHomeId = currentHomeId;
    }

    //    public List<HomeApi.HomesResponse.Home> getHomeList() {
    public List<HomeExtendApi.HomesResponse.Home> getHomeList() {
        return mHomeList;
    }

    public void syncHomeList(final XlinkRequestCallback<List<HomeExtendApi.HomesResponse.Home>> callback) {
//        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeApi.HomesResponse>() {
        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeExtendApi.HomesResponse>() {
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
            public void onSuccess(HomeExtendApi.HomesResponse response) {
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
