package com.inledco.exoterra.xlink;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RoomApi {
    @Headers ({"Content-Type: application/json"})
    @POST ("/v2/home/{home_id}/room")
    Call<RoomResponse> postRoom(@Path ("home_id") String homeid, @Body RoomRequest request);

    @Headers ({"Content-Type: application/json"})
    @PUT ("/v2/home/{home_id}/room/{room_id}")
    Call<RoomResponse> putRoom(@Path ("home_id") String homeid, @Path ("room_id") String roomid, @Body RoomRequest request);

    @Headers ({"Content-Type: application/json"})
    @DELETE ("/v2/home/{home_id}/room/{room_id}")
    Call<String> deleteRoom(@Path ("home_id") String homeid, @Path ("room_id") String roomid);

    @Headers ({"Content-Type: application/json"})
    @GET ("/v2/home/{home_id}/room/{room_id}")
    Call<RoomInfoResponse> getRoomInfo(@Path ("home_id") String homeid, @Path ("room_id") String roomid);

    @Headers ({"Content-Type: application/json"})
    @POST ("/v2/home/{home_id}/room/{room_id}/device_add")
    Call<String> addRoomDevice(@Path ("home_id") String homeid, @Path ("room_id") String roomid, @Body RoomDeviceRequest request);

    @Headers ({"Content-Type: application/json"})
    @POST ("/v2/home/{home_id}/room/{room_id}/device_remove")
    Call<String> removeRoomDevice(@Path ("home_id") String homeid, @Path ("room_id") String roomid, @Body RoomDeviceRequest request);

    class RoomResponse {
        public String id;
        public String name;
    }

    class RoomRequest {
        public String name;
    }

    class RoomInfoResponse {
        public String id;
        public String name;
        public List<String> zone_ids;
        public List<String> device_ids;
    }

    class RoomDeviceRequest {
        public int device_id;
    }
}
