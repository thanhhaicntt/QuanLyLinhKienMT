package com.dqk.myapp.adapter;

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
import com.dqk.myapp.utils.CartManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartManager.CartItem> cartItems;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public void setOnCartChangeListener(OnCartChangeListener listener) {
        this.listener = listener;
    }

    public CartAdapter(Context context, List<CartManager.CartItem> cartItems) {
        this.context   = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartManager.CartItem item = cartItems.get(position);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvName.setText(item.product.getName());
        holder.tvQuantity.setText(String.valueOf(item.quantity));
        holder.tvPrice.setText(fmt.format(item.getTotalPrice()) + " đ");

        String displayImageUrl = getFirstImageUrl(item.product.getImageUrl());
        if (displayImageUrl != null && !displayImageUrl.isEmpty()) {
            Glide.with(context).load(displayImageUrl).into(holder.img);
        } else {
            holder.img.setImageResource(R.color.gray_placeholder);
        }

        // Giảm số lượng
        holder.btnMinus.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CartManager.CartItem currentItem = cartItems.get(adapterPos);
            if (currentItem.quantity > 1) {
                currentItem.quantity--;
                CartManager.getInstance().updateQuantity(
                        currentItem.product.getId(), currentItem.quantity);
                notifyItemChanged(adapterPos);
                if (listener != null) listener.onCartChanged();
            }
        });

        // Tăng số lượng
        holder.btnPlus.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CartManager.CartItem currentItem = cartItems.get(adapterPos);
            if (currentItem.quantity < currentItem.product.getStock()) {
                currentItem.quantity++;
                CartManager.getInstance().updateQuantity(
                        currentItem.product.getId(), currentItem.quantity);
                notifyItemChanged(adapterPos);
                if (listener != null) listener.onCartChanged();
            }
        });

        // Xóa khỏi giỏ
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CartManager.CartItem currentItem = cartItems.get(adapterPos);
            CartManager.getInstance().removeItem(currentItem.product.getId());
//            cartItems.remove(adapterPos);
            notifyItemRemoved(adapterPos);
            notifyItemRangeChanged(adapterPos, cartItems.size());
            if (listener != null) listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

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
        ImageView img;
        TextView tvName, tvPrice, tvQuantity;
        Button btnMinus, btnPlus, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img        = itemView.findViewById(R.id.imgProduct);
            tvName     = itemView.findViewById(R.id.tvProductName);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus   = itemView.findViewById(R.id.btnMinus);
            btnPlus    = itemView.findViewById(R.id.btnPlus);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}