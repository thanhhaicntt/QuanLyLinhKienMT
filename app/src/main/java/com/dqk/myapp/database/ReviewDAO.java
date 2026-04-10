package com.dqk.myapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dqk.myapp.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private final DatabaseHelper dbHelper;

    public ReviewDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Review review) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", review.getUserId());
        values.put("product_id", review.getProductId());
        values.put("rating", review.getRating());
        values.put("comment", review.getComment());
        long id = db.insert("reviews", null, values);
        db.close();
        return id;
    }

    // Lấy tất cả review của 1 sản phẩm — JOIN với users để có username
    public List<Review> getByProduct(int productId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Review> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT r.*, u.username FROM reviews r " +
                        "JOIN users u ON r.user_id = u.id " +
                        "WHERE r.product_id=? ORDER BY r.created_at DESC",
                new String[]{String.valueOf(productId)}
        );
        while (cursor.moveToNext()) {
            Review r = new Review(
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                    cursor.getString(cursor.getColumnIndexOrThrow("comment"))
            );
            r.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            r.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            r.setCreated_at(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            list.add(r);
        }
        cursor.close();
        db.close();
        return list;
    }

    // Tính rating trung bình của 1 sản phẩm
    public float getAvgRating(int productId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        float avg = 0;
        Cursor cursor = db.rawQuery(
                "SELECT AVG(rating) as avg_rating FROM reviews WHERE product_id=?",
                new String[]{String.valueOf(productId)}
        );
        if (cursor.moveToFirst()) {
            avg = cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating"));
        }
        cursor.close();
        db.close();
        return avg;
    }

    // Kiểm tra user đã review sản phẩm này chưa
    public boolean hasReviewed(int userId, int productId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM reviews WHERE user_id=? AND product_id=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}
        );
        boolean result = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return result;
    }
}