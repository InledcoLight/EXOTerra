package com.inledco.exoterra.push;

public interface FCMTokenListener {

    /**
     * token更新结果回调
     * @param token
     */
    void onTokenResult(String token);
}
