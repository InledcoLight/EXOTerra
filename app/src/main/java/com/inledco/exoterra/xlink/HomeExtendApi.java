package com.inledco.exoterra.xlink;

import com.inledco.exoterra.bean.Home2;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface HomeExtendApi {
    @Headers ({"Content-Type: application/json"})
    @GET ("/v2/home/{home_id}")
    Call<Home2> getHomeInfo(@Path("home_id") String homeid, @QueryMap Map<String, Object> var1);
}
