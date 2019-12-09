package com.inledco.exoterra.xlink;

import com.inledco.exoterra.bean.Home2;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

public interface HomesExtendApi {
    @Headers ({"Content-Type: application/json"})
    @GET ("/v2/homes")
    Call<HomesResponse> getHomes(@QueryMap Map<String, Object> var1);

    class HomesResponse {
        public int count;
        public List<Home2> list;
    }
}
