package com.dqk.myapp.activity.user;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dqk.myapp.R;
import com.dqk.myapp.service.OnlinePaymentService;
import com.dqk.myapp.utils.QrCodeGenerator;
import com.dqk.myapp.utils.SessionManager;

import com.google.zxing.WriterException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Locale;

public class PaymentConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "payment_mode";
    public static final int MODE_MOMO = 1;
    public static final int MODE_QR = 2;
    public static final String EXTRA_AMOUNT = "payment_amount";

    private OnlinePaymentService onlinePaymentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirm);

        int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_MOMO);
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                    mode == MODE_QR ? "Chuyển khoản QR" : "Thanh toán MoMo");
        }

        onlinePaymentService = new OnlinePaymentService();
        SessionManager session = new SessionManager(this);

        TextView tvTitle = findViewById(R.id.tvPaymentTitle);
        TextView tvAmount = findViewById(R.id.tvPaymentAmount);
        TextView tvHint = findViewById(R.id.tvPaymentHint);
        ImageView imgQr = findViewById(R.id.imgQr);
        TextView tvContent = findViewById(R.id.tvPaymentContent);
        Button btnMomoPay = findViewById(R.id.btnMomoPay);
        Button btnQrPaid = findViewById(R.id.btnQrPaid);
        TextView tvReceived = findViewById(R.id.tvPaymentReceived);
        Button btnFinalize = findViewById(R.id.btnFinalizeOrder);

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvAmount.setText("Số tiền: " + fmt.format(amount) + " đ");

        if (mode == MODE_MOMO) {
            tvTitle.setText("Thanh toán qua MoMo (demo)");
            tvHint.setText(
                    "Ứng dụng sẽ gọi dịch vụ online giả lập cổng thanh toán. "
                            + "Sau khi thành công, đơn hàng sẽ được tạo tự động.");
            btnMomoPay.setVisibility(View.VISIBLE);
            imgQr.setVisibility(View.GONE);
            tvContent.setVisibility(View.GONE);
            btnQrPaid.setVisibility(View.GONE);
            tvReceived.setVisibility(View.GONE);
            btnFinalize.setVisibility(View.GONE);

            btnMomoPay.setOnClickListener(v -> {
                btnMomoPay.setEnabled(false);
                Toast.makeText(this, "Đang kết nối cổng thanh toán...", Toast.LENGTH_SHORT).show();
                onlinePaymentService.processPayment(amount, new OnlinePaymentService.PaymentCallback() {
                    @Override
                    public void onSuccess() {
                        btnMomoPay.setEnabled(true);
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        btnMomoPay.setEnabled(true);
                        Toast.makeText(PaymentConfirmActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            });
        } else {
            tvTitle.setText("Chuyển khoản ngân hàng (QR demo)");
            int userId = session.getUserId();
            String rawPayload = "LKMT|USER:" + userId + "|AMT:" + amount + "|T:" + System.nanoTime();
            String paymentRef = sha256HexPrefix(rawPayload, 24);

            tvHint.setText(
                    "Quét mã QR bên dưới (nội dung demo). "
                            + "Nội dung chuyển khoản dùng mã băm để tránh trùng lặp.");

            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText("Nội dung CK: " + paymentRef);

            try {
                String qrPayload = "VIETQR-DEMO|" + paymentRef + "|" + fmt.format(amount);
                Bitmap bmp = QrCodeGenerator.encodeAsBitmap(qrPayload, 512);
                imgQr.setImageBitmap(bmp);
                imgQr.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                imgQr.setVisibility(View.GONE);
                Toast.makeText(this, "Không tạo được mã QR", Toast.LENGTH_SHORT).show();
            }

            btnQrPaid.setVisibility(View.VISIBLE);
            btnMomoPay.setVisibility(View.GONE);

            btnQrPaid.setOnClickListener(v -> {
                tvReceived.setVisibility(View.VISIBLE);
                btnFinalize.setVisibility(View.VISIBLE);
                btnQrPaid.setEnabled(false);
            });

            btnFinalize.setOnClickListener(v -> {
                setResult(RESULT_OK);
                finish();
            });
        }
    }

    private static String sha256HexPrefix(String input, int maxLen) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format(Locale.US, "%02x", b));
            }
            String full = sb.toString();
            return full.length() <= maxLen ? full : full.substring(0, maxLen);
        } catch (NoSuchAlgorithmException e) {
            return "REF" + System.currentTimeMillis();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
