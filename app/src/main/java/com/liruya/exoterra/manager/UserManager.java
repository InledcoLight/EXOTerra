package com.liruya.exoterra.manager;

import android.content.Context;
import android.text.TextUtils;

import com.liruya.exoterra.util.PrefUtil;
import com.liruya.exoterra.util.RegexUtil;

public class UserManager {
    /**
     * 用户信息存储文件
     */
    private static final String PREF_FILE_USER = "user_info";

    /**
     * 用户账户KEY
     */
    private static final String PREF_KEY_USER_ACCOUNT = "user_account";

    private static final String PREF_KEY_USER_PASSWORD = "user_password";

    private static final String PREF_KEY_USER_ID = "user_id";

    private static final String PREF_KEY_USER_AUTHORIZE = "user_authorize";

    private static final String PREF_KEY_USER_REFRESH_TOKEN = "user_refresh_token";


    private static boolean mLogin;

    public static void setIsLogin(boolean isLogin) {
        mLogin = isLogin;
    }

    public static boolean ismLogin() {
        return mLogin;
    }

    public static void setAccount(Context context, String account) {
        PrefUtil.putString(context, PREF_FILE_USER, PREF_KEY_USER_ACCOUNT, account);
    }

    public static String getAccount(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_ACCOUNT, "");
    }

    public static void removeAccount(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_ACCOUNT);
    }

    public static void setPassword(Context context, String password) {
        PrefUtil.putString(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD, password);
    }

    public static String getPassword(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD, "");
    }

    public static void removePassword(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD);
    }

    public static void setUserId(Context context, int userid) {
        PrefUtil.putInt(context, PREF_FILE_USER, PREF_KEY_USER_ID, userid);
    }

    public static int getUserId(Context context) {
        return PrefUtil.getInt(context, PREF_FILE_USER, PREF_KEY_USER_ID, 0);
    }

    public static void removeUserId(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_ID);
    }

    public static void setAuthorize(Context context, String authorize) {
        PrefUtil.putString(context, PREF_FILE_USER, PREF_KEY_USER_AUTHORIZE, authorize);
    }

    public static String getAuthorize(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_AUTHORIZE, "");
    }

    public static void removeAuthorize(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_AUTHORIZE);
    }

    public static void setRefreshToken(Context context, String refresh_token) {
        PrefUtil.putString(context, PREF_FILE_USER, PREF_KEY_USER_REFRESH_TOKEN, refresh_token);
    }

    public static String getRefreshToken(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_REFRESH_TOKEN, "");
    }

    public static void removeRefreshToken(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_REFRESH_TOKEN);
    }

    public static void clear(Context context) {
        PrefUtil.clear(context, PREF_FILE_USER);
        mLogin = false;
    }

//    public static boolean check(Context context) {
//        int userid = UserManager.getUserId(context);
//        String authorize = UserManager.getAuthorize(context);
//        String refresh_token = UserManager.getRefreshToken(context);
//        if (userid == 0 || TextUtils.isEmpty(authorize) || TextUtils.isEmpty(refresh_token)) {
//            return false;
//        }
//        return true;
//    }

    public static boolean checkAuthorize(int userid, String authorize, String refresh_token) {
        if (userid == 0 || TextUtils.isEmpty(authorize) || TextUtils.isEmpty(refresh_token)) {
            return false;
        }
        return true;
    }

    public static boolean checkAccount(String email, String password) {
        if (!RegexUtil.isEmail(email) || TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 16) {
            return false;
        }
        return true;
    }
}
