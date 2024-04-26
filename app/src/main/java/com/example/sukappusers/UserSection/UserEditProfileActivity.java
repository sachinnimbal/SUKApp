package com.example.sukappusers.UserSection;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserEditProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText parentNameEditText;
    private TextInputEditText parentPhoneEditText;
    private TextInputEditText addressEditText;
    private CircleImageView profile_image_view;
    private StorageReference storageReference;
    private TextView choose_image_button;
    private TextView name_view;
    private TextView usn_view;
    private FirebaseFirestore db;
    private ListenerRegistration userDataListener;
    private Button updateButton;
    Spinner batchOptionSpinner;
    private ArrayAdapter<String> batchAdapter;
    private LinearProgressIndicator toolbarProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_profile);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        name_view = findViewById(R.id.name_view);
        usn_view = findViewById(R.id.usn_view);
        profile_image_view = findViewById(R.id.profile_image_view);
        emailEditText = findViewById(R.id.email_view);
        phoneEditText = findViewById(R.id.edit_phone_view);
        parentNameEditText = findViewById(R.id.parentNameEditText);
        parentPhoneEditText = findViewById(R.id.parentPhoneEditText);
        addressEditText = findViewById(R.id.address);
        choose_image_button = findViewById(R.id.choose_image_button);
        emailEditText.setTextColor(Color.BLACK);
        updateButton = findViewById(R.id.update_button);
        updateButton.setVisibility(View.GONE);
        batchOptionSpinner = findViewById(R.id.batchOptionSpinner);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);

        toolbarProgressBar.setVisibility(View.VISIBLE);

        choose_image_button.setOnClickListener(view -> openImageChooser());
        fetchUserData();
        setupUpdateButton();

        storageReference = FirebaseStorage.getInstance().getReference();

        // Set up ArrayAdapter for the semester spinner
        batchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchOptionSpinner.setAdapter(batchAdapter);

        // Set up ValueEventListener to populate the spinner with semesters
        DatabaseReference semesterRef = FirebaseDatabase.getInstance().getReference("BatchYear");
        semesterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> batch = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String batchKey = snapshot.child("batchKey").getValue(String.class);
                    String batchName = snapshot.child("batchName").getValue(String.class);
                    if (batchKey != null && batchName != null) {
                        batch.add(batchName);
                    }
                }
                batchAdapter.clear();
                batchAdapter.addAll(batch);
                batchAdapter.notifyDataSetChanged();


                toolbarProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void setupUpdateButton() {
        updateButton.setOnClickListener(v -> updateUserData());
    }

    private void fetchUserData() {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userDataListener = userRef.addSnapshotListener((documentSnapshot, error) -> {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String usn = documentSnapshot.getString("usn");
                    String email = documentSnapshot.getString("email");
                    documentSnapshot.getString("batchYear");
                    String fullName = documentSnapshot.getString("fullName");
                    String phone = documentSnapshot.getString("phoneNumber");
                    String parentName = documentSnapshot.getString("parentName");
                    String parentPhone = documentSnapshot.getString("parentPhone");
                    String address = documentSnapshot.getString("address");

                    usn_view.setText(usn);
                    name_view.setText(fullName);
                    emailEditText.setText(email);
                    phoneEditText.setText(phone);
                    parentNameEditText.setText(parentName);
                    parentPhoneEditText.setText(parentPhone);
                    addressEditText.setText(address);
                    String profileImage = documentSnapshot.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(this)
                                .load(profileImage)
                                .placeholder(R.drawable.loading_image)
                                .error(R.drawable.error_loading_image)
                                .into(profile_image_view);
                    }
                }
            });
        }
    }

    private void updateUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            String phone = Objects.requireNonNull(phoneEditText.getText()).toString().trim();
            String parentName = Objects.requireNonNull(parentNameEditText.getText()).toString().trim();
            String parentPhone = Objects.requireNonNull(parentPhoneEditText.getText()).toString().trim();
            String address = Objects.requireNonNull(addressEditText.getText()).toString().trim();
            String batchyear = batchOptionSpinner.getSelectedItem().toString();

            if (phone.isEmpty()) {
                phoneEditText.setError("Phone number is required");
                phoneEditText.requestFocus();
                return;
            }

            if (!isValidIndianPhoneNumber(phone)) {
                phoneEditText.setError("Invalid Indian phone number");
                phoneEditText.requestFocus();
                return;
            }

            if (parentName.isEmpty()) {
                parentNameEditText.setError("Parent's name is required");
                parentNameEditText.requestFocus();
                return;
            }

            if (parentPhone.isEmpty()) {
                parentPhoneEditText.setError("Parent's phone number is required");
                parentPhoneEditText.requestFocus();
                return;
            }

            if (!isValidIndianPhoneNumber(parentPhone)) {
                parentPhoneEditText.setError("Invalid Indian phone number");
                parentPhoneEditText.requestFocus();
                return;
            }

            if (phone.equals(parentPhone)) {
                parentPhoneEditText.setError("Both phone numbers cannot be the same");
                parentPhoneEditText.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                addressEditText.setError("Address is required");
                addressEditText.requestFocus();
                return;
            } else {
                String[] addressWords = address.split("\\s+");
                int wordCount = addressWords.length;

                if (wordCount < 10) {
                    addressEditText.setError("Address should be at least 10 words");
                    addressEditText.requestFocus();
                    return;
                } else if (wordCount > 20) {
                    addressEditText.setError("Address should not exceed 20 words");
                    addressEditText.requestFocus();
                    return;
                }
            }

            toolbarProgressBar.setVisibility(View.VISIBLE);

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                transaction.update(userRef, "phoneNumber", phone);
                transaction.update(userRef, "parentName", parentName);
                transaction.update(userRef, "parentPhone", parentPhone);
                transaction.update(userRef, "address", address);
                transaction.update(userRef, "batchYear", batchyear); // Add batchYear field
                return null;
            }).addOnSuccessListener(aVoid -> {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UserEditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UserEditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean isValidIndianPhoneNumber(String phoneNumber) {
        // Indian phone number regex pattern
        String pattern = "^[6-9]\\d{9}$";
        return phoneNumber.matches(pattern);
    }

    private void uploadProfileImage(Uri imageUri) {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Get the original file extension from the imageUri
            String originalExtension = getExtensionFromUri(imageUri);
            StorageReference profileImageRef = storageReference.child("users_profile/" + userId + "." + originalExtension);

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }

                if (bitmap != null) {
                    // Compress the bitmap
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Adjust the quality as needed (0 - 100)
                    byte[] imageData = baos.toByteArray();

                    // Upload the compressed image data to Firebase Storage
                    profileImageRef.putBytes(imageData)
                            .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> db.collection("Users").document(userId)
                                    .update("profileImage", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(UserEditProfileActivity.this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(UserEditProfileActivity.this, "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                                    })))
                            .addOnFailureListener(e -> {
                                toolbarProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(UserEditProfileActivity.this, "Failed to upload profile photo", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    toolbarProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(UserEditProfileActivity.this, "Failed to decode the selected image", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UserEditProfileActivity.this, "Error while processing image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getExtensionFromUri(Uri uri) {
        // ContentResolver to query the file type based on the Uri's scheme
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Return the file extension based on the Uri's scheme
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profile_image_view.setImageURI(imageUri);
            uploadProfileImage(imageUri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDataListener != null) {
            userDataListener.remove();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);

        MenuItem editProfile = menu.findItem(R.id.action_edit);
        MenuItem updateProfile = menu.findItem(R.id.action_update);
        editProfile.setVisible(false);

        updateProfile.setOnMenuItemClickListener(menuItem -> {
            updateUserData();
            return false;
        });

        return true;
    }

}
