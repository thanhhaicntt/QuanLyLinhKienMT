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
import com.dqk.myapp.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Context context;
    private final List<User> userList;
    private OnActionListener listener;

    public interface OnActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    public UserAdapter(Context context, List<User> userList) {
        this.context  = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        // Avatar: lấy chữ cái đầu của username
        holder.tvAvatar.setText(user.getUsername()
                .substring(0, 1).toUpperCase());

        // Badge role
        if ("admin".equals(user.getRole())) {
            holder.tvRole.setText("ADMIN");
            holder.tvRole.setBackgroundColor(
                    context.getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvRole.setText("USER");
            holder.tvRole.setBackgroundColor(
                    context.getColor(android.R.color.holo_blue_dark));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(user);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user);
        });
    }

    @Override
    public int getItemCount() { return userList.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvUsername, tvEmail, tvRole;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar  = itemView.findViewById(R.id.tvAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail   = itemView.findViewById(R.id.tvEmail);
            tvRole    = itemView.findViewById(R.id.tvRole);
            btnEdit   = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}