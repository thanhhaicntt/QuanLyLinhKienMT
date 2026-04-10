package com.dqk.myapp.activity.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.CartAdapter;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.OrderDAO;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.model.Product;
import com.dqk.myapp.model.Order;
import com.dqk.myapp.model.OrderItem;
import com.dqk.myapp.service.OnlinePaymentService;
import com.dqk.myapp.utils.CartManager;
import com.dqk.myapp.utils.SessionManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private CartAdapter cartAdapter;
    private TextView tvTotalPrice, tvEmpty;
    private RecyclerView rvCart;
    private Button btnCheckout;

    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private SessionManager session;
    private OnlinePaymentService onlinePaymentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Giỏ hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        orderDAO   = new OrderDAO(DatabaseHelper.getInstance(this));
        productDAO = new ProductDAO(DatabaseHelper.getInstance(this));
        session    = new SessionManager(this);
        onlinePaymentService = new OnlinePaymentService();
        CartManager.getInstance().init(this, session.getUserId());

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmpty      = findViewById(R.id.tvEmpty);
        rvCart       = findViewById(R.id.rvCart);
        btnCheckout  = findViewById(R.id.btnCheckout);

        setupRecyclerView();
        updateTotal();

        btnCheckout.setOnClickListener(v -> showPaymentDialog());
    }

    private void setupRecyclerView() {
        List<CartManager.CartItem> items = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, items);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);

        // Cập nhật tổng tiền mỗi khi giỏ thay đổi
        cartAdapter.setOnCartChangeListener(this::updateTotal);

        updateEmptyState(items.isEmpty());
    }

    private void updateTotal() {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(fmt.format(CartManager.getInstance().getTotalPrice()) + " đ");
        updateEmptyState(CartManager.getInstance().getCartItems().isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnCheckout.setEnabled(!isEmpty);
    }

    private void showPaymentDialog() {
        List<CartManager.CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        String total = fmt.format(CartManager.getInstance().getTotalPrice());

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Tổng tiền: " + total + " đ\n\nBạn có chắc muốn thanh toán?")
                .setPositiveButton("Thanh toán", (dialog, which) -> handlePayment())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handlePayment() {
        List<CartManager.CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCheckout.setEnabled(false);
        Toast.makeText(this, "Đang kết nối cổng thanh toán online...", Toast.LENGTH_SHORT).show();

        double totalPrice = CartManager.getInstance().getTotalPrice();
        onlinePaymentService.processPayment(totalPrice, new OnlinePaymentService.PaymentCallback() {
            @Override
            public void onSuccess() {
                btnCheckout.setEnabled(true);
                placeOrderAfterPaymentSuccess();
            }

            @Override
            public void onFailure(String message) {
                btnCheckout.setEnabled(true);
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void placeOrderAfterPaymentSuccess() {
        int userId = session.getUserId();
//        double totalPrice = CartManager.getInstance().getTotalPrice();
        List<CartManager.CartItem> items = CartManager.getInstance().getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tồn kho realtime trước khi đặt
        for (CartManager.CartItem item : items) {
            Product latest = productDAO.getById(item.product.getId());
            if (latest == null || latest.getStock() < item.quantity) {
                Toast.makeText(
                        this,
                        "Sản phẩm \"" + item.product.getName() + "\" không đủ tồn kho. Vui lòng cập nhật giỏ hàng.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
        }
        double totalPrice = CartManager.getInstance().getTotalPrice();

        // Tạo đơn hàng
        Order order = new Order(userId, totalPrice);
        long orderId = orderDAO.insert(order);

        if (orderId == -1) {
            Toast.makeText(this, "Thanh toán thất bại, thử lại",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu từng item + cập nhật tồn kho
        for (CartManager.CartItem item : items) {
            OrderItem orderItem = new OrderItem(
                    (int) orderId,
                    item.product.getId(),
                    item.quantity,
                    item.product.getPrice()
            );
            orderDAO.insertOrderItem(orderItem);

            // Trừ tồn kho, tăng sold_count
            productDAO.updateSoldCount(item.product.getId(), item.quantity);
        }

        // Xóa giỏ hàng sau khi đặt thành công
        CartManager.getInstance().clear();

        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

        // Về trang chủ
        Intent intent = new Intent(this, UserMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}