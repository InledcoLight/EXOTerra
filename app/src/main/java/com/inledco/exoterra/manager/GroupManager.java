package com.inledco.exoterra.manager;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.event.GroupsRefreshedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupManager {
    private final String KEY_ZONE       = "zone";
    private final String KEY_SUNRISE    = "sunrise";
    private final String KEY_SUNSET     = "sunset";

    private List<Group> mGroups;
    private Map<String, Group> mGroupMap;

    private boolean mSynchronizing;
    private boolean mSynchronized;

    private GroupManager() {
        mGroups = new ArrayList<>();
        mGroupMap = new HashMap<>();
    }

    public static GroupManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void addGroup(Group group) {
        if (group == null) {
            return;
        }
        String key = group.groupid;
        mGroupMap.put(key, group);
//        if (mGroupMap.containsKey(key)) {
//            Group group = mGroupMap.get(key);
//            group.name = xGroup.name;
//            group.remark1 = xGroup.remark1;
//            group.remark2 = xGroup.remark2;
//            group.remark3 = xGroup.remark3;
//            group.creator = xGroup.creator;
//            group.create_time = xGroup.create_time;
//            group.update_time = xGroup.update_time;
//        } else {
//            mGroupMap.put(key, new Group(xGroup));
//        }
    }

    public void removeGroup(String key) {
        if (mGroupMap.containsKey(key)) {
            mGroupMap.remove(key);
            mGroups.clear();
            mGroups.addAll(mGroupMap.values());
        }
    }

    public void removeGroup(Group group) {
        if (group != null) {
            removeGroup(group.groupid);
        }
    }

    public void clear() {
        mGroupMap.clear();
        mGroups.clear();
        mSynchronized = false;
        mSynchronizing = false;
    }

    public boolean contains(String key) {
        if (!TextUtils.isEmpty(key)) {
            return mGroupMap.containsKey(key);
        }
        return false;
    }

    public boolean contains(Group group) {
        if (group != null) {
            return contains(group.groupid);
        }
        return false;
    }

    public Group getGroup(String key) {
        if (!contains(key)) {
            return null;
        }
        return mGroupMap.get(key);
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

    public List<Group> getAllGroups() {
        return mGroups;
    }

    private void updateGroups(List<Group> groups) {
        Set<String> oldsets = new HashSet<>(mGroupMap.keySet());
        Set<String> newsets = new HashSet<>();
        for (Group group : groups) {
            newsets.add(group.groupid);
        }
        for (String key : oldsets) {
            if (newsets.contains(key) == false) {
                mGroupMap.remove(key);
            }
        }
        for (Group group : groups) {
            addGroup(group);
        }
        mGroups.clear();
        mGroups.addAll(mGroupMap.values());
    }

    public void getGroups(final OnErrorCallback callback) {
        if (mSynchronizing) {
            return;
        }
        mSynchronizing = true;
        AliotServer.getInstance().getGroups(new HttpCallback<UserApi.GroupsResponse>() {
            @Override
            public void onError(String error) {
                mSynchronizing = false;
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.GroupsResponse result) {
                updateGroups(result.data);
                mSynchronized = true;
                mSynchronizing = false;
                EventBus.getDefault().post(new GroupsRefreshedEvent());
            }
        });
    }

    public void getGroups() {
        getGroups(null);
    }

    public Group getDeviceGroup(final String pkey, final String dname) {
        for (Group group : mGroupMap.values()) {
            if (group.devices != null) {
                for (Group.Device dev : group.devices) {
                    if (TextUtils.equals(pkey, dev.product_key) && TextUtils.equals(dname, dev.device_name)) {
                        return group;
                    }
                }
            }
        }
        return null;
    }

    private String getRemark1Text(final int zone, final int sunrise, final int sunset) {
        Map<String, Integer> map = new HashMap<>();
        map.put(KEY_ZONE, zone);
        map.put(KEY_SUNRISE, sunrise);
        map.put(KEY_SUNSET, sunset);
        return JSON.toJSONString(map);
    }

    public void setRemark1(final String groupid, final int zone, final int sunrise, final int sunset, final HttpCallback<UserApi.GroupResponse> callback) {
        if (contains(groupid) == false) {
            return;
        }
        final Group group = getGroup(groupid);
        String token = UserManager.getInstance().getToken();
        final UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.remark1 = getRemark1Text(zone, sunrise, sunset);
        AliotServer.getInstance().modifyGroupInfo(token, groupid, request, new HttpCallback<UserApi.GroupResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.GroupResponse result) {
                group.remark1 = request.remark1;
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        });
    }

    private void setRemark1(final String groupid, final String key, final int value, HttpCallback<UserApi.GroupResponse> callback) {
        if (contains(groupid) == false) {
            return;
        }
        Group group = getGroup(groupid);
        if (group == null) {
            return;
        }
        final Set<String> keys = new HashSet<>();
        keys.add(KEY_ZONE);
        keys.add(KEY_SUNRISE);
        keys.add(KEY_SUNSET);
        if (!keys.contains(key)) {
            return;
        }
        keys.remove(key);

        String remark1 = group.remark1;
        Map<String, Integer> map = new HashMap<>();
        if (!TextUtils.isEmpty(remark1)) {
            try {
                JSONObject jo = JSONObject.parseObject(remark1);
                if (jo != null) {
                    for (String str : keys) {
                        if (jo.containsKey(str)) {
                            int val = jo.getIntValue(str);
                            map.put(str, val);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        map.put(key, value);

        String token = UserManager.getInstance().getToken();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.remark1 = JSON.toJSONString(map);
        AliotServer.getInstance().modifyGroupInfo(token, groupid, request, callback);
    }

    public void setZone(final String groupid, final int zone, HttpCallback<UserApi.GroupResponse> callback) {
        setRemark1(groupid, KEY_ZONE, zone, callback);
    }

    public void setSunrise(final String groupid, final int sunrise, HttpCallback<UserApi.GroupResponse> callback) {
        setRemark1(groupid, KEY_SUNRISE, sunrise, callback);
    }

    public void setSunset(final String groupid, final int sunset, HttpCallback<UserApi.GroupResponse> callback) {
        setRemark1(groupid, KEY_SUNSET, sunset, callback);
    }

    private static class LazyHolder {
        private static final GroupManager INSTANCE = new GroupManager();
    }
}
