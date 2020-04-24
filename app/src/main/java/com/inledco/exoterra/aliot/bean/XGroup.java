package com.inledco.exoterra.aliot.bean;

import java.util.List;

public class XGroup {
    public String groupid;
    public String name;
    public String remark1;
    public String remark2;
    public String remark3;
    public String creator;
    public long create_time;
    public long update_time;
    public List<User> users;

    public static class User {
        public String userid;
        public String role;
    }
}
