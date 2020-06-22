package com.inledco.exoterra.aliot.bean;

import android.text.TextUtils;

public enum InviteAction {
    INVITE("invite"),
    CANCEL("cancel"),
    ACCEPT("accept"),
    DENY("deny"),
    REMOVE("remove"),
    EXIT("exit"),
    DELETE("delete");

    private final String action;

    InviteAction(String act) {
        action = act;
    }

    public String getAction() {
        return action;
    }

    public static InviteAction getInviteAction(String act) {
        for (InviteAction invite : InviteAction.values()) {
            if (TextUtils.equals(act, invite.getAction())) {
                return invite;
            }
        }
        return null;
    }

    public static InviteAction getInviteAction(InviteMessage msg) {
        if (msg != null) {
            for (InviteAction invite : InviteAction.values()) {
                if (TextUtils.equals(msg.getAction(), invite.getAction())) {
                    return invite;
                }
            }
        }
        return null;
    }
}
