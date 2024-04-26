package com.example.sukappusers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends BaseActivity {
    TextInputEditText usn, fullName, email, password, phone, parentNameEditText, parentPhoneEditText,
            addressEditText;
    Button register;
    FirebaseAuth fAuth;
    ProgressDialog progressDialog;
    private String deviceId;
    RadioGroup genderRadioGroup;
    private String deviceModel;
    String ipAddress = getDeviceIpAddress();
    ImageView bottomSheet;
    private boolean isFirstVisit = true;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usn = findViewById(R.id.usn_text);
        fullName = findViewById(R.id.fullNameText);
        email = findViewById(R.id.email_text);
        password = findViewById(R.id.password_text);
        register = findViewById(R.id.register_Button);
        phone = findViewById(R.id.phone_text);
        parentNameEditText = findViewById(R.id.parentNameEditText);
        parentPhoneEditText = findViewById(R.id.parentPhoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        bottomSheet = findViewById(R.id.open_Sheet);
        fAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Registering your account...");
        progressDialog.setCancelable(false);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        register.setOnClickListener(v -> {
            String iUSN = Objects.requireNonNull(usn.getText()).toString().trim();
            String iPhoneNumber = Objects.requireNonNull(phone.getText()).toString().trim();
            String iFullName = Objects.requireNonNull(fullName.getText()).toString().trim();
            String iEmail = Objects.requireNonNull(email.getText()).toString().trim();
            String iPassword = Objects.requireNonNull(password.getText()).toString().trim();
            String iParentName = Objects.requireNonNull(parentNameEditText.getText()).toString().trim();
            String iParentPhone = Objects.requireNonNull(parentPhoneEditText.getText()).toString().trim();
            String iAddress = Objects.requireNonNull(addressEditText.getText()).toString().trim();

            String usnPattern = "^[A-Z]{2}\\d{2}[A-Z]{3}\\d{3}$";

            if (TextUtils.isEmpty(iUSN)) {
                usn.setError("USN is required");
                usn.requestFocus();
                return;
            } else if (!iUSN.matches(usnPattern)) {
                usn.setError("Invalid USN format");
                usn.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(iPhoneNumber)) {
                phone.setError("Phone number is required");
                phone.requestFocus();
                return;
            } else if (!Patterns.PHONE.matcher(iPhoneNumber).matches() || iPhoneNumber.length() != 10) {
                phone.setError("Invalid phone number format");
                phone.requestFocus();
                return;
            }else if (!isValidIndianPhoneNumber(iPhoneNumber)) {
                phone.setError("Invalid Indian phone number");
                phone.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(iFullName)) {
                fullName.setError("Full name is required");
                fullName.requestFocus();
                return;
            } else if (!iFullName.matches("^[a-zA-Z\\s]+$")) {
                fullName.setError("Invalid full name format");
                fullName.requestFocus();
                return;
            } else if (iFullName.length() < 6) {
                fullName.setError("Full name must be at least 6 characters");
                fullName.requestFocus();
                return;
            }

            // Get the selected gender
            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String gender;
            if (selectedGenderId == R.id.radioMale) {
                gender = "Male";
            } else if (selectedGenderId == R.id.radioFemale) {
                gender = "Female";
            } else {
                return;
            }

            if (TextUtils.isEmpty(iEmail)) {
                email.setError("Email is required");
                email.requestFocus();
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(iEmail).matches()) {
                email.setError("Invalid email format");
                email.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(iPassword)) {
                password.setError("Password is required");
                password.requestFocus();
                return;
            } else if (iPassword.length() < 6) {
                password.setError("Password must be at least 6 characters");
                password.requestFocus();
                return;
            } else if (!iPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$#@!]).+$")) {
                password.setError("Invalid password format: Abc@$#!123");
                password.requestFocus();
                return;
            }

            progressDialog.show();

            fAuth.createUserWithEmailAndPassword(iEmail, iPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

                            FirebaseMessaging.getInstance().subscribeToTopic("Notice")
                                    .addOnCompleteListener(Task::isSuccessful);

                            FirebaseMessaging.getInstance().subscribeToTopic(userId)
                                    .addOnCompleteListener(Task::isSuccessful);

                            DocumentReference userRef = FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(userId);

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("usn", iUSN);
                            userData.put("phoneNumber", iPhoneNumber);
                            userData.put("fullName", iFullName);
                            userData.put("email", iEmail);
                            userData.put("batchYear", "New Users");
                            userData.put("parentName", TextUtils.isEmpty(iParentName) ? "" : iParentName);
                            userData.put("parentPhone", TextUtils.isEmpty(iParentPhone) ? "" : iParentPhone);
                            userData.put("address", TextUtils.isEmpty(iAddress) ? "" : iAddress);
                            userData.put("status", "pending");
                            userData.put("deviceId", deviceId);
                            userData.put("ipAddress", ipAddress);
                            userData.put("gender", gender);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String currentDate = dateFormat.format(new Date());

                            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                            String currentTime = timeFormat.format(new Date());

                            userData.put("registrationDate", currentDate);
                            userData.put("registrationTime", currentTime);

                            userRef.set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        // Check if USN exists after registration
                                        FirebaseFirestore.getInstance().collection("Users")
                                                .whereEqualTo("usn", iUSN)
                                                .get()
                                                .addOnCompleteListener(usnTask -> {
                                                    if (usnTask.isSuccessful()) {
                                                        QuerySnapshot querySnapshot = usnTask.getResult();
                                                        if (querySnapshot.size() > 1) {
                                                            // Multiple users with the same USN, block the user
                                                            Toast.makeText(RegisterActivity.this, "USN is already registered. User Removed.", Toast.LENGTH_LONG).show();
                                                            // Save user data in the "RemovedUsers" collection
                                                            DocumentSnapshot userSnapshot = querySnapshot.getDocuments().get(0);
                                                            DocumentReference blockedUserRef = FirebaseFirestore.getInstance()
                                                                    .collection("RemovedUsers")
                                                                    .document(userId);

                                                            blockedUserRef.set(Objects.requireNonNull(userSnapshot.getData()))
                                                                    .addOnSuccessListener(blockedUserVoid -> {
                                                                        // Delete the user data from the "Users" collection
                                                                        userRef.delete()
                                                                                .addOnSuccessListener(deleteVoid -> {
                                                                                    FirebaseAuth.getInstance().signOut();
                                                                                    // Send notification to admin
                                                                                    sendNotificationToAdmin(iFullName, iUSN, "User Removed While Registering");
                                                                                    // Show appropriate message to the user
                                                                                    showRegistrationBlockedDialog();
                                                                                })
                                                                                .addOnFailureListener(deleteException -> Toast.makeText(RegisterActivity.this, "Failed to register user. Please try again later.", Toast.LENGTH_LONG).show());
                                                                    })
                                                                    .addOnFailureListener(blockedUserException -> Toast.makeText(RegisterActivity.this, "Failed to register user. Please try again later.", Toast.LENGTH_LONG).show());
                                                        } else {
                                                            // USN is available or belongs to the current user, registration is successful
                                                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                            usn.getText().clear();
                                                            fullName.getText().clear();
                                                            email.getText().clear();
                                                            password.getText().clear();
                                                            phone.getText().clear();
                                                            // Send notification to admin
                                                            sendNotificationToAdmin(iFullName, iUSN, "User Registered Successfully");
                                                            showRegistrationSuccessDialog();
                                                        }
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Failed to register user. Please try again later.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Failed to register user. Please try again later.", Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Failed to register user. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    });

        });

        bottomSheet.setOnClickListener(view -> showBottomDialog());
        bottomSheet.setVisibility(View.GONE);
    }

    private void showBottomDialog() {
        BottomDialogFragment bottomDialogFragment = new BottomDialogFragment();
        bottomDialogFragment.show(getSupportFragmentManager(), bottomDialogFragment.getTag());
    }

    private boolean isValidIndianPhoneNumber(String phoneNumber) {
        // Indian phone number regex pattern
        String pattern = "^[6-9]\\d{9}$";
        return phoneNumber.matches(pattern);
    }

    private void showRegistrationBlockedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_register_blocked, null);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) ->
        {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRegistrationSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_register_success, null);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) ->
        {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
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

    public void openLoginPage(View view) {
        startActivity(new Intent(this, UserLoginActivity.class));
        finish();
    }

    private void sendNotificationToAdmin(String fullName, String usn, String message) {
        // Create the JSON payload for the notification
        JSONObject notification = new JSONObject();
        try {
            notification.put("title", "New User Registered");
            notification.put("body", "The user's Full Name is: " + fullName + " & USN is : " + usn + ". " + message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create the JSON payload for the FCM request
        JSONObject payload = new JSONObject();
        try {
            payload.put("to", "/topics/Admin");
            payload.put("notification", notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send the FCM request
        String url = Constants.BASE_URL + "/fcm/send";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                response -> {
                    // Notification sent successfully
                    // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Error sending notification
                    // Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set the Authorization header with the server key
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "key=" + Constants.SERVER_KEY);
                headers.put("Content-Type", Constants.CONTENT_TYPE);
                return headers;
            }
        };

        // Add the request to the request queue
        Volley.newRequestQueue(RegisterActivity.this).add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFirstVisit) {
//            showBottomDialog();
            isFirstVisit = false;
        }
    }
}
