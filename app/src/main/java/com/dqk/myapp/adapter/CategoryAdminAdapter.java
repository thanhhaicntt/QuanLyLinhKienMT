package com.dqk.myapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.model.Category;

import java.util.List;

public class CategoryAdminAdapter extends RecyclerView.Adapter<CategoryAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private final ProductDAO productDAO;
    private OnActionListener listener;

    public interface OnActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    public CategoryAdminAdapter(Context context, List<Category> categoryList) {
        this.context      = context;
        this.categoryList = categoryList;
        this.productDAO   = new ProductDAO(DatabaseHelper.getInstance(context));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvName.setText(category.getName());
        holder.tvDesc.setText(category.getDescription() != null
                ? category.getDescription() : "Không có mô tả");

        // Đếm số sản phẩm trong danh mục
        int count = productDAO.getByCategory(category.getId()).size();
        holder.tvProductCount.setText(count + " sản phẩm");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(category);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(category);
        });
    }

    @Override
    public int getItemCount() { return categoryList.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Category> newList) {
        categoryList.clear();
        categoryList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvProductCount;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvCategoryName);
            tvDesc         = itemView.findViewById(R.id.tvDescription);
            tvProductCount = itemView.findViewById(R.id.tvProductCount);
            btnEdit        = itemView.findViewById(R.id.btnEdit);
            btnDelete      = itemView.findViewById(R.id.btnDelete);
        }
    }
}