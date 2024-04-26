package com.example.sukappusers.UserSection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sukappusers.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sukappusers.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFacultiesActivity extends BaseActivity {

    private DatabaseReference facultyDatabaseReference;
    private FacultyAdapter facultyAdapter;
    private List<Faculty> facultyList;
    private ProgressBar progressBar;
    private ImageView backToTopImageView;
    private LinearProgressIndicator toolbarProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_faculties);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Faculty");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.circularProgress);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);

        // Initialize Firebase Storage and Realtime Database references
        facultyDatabaseReference = FirebaseDatabase.getInstance().getReference("Faculty");

        // Find RecyclerView by its ID
        RecyclerView recyclerView = findViewById(R.id.user_faculty_recycler_view);
        backToTopImageView = findViewById(R.id.backToTopImageView);

        // Set RecyclerView layout manager and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        facultyList = new ArrayList<>();
        // Handle the click event for editIcon
        facultyAdapter = new FacultyAdapter(facultyList, this::showEditDialog);
        recyclerView.setAdapter(facultyAdapter);

        // Scroll listener to show/hide the "back to top" icon based on scroll position
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    // Scrolling downwards
                    backToTopImageView.setVisibility(View.VISIBLE);
                } else {
                    // Scrolling upwards
                    backToTopImageView.setVisibility(View.INVISIBLE);
                }

            }
        });

        // Set click listener for the "back to top" icon
        backToTopImageView.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        // Fetch faculty data from Firebase
        fetchFacultyData();
    }

    private void fetchFacultyData() {
        progressBar.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        facultyDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                facultyList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Faculty faculty = snapshot.getValue(Faculty.class);
                        if (faculty != null) {
                            facultyList.add(faculty);
                        }
                    }
                    facultyAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    toolbarProgressBar.setVisibility(View.GONE);
                } else {
                    showNoDataAlertDialog();
                    progressBar.setVisibility(View.GONE);
                    toolbarProgressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(UserFacultiesActivity.this, "Failed to fetch faculty data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoDataAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserFacultiesActivity.this);
        builder.setTitle("No Faculty Found");
        builder.setMessage("There is no data available here.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed(); // Go back
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditDialog(final Faculty faculty) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_faculty_details, null);

        // Get references to dialog views
        TextView faculty_role_text = dialogView.findViewById(R.id.faculty_role_text);
        TextView nameEditText = dialogView.findViewById(R.id.faculty_Name_text_view);
        TextView descriptionEditText = dialogView.findViewById(R.id.faculty_description_text_view);
        CircleImageView facultyImageView = dialogView.findViewById(R.id.faculty_image_view);
        ImageView contact_image_view = dialogView.findViewById(R.id.contact_image_view);
        descriptionEditText.setTextColor(ContextCompat.getColor(this, R.color.gray));
        ImageView whatsaap_image_view = dialogView.findViewById(R.id.whatsapp_image_view);
        ImageView close = dialogView.findViewById(R.id.close);

        // Set initial values
        faculty_role_text.setText(faculty.getFacultyRole());
        nameEditText.setText(faculty.getFacultyName());
        descriptionEditText.setText(faculty.getFacultyDescription());

        contact_image_view.setOnClickListener(view -> {
            // Get the contact number from data
            String contactNumber = faculty.getFacultyContact();

            // Initiate a call
            Uri callUri = Uri.parse("tel:" + contactNumber);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, callUri);
            startActivity(callIntent);
        });

        whatsaap_image_view.setOnClickListener(view -> {
            String contactNumber = faculty.getFacultyContact();

            // Set the country code
            String countryCode = "+91";

            // Create the WhatsApp URL with the country code and contact number
            String url = "https://api.whatsapp.com/send?phone=" + countryCode + contactNumber;

            // Open WhatsApp
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        Glide.with(this).load(faculty.getImageUrl())
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.error_loading_image)
                .into(facultyImageView);

        // Create the Material Design bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        // Show the bottom sheet dialog
        bottomSheetDialog.show();

        close.setOnClickListener(view1 -> {
            bottomSheetDialog.cancel();
        });

    }
}
