package com.inledco.exoterra.aliot;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.inledco.exoterra.aliot.bean.XDevice;

import java.util.ArrayList;
import java.util.List;

public class Device extends ADevice{
    private final String TAG = "XDevice";

    private final String KEY_FIRMWARE_VERSION   = "FirmwareVersion";
    private final String KEY_DEVICE_INFO        = "DeviceInfo";
    private final String KEY_ZONE               = "Zone";
    private final String KEY_DEVICETIME         = "DeviceTime";
    private final String KEY_SUNRISE            = "Sunrise";
    private final String KEY_SUNSET             = "Sunset";

    private String name;
    private String mac;
    private String remark1;
    private String remark2;
    private String remark3;
    private String role;

    private boolean isOnline;
    private long statusUpdateTime;

    public Device(XDevice xDevice) {
        if (xDevice != null) {
            setProductKey(xDevice.product_key);
            setDeviceName(xDevice.device_name);
            setName(xDevice.name);
            setMac(xDevice.mac);
            setRemark1(xDevice.remark1);
            setRemark2(xDevice.remark2);
            setRemark3(xDevice.remark3);
            setOnline(TextUtils.equals(xDevice.is_online, "online"));
            setRole(xDevice.role);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public String getRemark3() {
        return remark3;
    }

    public void setRemark3(String remark3) {
        this.remark3 = remark3;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean updateOnlineStatus(StatusReponse status) {
        if (status == null || status.getStatusUpdateTime() < statusUpdateTime) {
            return false;
        }
//        if (TextUtils.equals(productKey, status.getProductKey()) == false
//            || TextUtils.equals(deviceName, status.getDeviceName()) == false) {
//            return;
//        }
        isOnline = status.isOnline();
        statusUpdateTime = status.getStatusUpdateTime();
        return true;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getPropertyInt(String key) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof Integer) {
            return (int) obj;
        }
        return 0;
    }

    public boolean getPropertyBool(String key) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof Integer) {
            int result = (int) obj;
            return (result == 1);
        }
        return false;
    }

    public String getPropertyString(String key) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    public int[] getPropertyIntArray(String key) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            int[] result = new int[ja.size()];
            for (int i = 0; i < ja.size(); i++) {
                if (ja.get(i) instanceof Integer) {
                    result[i] = ja.getIntValue(i);
                } else {
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public <T> T getPropertyObject(String key, Class<T> clazz) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof JSONObject) {
            JSONObject jo = (JSONObject) obj;
            try {
                return jo.toJavaObject(clazz);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public <T> List<T> getPropertyArray(String key, Class<T> clazz) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            List<T> result = new ArrayList<>();
            for (int i = 0; i < ja.size(); i++) {
                if (ja.get(i) != null) {
                    result.add(ja.getObject(i, clazz));
                } else {
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public DeviceInfo getDeviceInfo() {
        return getPropertyObject(KEY_DEVICE_INFO, DeviceInfo.class);
    }

    public int getFirmwareVersion() {
        return getPropertyInt(KEY_FIRMWARE_VERSION);
    }

    public int getZone() {
        return getPropertyInt(KEY_ZONE);
    }

    public String getDeviceTime() {
        return getPropertyString(KEY_DEVICETIME);
    }

    public int getSunrise() {
        return getPropertyInt(KEY_SUNRISE);
    }

    public int getSunset() {
        return getPropertyInt(KEY_SUNSET);
    }

    public KeyValue setZone(int zone) {
        if (zone < -720 || zone > 720) {
            return null;
        }
        return new KeyValue(KEY_ZONE, zone);
    }

    public KeyValue setSunrise(int sunrise) {
        if (sunrise < 0 || sunrise > 1439) {
            return null;
        }
        return new KeyValue(KEY_SUNRISE, sunrise);
    }

    public KeyValue setSunset(int sunset) {
        if (sunset < 0 || sunset > 1439) {
            return null;
        }
        return new KeyValue(KEY_SUNSET, sunset);
    }

    protected String getProductName() {
        return null;
    }
}
