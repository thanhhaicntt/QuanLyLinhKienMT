package com.dqk.myapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dqk.myapp.model.Order;
import com.dqk.myapp.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private final DatabaseHelper dbHelper;

    public OrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //Taoj đơn hàng mới - trar về id nếu thành công và -1 thì thất bại
    public long insert(Order order) {
        SQLiteDatabase DB = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", order.getUserId());
        values.put("total_price", order.getTotalPrice());
        values.put("status", "pending");
        long id = DB.insert("orders", null, values);
        DB.close();
        return id;
    }

    //Theem tuwng item vào đơn hàng của 1 usẻ
    public void insertOrderItem(OrderItem item) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("order_id", item.getOrderId());
        values.put("product_id", item.getProductId());
        values.put("quantity", item.getQuantity());
        values.put("price", item.getPrice());
        db.insert("order_items", null, values);
        db.close();
    }

    // Lay lịch sử đơn hàng của 1 user
    public List<Order> getOrdersByUserId(int userId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Order> ds = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT * FROM orders WHERE user_id=? ORDER BY order_date DESC", //Sắp xếp theo ngày giảm dần
                new String[]{String.valueOf(userId)}
        );
        while (c.moveToNext()) {
            ds.add(cursorToOrder(c));
        }
        c.close();
        db.close();
        return ds;
    }

    //Lấy items của 1 đơn hàng (join với producuts)
    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<OrderItem> ds = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT oi.*, p.name as product_name FROM order_items oi " +
                        "JOIN products p ON oi.product_id = p.id WHERE oi.order_id=?",
                new String[]{String.valueOf(orderId)});
        //Lấy thông tin sản phẩm từ bảng products
        //cụ thể là lấy chi tiết thông tin của s.add(cursorToOrderItem(c));các món hàng trong một đơn hàng cụ thể, kèm theo tên của sản phẩm đó.

        while (c.moveToNext()) {
            OrderItem it = new OrderItem(
                    c.getInt(c.getColumnIndexOrThrow("order_id")),
                    c.getInt(c.getColumnIndexOrThrow("product_id")),
                    c.getInt(c.getColumnIndexOrThrow("quantity")),
                    c.getDouble(c.getColumnIndexOrThrow("price"))
            );
            it.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            it.setProductName(c.getString(c.getColumnIndexOrThrow("product_name")));
            ds.add(it);
        }
        c.close();
        db.close();
        return ds;
    }

    /**
     * Kiểm tra xem người dùng đã từng mua sản phẩm này hay chưa
     * Thường là kiểm tra trong các đơn hàng đã được giao (delivered)
     */
    public boolean hasPurchasedProduct(int userId, int productId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        String query = "SELECT 1 FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.user_id = ? AND oi.product_id = ? AND o.status = 'delivered' LIMIT 1";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(productId)});
        boolean hasPurchased = c.getCount() > 0;
        c.close();
        db.close();
        return hasPurchased;
    }

    //Lấy tất cả các đơn hàng(Admin) -join với users để có username
    public List<Order> getAllOrders() {
        SQLiteDatabase DB = dbHelper.openDatabase();
        List<Order> ds = new ArrayList<>();
        Cursor c = DB.rawQuery(
                "SELECT o.*, u.username as username FROM orders o " +
                        "JOIN users u ON o.user_id = u.id ORDER BY o.order_date DESC",
                null
        );
        while (c.moveToNext()) {
            Order o = cursorToOrder(c);
            o.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            ds.add(o);
        }
        c.close();
        DB.close();
        return ds;
    }

    //update status đơn hàng(Admin)
    public int updateStatus(int orderId, String newStatus) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        int rows = db.update("orders", values, "id = ?",
                new String[]{String.valueOf(orderId)});
        db.close();
        return rows;
    }

    // Tổng doanh thu từ đơn đã giao
    public double getTotalRevenue() {
        SQLiteDatabase db = dbHelper.openDatabase();
        double revenue = 0;
        Cursor c = db.rawQuery(
                "SELECT SUM(total_price) as revenue FROM orders WHERE status='delivered'",
                null);
        if (c.moveToFirst()) {
            revenue = c.getDouble(c.getColumnIndexOrThrow("revenue"));
        }
        c.close();
        db.close();
        return revenue;
    }

    // Đếm số đơn đang chờ xử lý
    public int countPendingOrders() {
        SQLiteDatabase db = dbHelper.openDatabase();
        int count = 0;
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) as cnt FROM orders WHERE status='pending'", null);
        if (c.moveToFirst()) {
            count = c.getInt(c.getColumnIndexOrThrow("cnt"));
        }
        c.close();
        db.close();
        return count;
    }

    // Lọc đơn hàng theo trạng thái
    public List<Order> getOrdersByStatus(String status) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Order> ds = new ArrayList<>();
        String query = status.equals("all")
                ? "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id ORDER BY o.order_date DESC"
                : "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id WHERE o.status=? ORDER BY o.order_date DESC";
        Cursor c = status.equals("all")
                ? db.rawQuery(query, null)
                : db.rawQuery(query, new String[]{status});
        while (c.moveToNext()) {
            Order o = cursorToOrder(c);
            o.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            ds.add(o);
        }
        c.close();
        db.close();
        return ds;
    }

    // Lọc đơn hàng theo danh mục sản phẩm
    public List<Order> getOrdersByCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<Order> ds = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT o.*, u.username FROM orders o " +
                        "JOIN users u ON o.user_id = u.id " +
                        "JOIN order_items oi ON o.id = oi.order_id " +
                        "JOIN products p ON oi.product_id = p.id " +
                        "WHERE p.category_id = ? ORDER BY o.order_date DESC",
                new String[]{String.valueOf(categoryId)});
        while (c.moveToNext()) {
            Order o = cursorToOrder(c);
            o.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            ds.add(o);
        }
        c.close();
        db.close();
        return ds;
    }

    private Order cursorToOrder(Cursor c) {
        Order o = new Order(
                c.getInt(c.getColumnIndexOrThrow("user_id")),
                c.getDouble(c.getColumnIndexOrThrow("total_price"))
        );
        o.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        o.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
        o.setOrderDate(c.getString(c.getColumnIndexOrThrow("order_date")));
        return o;
    }


}
