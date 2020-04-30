package com.inledco.exoterra.aliot.bean;

public enum InviteStatus {
    PENDING(0),
    CANCELLED(1),
    ACCEPTED(2),
    DENIED(3),
    EXPIRED(4)
    ;

    private int status;

    InviteStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static InviteStatus getInviteStatus(int status) {
        for (InviteStatus sta : InviteStatus.values()) {
            if (sta.getStatus() == status) {
                return sta;
            }
        }
        return null;
    }
}
