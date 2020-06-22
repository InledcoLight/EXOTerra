package com.inledco.exoterra.cloudapi;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignatureUtil {
    private static final String TAG = "SignatureUtil";

    private final static String CHARSET_UTF8 = "utf8";
    private final static String ALGORITHM = "UTF-8";
    private final static String SEPARATOR = "&";

    public static Map<String, String> splitQueryString(String url) throws URISyntaxException, UnsupportedEncodingException {
        URI uri = new URI(url);
        String query = uri.getQuery();
        final String[] pairs = query.split("&");
        TreeMap<String, String> queryMap = new TreeMap<>();
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? pair.substring(0, idx) : pair;
            if (!queryMap.containsKey(key)) {
                queryMap.put(key, URLDecoder.decode(pair.substring(idx + 1), CHARSET_UTF8));
            }
        }
        return queryMap;
    }

    public static String generate(String method, Map<String, String> parameter, String accessKeySecret) throws Exception {
        String signString = generateSignString(method, parameter);
        Log.e(TAG, "generate: signString---" + signString);
        byte[] signBytes = hmacSHA1Signature(accessKeySecret + "&", signString);
        String signature = newStringByBase64(signBytes);
        Log.e(TAG, "generate: signature----" + signature);
        if ("POST".equals(method)) {
            return signature;
        }
        return URLEncoder.encode(signature, "UTF-8");
    }

    public static String generateQueryString(Map<String, String> params, boolean isEncodeKV) {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (isEncodeKV) {
                queryString.append(percentEncode(entry.getKey()))
                           .append("=")
                           .append(percentEncode(entry.getValue()))
                           .append("&");
            } else {
                queryString.append(entry.getKey())
                           .append("=")
                           .append(entry.getValue())
                           .append("&");
            }
        }
        if (queryString.length() > 1) {
            queryString.setLength(queryString.length() - 1);
        }
        return queryString.toString();
    }

    public static String generateSignString(String httpMethod, Map<String, String> parameter) {
        TreeMap<String, String> sortParameter = new TreeMap<>();
        sortParameter.putAll(parameter);
        String canonicalizedQueryString = generateQueryString(sortParameter, true);
        if (null == httpMethod) {
            throw new RuntimeException("httpMethod can not be empty");
        }
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(httpMethod).append(SEPARATOR);
        stringToSign.append(percentEncode("/")).append(SEPARATOR);
        stringToSign.append(percentEncode(canonicalizedQueryString));
        return stringToSign.toString();
    }

    public static String percentEncode(String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return URLEncoder.encode(value, CHARSET_UTF8)
                                 .replace("+", "%20")
                                 .replace("*", "%2A")
                                 .replace("%7E", "~");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static byte[] hmacSHA1Signature(String secret, String baseString) throws Exception {
        if (TextUtils.isEmpty(secret)) {
            throw new IOException("secret can not be empty");
        }
        if (TextUtils.isEmpty(baseString)) {
            return null;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), ALGORITHM);
        mac.init(keySpec);
        return mac.doFinal(baseString.getBytes(CHARSET_UTF8));
    }

    public static String newStringByBase64(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new String(Base64.encodeBase64(bytes, false), CHARSET_UTF8);
    }
}
