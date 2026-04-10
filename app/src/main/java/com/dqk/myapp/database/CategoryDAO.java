package com.dqk.myapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dqk.myapp.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final DatabaseHelper dbHelper;

    public CategoryDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Category c) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("name", c.getName());
        values.put("description", c.getDescription());
        long id = db.insert("categories", null, values);
        db.close();
        return id;

    }

    //Lấy tất cả danh mục
    public List<Category> getAll() {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Category> ds = new ArrayList<>();
        Cursor cs = db.rawQuery("SELECT * FROM categories ORDER BY name ASC", null);
        while (cs.moveToNext()) {
            Category c = new Category(
                    cs.getString(cs.getColumnIndexOrThrow("name")), //Lấy tên danh mục
                    cs.getString(cs.getColumnIndexOrThrow("description")) //Lấy mô tả
            );
            c.setId(cs.getInt(cs.getColumnIndexOrThrow("id"))); //Lấy id
            ds.add(c); //Thêm vào danh sách
        }
        cs.close();
        db.close();
        return ds;
    }

    //Caajp nhật danh mục
    public int update(Category c) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("name", c.getName());
        values.put("description", c.getDescription());
        int rows = db.update("categories", values, "id = ?",
                new String[]{String.valueOf(c.getId())});
        db.close();
        return rows;
    }

    //Kieerm tra sản phẩm còn trong danh mục không . Nếu không thì có thể xóa
    public boolean hasProducts(int categoryId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM products WHERE category_id=? AND is_active=1",
                new String[]{String.valueOf(categoryId)}
        );
        boolean has = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return has;
    }

    //Sau khi ddax kiểm tra không còn thì xáo được
    public int delete(int id) {
        SQLiteDatabase db = dbHelper.openDatabase();
        int rows = db.delete("categories", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}
