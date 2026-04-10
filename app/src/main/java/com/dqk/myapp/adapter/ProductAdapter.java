package com.dqk.myapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dqk.myapp.R;
import com.dqk.myapp.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private OnItemClickListener listener;

    // Interface để xử lý click từ Activity
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategoryName());

        // Format giá tiền: 3500000 → 3.500.000 đ
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(fmt.format(product.getPrice()) + " đ");

        // Load ảnh bằng Glide — nếu không có ảnh thì hiện màu xám
//        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
        String displayImageUrl = getFirstImageUrl(product.getImageUrl());
        if (displayImageUrl != null && !displayImageUrl.isEmpty()) {
            Glide.with(context)
//                    .load(product.getImageUrl())
                    .load(displayImageUrl)
                    .placeholder(R.color.gray_placeholder)
                    .error(R.color.gray_placeholder)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.color.gray_placeholder);
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Cập nhật danh sách (dùng cho tìm kiếm/lọc)
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    // lấy ảnh đầu tiên
    private String getFirstImageUrl(String rawImageUrls) {
        if (rawImageUrls == null) return "";
        String[] parts = rawImageUrls.split("[,;\\n]");
        for (String part : parts) {
            String url = part.trim();
            if (!url.isEmpty()) return url;
        }
        return "";
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
        }
    }
}
