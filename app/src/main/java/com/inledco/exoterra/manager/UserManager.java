package com.inledco.exoterra.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.inledco.exoterra.util.PrefUtil;
import com.inledco.exoterra.util.RegexUtil;

public class UserManager {
    private static final String TAG = "UserManager";

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

    private static final String PREF_KEY_USER_TOKEN = "user_token";

    private static String mEmail;
    private static String mNickname;

    public static String getEmail() {
        return mEmail;
    }

    public static void setEmail(String email) {
        mEmail = email;
    }

    public static String getNickname() {
        return mNickname;
    }

    public static void setNickname(String nickname) {
        mNickname = nickname;
    }

    public static void setAccount(Context context, String account) {
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_ACCOUNT, account);
    }

    public static String getAccount(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_ACCOUNT, "");
    }

    public static void removeAccount(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_ACCOUNT);
    }

    public static void setPassword(Context context, String password) {
        String data = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        Log.e(TAG, "setPassword: " + data);
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD, data);
    }

    public static String getPassword(Context context) {
        String data = PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD, "");
        String password = new String(Base64.decode(data, Base64.DEFAULT));
        Log.e(TAG, "getPassword: " + password);
        return password;
    }

    public static void removePassword(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD);
    }

    public static void setUserId(Context context, String userid) {
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_ID, userid);
    }

    public static String getUserId(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_ID, "");
    }

    public static void removeUserId(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_ID);
    }

    public static void setToken(Context context, String token) {
        String data = Base64.encodeToString(token.getBytes(), Base64.DEFAULT);
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_TOKEN, data);
    }

    public static String getToken(Context context) {
        String data = PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_TOKEN, "");
        return new String(Base64.decode(data, Base64.DEFAULT));
    }

    public static void removeToken(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_TOKEN);
    }

    public static void clear(Context context) {
        PrefUtil.clear(context, PREF_FILE_USER);
        mEmail = null;
        mNickname = null;
    }

    public static boolean checkAuthorize(String userid, String token) {
        if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(token)) {
            return false;
        }
        return true;
    }

    public static boolean checkAuthorize(Context context) {
        String userid = getUserId(context);
        String token = getToken(context);
        if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(token)) {
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
