package com.inledco.exoterra.aliot.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private final String KEY_ZONE       = "zone";
    private final String KEY_SUNRISE    = "sunrise";
    private final String KEY_SUNSET     = "sunset";

    public String groupid;
    public String name;
    public String remark1;
    public String remark2;
    public String remark3;
    public String creator;
    public boolean isDefault;
    public long create_time;
    public long update_time;
    public List<User> users;
    public List<Device> devices;

    public int getDeviceCount() {
        return devices == null ? 0 : devices.size();
    }

    public static class User {
        public String userid;
        public String email;
        public String nickname;
        public String avatar;
        public String remark1;
        public String remark2;
        public String remark3;
        public String role;
    }

    public static class Device {
        public String product_key;
        public String device_name;
        public String name;
    }

//    public Group(XGroup xGroup) {
//        if (xGroup != null) {
//            groupid = xGroup.groupid;
//            name = xGroup.name;
//            remark1 = xGroup.remark1;
//            remark2 = xGroup.remark2;
//            remark3 = xGroup.remark3;
//            creator = xGroup.creator;
//            create_time = xGroup.create_time;
//            update_time = xGroup.update_time;
//        }
//    }

    public void addDevice(String pkey, String dname, String name) {
        if (devices == null) {
            devices = new ArrayList<>();
        }
        Device device = new Device();
        device.product_key = pkey;
        device.device_name = dname;
        device.name = name;
        devices.add(device);
    }

    public void removeDevice(String pkey, String dname) {
        if (devices == null) {
            return;
        }
        for (int i = 0; i < devices.size(); i++) {
            if (TextUtils.equals(devices.get(i).product_key, pkey)
                && TextUtils.equals(devices.get(i).device_name, dname)) {
                devices.remove(i);
                return;
            }
        }
    }

    public void removeDevice(Device device) {
        if (devices == null || device == null) {
            return;
        }
        for (int i = 0; i < devices.size(); i++) {
            if (TextUtils.equals(devices.get(i).product_key, device.product_key)
                && TextUtils.equals(devices.get(i).device_name, device.device_name)) {
                devices.remove(i);
                return;
            }
        }
    }

    public void removeUser(String userid) {
        if (users != null && userid != null) {
            for (int i = 0; i < users.size(); i++) {
                if (TextUtils.equals(userid, users.get(i).userid)) {
                    users.remove(i);
                    return;
                }
            }
        }
    }

    public int getZone() {
        try {
            JSONObject jo = JSONObject.parseObject(remark1);
            if (jo !=null && jo.containsKey(KEY_ZONE)) {
                return jo.getIntValue(KEY_ZONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSunrise() {
        try {
            JSONObject jo = JSONObject.parseObject(remark1);
            if (jo !=null && jo.containsKey(KEY_SUNRISE)) {
                return jo.getIntValue(KEY_SUNRISE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 360;
    }

    public int getSunset() {
        try {
            JSONObject jo = JSONObject.parseObject(remark1);
            if (jo !=null && jo.containsKey(KEY_SUNSET)) {
                return jo.getIntValue(KEY_SUNSET);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 1080;
    }

//    public String setRemark1(final int zone, final int sunrise, final int sunset) {
//        Map<String, Integer> map = new HashMap<>();
//        map.put(KEY_ZONE, zone);
//        map.put(KEY_SUNRISE, sunrise);
//        map.put(KEY_SUNSET, sunset);
//        return JSON.toJSONString(map);
//    }
//
//    public String setRemark1(final String key, final int value) {
//        final Set<String> keys = new HashSet<>();
//        keys.add(KEY_ZONE);
//        keys.add(KEY_SUNRISE);
//        keys.add(KEY_SUNSET);
//        if (!keys.contains(key)) {
//            return null;
//        }
//        keys.remove(key);
//
//        Map<String, Integer> map = new HashMap<>();
//        if (!TextUtils.isEmpty(remark1)) {
//            try {
//                JSONObject jo = JSONObject.parseObject(remark1);
//                if (jo != null) {
//                    for (String str : keys) {
//                        if (jo.containsKey(str)) {
//                            int val = jo.getIntValue(str);
//                            map.put(str, val);
//                        }
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        map.put(key, value);
//        return JSON.toJSONString(map);
//    }
//
//    public String setZone(int zone) {
//        return setRemark1(KEY_ZONE, zone);
//    }
//
//    public String setSunrise(int sunrise) {
//        return setRemark1(KEY_SUNRISE, sunrise);
//    }
//
//    public String setSunset(int sunset) {
//        return setRemark1(KEY_SUNSET, sunset);
//    }
}
