package com.inledco.exoterra.manager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.bean.EXOMonsoon;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.event.DatapointChangedEvent;
import com.inledco.exoterra.event.DevicesRefreshedEvent;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkConstants;
import com.inledco.exoterra.xlink.XlinkTaskCallback;
import com.inledco.exoterra.xlink.XlinkTaskHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.model.XDevice;

public class DeviceManager {
    private static final String TAG = "DeviceManager";

    private final Map<String, Device> mSubcribedDevices;
    private final List<Device> mDevices;

    private boolean mSyncing;
    private AsyncTask<Void, Void, Void> mAsyncTask;

    private DeviceManager() {
        mSubcribedDevices = new ConcurrentHashMap<>();
        mDevices = new ArrayList<>();
    }

    public static DeviceManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getDeviceTag(@NonNull XDevice xDevice) {
        return xDevice.getProductId() + "_" +xDevice.getMacAddress();
    }

    private void addDevice(XDevice xDevice) {
        if (xDevice != null) {
            String key = getDeviceTag(xDevice);
            String pid = xDevice.getProductId();
            if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_LEDSTRIP)) {
                mSubcribedDevices.put(key, new EXOLedstrip(xDevice));
            } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_SOCKET)) {
                mSubcribedDevices.put(key, new EXOSocket(xDevice));
            } else if (TextUtils.equals(pid, XlinkConstants.PRODUCT_ID_MONSOON)) {
                mSubcribedDevices.put(pid, new EXOMonsoon(xDevice));
            }
        }
    }

    public void removeDevice(String key) {
        if (!TextUtils.isEmpty(key)) {
            if (mSubcribedDevices.containsKey(key)) {
                mSubcribedDevices.remove(key);
            }
        }
    }

    public void removeDevice(XDevice xDevice) {
        if (xDevice != null) {
            String key = getDeviceTag(xDevice);
            removeDevice(key);
        }
    }

    public void removeDevice(Device device) {
        if (device != null) {
            removeDevice(device.getXDevice());
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

    public boolean contains(XDevice xDevice) {
        if (xDevice != null) {
            String key = getDeviceTag(xDevice);
            return contains(key);
        }
        return false;
    }

    public boolean contains(Device device) {
        if (device != null) {
            return contains(device.getXDevice());
        }
        return false;
    }

    public Device getDevice(String key) {
        if (!contains(key)) {
            return null;
        }
        return mSubcribedDevices.get(key);
    }

    public Device getDevice(XDevice xDevice) {
        if (xDevice != null) {
            String key = getDeviceTag(xDevice);
            return getDevice(key);
        }
        return null;
    }

    public Device getDeviceBySN(@NonNull final String sn) {
        for (Device device : mSubcribedDevices.values()) {
            if (sn.equals(device.getXDevice().getSN())) {
                return device;
            }
        }
        return null;
    }

    public Device getDeviceByDevid(final int devid) {
        for (Device device : mSubcribedDevices.values()) {
            if (device.getXDevice().getDeviceId() == devid) {
                return device;
            }
        }
        return null;
    }

    public List<Device> getAllDevices() {
//        List<Device> list = new ArrayList<>(mSubcribedDevices.values());
//        Collections.sort(list, new Comparator<Device>() {
//            @Override
//            public int compare(Device o1, Device o2) {
//                if (o1.getXDevice().isOnline() && !o2.getXDevice().isOnline()) {
//                    return -1;
//                }
//                if (!o1.getXDevice().isOnline() && o2.getXDevice().isOnline()) {
//                    return 1;
//                }
//                return 0;
//            }
//        });
//        return list;
        return mDevices;
    }

    public Set<String> getAllDeviceAddress() {
        return new HashSet<>(mSubcribedDevices.keySet());
    }

    public void updateDevice(@NonNull final XDevice xDevice) {
        Device device = getDevice(xDevice);
        if (device == null) {
            addDevice(xDevice);
        } else {
            device.setXDevice(xDevice);
        }
    }

    private void updateDevices(@NonNull final List<XDevice> xDevices) {
        Set<String> oldsets = new HashSet<>(mSubcribedDevices.keySet());
        Set<String> newsets = new HashSet<>();
        for (XDevice xdev : xDevices) {
            newsets.add(xdev.getProductId() + "_" + xdev.getMacAddress());
        }
        for (String key : oldsets) {
            if (newsets.contains(key) == false) {
                mSubcribedDevices.remove(key);
            }
        }
        for (XDevice xdev : xDevices) {
            updateDevice(xdev);
        }
        mDevices.clear();
        mDevices.addAll(mSubcribedDevices.values());
    }

    public void refreshDevice(@NonNull final Device device) {
        if (contains(device) && device.getXDevice() != null && device.getXDevice().isOnline()) {
            XlinkCloudManager.getInstance().getDeviceDatapoints(device.getXDevice(), new XlinkTaskCallback<List<XLinkDataPoint>>() {
                @Override
                public void onError(String error) {

                }

                @Override
                public void onComplete(List<XLinkDataPoint> dataPoints) {
                    Collections.sort(dataPoints, new Comparator<XLinkDataPoint>() {
                        @Override
                        public int compare(XLinkDataPoint o1, XLinkDataPoint o2) {
                            return o1.getIndex() - o2.getIndex();
                        }
                    });
                    device.setDataPointList(dataPoints);
                    EventBus.getDefault().post(new DatapointChangedEvent(device.getDeviceTag()));
                }
            });
        }
    }

    public void refreshSubcribeDevices(final XlinkTaskCallback<List<Device>> listener) {
        XlinkCloudManager.getInstance().syncSubscribedDevices(new XlinkTaskCallback<List<XDevice>>() {
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError(error);
                }
            }

            @Override
            public void onStart() {
                if (listener != null) {
                    listener.onStart();
                }
            }

            @Override
            public void onComplete(List<XDevice> xDevices) {
                mSubcribedDevices.clear();
                mDevices.clear();
                if (xDevices != null) {
                    for (XDevice xDevice : xDevices) {
                        if (xDevice != null) {
                            updateDevice(xDevice);
                        }
                    }
                }
                mDevices.addAll(mSubcribedDevices.values());
                if (listener != null) {
                    listener.onComplete(mDevices);
                }
            }
        });
    }

    public void syncSubcribeDevices(final XlinkTaskCallback<List<Device>> listener) {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        XlinkCloudManager.getInstance().syncSubscribedDevices(new XlinkTaskCallback<List<XDevice>>() {
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError(error);
                }
            }

            @Override
            public void onStart() {
                if (listener != null) {
                    listener.onStart();
                }
            }

            @Override
            public void onComplete(List<XDevice> xDevices) {
                updateDevices(xDevices);
                EventBus.getDefault().post(new DevicesRefreshedEvent());
                if (listener != null) {
                    listener.onComplete(mDevices);
                }
                getAllDeviceDatapoints();

//                mSubcribedDevices.clear();
//                mDevices.clear();
//                if (xDevices != null) {
//                    for (final XDevice xDevice : xDevices) {
//                        if (xDevice != null) {
//                            updateDevice(xDevice);
//                        }
//                    }
//                }
//                mDevices.addAll(mSubcribedDevices.values());
//                if (listener != null) {
//                    listener.onComplete(mDevices);
//                }
//
//                getAllDeviceDatapoints();
            }
        });
    }

    public void getAllDeviceDatapoints() {
        if (mSubcribedDevices.size() == 0) {
            return;
        }
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (Device dev : mDevices) {
                    if (dev.getXDevice().isOnline() && !dev.isSynchronized()) {
                        XlinkTaskHandler<List<XLinkDataPoint>> callback = new XlinkTaskHandler<>();
                        XlinkCloudManager.getInstance().getDeviceDatapoints(dev.getXDevice(), callback);
                        while (!callback.isOver());
                        if (callback.isSuccess()) {
                            dev.setDataPointList(callback.getResult());
                            EventBus.getDefault().post(new DatapointChangedEvent(dev.getDeviceTag()));
                        }
                    }
                }
                return null;
            }
        };
        mAsyncTask.execute();
    }

    private static class LazyHolder {
        private static final DeviceManager INSTANCE = new DeviceManager();
    }
}
