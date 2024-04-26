package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.sukappusers.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {
    private final Context context;
    private final List<Notice> noticeItemList;

    public NoticeAdapter(Context context, List<Notice> noticeItemList) {
        this.context = context;
        this.noticeItemList = noticeItemList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_item_user, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeItemList.get(position);
        holder.bind(notice);
    }

    @Override
    public int getItemCount() {
        return noticeItemList.size();
    }

    public class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final ImageView imageView;
        private final TextView dateTextView;
        private final TextView timeTextView;
        CircularProgressIndicator progressBar;
        TextView status;
        private boolean isExpanded = false;
        @SuppressLint("SetTextI18n")
        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            CircleImageView adminProfileImage = itemView.findViewById(R.id.admin_profileImage);
            TextView fullNameTextView = itemView.findViewById(R.id.fullNameTextView);
            TextView deptTextView = itemView.findViewById(R.id.DeptTextView);
            ImageView shareImageView = itemView.findViewById(R.id.shareImageView);
            progressBar = itemView.findViewById(R.id.circularProgress);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            imageView = itemView.findViewById(R.id.imageView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            status = itemView.findViewById(R.id.status_view);

            Glide.with(context)
                    .load(R.drawable.sharnbasva)
                    .placeholder(R.drawable.sharnbasva)
                    .error(R.drawable.sharnbasva)
                    .into(adminProfileImage);
            adminProfileImage.setImageResource(R.drawable.sharnbasva);
            fullNameTextView.setText("Sharnbasva University");
            deptTextView.setText("Department of MCA");

            descriptionTextView.setOnClickListener(v -> {
                // Toggle the expanded state and update the TextView accordingly
                isExpanded = !isExpanded;
                if (isExpanded) {
                    descriptionTextView.setMaxLines(Integer.MAX_VALUE);
                } else {
                    descriptionTextView.setMaxLines(2);
                }
            });

            shareImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    shareImageView.setVisibility(View.GONE);
                    shareCardView(itemView); // Share the card view content
                }
            });
        }

        public void bind(Notice notice) {
            // Set notice data
            titleTextView.setText(notice.getNoticeTitle());
            descriptionTextView.setText(notice.getNoticeDescription());
            dateTextView.setText(notice.getNoticeDate());
            timeTextView.setText(notice.getNoticeTime());

            imageView.setOnClickListener(v -> {
                // Start FullScreenImageActivity and pass the image URL to it
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("imageUrl", notice.getImageUrl());
                intent.putExtra("title", notice.getNoticeTitle());
                intent.putExtra("description", notice.getNoticeDescription());
                intent.putExtra("date", notice.getNoticeDate());
                intent.putExtra("time", notice.getNoticeTime());
                context.startActivity(intent);
            });
            // Load notice image using Glide into an ImageView
            Glide.with(context)
                    .load(notice.getImageUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE); // Hide the progress bar on successful image load
                            return false;
                        }
                    })
                    .into(imageView);

            status.setText(notice.getStatus());
            status.setAllCaps(true);

            // Set text color based on complaint status
            switch (notice.getStatus()) {
                case "new":
                    status.setBackgroundResource(R.drawable.corner_green);
                    break;
                case "old":
                    status.setBackgroundResource(R.drawable.corner_red);
                    break;
            }
        }

        private Bitmap createBitmapFromView(View view) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }

        private void shareCardView(View cardView) {
            Bitmap cardBitmap = createBitmapFromView(cardView);

            // Save the Bitmap to a temporary file
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            try {
                FileOutputStream outputStream = new FileOutputStream(cachePath + "/card.png");
                cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get the temporary card file URI
            File imagePath = new File(context.getCacheDir(), "images");
            File newFile = new File(imagePath, "card.png");
            Uri cardUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", newFile);

            // Create a sharing intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, cardUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Add the card description to the sharing intent
            String description = descriptionTextView.getText().toString();
            shareIntent.putExtra(Intent.EXTRA_TEXT, description);

            // Launch the share activity
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            ImageView shareImageView = itemView.findViewById(R.id.shareImageView);
            shareImageView.setVisibility(View.VISIBLE);
        }
    }

}
