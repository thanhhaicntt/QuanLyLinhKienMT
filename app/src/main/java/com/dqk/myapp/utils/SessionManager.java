package com.dqk.myapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String pref_name = "UserSession";
    private static final String key_is_logged_in = "isLoggedIn";
    private static final String key_username = "username";
    private static final String key_full_name = "fullName";
    private static final String key_user_id = "userId";
    private static final String key_role = "role";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;


    public SessionManager(Context context) {
        this.pref = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    //Save session khi login thành công
    public void createSession(int userId, String username, String fullName, String role) {
        editor.putBoolean(key_is_logged_in, true);
        editor.putString(key_user_id, String.valueOf(userId));    // ← phải là putInt
        editor.putString(key_username, username);
        editor.putString(key_full_name, fullName);
        editor.putString(key_role, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(key_is_logged_in, false);
    }

    public int getUserId() {
        String id = pref.getString(key_user_id, "-1");
        return Integer.parseInt(id);
    }

    public String getUsername() {
        return pref.getString(key_username, "");
    }

    public String getFullName() {
        return pref.getString(key_full_name, "");
    }

    // Quan trọng: dùng để phân quyền
    public String getRole() {
        return pref.getString(key_role, "user");
    }

    //Kiểm tra quyền admin
    public boolean isAdmin() {
        return "admin".equals(getRole());
    }

    // Xóa session khi đăng xuất
    public void logout() {
        editor.clear();
        editor.apply();
    }


}
