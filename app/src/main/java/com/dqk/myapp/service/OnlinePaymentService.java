package com.dqk.myapp.service;

import android.os.Handler;
import android.os.Looper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnlinePaymentService {

    public interface PaymentCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void processPayment(double amount, PaymentCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                // Demo gọi service online để mô phỏng cổng thanh toán phản hồi thành công.
                URL url = new URL("https://httpbin.org/status/200?amount=" + amount);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    mainHandler.post(() -> callback.onFailure("Cổng thanh toán từ chối giao dịch (" + responseCode + ")"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onFailure("Không kết nối được dịch vụ thanh toán online"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}