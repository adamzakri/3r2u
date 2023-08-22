package com.example.r2u01;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private List<ListItem> itemList;
    private OnItemClickListener itemClickListener;

    public CustomAdapter(List<ListItem> itemList, OnItemClickListener itemClickListener) {
        this.itemList = itemList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem item = itemList.get(position);
        holder.bind(item, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textDescription;
        ImageView imageView;
        Button buttonEdit;

        Button buttonFind;

        public ViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textViewItem);
            textDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView);


        }

        public void bind(final ListItem item, final OnItemClickListener listener) {
            textTitle.setText(item.getTitle());
            textDescription.setText(item.getDescription());
            if (item.getImageUri() != null) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUri())
                        .apply(RequestOptions.centerCropTransform())
                        .into(imageView);
            }


            imageView.setOnClickListener(v -> listener.onEditClick(getAdapterPosition(), imageView));

        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position, ImageView imageView);
        void onDeleteClick(int position);


    }

}
