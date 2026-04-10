package com.dqk.myapp.model;

public class Order {
    private int id;
    private int userId;
    private double totalPrice;
    private String status;
    private String orderDate;

    private String username; // Dùng để hiển thị phía admin

    // Tạo contructor
    public Order(){}

    public Order(int userId , double totalPrice){
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = "pending"; // mặc định là đơn hàng đang chờ xác nhận
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
