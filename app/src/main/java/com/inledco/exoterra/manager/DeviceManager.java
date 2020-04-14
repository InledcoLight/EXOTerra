package com.inledco.exoterra.manager;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.XDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {
    private static final String TAG = "DeviceManager";
//
    private final Map<String, Device> mSubcribedDevices;
    private final List<Device> mDevices;

    private boolean mSynchronized;
//    private boolean mSyncing;
//    private AsyncTask<Void, Void, Void> mAsyncTask;
//
    private DeviceManager() {
        mSubcribedDevices = new ConcurrentHashMap<>();
        mDevices = new ArrayList<>();
    }

    public static DeviceManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private String getXDeviceTag(XDevice xDevice) {
        if (xDevice != null) {
            return xDevice.product_key + "_" + xDevice.device_name;
        }
        return null;
    }

    public void addDevice(XDevice xdevice) {
        if (xdevice != null) {
            String key = getXDeviceTag(xdevice);
            if (mSubcribedDevices.containsKey(key)) {
                Device device = mSubcribedDevices.get(key);
                device.setName(xdevice.name);
                device.setMac(xdevice.mac);
                device.setRemark1(xdevice.remark1);
                device.setRemark2(xdevice.remark2);
                device.setRemark3(xdevice.remark3);
            } else {
                if (TextUtils.equals(xdevice.product_key, AliotConsts.PRODUCT_KEY_EXOLED)) {
                    mSubcribedDevices.put(key, new ExoLed(xdevice));
                } else if (TextUtils.equals(xdevice.product_key, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
                    mSubcribedDevices.put(key, new ExoSocket(xdevice));
                } else if (TextUtils.equals(xdevice.product_key, AliotConsts.PRODUCT_KEY_EXOMONSOON)) {
                    mSubcribedDevices.put(key, new ExoMonsoon(xdevice));
                }
            }
        }
    }

    public void removeDevice(String key) {
        if (!TextUtils.isEmpty(key) && mSubcribedDevices.containsKey(key)) {
            mSubcribedDevices.remove(key);
        }
    }

    public void removeDevice(Device device) {
        if (device != null) {
            removeDevice(device.getTag());
        }
    }

    public void clear() {
        mSubcribedDevices.clear();
        mDevices.clear();
    }

    public boolean contains(String key) {
        if (!TextUtils.isEmpty(key)) {
            return mSubcribedDevices.containsKey(key);
        }
        return false;
    }

    public boolean contains(Device device) {
        if (device != null) {
            return contains(device.getTag());
        }
        return false;
    }

    public Device getDevice(String key) {
        if (!contains(key)) {
            return null;
        }
        return mSubcribedDevices.get(key);
    }

    public List<Device> getAllDevices() {
        List<Device> list = new ArrayList<>(mSubcribedDevices.values());
        Collections.sort(list, new Comparator<Device>() {
            @Override
            public int compare(Device o1, Device o2) {
                if (o1.isOnline() && !o2.isOnline()) {
                    return -1;
                }
                if (!o1.isOnline() && o2.isOnline()) {
                    return 1;
                }
                return 0;
            }
        });
        return list;
    }

    public boolean isSynchronized() {
        return mSynchronized;
    }

    private void updateDevices(@NonNull final List<XDevice> xdevices) {
        Set<String> oldsets = new HashSet<>(mSubcribedDevices.keySet());
        Set<String> newsets = new HashSet<>();
        for (XDevice dev : xdevices) {
            newsets.add(getXDeviceTag(dev));
        }
        for (String key : oldsets) {
            if (newsets.contains(key) == false) {
                mSubcribedDevices.remove(key);
            }
        }
        for (XDevice xdevice : xdevices) {
            addDevice(xdevice);
        }
        mDevices.clear();
        mDevices.addAll(mSubcribedDevices.values());

//        mDevices.clear();
//        mSubcribedDevices.clear();
//        for (Device device : xdevices) {
//            addDevice(device);
//        }
//        mDevices.addAll(mSubcribedDevices.values());
    }

    public void getSubscribedDevices(final HttpCallback<UserApi.UserSubscribedDevicesResponse> callback) {
        final String userid = UserManager.getInstance().getUserid();
        final String token = UserManager.getInstance().getToken();
        AliotServer.getInstance().getSubscribeDevices(userid, token, new HttpCallback<UserApi.UserSubscribedDevicesResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.UserSubscribedDevicesResponse result) {
                updateDevices(result.data);
                mSynchronized = true;
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        });
    }

//    public void refreshDevice(@NonNull final XDevice device) {
//        if (contains(device) && device.getXDevice() != null && device.getXDevice().isOnline()) {
//            XlinkCloudManager.getInstance().getDeviceDatapoints(device.getXDevice(), new XlinkTaskCallback<List<XLinkDataPoint>>() {
//                @Override
//                public void onError(String error) {
//
//                }
//
//                @Override
//                public void onComplete(List<XLinkDataPoint> dataPoints) {
//                    Collections.sort(dataPoints, new Comparator<XLinkDataPoint>() {
//                        @Override
//                        public int compare(XLinkDataPoint o1, XLinkDataPoint o2) {
//                            return o1.getIndex() - o2.getIndex();
//                        }
//                    });
//                    device.setDataPointList(dataPoints);
//                    EventBus.getDefault().post(new DatapointChangedEvent(device.getDeviceTag()));
//                }
//            });
//        }
//    }
//
//    public void refreshSubcribeDevices(final XlinkTaskCallback<List<XDevice>> listener) {
//        XlinkCloudManager.getInstance().syncSubscribedDevices(new XlinkTaskCallback<List<XDevice>>() {
//            @Override
//            public void onError(String error) {
//                if (listener != null) {
//                    listener.onError(error);
//                }
//            }
//
//            @Override
//            public void onStart() {
//                if (listener != null) {
//                    listener.onStart();
//                }
//            }
//
//            @Override
//            public void onComplete(List<XDevice> xDevices) {
//                mSubcribedDevices.clear();
//                mDevices.clear();
//                if (xDevices != null) {
//                    for (XDevice xDevice : xDevices) {
//                        if (xDevice != null) {
//                            updateDevice(xDevice);
//                        }
//                    }
//                }
//                mDevices.addAll(mSubcribedDevices.values());
//                if (listener != null) {
//                    listener.onComplete(mDevices);
//                }
//            }
//        });
//    }
//
//    public void syncSubcribeDevices(final XlinkTaskCallback<List<XDevice>> listener) {
//        if (mAsyncTask != null) {
//            mAsyncTask.cancel(true);
//        }
//        XlinkCloudManager.getInstance().syncSubscribedDevices(new XlinkTaskCallback<List<XDevice>>() {
//            @Override
//            public void onError(String error) {
//                if (listener != null) {
//                    listener.onError(error);
//                }
//            }
//
//            @Override
//            public void onStart() {
//                if (listener != null) {
//                    listener.onStart();
//                }
//            }
//
//            @Override
//            public void onComplete(List<XDevice> xDevices) {
//                updateDevices(xDevices);
//                EventBus.getDefault().post(new DevicesRefreshedEvent());
//                if (listener != null) {
//                    listener.onComplete(mDevices);
//                }
//                getAllDeviceDatapoints();
//
////                mSubcribedDevices.clear();
////                mDevices.clear();
////                if (xDevices != null) {
////                    for (final XDevice xDevice : xDevices) {
////                        if (xDevice != null) {
////                            updateDevice(xDevice);
////                        }
////                    }
////                }
////                mDevices.addAll(mSubcribedDevices.values());
////                if (listener != null) {
////                    listener.onComplete(mDevices);
////                }
////
////                getAllDeviceDatapoints();
//            }
//        });
//    }
//
//    public void getAllDeviceDatapoints() {
//        if (mSubcribedDevices.size() == 0) {
//            return;
//        }
//        if (mAsyncTask != null) {
//            mAsyncTask.cancel(true);
//        }
//        mAsyncTask = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                for (XDevice dev : mDevices) {
//                    if (dev.getXDevice().isOnline() && !dev.isSynchronized()) {
//                        XlinkTaskHandler<List<XLinkDataPoint>> callback = new XlinkTaskHandler<>();
//                        XlinkCloudManager.getInstance().getDeviceDatapoints(dev.getXDevice(), callback);
//                        while (!callback.isOver());
//                        if (callback.isSuccess()) {
//                            dev.setDataPointList(callback.getResult());
//                            EventBus.getDefault().post(new DatapointChangedEvent(dev.getDeviceTag()));
//                        }
//                    }
//                }
//                return null;
//            }
//        };
//        mAsyncTask.execute();
//    }

    private static class LazyHolder {
        private static final DeviceManager INSTANCE = new DeviceManager();
    }
}
