package com.inledco.exoterra.main;

import android.text.TextUtils;

import com.inledco.exoterra.base.BaseViewModel;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.bean.XHome;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.xlink.HomesExtendApi;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import java.util.Collections;
import java.util.Comparator;

import cn.xlink.restful.api.app.HomeApi;

public class HomeViewModel extends BaseViewModel<XHome> {
    private XlinkRequestCallback<String> mGetHomeInfoCallback;

    public void setGetHomeInfoCallback(XlinkRequestCallback<String> callback) {
        mGetHomeInfoCallback = callback;
    }

    public void refreshHomeInfo() {
        final String homeid = Home2Manager.getInstance().getCurrentHomeId();
        if (TextUtils.isEmpty(homeid)) {
            if (mGetHomeInfoCallback != null) {
                mGetHomeInfoCallback.onError("No Home2 Exists.");
            }
            return;
        }
        XlinkCloudManager.getInstance().getHomes(new XlinkRequestCallback<HomesExtendApi.HomesResponse>() {
            @Override
            public void onError(String error) {
                postValue();
                if (mGetHomeInfoCallback != null) {
                    mGetHomeInfoCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(HomesExtendApi.HomesResponse response) {
                for (final Home2 home2 : response.list) {
                    if (TextUtils.equals(homeid, home2.id)) {
                        XlinkCloudManager.getInstance().getHomeDeviceList(homeid, new XlinkRequestCallback<HomeApi.HomeDevicesResponse>() {
                            @Override
                            public void onError(String error) {
                                postValue();
                                if (mGetHomeInfoCallback != null) {
                                    mGetHomeInfoCallback.onError(error);
                                }
                            }

                            @Override
                            public void onSuccess(HomeApi.HomeDevicesResponse response) {
                                Collections.sort(response.list, new Comparator<HomeApi.HomeDevicesResponse.Device>() {
                                    @Override
                                    public int compare(HomeApi.HomeDevicesResponse.Device o1, HomeApi.HomeDevicesResponse.Device o2) {
                                        if (o1.isOnline && !o2.isOnline) {
                                            return -1;
                                        }
                                        if (!o1.isOnline && o2.isOnline) {
                                            return 1;
                                        }
                                        return 0;
                                    }
                                });
                                XHome xHome = new XHome(home2);
                                xHome.setDevices(response.list);
                                setData(xHome);
                                postValue();

                                if (mGetHomeInfoCallback != null) {
                                    mGetHomeInfoCallback.onSuccess(null);
                                }
                            }
                        });
                        return;
                    }
                }
                if (mGetHomeInfoCallback != null) {
                    postValue();
                    mGetHomeInfoCallback.onError("No Home2 Exists.");
                }
            }
        });
    }
}
