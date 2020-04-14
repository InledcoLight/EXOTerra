package com.inledco.exoterra.manager;

import android.content.Context;
import android.text.TextUtils;

import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.User;

public class UserManager {

    private User mUser;
    private String mUserid;
    private String mToken;
    private String mSecret;

    private UserManager() {

    }

    public static UserManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean isAuthorized() {
//        return (mUser != null) && (!TextUtils.isEmpty(mToken));
        return (!TextUtils.isEmpty(mUserid)) && (!TextUtils.isEmpty(mToken));
    }

    public User getUser() {
        return mUser;
    }

    public String getUserid() {
        return mUserid;
    }

    public String getNickname() {
        if (mUser == null) {
            return null;
        }
        return mUser.nickname;
    }

    public String getEmail() {
        if (mUser == null) {
            return null;
        }
        return mUser.email;
    }

    public String getToken() {
        return mToken;
    }

    public String getSecret() {
        return mSecret;
    }

    //    public void login(final Context context, final String email, final String password, final Callback callback) {
//        AliotServer.getInstance().login(email, password, new HttpCallback<UserApi.UserLoginResponse>() {
//            @Override
//            public void onError(String error) {
//                if (callback != null) {
//                    callback.onError(error);
//                }
//            }
//
//            @Override
//            public void onSuccess(UserApi.UserLoginResponse loginResponse) {
//                final String userid = loginResponse.userId;
//                mToken = loginResponse.access_token;
//                final String secret = loginResponse.secret;
//                UserPref.saveEmailPassword(context, email, password);
//                UserPref.saveAuthorization(context, userid, mToken, secret);
//                AliotServer.getInstance().getUserInfo(userid, mToken, new HttpCallback<UserApi.GetUserInfoResponse>() {
//                    @Override
//                    public void onError(String error) {
//                        if (callback != null) {
//                            callback.onError(error);
//                        }
//                    }
//
//                    @Override
//                    public void onSuccess(UserApi.GetUserInfoResponse result) {
//                        mUser = result.data;
//                        if (callback != null) {
//                            callback.onSuccess();
//                        }
//                    }
//                });
//            }
//        });
//    }

    public String login(final Context context, final String email, final String password) {
        UserApi.UserLoginResponse response = AliotServer.getInstance().login(email, password);
        if (response != null && response.code == 0) {
            mUserid = response.userId;
            mToken = response.access_token;
            mSecret = response.secret;
            final String userid = response.userId;
            UserPref.saveEmailPassword(context, email, password);
            UserPref.saveAuthorization(context, userid, mToken, mSecret);
            getUserInfo(response.userId, mToken, null);
            return null;
        }
        return (response == null ? "Login failed" : response.msg);
    }

    public boolean getUserInfo(final String userid, final String token) {
        UserApi.GetUserInfoResponse response = AliotServer.getInstance().getUserInfo(userid, token);
        if (response != null && response.code == 0) {
            mUserid = userid;
            mToken = token;
            mUser = response.data;
//            mSecret = response.data.secret;
            return true;
        }
        return false;
    }

    public void getUserInfo(final String userid, final String token, final Callback callback) {
        AliotServer.getInstance().getUserInfo(userid, token, new HttpCallback<UserApi.GetUserInfoResponse>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.GetUserInfoResponse result) {
                mUserid = userid;
                mToken = token;
                mUser = result.data;
//                mSecret = result.data.secret;
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public void modifyUserNickname(final String nickname, final Callback callback) {
        if (mUser == null || TextUtils.isEmpty(mToken) || TextUtils.isEmpty(nickname)) {
            return;
        }
        AliotServer.getInstance().modifyUserNickname(mUser.userid, mToken, nickname, new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                mUser.nickname = nickname;
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public void modifyUserInfo(final UserApi.SetUserInfoRequest request, final Callback callback) {
        if (mUser == null || TextUtils.isEmpty(mToken) || request == null) {
            return;
        }
        AliotServer.getInstance().modifyUserInfo(mUser.userid, mToken, request, new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                if (request.nickname != null) {
                    mUser.nickname = request.nickname;
                }
                if (request.avatar != null) {
                    mUser.avatar = request.avatar;
                }
                if (request.remark1 != null) {
                    mUser.remark1 = request.remark1;
                }
                if (request.remark2 != null) {
                    mUser.remark2 = request.remark2;
                }
                if (request.remark1 != null) {
                    mUser.remark2 = request.remark2;
                }
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public void modifyPassword(final String old_psw, final String new_psw, final Callback callback) {
        if (mUser == null || TextUtils.isEmpty(mToken)) {
            return;
        }
        AliotServer.getInstance().modifyPassword(mToken, old_psw, new_psw, new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public void logout(final Callback callback) {
        if (TextUtils.isEmpty(mToken)) {
            return;
        }
        AliotServer.getInstance().logout(mToken, new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                mUser = null;
                mToken = null;
                mSecret = null;
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public interface Callback {
        void onError(String error);

        void onSuccess();
    }

    private static class LazyHolder {
        private static final UserManager INSTANCE = new UserManager();
    }
}
