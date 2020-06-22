package com.inledco.exoterra.manager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.inledco.exoterra.aliot.ADevice;
import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoLed;
import com.inledco.exoterra.aliot.ExoMonsoon;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.XDevice;
import com.inledco.exoterra.event.DevicesRefreshedEvent;

import org.greenrobot.eventbus.EventBus;

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

    private boolean mSynchronizing;
    private boolean mSynchronized;

    private AsyncTask<Void, Void, Void> mAsyncTask;

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
                device.setOnline(TextUtils.equals(xdevice.is_online, "online"));
                device.setRole(xdevice.role);
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
        mSynchronized = false;
        mSynchronizing = false;
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

    public boolean isSynchronizing() {
        return mSynchronizing;
    }

    public boolean isSynchronized() {
        return mSynchronized;
    }

    public boolean needSynchronize() {
        return !mSynchronized && !mSynchronizing;
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
    }

    public void getSubscribedDevices(final OnErrorCallback callback) {
        if (mSynchronizing) {
            return;
        }
        mSynchronizing = true;
        AliotServer.getInstance().getSubscribeDevices(new HttpCallback<UserApi.UserSubscribedDevicesResponse>() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: " + error);
                mSynchronizing = false;
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.UserSubscribedDevicesResponse result) {
                updateDevices(result.data);
                mSynchronized = true;
                mSynchronizing = false;
                EventBus.getDefault().post(new DevicesRefreshedEvent());

                getExoSocketSensors();
            }
        });
    }

    public void getSubscribedDevices() {
        getSubscribedDevices(null);
    }

    public void getExoSocketSensors() {
        final Set<Device> devices = new HashSet<>();
        for (Device device : mDevices) {
            if (TextUtils.equals(device.getProductKey(), AliotConsts.PRODUCT_KEY_EXOSOCKET)
                && device.isOnline()) {
                devices.add(device);
            }
        }
        if (devices.size() == 0) {
            return;
        }
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
//                HttpCallback<UserApi.PublishTopicResponse> callback = new HttpCallback<UserApi.PublishTopicResponse>() {
//                    @Override
//                    public void onError(String error) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(UserApi.PublishTopicResponse result) {
//
//                    }
//                };
                HttpCallback<UserApi.GetDevicePropertiesResponse> callback = new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
                    @Override
                    public void onError(String error) {

                    }

                    @Override
                    public void onSuccess(UserApi.GetDevicePropertiesResponse result) {

                    }
                };
                for (final Device device : devices) {
                    AliotServer.getInstance().getDeviceProperties(device.getProductKey(),
                                                                  device.getDeviceName(),
                                                                  new HttpCallback<UserApi.GetDevicePropertiesResponse>() {
                                                                      @Override
                                                                      public void onError(String error) {

                                                                      }

                                                                      @Override
                                                                      public void onSuccess(UserApi.GetDevicePropertiesResponse result) {
                                                                          device.updateProperties(result.data);
                                                                          ADevice adev = new ADevice(device.getProductKey(), device.getDeviceName());
                                                                          EventBus.getDefault().post(adev);
                                                                      }
                                                                  });
//                    AliotServer.getInstance()
//                               .getDeviceProperties(AliotConsts.PRODUCT_KEY_EXOSOCKET,
//                                                    dname,
//                                                    callback,
//                                                    "SensorAvailable", "Sensor");

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
