package com.liruya.exoterra.manager;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.liruya.exoterra.bean.Device;
import com.liruya.exoterra.xlink.XlinkCloudManager;
import com.liruya.exoterra.xlink.XlinkTaskCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.xlink.sdk.v5.model.XDevice;

public class DeviceManager {
    private static final String TAG = "DeviceManager";

    private final Map<String, Device> mSubcribedDevices;

    private DeviceManager() {
        mSubcribedDevices = new ConcurrentHashMap<>();
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
            if (!TextUtils.isEmpty(key)) {
                mSubcribedDevices.put(key, new Device(xDevice));
            }
        }
    }

    private void addDevice(Device device) {
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
                if (xDevices != null) {
                    for (XDevice xDevice : xDevices) {
                        if (xDevice != null) {
                            updateDevice(xDevice);
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
