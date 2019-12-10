package com.inledco.exoterra.manager;

import android.os.AsyncTask;

import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.bean.HomeProperty;
import com.inledco.exoterra.event.HomeDeviceChangedEvent;
import com.inledco.exoterra.event.HomePropertyChangedEvent;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.XlinkResult;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.api.app.HomeApi;

public class HomeManager {
    private final String TAG = "HomeManager";

    private final List<Home> mHomeList;

    private AsyncTask<Void, Void, Void> mAsyncTask;

    private HomeManager() {
        mHomeList = new ArrayList<>();
    }

    public static HomeManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mHomeList.clear();
    }

    public List<Home> getHomeList() {
        return mHomeList;
    }

    public void refreshHomeList(final XlinkRequestCallback<List<Home>> callback) {
        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomeApi.HomesResponse response) {
                mHomeList.clear();
                for (HomeApi.HomesResponse.Home home : response.list) {
                    mHomeList.add(new Home(home));
                }
                if (callback != null) {
                    callback.onSuccess(mHomeList);
                }
                getAllHomePropertiesAndDevices();
            }
        });
    }

    public void getAllHomePropertiesAndDevices() {
        if (mHomeList.size() == 0) {
            return;
        }
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (Home home : mHomeList) {
                    String homeid = home.getHome().id;
                    XlinkResult<HomeProperty> result1 = XlinkCloudManager.getInstance().getHomeProperty(homeid);
                    if (result1.isSuccess()) {
                        home.setProperty(result1.getResult());
                        EventBus.getDefault().post(new HomePropertyChangedEvent(homeid));
                    }
                    XlinkResult<HomeApi.HomeDevicesResponse> result2 = XlinkCloudManager.getInstance().getHomeDeviceList(homeid);
                    if (result2.isSuccess()) {
                        home.setDevices(result2.getResult().list);
                        EventBus.getDefault().post(new HomeDeviceChangedEvent(homeid));
                    }
                }
                return null;
            }
        };
        mAsyncTask.execute();
    }

    private static class LazyHolder {
        private static final HomeManager INSTANCE = new HomeManager();
    }
}
