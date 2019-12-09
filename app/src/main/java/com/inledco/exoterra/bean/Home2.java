package com.inledco.exoterra.bean;

import java.util.List;

public class Home2 {
    public String id;
    public String name;
    public int creator;
    public String create_time;
    public String update_time;
    public List<User> user_list;
    public List<Room> rooms;
    public List<Zone> zones;

    public static class User {
        public int user_id;
        public int role;
        public String expire_time;
        public String email;
        public String nickname;
    }

    public static class Room {
        public String id;
        public String name;
        public List<String> device_ids;
    }

    public static class Zone {
        public String id;
        public String name;
        public List<String> room_ids;
    }
}
