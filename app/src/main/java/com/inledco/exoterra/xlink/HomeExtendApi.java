package com.inledco.exoterra.xlink;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

public interface HomeExtendApi {
    @Headers ({"Content-Type: application/json"})
    @GET ("/v2/homes")
    Call<HomesResponse> getHomes(@QueryMap Map<String, Object> var1);

    class HomesResponse {
        public int count;
        public List<Home> list;

        public static class Home {
            public String id;
            public String name;
            public int type;
//            public XLinkRestfulEnum.HomeType type;
            @SerializedName ("user_list")
            public List<User> userList;
            public int creator;
            @SerializedName("update_time")
            public String updateTime;
            @SerializedName("create_time")
            public String createTime;
            public String version;
            public List<Room> rooms;
            public List<Zone> zones;

            @NonNull
            @Override
            public String toString() {
                return new Gson().toJson(this);
            }

            public static class User {
                @SerializedName("user_id")
                public int userId;
                public String phone;
                public String email;
                public String nickname;
                public String avatar;
                public int role;
//                public XLinkRestfulEnum.HomeUserType role;
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
    }
}
