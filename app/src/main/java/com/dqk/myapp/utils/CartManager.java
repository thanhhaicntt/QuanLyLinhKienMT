package com.dqk.myapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartManager {


    private static final String PREF_NAME = "CartPref";
    private static final String KEY_PREFIX = "cart_user_";
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private SharedPreferences pref;
    private ProductDAO productDAO;
    private int currentUserId = -1;

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // thêm
    public void init(Context context, int userId) {
        if (context == null || userId <= 0) return;
        if (userId == currentUserId && pref != null && productDAO != null) return;

        Context appContext = context.getApplicationContext();
        pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        productDAO = new ProductDAO(DatabaseHelper.getInstance(appContext));
        currentUserId = userId;
        loadFromStorage();
    }

    // Model item trong giỏ hàng
    public static class CartItem {
        public Product product;
        public int quantity;

        public CartItem(Product product, int quantity) {
            this.product  = product;
            this.quantity = quantity;
        }

        public double getTotalPrice() {

            return product.getPrice() * quantity;
        }
    }

    // Thêm vào giỏ — nếu đã có thì tăng số lượng
    public int addToCart(Product product, int quantity) {
        if (product == null || quantity <= 0) return 0;

        int maxStock = Math.max(product.getStock(), 0);
        if (maxStock == 0) return 0;

        for (CartItem item : cartItems) {
            if (item.product.getId() == product.getId()) {
                int oldQty = item.quantity;
                item.quantity = Math.min(maxStock, oldQty + quantity);
                int added = item.quantity - oldQty;
                saveToStorage();
                return added;
            }
        }
        int added = Math.min(maxStock, quantity);
        cartItems.add(new CartItem(product, added));
        saveToStorage();
        return added;
    }

    public List<CartItem> getCartItems() { return cartItems; }

    public int getTotalItems() {
        int total = 0;
        for (CartItem item : cartItems) total += item.quantity;
        return total;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) total += item.getTotalPrice();
        return total;
    }

    public void removeItem(int productId) {
        cartItems.removeIf(item -> item.product.getId() == productId);
        saveToStorage();
    }

    public void updateQuantity(int productId, int newQty) {
        for (CartItem item : cartItems) {
            if (item.product.getId() == productId) {
                int maxStock = Math.max(item.product.getStock(), 0);
                if (newQty <= 0) {
                    removeItem(productId);
                    return;
                }
                item.quantity = Math.min(newQty, maxStock);
                saveToStorage();
                return;
            }
        }
    }

    public void clear() {
        cartItems.clear();
        saveToStorage();
    }

    private String buildUserCartKey() {
        return KEY_PREFIX + currentUserId;
    }

    private void loadFromStorage() {
        cartItems.clear();
        if (pref == null || productDAO == null || currentUserId <= 0) return;

        String raw = pref.getString(buildUserCartKey(), "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int productId = obj.optInt("product_id", -1);
                int quantity = obj.optInt("quantity", 0);
                if (productId <= 0 || quantity <= 0) continue;

                Product product = productDAO.getById(productId);
                if (product == null || product.getStock() <= 0) continue;

                cartItems.add(new CartItem(product, Math.min(quantity, product.getStock())));
            }
        } catch (Exception ignored) {
            cartItems.clear();
        }
    }

    private void saveToStorage() {
        if (pref == null || currentUserId <= 0) return;

        JSONArray arr = new JSONArray();
        for (CartItem item : cartItems) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("product_id", item.product.getId());
                obj.put("quantity", item.quantity);
                arr.put(obj);
            } catch (Exception ignored) {
                // Skip invalid item
            }
        }

        pref.edit().putString(buildUserCartKey(), arr.toString()).apply();
    }
}