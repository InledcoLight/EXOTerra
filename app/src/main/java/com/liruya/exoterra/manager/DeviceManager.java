package com.liruya.exoterra.manager;

import android.text.TextUtils;

import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.xlink.XlinkCloudManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.xlink.sdk.core.XLinkCoreException;
import cn.xlink.sdk.v5.listener.XLinkTaskListener;
import cn.xlink.sdk.v5.model.XDevice;

public class DeviceManager {
    private static final String TAG = "DeviceManager";

    private Map<String, Device> mSubcribedDevices;

    private DeviceManager() {
        mSubcribedDevices = new ConcurrentHashMap<>();
    }

    public static DeviceManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addDevice(XDevice xDevice) {
        if (xDevice != null) {
            String key = xDevice.getDeviceTag();
            if (!TextUtils.isEmpty(key)) {
                mSubcribedDevices.put(key, new Device(xDevice));
            }
        }
    }

    public void addDevice(Device device) {
        if (device != null) {
            addDevice(device.getXDevice());
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
            String key = xDevice.getDeviceTag();
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
    }

    public boolean contains(String key) {
        if (!TextUtils.isEmpty(key)) {
            return mSubcribedDevices.containsKey(key);
        }
        return false;
    }

    public boolean contains(XDevice xDevice) {
        if (xDevice != null) {
            String key = xDevice.getDeviceTag();
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
            String key = xDevice.getDeviceTag();
            return getDevice(key);
        }
        return null;
    }

    public List<Device> getAllDevices() {
        return new ArrayList<>(mSubcribedDevices.values());
    }

    public Set<String> getAllDeviceAddress() {
        return new HashSet<>(mSubcribedDevices.keySet());
    }

    public void updateDevice(XDevice xDevice) {
        Device device = getDevice(xDevice);
        if (device == null) {
            addDevice(xDevice);
        } else {
            device.setXDevice(xDevice);
        }
    }

    public void refreshSubcribeDevices(final XLinkTaskListener<List<Device>> listener) {
        XlinkCloudManager.getInstance().syncSubscribedDevices(new XLinkTaskListener<List<XDevice>>() {
            @Override
            public void onError(XLinkCoreException e) {
                if (listener != null) {
                    listener.onError(e);
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
                if (xDevices == null || xDevices.size() == 0) {
                    mSubcribedDevices.clear();
                }
                else {
                    mSubcribedDevices.clear();
                    Set<String> keys = new HashSet<>();
                    for (XDevice xDevice : xDevices) {
                        if (xDevice != null) {
                            keys.add(xDevice.getDeviceTag());
                            updateDevice(xDevice);
                        }
                    }
                    for (String key : getAllDeviceAddress()) {
                        if (!keys.contains(key)) {
                            removeDevice(key);
                        }
                    }
                }

                if (listener != null) {
                    listener.onComplete(getAllDevices());
                }
            }
        });
    }

    private static class LazyHolder {
        private static final DeviceManager INSTANCE = new DeviceManager();
    }
}
