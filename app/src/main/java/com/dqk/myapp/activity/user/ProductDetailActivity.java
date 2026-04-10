package com.dqk.myapp.activity.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;

import androidx.appcompat.app.AppCompatActivity;

//import com.bumptech.glide.Glide;
import com.dqk.myapp.R;
import com.dqk.myapp.adapter.ProductImagePagerAdapter;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.database.ReviewDAO;
import com.dqk.myapp.model.Product;
import com.dqk.myapp.model.Review;
import com.dqk.myapp.utils.CartManager;
import com.dqk.myapp.utils.SessionManager;

import java.util.ArrayList;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private Product product;
    private int quantity = 1;
    private TextView tvQuantity;

    private ProductDAO productDAO;
    private ReviewDAO reviewDAO;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Thêm nút back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết sản phẩm");
        }

        productDAO = new ProductDAO(DatabaseHelper.getInstance(this));
        reviewDAO = new ReviewDAO(DatabaseHelper.getInstance(this));
        session = new SessionManager(this);
        CartManager.getInstance().init(this, session.getUserId());

        // Lấy product_id được truyền từ màn hình trước
        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        product = productDAO.getById(productId);
        if (product == null) {
            finish();
            return;
        }

        bindProductData();
        loadReviews();
        setupButtons();
    }

    @SuppressLint("SetTextI18n")
    private void bindProductData() {
        TextView tvName = findViewById(R.id.tvProductName);
        TextView tvPrice = findViewById(R.id.tvProductPrice);
        TextView tvCat = findViewById(R.id.tvCategory);
        TextView tvStock = findViewById(R.id.tvStock);
        TextView tvDesc = findViewById(R.id.tvDescription);
//        ImageView imgProduct = findViewById(R.id.imgProduct);
        ViewPager2 vpProductImages = findViewById(R.id.vpProductImages);
        TextView tvImageIndicator = findViewById(R.id.tvImageIndicator);
        TextView tvSpec = findViewById(R.id.tvSpecification);
        tvQuantity = findViewById(R.id.tvQuantity);

        tvName.setText(product.getName());
        tvCat.setText("Danh mục: " + product.getCategoryName());
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "Không có mô tả");

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvPrice.setText(fmt.format(product.getPrice()) + " đ");

        if (product.getSpecification() != null && !product.getSpecification().isEmpty()) {
            tvSpec.setText("Thông số: " + product.getSpecification());
            tvSpec.setVisibility(View.VISIBLE);
        } else {
            tvSpec.setVisibility(View.GONE);
        }

        if (product.getStock() > 0) {
            tvStock.setText("Còn hàng (" + product.getStock() + " sản phẩm)");
            tvStock.setTextColor(getColor(android.R.color.holo_green_dark));
        }
        // Thêm vào hàm bindProductData() sau khi set tvStock
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        if (product.getStock() <= 0) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Hết hàng");
            btnAddToCart.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        }
        List<String> imageUrls = parseImageUrls(product.getImageUrl());
        if (imageUrls.isEmpty()) {
            imageUrls.add("");
        }
        ProductImagePagerAdapter imageAdapter = new ProductImagePagerAdapter(this, imageUrls);
        vpProductImages.setAdapter(imageAdapter);
        tvImageIndicator.setText("1/" + imageUrls.size());
        vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvImageIndicator.setText((position + 1) + "/" + imageUrls.size());
            }
        });
    }

    private List<String> parseImageUrls(String rawImageUrls) {
        List<String> imageUrls = new ArrayList<>();
        if (rawImageUrls == null || rawImageUrls.trim().isEmpty()) return imageUrls;

        String[] parts = rawImageUrls.split("[,;\\n]");
        for (String part : parts) {
            String url = part.trim();
            if (!url.isEmpty()) {
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    private void loadReviews() {
        RatingBar ratingBar = findViewById(R.id.ratingBarAvg);
        TextView tvRatingCount = findViewById(R.id.tvRatingCount);
        TextView tvReviewList = findViewById(R.id.tvReviewList);

        float avg = reviewDAO.getAvgRating(product.getId());
        List<Review> reviews = reviewDAO.getByProduct(product.getId());

        if (reviews.isEmpty()) {
            tvRatingCount.setText("Chưa có đánh giá nào");
            ratingBar.setRating(0);
            tvReviewList.setText("Chưa có đánh giá nào");
        } else {
            ratingBar.setRating(avg);
            tvRatingCount.setText(String.format(Locale.getDefault(), "%.1f/5 (%d đánh giá)", avg, reviews.size()));
           // xem đánh giá user
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < reviews.size(); i++) {
                Review review = reviews.get(i);
                String username = review.getUsername() != null && !review.getUsername().isEmpty()
                        ? review.getUsername()
                        : "Ẩn danh";
                String comment = review.getComment() != null && !review.getComment().trim().isEmpty()
                        ? review.getComment().trim()
                        : "Không có nội dung";
                String createdAt = review.getCreatedAt() != null ? review.getCreatedAt() : "";

                builder.append("• ")
                        .append(username)
                        .append(" - ")
                        .append(review.getRating())
                        .append("/5 sao\n")
                        .append(comment);

                if (!createdAt.isEmpty()) {
                    builder.append("\n")
                            .append(createdAt);
                }

                if (i < reviews.size() - 1) {
                    builder.append("\n\n");
                }
            }
            tvReviewList.setText(builder.toString());
        }
    }

    private void setupButtons() {
        Button btnMinus = findViewById(R.id.btnMinus);
        Button btnPlus = findViewById(R.id.btnPlus);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        Button btnReview = findViewById(R.id.btnWriteReview);
        tvQuantity = findViewById(R.id.tvQuantity);

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity < product.getStock()) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (product.getStock() <= 0) {
                Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }
//            CartManager.getInstance().addToCart(product, quantity);
//            Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            int added = CartManager.getInstance().addToCart(product, quantity);
            if (added <= 0) {
                Toast.makeText(this, "Số lượng trong giỏ đã đạt tối đa tồn kho", Toast.LENGTH_SHORT).show();
            } else if (added < quantity) {
                Toast.makeText(this, "Chỉ thêm được " + added + " sản phẩm do giới hạn tồn kho", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã thêm " + added + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        btnReview.setOnClickListener(v -> showReviewDialog());
    }

    private void showReviewDialog() {
        int userId = session.getUserId();

        if (reviewDAO.hasReviewed(userId, product.getId())) {
            Toast.makeText(this, "Bạn đã đánh giá sản phẩm này rồi", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);

        new AlertDialog.Builder(this).setTitle("Đánh giá sản phẩm").setView(dialogView).setPositiveButton("Gửi", (dialog, which) -> {
            int rating = (int) ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            Review review = new Review(userId, product.getId(), rating, comment);
            long result = reviewDAO.insert(review);

            if (result != -1) {
                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                loadReviews(); // Reload rating
            } else {
                Toast.makeText(this, "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Hủy", null).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}