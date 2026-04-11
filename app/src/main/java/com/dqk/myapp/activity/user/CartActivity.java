package com.dqk.myapp.activity.user;


import android.content.Intent;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private final ActivityResultLauncher<Intent> paymentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    placeOrderAfterPaymentSuccess();
                }
            });

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
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_payment_method, null);
        bottomSheet.setContentView(sheetView);

        TextView tvTotal = sheetView.findViewById(R.id.tvPaymentTotal);
        RadioButton rbMoMo = sheetView.findViewById(R.id.rbMoMo);
        RadioButton rbQR = sheetView.findViewById(R.id.rbQR);
        View layoutMoMo = sheetView.findViewById(R.id.layoutMoMo);
        View layoutQR = sheetView.findViewById(R.id.layoutQR);
        Button btnContinue = sheetView.findViewById(R.id.btnContinuePayment);
        Button btnCancel = sheetView.findViewById(R.id.btnCancel);

        tvTotal.setText("Tổng tiền: " + total + " đ");

        final int[] selectedMethod = {-1};
        layoutMoMo.setOnClickListener(v -> {
            selectedMethod[0] = 0;
            rbMoMo.setChecked(true);
            rbQR.setChecked(false);
        });

        layoutQR.setOnClickListener(v -> {
            selectedMethod[0] = 1;
            rbMoMo.setChecked(false);
            rbQR.setChecked(true);
        });

        btnContinue.setOnClickListener(v -> {
            if (selectedMethod[0] == 0) {
                bottomSheet.dismiss();
                openPaymentConfirm(PaymentConfirmActivity.MODE_MOMO);
            } else if (selectedMethod[0] == 1) {
                bottomSheet.dismiss();
                openPaymentConfirm(PaymentConfirmActivity.MODE_QR);
            } else {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.show();
    }

    private void openPaymentConfirm(int mode) {
        double totalPrice = CartManager.getInstance().getTotalPrice();
        Intent intent = new Intent(this, PaymentConfirmActivity.class);
        intent.putExtra(PaymentConfirmActivity.EXTRA_MODE, mode);
        intent.putExtra(PaymentConfirmActivity.EXTRA_AMOUNT, totalPrice);
        paymentLauncher.launch(intent);
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