package com.inledco.exoterra.manager;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {
    private static final String TAG = "DeviceManager";
//
    private final Map<String, Device> mSubcribedDevices;
    private final List<Device> mDevices;
//
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

    public void addDevice(Device device) {
        if (device != null) {
            String key = device.getTag();
            String pkey = device.getProductKey();
            if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOLED)) {
                mSubcribedDevices.put(key, device);
            } else if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
                mSubcribedDevices.put(key, device);
            } else if (TextUtils.equals(pkey, AliotConsts.PRODUCT_KEY_EXOMONSOON)) {
                mSubcribedDevices.put(key, device);
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

    private void updateDevices(@NonNull final List<Device> devices) {
//        Set<String> oldsets = new HashSet<>(mSubcribedDevices.keySet());
//        Set<String> newsets = new HashSet<>();
//        for (Device dev : devices) {
//            newsets.add(getDeviceTag(dev));
//        }
//        for (String key : oldsets) {
//            if (newsets.contains(key) == false) {
//                mSubcribedDevices.remove(key);
//            }
//        }
//        for (Device device : devices) {
//            addDevice(device);
//        }
//        mDevices.clear();
//        mDevices.addAll(mSubcribedDevices.values());

        mDevices.clear();
        mSubcribedDevices.clear();
        for (Device device : devices) {
            addDevice(device);
        }
        mDevices.addAll(mSubcribedDevices.values());
    }

//    public void refreshDevice(@NonNull final Device device) {
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
//    public void refreshSubcribeDevices(final XlinkTaskCallback<List<Device>> listener) {
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
//    public void syncSubcribeDevices(final XlinkTaskCallback<List<Device>> listener) {
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
//                for (Device dev : mDevices) {
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
