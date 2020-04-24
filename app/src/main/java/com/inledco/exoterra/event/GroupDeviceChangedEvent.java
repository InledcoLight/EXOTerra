package com.inledco.exoterra.event;

public class GroupDeviceChangedEvent {
    private String mGroupid;

    public GroupDeviceChangedEvent(String groupid) {
        mGroupid = groupid;
    }

    public String getGroupid() {
        return mGroupid;
    }
}
