package com.inledco.exoterra.cloudapi;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

public class CloudApi {
    private static final String TAG = "CloudApi";

    private static final String BASE_URL_FMT = "http://iot.%1$s.aliyuncs.com/";
    private static final String REGION = "us-west-1";
    private static final String ACCESS_KEY = "LTAI4FxN99U6ngaT9xfGCUbw";
    private static final String ACCESS_SECRET = "YNcOMBLc8jv2QDLTcgE47DKGsYgj05";
    private static final String BASE_URL = String.format(BASE_URL_FMT, REGION);

    private static final String CLOUD_API_VERSION = "2018-01-20";

    public static String test() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "UTC"));
        long time = System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        // 公共参数
        map.put("Format", "JSON");
        map.put("Version", CLOUD_API_VERSION);
        map.put("AccessKeyId", ACCESS_KEY);
        map.put("SignatureMethod", "HMAC-SHA1");
        map.put("Timestamp", df.format(new Date(time)));
        map.put("SignatureVersion", "1.0");
        map.put("SignatureNonce", String.valueOf(time));
        map.put("RegionId", REGION);
        // 请求参数
        map.put("Action", "QueryDevicePropertyStatus");
        map.put("DeviceName", "2CF432121F42");
        map.put("ProductKey", "a3pXBGXhUbn");
        try {
            String signature = SignatureUtil.generate("GET", map, ACCESS_SECRET);
            StringBuilder sb = new StringBuilder(BASE_URL);
            sb.append("?");
            for (String key : map.keySet()) {
                sb.append(key).append("=").append(map.get(key)).append("&");
            }
            sb.append("Signature").append("=").append(signature);
            Log.e(TAG, "test: " + sb);
            return new String(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
