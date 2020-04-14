package com.inledco.exoterra.manager;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.aliot.bean.XGroup;

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

    private boolean mSynchronized;

    private GroupManager() {
        mGroups = new ArrayList<>();
        mGroupMap = new HashMap<>();
    }

    public static GroupManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addGroup(XGroup xGroup) {
        if (xGroup == null) {
            return;
        }
        String key = xGroup.groupid;
        if (mGroupMap.containsKey(key)) {
            Group group = mGroupMap.get(key);
            group.name = xGroup.name;
            group.remark1 = xGroup.remark1;
            group.remark2 = xGroup.remark2;
            group.remark3 = xGroup.remark3;
            group.creator = xGroup.creator;
            group.create_time = xGroup.create_time;
            group.update_time = xGroup.update_time;
        } else {
            mGroupMap.put(key, new Group(xGroup));
        }
    }

    public void removeGroup(String key) {
        if (mGroupMap.containsKey(key)) {
            mGroupMap.remove(key);
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

    public boolean isSynchronized() {
        return mSynchronized;
    }

    public List<Group> getAllGroups() {
        return mGroups;
    }

    private void updateGroups(List<XGroup> xGroups) {
        Set<String> oldsets = new HashSet<>(mGroupMap.keySet());
        Set<String> newsets = new HashSet<>();
        for (XGroup xGroup : xGroups) {
            newsets.add(xGroup.groupid);
        }
        for (String key : oldsets) {
            if (newsets.contains(key) == false) {
                mGroupMap.remove(key);
            }
        }
        for (XGroup xGroup : xGroups) {
            addGroup(xGroup);
        }
        mGroups.clear();
        mGroups.addAll(mGroupMap.values());
    }

    public void getGroups(final HttpCallback<UserApi.GroupsResponse> callback) {
        String userid = UserManager.getInstance().getUserid();
        String token = UserManager.getInstance().getToken();
        AliotServer.getInstance().getGroups(userid, token, new HttpCallback<UserApi.GroupsResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.GroupsResponse result) {
                updateGroups(result.data);
                mSynchronized = true;
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        });
    }

    public String getRemark1Text(final int zone, final int sunrise, final int sunset) {
        Map<String, Integer> map = new HashMap<>();
        map.put(KEY_ZONE, zone);
        map.put(KEY_SUNRISE, sunrise);
        map.put(KEY_SUNSET, sunset);
        return JSON.toJSONString(map);
    }

    public void setRemark1(final String groupid, final int zone, final int sunrise, final int sunset, HttpCallback<UserApi.GroupResponse> callback) {
        if (contains(groupid) == false) {
            return;
        }
        String token = UserManager.getInstance().getToken();
        UserApi.GroupRequest request = new UserApi.GroupRequest();
        request.remark1 = getRemark1Text(zone, sunrise, sunset);
        AliotServer.getInstance().modifyGroupInfo(token, groupid, request, callback);
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
