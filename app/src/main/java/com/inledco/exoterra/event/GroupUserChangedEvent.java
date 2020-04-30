package com.inledco.exoterra.event;

public class GroupUserChangedEvent {
    private String mGroupid;

    public GroupUserChangedEvent(String groupid) {
        mGroupid = groupid;
    }

    public String getGroupid() {
        return mGroupid;
    }
}
