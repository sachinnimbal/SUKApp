package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.Constants;
import com.example.sukappusers.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class UserComplaintActivity extends BaseActivity {
    private TextInputEditText subjectEditText;
    private TextInputEditText subjectDescEditText;
    private DatabaseReference complaintRef;
    Button SaveButton;
    ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private TextView referenceNumberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_complaint);

        db = FirebaseFirestore.getInstance();

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Complaint Form");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        subjectEditText = findViewById(R.id.subjectEditText);
        subjectDescEditText = findViewById(R.id.subjectDescEditText);
        SaveButton = findViewById(R.id.SaveButton);
        referenceNumberText = findViewById(R.id.reference_number_text);
        TextInputLayout subjectDescTextInputLayout = findViewById(R.id.subjectDescTextInputLayout);
        TextInputLayout subjectTextInputLayout = findViewById(R.id.subjectTextInputLayout);

        subjectTextInputLayout.setCounterEnabled(true);
        subjectTextInputLayout.setHelperText(getResources().getString(R.string.character_count_min_24));

        subjectDescTextInputLayout.setCounterEnabled(true);
        subjectDescTextInputLayout.setHelperText(getResources().getString(R.string.character_count_min_300));


        // Initialize database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        complaintRef = firebaseDatabase.getReference("Complaints");

        SaveButton.setOnClickListener(view -> saveData());
    }

    @SuppressLint("SetTextI18n")
    private void saveData() {
        String subject = Objects.requireNonNull(subjectEditText.getText()).toString();
        String description = Objects.requireNonNull(subjectDescEditText.getText()).toString();

        if (subject.isEmpty()) {
            subjectEditText.setError("Please enter a subject name");
            subjectEditText.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            subjectDescEditText.setError("Please enter a description");
            subjectDescEditText.requestFocus();
            return;
        }

        progressDialog = new ProgressDialog(UserComplaintActivity.this);
        progressDialog.setMessage("Sending complaint...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String usn = documentSnapshot.getString("usn");
                    String batchYear = documentSnapshot.getString("batchYear");

                    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date());

                    String status = "Active";

                    // Generate a unique key for the complaint in the "Complaint" node
                    String complaintKey = complaintRef.push().getKey();


                    String referenceNumber = "SUK" + generateRandomNumber();

                    // Create a new complaint instance
                    Complaint complaint = new Complaint(complaintKey, subject, description, fullName, usn, batchYear, currentDate, currentTime, status, userId, referenceNumber);

                    complaint.setUserId(userId);
                    FirebaseMessaging.getInstance().subscribeToTopic(userId)
                            .addOnCompleteListener(Task::isSuccessful);

                    // Save the complaint to the real-time database
                    assert batchYear != null;
                    assert complaintKey != null;
                    complaintRef.child(complaintKey).setValue(complaint)
                            .addOnSuccessListener(aVoid -> {
                                // Send push notification to admin
                                sendPushNotificationToAdmin(fullName, usn, complaintKey);
                                progressDialog.dismiss();
                                Toast.makeText(UserComplaintActivity.this, "Complaint sent successfully", Toast.LENGTH_SHORT).show();
                                subjectEditText.setText("");
                                subjectDescEditText.setText("");

                                TextView complaintTextView = findViewById(R.id.Complaint_textView);
                                complaintTextView.setVisibility(View.VISIBLE);

                                referenceNumberText.setVisibility(View.VISIBLE);
                                referenceNumberText.setText("Reference Number: " + referenceNumber);
                                ExtendedFloatingActionButton referenceNumberFab = findViewById(R.id.reference_number_text);
                                referenceNumberFab.setOnClickListener(v -> {
                                    // Copy the reference number to the clipboard
                                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clipData = ClipData.newPlainText("Reference Number", referenceNumber);
                                    clipboardManager.setPrimaryClip(clipData);

                                    // Hide the reference number and show a Toast indicating the number has been copied
                                    referenceNumberText.setVisibility(View.VISIBLE);
                                    Toast.makeText(UserComplaintActivity.this, "Reference number copied to clipboard", Toast.LENGTH_SHORT).show();
                                    finish();
                                });


                                SaveButton.setEnabled(false);

                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(UserComplaintActivity.this, "Failed to send complaint. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }
    }

    private int generateRandomNumber() {
        Random random = new Random();
        int min = (int) Math.pow(10, 4 - 1); // Minimum value for the specified number of digits
        int max = (int) Math.pow(10, 4) - 1; // Maximum value for the specified number of digits
        return random.nextInt(max - min + 1) + min;
    }

    private void sendPushNotificationToAdmin(String fullName, String usn, String userId) {
        // Create the JSON payload for the notification
        JSONObject notification = new JSONObject();
        try {
            notification.put("title", "New Complaint");
            notification.put("body", "A new complaint has been registered by a user: " + fullName + " (USN: " + usn + ")");
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
        String url = Constants.BASE_URL + "/fcm/send"; // FCM API endpoint
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                response -> {
                    // Notification sent successfully
                    Toast.makeText(UserComplaintActivity.this, "Notification sent to admin", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Error sending notification
                    Toast.makeText(UserComplaintActivity.this, "Failed to send notification to admin", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set the Authorization header with your FCM server key
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "key=" + Constants.SERVER_KEY);
                headers.put("Content-Type", Constants.CONTENT_TYPE);
                return headers;
            }
        };

        // Add the request to the request queue
        Volley.newRequestQueue(this).add(request);
    }

}
