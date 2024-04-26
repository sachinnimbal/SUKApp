package com.example.sukappusers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserLoginActivity extends AppCompatActivity {
    TextInputEditText email, password;
    FirebaseAuth fAuth;
    ProgressDialog progressDialog;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            redirectToMainScreen();
            return;
        }
        setContentView(R.layout.activity_user_login);

        email = findViewById(R.id.email_text);
        password = findViewById(R.id.password_text);
        fAuth = FirebaseAuth.getInstance();
        login = findViewById(R.id.login_Button);

        progressDialog = new ProgressDialog(UserLoginActivity.this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        login.setOnClickListener(view -> {
            String iEmail = Objects.requireNonNull(email.getText()).toString().trim();
            String iPassword = Objects.requireNonNull(password.getText()).toString().trim();

            if (iEmail.isEmpty()) {
                email.setError("Email is required");
                email.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(iEmail).matches()) {
                email.setError("Invalid email format");
                email.requestFocus();
                return;
            }

            if (iPassword.isEmpty()) {
                password.setError("Password is required");
                password.requestFocus();
                return;
            }

            FirebaseMessaging.getInstance().subscribeToTopic("Notice")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Subscription success
                            loginUser(iEmail, iPassword);
                        } else {
                            // Subscription failed
                            Toast.makeText(UserLoginActivity.this, "Failed to subscribe to topic.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loginUser(String email, String password) {
        progressDialog.show();

        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = fAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Check if the user is a regular user
                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(userId)
                                    .get()
                                    .addOnCompleteListener(usersTask -> {
                                        progressDialog.dismiss();
                                        if (usersTask.isSuccessful() && usersTask.getResult().exists()) {
                                            // Get the user document
                                            DocumentSnapshot documentSnapshot = usersTask.getResult();
                                            String status = documentSnapshot.getString("status");
                                            if ("active".equals(status)) {
                                                // Update device ID, device model, and IP address
                                                @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                                                String ipAddress = getDeviceIpAddress();

                                                Map<String, Object> data = new HashMap<>();
                                                data.put("deviceId", deviceId);
                                                data.put("ipAddress", ipAddress);
                                                data.put("deviceName", getDeviceName());

                                                FirebaseFirestore.getInstance().collection("Users")
                                                        .document(userId)
                                                        .set(data, SetOptions.merge())
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Redirect to UserMainActivity
                                                            redirectToActivity();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(UserLoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show());
                                            } else if ("pending".equals(status)) {
                                                // Account is still being verified, show appropriate message
                                                showVerificationPendingDialog();
                                            }
                                        } else {
                                            // Check if the user is in the BlockedUsers collection
                                            FirebaseFirestore.getInstance().collection("BlockedUsers")
                                                    .document(userId)
                                                    .get()
                                                    .addOnCompleteListener(blockedUsersTask -> {
                                                        progressDialog.dismiss();
                                                        if (blockedUsersTask.isSuccessful() && blockedUsersTask.getResult().exists()) {
                                                            // Account is blocked, show alert dialog
                                                            showAccountBlockedDialog();
                                                        } else {
                                                            // Check if the user is in the RemovedUsers collection
                                                            FirebaseFirestore.getInstance().collection("RemovedUsers")
                                                                    .document(userId)
                                                                    .get()
                                                                    .addOnCompleteListener(removedUsersTask -> {
                                                                        progressDialog.dismiss();
                                                                        if (removedUsersTask.isSuccessful() && removedUsersTask.getResult().exists()) {
                                                                            // Account is removed, show alert dialog
                                                                            showAccountRemovedDialog();
                                                                        } else {
                                                                            // User not found in any collection, show appropriate message
                                                                            showUserNotFoundDialog();
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(UserLoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private String getDeviceIpAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void redirectToMainScreen() {
        Intent intent = new Intent(UserLoginActivity.this, UserMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToActivity() {
        Toast.makeText(UserLoginActivity.this, "Welcome User.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(UserLoginActivity.this, UserMainActivity.class));
        finish();
    }

    private void showAccountRemovedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this);
        LayoutInflater inflater = LayoutInflater.from(UserLoginActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_register_blocked, null);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAccountBlockedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this);
        LayoutInflater inflater = LayoutInflater.from(UserLoginActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_blocked, null);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUserNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this);
        LayoutInflater inflater = LayoutInflater.from(UserLoginActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_not_found, null);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showVerificationPendingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this);
        LayoutInflater inflater = LayoutInflater.from(UserLoginActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_pending, null);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void openRegisterPage(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void openResetPage(View view) {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }
}
