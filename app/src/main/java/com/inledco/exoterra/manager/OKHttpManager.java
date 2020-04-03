package com.inledco.exoterra.manager;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpManager {
    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient mHttpClient;

    private OKHttpManager() {
        mHttpClient = new OkHttpClient();
    }

    public static OKHttpManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * @param url
     * @param headers new Headers.Builder().add( key, value );
     * @param callback
     */
    public boolean get(String url, Headers headers, Callback callback) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .build();
        }
        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    public <T> T blockGet(String url, Headers headers, Class<T> clazz) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .build();
        }
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                return JSON.parseObject(response.body().string(), clazz);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post request
     *
     * @param url post address
     * @param headers new Headers.Builder().add( key, value );
     * @param body new  FormBody.Builder().add( key, value );
     * @param callback
     */
    public boolean post(String url, Headers headers, RequestBody body, Callback callback) {
        if (TextUtils.isEmpty(url) || body == null) {
            return false;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .post(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .post(body)
                                           .build();
        }
        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    /**
     * @param url
     * @param headers new Headers.Builder().add( key, value );
     * @param json
     * @param callback
     */
    public boolean post(String url, Headers headers, String json, Callback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(json)) {
            return false;
        }
        RequestBody body = RequestBody.create(CONTENT_TYPE_JSON, json);
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .post(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .post(body)
                                           .build();
        }

        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    public <T> T blockPost(String url, Headers headers, RequestBody body, Class<T> clazz) {
        if (TextUtils.isEmpty(url) || body == null) {
            return null;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .post(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .post(body)
                                           .build();
        }
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                return JSON.parseObject(response.body().string(), clazz);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T blockPost(String url, Headers headers, String json, Class<T> clazz) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(json)) {
            return null;
        }
        RequestBody body = RequestBody.create(CONTENT_TYPE_JSON, json);
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .post(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .post(body)
                                           .build();
        }

        try {
            Response response = mHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return JSON.parseObject(response.body().string(), clazz);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean put(String url, Headers headers, RequestBody body, Callback callback) {
        if (TextUtils.isEmpty(url) || body == null) {
            return false;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .put(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .put(body)
                                           .build();
        }
        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    public boolean put(String url, Headers headers, String json, Callback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(json)) {
            return false;
        }
        RequestBody body = RequestBody.create(CONTENT_TYPE_JSON, json);
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .put(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .put(body)
                                           .build();
        }

        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    public <T> T blockPut(String url, Headers headers, RequestBody body, Class<T> clazz) {
        if (TextUtils.isEmpty(url) || body == null) {
            return null;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .put(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .put(body)
                                           .build();
        }
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                return JSON.parseObject(response.body().string(), clazz);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T blockPut(String url, Headers headers, String json, Class<T> clazz) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(json)) {
            return null;
        }
        RequestBody body = RequestBody.create(CONTENT_TYPE_JSON, json);
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .put(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .put(body)
                                           .build();
        }

        try {
            Response response = mHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return JSON.parseObject(response.body().string(), clazz);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(String url, Headers headers, RequestBody body, Callback callback) {
        if (TextUtils.isEmpty(url) || body == null) {
            return false;
        }
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .delete(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .delete(body)
                                           .build();
        }
        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    public boolean delete(String url, Headers headers, String json, Callback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(json)) {
            return false;
        }
        RequestBody body = RequestBody.create(CONTENT_TYPE_JSON, json);
        Request request;
        if (headers == null) {
            request = new Request.Builder().url(url)
                                           .delete(body)
                                           .build();
        }
        else {
            request = new Request.Builder().url(url)
                                           .headers(headers)
                                           .delete(body)
                                           .build();
        }

        mHttpClient.newCall(request)
                   .enqueue(callback);
        return true;
    }

    /**
     * @param url
     * @param file
     * @param callback
     */
    public boolean download(String url, final File file, final DownloadCallback callback) {
        if (TextUtils.isEmpty(url) || file == null) {
            return false;
        }
        if (file.exists()) {
            return false;
        }
        Request request = new Request.Builder().url(url).build();
        mHttpClient.newCall(request)
                   .enqueue(new Callback() {
                       @Override
                       public void onFailure(Call call, IOException e) {
                           if (callback != null) {
                               callback.onError(e.getMessage());
                           }
                       }

                       @Override
                       public void onResponse(Call call, Response response) {
                           if (response.isSuccessful()) {
                               InputStream is = null;
                               FileOutputStream fos = null;
                               byte[] buf = new byte[1024];
                               int len;
                               try {
                                   long total = response.body()
                                                        .contentLength();
                                   long current = 0;
                                   is = response.body()
                                                .byteStream();
                                   fos = new FileOutputStream(file);
                                   while ((len = is.read(buf)) != -1) {
                                       current += len;
                                       fos.write(buf, 0, len);
                                       if (callback != null) {
                                           callback.onProgress(total, current);
                                       }
                                   }
                                   fos.flush();
                                   if (callback != null) {
                                       callback.onSuccess(file);
                                   }
                               } catch (Exception e) {
                                   if (callback != null) {
                                       callback.onError(e.getMessage());
                                   }
                               } finally {
                                   try {
                                       if (is != null) {
                                           is.close();
                                       }
                                       if (fos != null) {
                                           fos.close();
                                       }
                                   } catch (Exception e) {
                                       if (callback != null) {
                                           callback.onError(e.getMessage());
                                       }
                                   }
                               }
                           }
                       }
                   });
        return true;
    }

    private static class LazyHolder {
        private static final OKHttpManager INSTANCE = new OKHttpManager();
    }

    /**
     *
     * @param <T>   必须为外部类或静态内部类
     */
    public abstract static class HttpCallback<T> implements Callback {
        private Type mType;

        public HttpCallback() {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                mType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String json = response.body().string();
                if (json == null ) {
                    onError(response.code(), null);
                    return;
                }
                try {
                    T result = JSON.parseObject(json, mType);
                    if (result != null) {
                        onSuccess(result);
                    } else {
                        onError(response.code(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onError(response.code(), response.message());
                }
            } else {
                onError(response.code(), response.message());
            }
        }

        public abstract void onError(int code, String msg);

        public abstract void onSuccess(T result);
    }

    public interface DownloadCallback
    {
        void onError( String msg );

        void onProgress( long total, long current );

        void onSuccess( File file );
    }
}
