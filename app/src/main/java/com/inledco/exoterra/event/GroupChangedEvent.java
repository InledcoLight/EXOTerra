package com.inledco.exoterra.event;

public class GroupChangedEvent {
    private String mGroupid;

    public GroupChangedEvent(String groupid) {
        mGroupid = groupid;
    }

    public String getGroupid() {
        return mGroupid;
    }
}
