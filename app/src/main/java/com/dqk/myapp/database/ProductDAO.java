package com.dqk.myapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dqk.myapp.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final DatabaseHelper dbHelper;

    public ProductDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //Theem sản phẩm mới
    public long insert(Product p) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("category_id", p.getCategoryId());
        values.put("name", p.getName());
        values.put("description", p.getDescription());
        values.put("price", p.getPrice());
        values.put("stock", p.getStock());
        values.put("image_url", p.getImageUrl());
        values.put("is_active", 1);
        values.put("specification", p.getSpecification() != null ? p.getSpecification() : "");
        long id = db.insert("products", null, values);
        db.close();
        return id;
    }

    //Lấy tất cả các sản phẩm - join với categories để có categoryName
    public List<Product> getAllProducts() {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Product> ds = new ArrayList<>();
        Cursor cs = db.rawQuery("SELECT p.*, c.name as category_name FROM products p " + "JOIN categories c ON p.category_id = c.id " + "WHERE p.is_active = 1 ORDER BY p.id DESC", null);//kết hợp JOIN để lấy danh sách sản phẩm, kèm theo tên danh mục của từng sản phẩm.

        while (cs.moveToNext()) {
            ds.add(cursorToProduct(cs));
        }
        cs.close();
        db.close();
        return ds;
    }

    //Laay sản phm theo danh mục
    public List<Product> getByCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Product> ds = new ArrayList<>();
        Cursor cs = db.rawQuery("SELECT p.*, c.name as category_name FROM products p " + "JOIN categories c ON p.category_id = c.id " + "WHERE p.category_id = ? AND p.is_active = 1 ORDER BY p.id DESC", new String[]{String.valueOf(categoryId)});
        while (cs.moveToNext()) {
            ds.add(cursorToProduct(cs));
        }
        cs.close();
        db.close();
        return ds;
    }

    //Tifm kiếm sản phẩm theo tên , JOIN để lấyt luôn danh mục
    public List<Product> searchProducts(String keyword) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Product> ds = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT p.*, c.name as category_name FROM products p " + "JOIN categories c ON p.category_id = c.id " + "WHERE p.name LIKE ? COLLATE NOCASE AND p.is_active=1", new String[]{"%" + keyword + "%"});

        while (c.moveToNext()) {
            ds.add(cursorToProduct(c));
        }
        c.close();
        db.close();
        return ds;
    }

    //Laasy sản phẩm mới nhất(Homepage)
    public List<Product> getNewestProducts(int limit) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Product> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT p.*, c.name as category_name FROM products p " + "JOIN categories c ON p.category_id = c.id " + "WHERE p.is_active=1 ORDER BY p.created_at DESC LIMIT ?", new String[]{String.valueOf(limit)});
        while (cursor.moveToNext()) {
            list.add(cursorToProduct(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    //Lấy sản phẩm bán chạy nhất (Homepage)
    public List<Product> getBestSellerProducts(int limit) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Product> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT p.*, c.name as category_name FROM products p " + "JOIN categories c ON p.category_id = c.id " + "WHERE p.is_active=1 ORDER BY p.sold_count DESC LIMIT ?", new String[]{String.valueOf(limit)});
        while (cursor.moveToNext()) {
            list.add(cursorToProduct(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    //Lấy sản phẩm theo ID
    public Product getById(int id) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Product p = null;
        Cursor cs = db.rawQuery("SELECT p.*, c.name as category_name FROM products p " + "JOIN categories c ON p.category_id = c.id WHERE p.id=?", new String[]{String.valueOf(id)});

        if (cs.moveToFirst()) {
            p = cursorToProduct(cs);
        }
        cs.close();
        db.close();
        return p;

    }

    //Cạap nhật sản phẩm
    public int update(Product p) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("category_id", p.getCategoryId());
        values.put("name", p.getName());
        values.put("description", p.getDescription());
        values.put("price", p.getPrice());
        values.put("stock", p.getStock());
        values.put("image_url", p.getImageUrl());
        values.put("specification", p.getSpecification() != null ? p.getSpecification() : "");
        int rows = db.update("products", values, "id = ?", new String[]{String.valueOf(p.getId())});
        db.close();
        return rows;
    }

    //Xoas sản phẩm(Chỉ ẩn đi chứ ko xóa)
    public int delete(int id) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0); // Đặt is_active thành 0 để ẩn sản phẩm
        int rows = db.update("products", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    //Caajp nhật sold_count sau khi đặt hàng thành công
    public void updateSoldCount(int productId, int quantity) {
        SQLiteDatabase db = dbHelper.openDatabase();
        db.execSQL("UPDATE products SET sold_count = sold_count + ?, " + "stock = stock - ? WHERE id = ?", new Object[]{quantity, quantity, productId});
        db.close();
    }


    private Product cursorToProduct(Cursor cs) {
        Product p = new Product(cs.getInt(cs.getColumnIndexOrThrow("category_id")), cs.getString(cs.getColumnIndexOrThrow("name")), cs.getString(cs.getColumnIndexOrThrow("description")), cs.getDouble(cs.getColumnIndexOrThrow("price")), cs.getInt(cs.getColumnIndexOrThrow("stock")), cs.getString(cs.getColumnIndexOrThrow("image_url")));
                // Dùng try-catch vì cột có thể không tồn tại ở DB cũ
        try {
            p.setSpecification(cs.getString(cs.getColumnIndexOrThrow("specification")));
        } catch (Exception e) {
            p.setSpecification("");
        }
        p.setId(cs.getInt(cs.getColumnIndexOrThrow("id")));
        p.setSoldCount(cs.getInt(cs.getColumnIndexOrThrow("sold_count")));
        p.setIsActive(cs.getInt(cs.getColumnIndexOrThrow("is_active")));
        p.setCreatedAt(cs.getString(cs.getColumnIndexOrThrow("created_at")));
        p.setCategoryName(cs.getString(cs.getColumnIndexOrThrow("category_name")));
        return p;
    }

}
