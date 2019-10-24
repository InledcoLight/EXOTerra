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

public interface ZoneApi {
    @Headers ({"Content-Type: application/json"})
    @POST ("/v2/home/{home_id}/zone")
    Call<ZoneResponse> postZone(@Path ("home_id") String homeid, @Body ZoneRequest request);

    @Headers ({"Content-Type: application/json"})
    @PUT ("/v2/home/{home_id}/zone/{zone_id}")
    Call<ZoneResponse> putZone(@Path ("home_id") String homeid, @Path ("zone_id") String zoneid, @Body ZoneRequest request);

    @Headers ({"Content-Type: application/json"})
    @DELETE ("/v2/home/{home_id}/zone/{zone_id}")
    Call<String> deleteZone(@Path ("home_id") String homeid, @Path ("zone_id") String zoneid);

    @Headers ({"Content-Type: application/json"})
    @GET ("/v2/home/{home_id}/zone/{zone_id}")
    Call<ZoneInfoResponse> getzoneInfo(@Path ("home_id") String homeid, @Path ("zone_id") String zoneid);

    @Headers ({"Content-Type: application/json"})
    @POST ("/v2/home/{home_id}/zone/{zone_id}/room_add")
    Call<String> addZoneRoom(@Path ("home_id") String homeid, @Path ("zone_id") String zoneid, @Body ZoneRoomRequest request);

    @Headers ({"Content-Type: application/json"})
    @POST ("/v2/home/{home_id}/zone/{zone_id}/room_remove")
    Call<String> removeZoneRoom(@Path ("home_id") String homeid, @Path ("zone_id") String zoneid, @Body ZoneRoomRequest request);

    class ZoneResponse {
        public String id;
        public String name;
    }

    class ZoneRequest {
        public String name;
    }

    class ZoneInfoResponse {
        public String id;
        public String name;
        public List<String> room_ids;
    }

    class ZoneRoomRequest {
        public String room_id;
    }
}
