package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.example.sukappusers.UserLoginActivity;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends BaseActivity {

    private CircleImageView profile_image_view;
    private FirebaseFirestore db;
    FirebaseAuth mAuth;
    private LinearProgressIndicator toolbarProgressBar;
    TextView email_view, full_name_view, change_password, batch_view,
            phone_view, parent_name, parent_phone, date_view,
            address_view, usn_view, name_view;
    TextView deviceId_text, deviceName_text, ipAddress_text;
    CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        TextView textViewDeviceInfo = findViewById(R.id.textViewDeviceInfo);
        textViewDeviceInfo.setOnClickListener(v -> showSnackbar());

        usn_view = findViewById(R.id.usn_view);
        name_view = findViewById(R.id.name_view);
        batch_view = findViewById(R.id.batch_view);
        email_view = findViewById(R.id.email_view);
        full_name_view = findViewById(R.id.full_name_view);
        phone_view = findViewById(R.id.phone_view);
        parent_name = findViewById(R.id.parent_name);
        parent_phone = findViewById(R.id.parent_phone);
        address_view = findViewById(R.id.address_view);
        change_password = findViewById(R.id.change_password);
        date_view = findViewById(R.id.date_view);
        profile_image_view = findViewById(R.id.profile_image_view);
        deviceId_text = findViewById(R.id.deviceId_text);
        deviceName_text = findViewById(R.id.deviceName_text);
        ipAddress_text = findViewById(R.id.ipAddress_text);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.circularProgress);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);
        db = FirebaseFirestore.getInstance();
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("users_profile/" + currentUserId + ".jpg");
        change_password.setOnClickListener(view -> showChangePassword());

        fetchUserData();
    }

    private void showSnackbar() {
        View view = findViewById(android.R.id.content);
        Snackbar.make(view, "When you log in on another device, you will be logged out automatically.", Snackbar.LENGTH_LONG).show();
    }

    @SuppressLint("SetTextI18n")
    private void fetchUserData() {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userRef.addSnapshotListener((documentSnapshot, error) -> {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String usn = documentSnapshot.getString("usn");
                    String batch = documentSnapshot.getString("batchYear");
                    String email = documentSnapshot.getString("email");
                    String fullName = documentSnapshot.getString("fullName");
                    String phone = documentSnapshot.getString("phoneNumber");
                    String parentName = documentSnapshot.getString("parentName");
                    String parentPhone = documentSnapshot.getString("parentPhone");
                    String address = documentSnapshot.getString("address");
                    String date = documentSnapshot.getString("registrationDate");
                    String time = documentSnapshot.getString("registrationTime");
                    String deviceId = documentSnapshot.getString("deviceId");
                    String deviceName = documentSnapshot.getString("deviceName");
                    String ipAddress = documentSnapshot.getString("ipAddress");

                    // Set the retrieved data to the respective TextView fields
                    usn_view.setText(usn);
                    name_view.setText("Hi, " + fullName);
                    batch_view.setText("Batch : " + batch);
                    full_name_view.setText(fullName);
                    email_view.setText(email);
                    phone_view.setText(phone);
                    parent_name.setText(parentName);
                    date_view.setText(date + " " + time);
                    parent_phone.setText(parentPhone);
                    address_view.setText(address);
                    deviceId_text.setText("Device Id: " + deviceId);
                    deviceName_text.setText("Device Name: " + deviceName);
                    ipAddress_text.setText("IP Address: " + ipAddress);
                    String profileImage = documentSnapshot.getString("profileImage");

                    profile_image_view.setOnClickListener(v -> {
                        // Start FullScreenImageActivity and pass the image URL to it
                        Intent intent = new Intent(this, FullScreenImageActivity.class);
                        intent.putExtra("imageUrl", profileImage);
                        intent.putExtra("title", fullName);
                        intent.putExtra("description", usn);
                        this.startActivity(intent);
                    });

                    // Load notice image using Glide into an ImageView
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this)
                                .load(profileImage)
                                .placeholder(R.drawable.loading_image)
                                .error(R.drawable.error_loading_image)
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
                                .into(profile_image_view);
                    }
                }
            });
        }
    }

    private void showChangePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_pass, null);
        builder.setView(dialogView);

        final EditText currentPasswordEditText = dialogView.findViewById(R.id.current_password_text);
        final EditText newPasswordEditText = dialogView.findViewById(R.id.new_password_text);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String currentPassword = currentPasswordEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();

            if (TextUtils.isEmpty(currentPassword)) {
                Toast.makeText(UserProfileActivity.this, "Enter your current password", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(UserProfileActivity.this, "New Password is required", Toast.LENGTH_SHORT).show();
                return;
            } else if (newPassword.length() < 6) {
                Toast.makeText(UserProfileActivity.this, "New Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            } else if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$#@!]).+$")) {
                Toast.makeText(UserProfileActivity.this, "Invalid new password format", Toast.LENGTH_SHORT).show();
                return;
            }
            updatePassword(currentPassword, newPassword);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updatePassword(String currentPassword, String newPassword) {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = mAuth.getCurrentUser();

        assert user != null;
        AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), currentPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(unused -> {
                    // successfully authenticated
                    toolbarProgressBar.setVisibility(View.INVISIBLE);
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused1 -> {
                                toolbarProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(UserProfileActivity.this, "Password Updated...", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                // Redirect to login or main activity
                                Intent intent = new Intent(UserProfileActivity.this, UserLoginActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                toolbarProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(UserProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    //authenticated failed
                    toolbarProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(UserProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    public void openEditEmail(View view) {
        startActivity(new Intent(this, ChangeEmailActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);

        MenuItem editProfile = menu.findItem(R.id.action_edit);
        MenuItem updateProfile = menu.findItem(R.id.action_update);
        updateProfile.setVisible(false);
        editProfile.setOnMenuItemClickListener(menuItem -> {
            startActivity(new Intent(UserProfileActivity.this, UserEditProfileActivity.class));
            return false;
        });
        return true;
    }
}
