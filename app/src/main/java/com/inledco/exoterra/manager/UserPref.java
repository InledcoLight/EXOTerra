package com.inledco.exoterra.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.inledco.exoterra.util.PrefUtil;
import com.inledco.exoterra.util.RegexUtil;

public class UserPref {
    private static final String TAG = "UserPref";

    /**
     * 用户信息存储文件
     */
    private static final String PREF_FILE_USER = "user_info";

    /**
     * 用户账户KEY
     */
    private static final String PREF_KEY_USER_EMAIL = "user_email";

    private static final String PREF_KEY_USER_PASSWORD = "user_password";

    private static final String PREF_KEY_USER_ID = "user_id";

    private static final String PREF_KEY_USER_TOKEN = "user_token";

    private static final String PREF_KEY_USER_SECRET = "user_secret";

    public static void saveEmailPassword(Context context, String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return;
        }
        if (TextUtils.equals(email, readEmail(context))
            && TextUtils.equals(password, readPassword(context))) {
            return;
        }
        String data = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        SharedPreferences sp = context.getSharedPreferences(PREF_FILE_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putString(PREF_KEY_USER_EMAIL, email)
                                            .putString(PREF_KEY_USER_PASSWORD, data);
        editor.apply();
    }

    public static void saveEmail(Context context, String email) {
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_EMAIL, email);
    }

    public static String readEmail(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_EMAIL, "");
    }

    public static void removeEmail(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_EMAIL);
    }

    public static void savePassword(Context context, String password) {
        String data = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD, data);
    }

    public static String readPassword(Context context) {
        String data = PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD, "");
        String password = new String(Base64.decode(data, Base64.DEFAULT));
        return password;
    }

    public static void removePassword(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_PASSWORD);
    }

    public static void saveAuthorization(Context context, String userid, String token, String secret) {
        if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(token) || TextUtils.isEmpty(secret)) {
            return;
        }
        if (TextUtils.equals(userid, readUserId(context))
            && TextUtils.equals(token, readAccessToken(context))
            && TextUtils.equals(secret, readSecret(context))) {
            return;
        }
        String data1 = Base64.encodeToString(token.getBytes(), Base64.DEFAULT);
        String data2 = Base64.encodeToString(secret.getBytes(), Base64.DEFAULT);
        SharedPreferences sp = context.getSharedPreferences(PREF_FILE_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit()
                                            .putString(PREF_KEY_USER_ID, userid)
                                            .putString(PREF_KEY_USER_TOKEN, data1)
                                            .putString(PREF_KEY_USER_SECRET, data2);
        editor.apply();
    }

    public static void saveUserId(Context context, String userid) {
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_ID, userid);
    }

    public static String readUserId(Context context) {
        return PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_ID, "");
    }

    public static void removeUserId(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_ID);
    }

    public static void saveAccessToken(Context context, String token) {
        String data = Base64.encodeToString(token.getBytes(), Base64.DEFAULT);
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_TOKEN, data);
    }

    public static String readAccessToken(Context context) {
        String data = PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_TOKEN, "");
        return new String(Base64.decode(data, Base64.DEFAULT));
    }

    public static void removeAccessToken(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_TOKEN);
    }

    public static void saveSecret(Context context, String secret) {
        String data = Base64.encodeToString(secret.getBytes(), Base64.DEFAULT);
        PrefUtil.put(context, PREF_FILE_USER, PREF_KEY_USER_SECRET, data);
    }

    public static String readSecret(Context context) {
        String data = PrefUtil.getString(context, PREF_FILE_USER, PREF_KEY_USER_SECRET, "");
        return new String(Base64.decode(data, Base64.DEFAULT));
    }

    public static void removeSecret(Context context) {
        PrefUtil.remove(context, PREF_FILE_USER, PREF_KEY_USER_SECRET);
    }

    public static void clear(Context context) {
        PrefUtil.clear(context, PREF_FILE_USER);
    }

    public static boolean checkAuthorize(Context context) {
        String userid = readUserId(context);
        String token = readAccessToken(context);
        String secret = readSecret(context);
        if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(token) || TextUtils.isEmpty(secret)) {
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
