package com.inledco.exoterra.device;

import com.inledco.exoterra.base.BaseViewModel;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;
import com.inledco.exoterra.xlink.XlinkTaskCallback;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.model.XDevice;

public class DeviceViewModel<T extends Device> extends BaseViewModel<T> {
    private String mRoomId;

    private XlinkTaskCallback<XDevice> mSetCallback;
    private XlinkTaskCallback<List<XLinkDataPoint>> mGetCallback;

    public void setSetCallback(XlinkTaskCallback<XDevice> callback) {
        mSetCallback = callback;
    }

    public void setGetCallback(XlinkTaskCallback<List<XLinkDataPoint>> callback) {
        mGetCallback = callback;
    }

    protected void setDeviceDatapoint(XLinkDataPoint dp) {
        setDeviceDatapoint(dp, mSetCallback);
    }

    protected void setDeviceDatapoint(XLinkDataPoint dp, final XlinkTaskCallback<XDevice> callback) {
        if (dp != null) {
            List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp);
            XlinkCloudManager.getInstance().setDeviceDatapoints(getData().getXDevice(), dps, callback);
        }
    }

    protected void setDeviceDatapoints(List<XLinkDataPoint> dps) {
        setDeviceDatapoints(dps, mSetCallback);
    }

    protected void setDeviceDatapoints(List<XLinkDataPoint> dps, final XlinkTaskCallback<XDevice> callback) {
        if (dps != null) {
            XlinkCloudManager.getInstance().setDeviceDatapoints(getData().getXDevice(), dps, callback);
        }
    }

    protected void setApplicationSetDatapoints(List<XLinkDataPoint> dps) {
        XlinkCloudManager.getInstance().setApplicationSetDatapoints(getData().getXDevice(), dps, new XlinkRequestCallback<List<XLinkDataPoint>>() {
            @Override
            public void onStart() {
                if (mSetCallback != null) {
                    mSetCallback.onStart();
                }
            }

            @Override
            public void onError(String error) {
                if (mSetCallback != null) {
                    mSetCallback.onError(error);
                }
            }

            @Override
            public void onSuccess(List<XLinkDataPoint> dataPoints) {
                if (mSetCallback != null) {
                    mSetCallback.onComplete(getData().getXDevice());
                }
                if (mGetCallback != null) {
                    mGetCallback.onComplete(dataPoints);
                }
            }
        });
    }

    protected void setApplicationSetDatapoint(XLinkDataPoint dp) {
        if (dp != null && dp.getSource() == XLinkRestfulEnum.DataPointSource.APPLICATION_SET.getValue()) {
            final List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp);
            setApplicationSetDatapoints(dps);
        }
    }

    public void setZone(short zone) {
        setDeviceDatapoint(getData().setZone(zone));
    }

    public void setLongitude(float longitude) {
        setDeviceDatapoint(getData().setLongitude(longitude));
    }

    public void setLatitude(float latitude) {
        setDeviceDatapoint(getData().setLatitude(latitude));
    }

    public void probeDeviceDatetime(XlinkTaskCallback<List<XLinkDataPoint>> callback) {
        List<Integer> ids = new ArrayList<>();
        ids.add(getData().getDeviceDatetimeIndex());
        XlinkCloudManager.getInstance().probeDevice(getData().getXDevice(), ids, callback);
    }

    public void syncDeviceDatetime() {
        setDeviceDatapoint(getData().setSyncDatetime());
    }

    public void getDatapoints() {
        XlinkCloudManager.getInstance().getDeviceDatapoints(getData().getXDevice(), mGetCallback);
    }

    public void getDatapoints(XlinkTaskCallback<List<XLinkDataPoint>> callback) {
        XlinkCloudManager.getInstance().getDeviceDatapoints(getData().getXDevice(), callback);
    }
}