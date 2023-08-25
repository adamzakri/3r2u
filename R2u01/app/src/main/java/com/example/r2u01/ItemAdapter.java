package com.example.r2u01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final List<Item> itemList;
    private final String fromWhere;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Item item);
    }
    // LongClickListener.java


    public ItemAdapter(List<Item> itemList,OnItemClickListener listener,String fromWhere) {
        this.itemList = itemList;
        this.listener = listener;
        this.fromWhere = fromWhere;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getImgUrl())
                .placeholder(R.drawable.ic_default_image) // Placeholder image while loading
                .error(R.drawable.ic_default_image) // Image to show in case of error
                .into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!fromWhere.equals("collector")){
                    showPopupMenu(holder.itemView, item);
                }
                return true;
            }
        });

    }
    private void showPopupMenu(View anchorView, Item item) {
        // Inflate the custom layout
        View popupView = LayoutInflater.from(anchorView.getContext())
                .inflate(R.layout.popup_options_layout, null);

        // Initialize the views within the popup layout
        TextView editOption = popupView.findViewById(R.id.editOption);
        TextView deleteOption = popupView.findViewById(R.id.deleteOption);

        // Create a PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set up listeners for edit and delete options
        editOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Edit action
                // Pass the item information to EditActivity
                Intent editIntent = new Intent(anchorView.getContext(), EditItemActivity.class);

                editIntent.putExtra("imgUrl", item.getImgUrl());
                editIntent.putExtra("itemId", item.getItemId());
                editIntent.putExtra("itemDesc", item.getDescription());
                editIntent.putExtra("itemTitle", item.getTitle());
                editIntent.putExtra("itemLat", item.getLatitude());
                editIntent.putExtra("itemLng", item.getLongitude());

                anchorView.getContext().startActivity(editIntent);
                popupWindow.dismiss();
            }
        });

        deleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(anchorView.getContext(), item);
                popupWindow.dismiss();
            }
        });

        // Show the popup window
        popupWindow.showAsDropDown(anchorView);
    }

    private void deleteItemFromFirebase(Context context, String itemId) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items").child(uid); // Reference to your items collection
            itemsRef.child(itemId).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Item successfully deleted
                            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete item
                        }
                    });

    }
    private void showDeleteConfirmationDialog(Context context, Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this item?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItemFromFirebase(context, item.getItemId());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Cancel the deletion
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvTitle, tvDescription;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImageView);
            tvTitle = itemView.findViewById(R.id.itemTitleTextView);
            tvDescription = itemView.findViewById(R.id.itemDescriptionTextView);
        }
    }
}
