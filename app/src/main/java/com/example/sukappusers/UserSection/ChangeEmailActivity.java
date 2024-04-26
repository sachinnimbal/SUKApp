package com.example.sukappusers.UserSection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sukappusers.R;
import com.example.sukappusers.UserLoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeEmailActivity extends AppCompatActivity {
    private TextInputEditText current_email_field, new_email_field;
    private MaterialButton changeEmailBtn;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Change Email Address");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressDialog = new ProgressDialog(ChangeEmailActivity.this);
        progressDialog.setCancelable(false);

        current_email_field = findViewById(R.id.current_email_field);
        new_email_field = findViewById(R.id.new_email_field);
        changeEmailBtn = findViewById(R.id.changeEmailBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        changeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmail()) {
                    changeEmail();
                }
            }
        });
    }

    private boolean validateEmail() {
        String currentEmail = current_email_field.getText().toString().trim();
        String newEmail = new_email_field.getText().toString().trim();

        if (TextUtils.isEmpty(currentEmail)) {
            current_email_field.setError("Current Email is required");
            current_email_field.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            current_email_field.setError("Invalid current email format");
            current_email_field.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newEmail)) {
            new_email_field.setError("New Email is required");
            new_email_field.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            new_email_field.setError("Invalid new email format");
            new_email_field.requestFocus();
            return false;
        }

        return true;
    }

    private void changeEmail() {
        String currentEmail = current_email_field.getText().toString().trim();
        String newEmail = new_email_field.getText().toString().trim();

        // Create and show the progress dialog
        progressDialog.setMessage("Checking current email...");
        progressDialog.show();

        // Check if the current email matches the email of the currently logged-in user
        if (!currentEmail.equals(currentUser.getEmail())) {
            progressDialog.dismiss();
            current_email_field.setError("Current email is incorrect");
            current_email_field.requestFocus();
            return;
        }

        // Check if the new email is the same as the current email
        if (newEmail.equals(currentUser.getEmail())) {
            progressDialog.dismiss();
            new_email_field.setError("New email cannot be the same as the current email");
            new_email_field.requestFocus();
            return;
        }

        // Update the email address
        progressDialog.setMessage("Updating email...");
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        updateEmailInFirestore(newEmail);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ChangeEmailActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmailInFirestore(String newEmail) {
        DocumentReference userRef = firestore.collection("Users").document(currentUser.getUid());

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email", newEmail);

        userRef.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChangeEmailActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();

                    // Show alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeEmailActivity.this);
                    builder.setTitle("Email Updated");
                    builder.setMessage("Please log in again with your new email address.");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                        firebaseAuth.signOut();
                        startActivity(new Intent(ChangeEmailActivity.this, UserLoginActivity.class));
                        finish();
                    });
                    builder.setCancelable(false);
                    builder.show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChangeEmailActivity.this, "Failed to update email in Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}
