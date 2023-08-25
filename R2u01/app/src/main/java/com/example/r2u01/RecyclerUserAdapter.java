package com.example.r2u01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// RecyclerUserAdapter.java
public class RecyclerUserAdapter extends RecyclerView.Adapter<RecyclerUserAdapter.UserViewHolder> {
    private List<RecyclerUser> userList;
    private OnItemClickListener listener; // Add this member variable
    public interface OnItemClickListener {
        void onItemClick(RecyclerUser user);
    }


    // Constructor that accepts the listener
    public RecyclerUserAdapter(List<RecyclerUser> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        RecyclerUser user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());

        // Load and display the user's profile image using Glide
        Glide.with(holder.itemView.getContext())
                .load(user.getImgUrl())
                .placeholder(R.drawable.ic_default_image)
                .error(R.drawable.ic_default_image)
                .into(holder.profileImageView);

        holder.itemView.setOnClickListener(v -> {
            // Call the onItemClick method of the listener
            listener.onItemClick(user);
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView usernameTextView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
        }
    }
}
