package com.example.sukappusers.UserSection;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class FullScreenImageActivity extends BaseActivity {
    CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView fullImageView = findViewById(R.id.fullImageView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView facultyNameTextView = findViewById(R.id.facultyNameTextView);
        TextView facultyDescriptionTextView = findViewById(R.id.facultyDescriptionTextView);
        LinearLayout facultyLayout = findViewById(R.id.facultyLayout);
        LinearLayout noticeLayout = findViewById(R.id.noticeLayout);
        progressBar = findViewById(R.id.circularProgress);
        // Retrieve the image URL from the intent extras
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String facultyName = getIntent().getStringExtra("faculty_name");
        String facultyDescription = getIntent().getStringExtra("faculty_description");

        // Determine the item type (faculty or notice) based on the presence of facultyName
        boolean isFaculty = facultyName != null;

        // Set the visibility of the faculty and notice layouts accordingly
        facultyLayout.setVisibility(isFaculty ? View.VISIBLE : View.GONE);
        noticeLayout.setVisibility(isFaculty ? View.GONE : View.VISIBLE);

        facultyDescriptionTextView.setOnClickListener(new View.OnClickListener() {
            boolean isDescriptionVisible = true;

            @Override
            public void onClick(View v) {
                if (isDescriptionVisible) {
                    // Description is currently visible, hide it
                    facultyDescriptionTextView.setMaxLines(Integer.MAX_VALUE);
                    isDescriptionVisible = false;
                } else {
                    // Description is currently hidden, show it

                    facultyDescriptionTextView.setMaxLines(2);
                    isDescriptionVisible = true;
                }
            }
        });
        descriptionTextView.setOnClickListener(new View.OnClickListener() {
            boolean isDescriptionVisible = true;

            @Override
            public void onClick(View v) {
                if (isDescriptionVisible) {
                    // Description is currently visible, hide it
                    descriptionTextView.setMaxLines(Integer.MAX_VALUE);
                    isDescriptionVisible = false;
                } else {
                    // Description is currently hidden, show it
                    descriptionTextView.setMaxLines(2);
                    isDescriptionVisible = true;
                }
            }
        });

        // Load notice image using Glide into an ImageView
        Glide.with(this)
                .load(imageUrl)
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
                .into(fullImageView);

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        dateTextView.setText(date);
        timeTextView.setText(time);
        facultyNameTextView.setText(facultyName);
        facultyDescriptionTextView.setText(facultyDescription);
    }
}
