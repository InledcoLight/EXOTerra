package com.inledco.exoterra.event;

public class FragmentShowEvent {
    private String tag;
    private boolean show;

    public FragmentShowEvent(String tag, boolean show) {
        this.tag = tag;
        this.show = show;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
