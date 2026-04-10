package com.dqk.myapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dqk.myapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseHelper dbHelper;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //Thêm user mới - trả về id nếu thành công và -1 thì thất bại
    public long insert(User user) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("full_name", user.getFullname());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole());

        long id = db.insert("users", null, values);
        db.close();
        return id;
    }

    //Login - trả về User nếu OK và null nếu thất bại
    public User login(String username, String password) {
        SQLiteDatabase db = dbHelper.openDatabase();
        User user = null;

        Cursor cs = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, password});
        if (cs.moveToFirst()) {
            user = cursorToUser(cs);
        }
        cs.close();
        db.close();
        return user;
    }

    //Check username đã tồn tại chưa( lúc đăng ký )
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cs = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});

        boolean exists = cs.getCount() > 0;
        cs.close();
        db.close();
        return exists;
    }

    //Check email đã tồn tại chưa( lúc đăng ký )
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cs = db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});

        boolean exists = cs.getCount() > 0;
        cs.close();
        db.close();
        return exists;
    }

    //Lấy tất cả User(Admin dùng)
    public List<User> getAllUsers() {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<User> ds = new ArrayList<>();

        Cursor cs = db.rawQuery("SELECT * FROM users ORDER BY id DESC", null);//Sắp xếp theo id giảm dần

        while (cs.moveToNext()) {
            ds.add(cursorToUser(cs));
        }
        cs.close();
        db.close();
        return ds;
    }

    //Lấy User theo Id
    public User getUserById(int id) {
        SQLiteDatabase db = dbHelper.openDatabase();
        User user = null;
        Cursor cs = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(id)});
        if (cs.moveToFirst()) {
            user = cursorToUser(cs);
        }

        cs.close();
        db.close();
        return user;

    }

    //Cập nhật user
    public int update(User user) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("full_name", user.getFullname());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole());

        int rows = db.update("users", values, "id = ?", new String[]{String.valueOf(user.getId())});
        db.close();
        return rows;
    }

    //Xóa user
    public int delete(int id) {
        SQLiteDatabase db = dbHelper.openDatabase();
        int rows = db.delete("users", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    //Tìm kiếm user theo usẻname hoặc theo email(Admin quản lý cái này)
    public List<User> searchUsers(String keyword) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<User> ds = new ArrayList<>();
        Cursor cs = db.rawQuery("SELECT * FROM users WHERE username LIKE ? OR email LIKE ?",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"});//Tìm kiếm theo username hoặc email

        while (cs.moveToNext()) {
            ds.add(cursorToUser(cs));
        }
        cs.close();
        db.close();
        return ds;
    }

    //chuyển đổi Cursor thành User
    private User cursorToUser(Cursor cs) {
        User user = new User(
                cs.getString(cs.getColumnIndexOrThrow("username")),
                cs.getString(cs.getColumnIndexOrThrow("password")),
                cs.getString(cs.getColumnIndexOrThrow("full_name")),
                cs.getString(cs.getColumnIndexOrThrow("email")),
                cs.getString(cs.getColumnIndexOrThrow("phone")),
                cs.getString(cs.getColumnIndexOrThrow("role"))
        );
        user.setId(cs.getInt(cs.getColumnIndexOrThrow("id")));
        user.setCreatedAt(cs.getString(cs.getColumnIndexOrThrow("created_at")));
        return user;
    }
}
