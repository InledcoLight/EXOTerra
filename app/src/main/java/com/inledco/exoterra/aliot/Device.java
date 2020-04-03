package com.inledco.exoterra.aliot;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class Device extends ADevice{
    private final String TAG = "Device";

    private final String KEY_ZONE = "Zone";
    private final String KEY_DEVICETIME = "DeviceTime";
    private final String KEY_SUNRISE = "Sunrise";
    private final String KEY_SUNSET = "Sunset";

    private String name;
    private String mac;
    private String remark1;
    private String remark2;
    private String remark3;

    private int firmwareVersion;
    private boolean isOnline;
    private long statusUpdateTime;

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

    public void updateOnlineStatus(StatusReponse status) {
        if (status == null || status.getStatusUpdateTime() < statusUpdateTime) {
            return;
        }
        isOnline = status.isOnline();
    }

    public boolean isOnline() {
        return isOnline;
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
        if (obj != null && obj.getClass().equals(clazz)) {
            return (T) obj;
        }
        return null;
    }

    public <T> List<T> getPropertyArray(String key, Class<T> clazz) {
        Object obj = getPropertyValue(key);
        if (obj != null && obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            List<T> result = new ArrayList<>();
            for (int i = 0; i < ja.size(); i++) {
                if (ja.get(i) != null && ja.get(i).getClass().equals(clazz)) {
                    result.add(ja.getObject(i, clazz));
                } else {
                    return null;
                }
            }
            return result;
        }
        return null;
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

//    public class KeyValue {
//        private final String attrKey;
//        private Object attrValue;
//
//        public KeyValue(String attrKey, Object attrValue) {
//            this.attrKey = attrKey;
//            this.attrValue = attrValue;
//        }
//
//        public String getAttrKey() {
//            return attrKey;
//        }
//
//        public Object getAttrValue() {
//            return attrValue;
//        }
//    }
}
