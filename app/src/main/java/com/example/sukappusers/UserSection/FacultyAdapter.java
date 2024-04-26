package com.example.sukappusers.UserSection;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sukappusers.UserSection.FullScreenImageActivity;
import com.example.sukappusers.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.ViewHolder> {

    private final List<Faculty> facultyList;
    private final OnItemClickListener listener;

    public FacultyAdapter(List<Faculty> facultyList, OnItemClickListener listener) {
        this.facultyList = facultyList;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Faculty faculty);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView facultyItemImage;
        public TextView facultyItemName , facultyItemRole;
        public TextView facultyItemDescription;
        public ImageView editIcon;
        public LinearLayout item_faculty;

        public ViewHolder(View itemView) {
            super(itemView);
            item_faculty = itemView.findViewById(R.id.item_faculty);
            facultyItemImage = itemView.findViewById(R.id.faculty_item_image);
            facultyItemName = itemView.findViewById(R.id.faculty_item_name);
            facultyItemRole = itemView.findViewById(R.id.faculty_item_role);
            facultyItemDescription = itemView.findViewById(R.id.faculty_item_description);
            editIcon = itemView.findViewById(R.id.edit_icon);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_faculty, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Faculty faculty = facultyList.get(position);
        holder.facultyItemName.setText(faculty.getFacultyName());
        holder.facultyItemRole.setText(faculty.getFacultyRole());
        holder.facultyItemDescription.setText(faculty.getFacultyDescription());
        Glide.with(holder.itemView.getContext())
                .load(faculty.getImageUrl())
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.error_loading_image)
                .into(holder.facultyItemImage);

        holder.facultyItemImage.setOnClickListener(view -> {
            // Create an intent to start the FullScreenImageActivity
            Intent intent = new Intent(holder.itemView.getContext(), FullScreenImageActivity.class);
            intent.putExtra("imageUrl", faculty.getImageUrl());
            intent.putExtra("faculty_name", faculty.getFacultyName());
            intent.putExtra("faculty_description", faculty.getFacultyDescription());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.item_faculty.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(faculty);
            }
        });
        holder.editIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(faculty);
            }
        });
    }

    @Override
    public int getItemCount() {
        return facultyList.size();
    }
}
