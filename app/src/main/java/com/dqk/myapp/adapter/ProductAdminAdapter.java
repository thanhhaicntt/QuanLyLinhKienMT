package com.dqk.myapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private OnActionListener listener;

    public interface OnActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    public ProductAdminAdapter(Context context, List<Product> productList) {
        this.context     = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_admin, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = productList.get(position);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(fmt.format(p.getPrice()) + " đ");
        holder.tvCategory.setText(p.getCategoryName());
        holder.tvStock.setText("Kho: " + p.getStock());

        // Load ảnh
//        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
//            Glide.with(context).load(p.getImageUrl())
        String displayImageUrl = "";
        if (p.getImageUrl() != null) {
            String[] parts = p.getImageUrl().split("[,;\\n]");
            for (String part : parts) {
                String url = part.trim();
                if (!url.isEmpty()) {
                    displayImageUrl = url;
                    break;
                }
            }
        }
        if (displayImageUrl != null && !displayImageUrl.isEmpty()) {
            Glide.with(context).load(displayImageUrl)
                    .placeholder(R.color.gray_placeholder)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.color.gray_placeholder);
        }

        // Tô màu đỏ nếu hết hàng
        holder.tvStock.setTextColor(p.getStock() == 0
                ? context.getColor(android.R.color.holo_red_dark)
                : context.getColor(android.R.color.holo_green_dark));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvCategory, tvStock;
        Button btnEdit, btnDelete;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName     = itemView.findViewById(R.id.tvProductName);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStock    = itemView.findViewById(R.id.tvStock);
            btnEdit    = itemView.findViewById(R.id.btnEdit);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}