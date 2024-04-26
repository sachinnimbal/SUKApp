package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserCheckStatusActivity extends BaseActivity {

    private DatabaseReference complaintRef;
    private DatabaseReference closedComplaintRef;
    private DatabaseReference removedComplaintRef;
    TextView referenceNumberTextView;
    TextView subjectTextView;
    TextView descriptionTextView;
    TextView batch_view;
    TextView usn_view;
    TextView name_view;
    TextView datetimeTextView, noDataTextView;
    TextView statusTextView;
    private CircularProgressIndicator circularProgressIndicator;
    private LinearProgressIndicator toolbarProgressBar;
    private MaterialCardView cardView, helpCard;
    TextView updatedTimeTextView;
    TextView updatedDateTextView;
    TextView responseTextView, help_text;
    ImageView view_icon;
    private Complaint selectedComplaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check_status);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Complaint Status");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        SearchView searchView = findViewById(R.id.searchView);
        ExtendedFloatingActionButton extended_fab = findViewById(R.id.extended_fab);
        referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        subjectTextView = findViewById(R.id.subjectTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        batch_view = findViewById(R.id.batch_view);
        name_view = findViewById(R.id.name_view);
        usn_view = findViewById(R.id.usn_view);
        datetimeTextView = findViewById(R.id.datetimeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        circularProgressIndicator = findViewById(R.id.circularProgress);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);
        cardView = findViewById(R.id.CardView);
        helpCard = findViewById(R.id.helpCard);
        noDataTextView = findViewById(R.id.noDataTextView);
        responseTextView = findViewById(R.id.responseTextView);
        updatedTimeTextView = findViewById(R.id.updatedTimeTextView);
        updatedDateTextView = findViewById(R.id.updatedDateTextView);
        help_text = findViewById(R.id.help_text);
        view_icon = findViewById(R.id.view_icon);
        searchView.setQueryHint("Search: SUK1234");

        // Initialize database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        complaintRef = firebaseDatabase.getReference("Complaints");
        closedComplaintRef = firebaseDatabase.getReference("ClosedComplaints");
        removedComplaintRef = firebaseDatabase.getReference("RemovedComplaints");
        extended_fab.setOnClickListener(view -> startActivity(new Intent(this, UserComplaintActivity.class)));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchComplaint(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        cardView.setOnClickListener(view -> {
            if (selectedComplaint != null) {
                showAlertDialog(view, selectedComplaint);
                help_text.setVisibility(View.GONE);
                helpCard.setVisibility(View.GONE);
            } else {
                help_text.setVisibility(View.GONE);
                helpCard.setVisibility(View.GONE);
                Toast.makeText(this, "No complaint found", Toast.LENGTH_SHORT).show();
            }
        });
        view_icon.setOnClickListener(v -> {
            if (selectedComplaint != null) {
                showAlertDialog(v, selectedComplaint);
                help_text.setVisibility(View.GONE);
                helpCard.setVisibility(View.GONE);
            } else {
                help_text.setVisibility(View.GONE);
                helpCard.setVisibility(View.GONE);
                Toast.makeText(this, "No complaint found", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showAlertDialog(View v, Complaint complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Complaint Details");

        // Inflate custom layout for the AlertDialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_view_complaint, null);

        TextView date_view = dialogView.findViewById(R.id.date_view);
        TextView updated_date_view = dialogView.findViewById(R.id.updated_date_view);
        TextView name_view = dialogView.findViewById(R.id.name_view);
        TextView status = dialogView.findViewById(R.id.statusTextView);
        TextView batch_view = dialogView.findViewById(R.id.batch_view);
        TextView usnTextView = dialogView.findViewById(R.id.usn_view);
        TextView subjectTextView = dialogView.findViewById(R.id.subjectTextView);
        TextView descriptionTextView = dialogView.findViewById(R.id.descriptionTextView);
        TextView responseTextView = dialogView.findViewById(R.id.responseTextView);

        name_view.setText("Name: " + complaint.getFullName());
        status.setText(complaint.getStatus());
        status.setAllCaps(true);
        batch_view.setText("Batch: " + complaint.getBatchYear());
        usnTextView.setText("USN: " + complaint.getUsn());
        subjectTextView.setText("Subject Name: " + complaint.getSubject());
        descriptionTextView.setText("Description: " + complaint.getDescription());
        responseTextView.setText("Response: " + complaint.getResponse());
        date_view.setText("Registered: " + " " + complaint.getDate() + " | " + complaint.getTime());
        updated_date_view.setText("Responded: " + complaint.getUpdatedDate() + " | " + complaint.getUpdatedTime());
        // Set the custom layout to the AlertDialog
        builder.setView(dialogView);

        // Set background drawable based on complaint status
        if (complaint.getStatus().equals("Active")) {
            status.setBackgroundResource(R.drawable.corner_green); // Set green complaint_corner drawable
        } else if (complaint.getStatus().equals("Closed")) {
            status.setBackgroundResource(R.drawable.corner_red); // Set red complaint_corner drawable
        }

        // Set positive button and its click listener
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Handle positive button click if needed
            dialog.dismiss();
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void searchComplaint(String referenceNumber) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        help_text.setVisibility(View.GONE);
        helpCard.setVisibility(View.GONE);

        // Search in "Complaints" node
        complaintRef.orderByChild("referenceNumber").equalTo(referenceNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        circularProgressIndicator.setVisibility(View.GONE);
                        toolbarProgressBar.setVisibility(View.GONE);
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot complaintSnapshot : dataSnapshot.getChildren()) {
                                Complaint complaint = complaintSnapshot.getValue(Complaint.class);
                                if (complaint != null) {
                                    selectedComplaint = complaint; // Assign the complaint to the instance variable
                                    displayComplaintDetails(complaint);
                                    return; // Exit the loop after displaying the first matching complaint
                                }

                            }

                        } else {
                            // If not found in "Complaints" node, search in "ClosedComplaints" node
                            searchClosedComplaint(referenceNumber);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Error occurred while querying the database
                        circularProgressIndicator.setVisibility(View.GONE);
                        toolbarProgressBar.setVisibility(View.GONE);
                    }
                });


    }

    private void searchClosedComplaint(String referenceNumber) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        help_text.setVisibility(View.GONE);
        helpCard.setVisibility(View.GONE);

        closedComplaintRef.orderByChild("referenceNumber").equalTo(referenceNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        circularProgressIndicator.setVisibility(View.GONE);
                        toolbarProgressBar.setVisibility(View.GONE);
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot complaintSnapshot : dataSnapshot.getChildren()) {
                                Complaint complaint = complaintSnapshot.getValue(Complaint.class);
                                if (complaint != null) {
                                    selectedComplaint = complaint; // Assign the complaint to the instance variable
                                    displayComplaintDetails(complaint);
                                    return; // Exit the loop after displaying the first matching complaint
                                }
                            }
                        } else{
                            searchRemovedComplaint(referenceNumber);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Error occurred while querying the database
                        circularProgressIndicator.setVisibility(View.GONE);
                        toolbarProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void searchRemovedComplaint(String referenceNumber) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        help_text.setVisibility(View.GONE);
        helpCard.setVisibility(View.GONE);

        removedComplaintRef.orderByChild("referenceNumber").equalTo(referenceNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        circularProgressIndicator.setVisibility(View.GONE);
                        toolbarProgressBar.setVisibility(View.GONE);
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot complaintSnapshot : dataSnapshot.getChildren()) {
                                Complaint complaint = complaintSnapshot.getValue(Complaint.class);
                                if (complaint != null) {
                                    selectedComplaint = complaint; // Assign the complaint to the instance variable
                                    displayComplaintDetails(complaint);
                                    return; // Exit the loop after displaying the first matching complaint
                                }
                            }
                        } else {
                            // No matching complaint found in "ClosedComplaints" node
                            cardView.setVisibility(View.INVISIBLE);
                            noDataTextView.setVisibility(View.VISIBLE);
                            clearComplaintDetails();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Error occurred while querying the database
                        circularProgressIndicator.setVisibility(View.GONE);
                        toolbarProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void displayComplaintDetails(Complaint complaint) {
        cardView.setVisibility(View.VISIBLE);
        noDataTextView.setVisibility(View.GONE);
        help_text.setVisibility(View.GONE);
        helpCard.setVisibility(View.GONE);

        referenceNumberTextView.setText("Ref No.: " + complaint.getReferenceNumber());
        subjectTextView.setText("Subject: " + complaint.getSubject());
        name_view.setText("Name: " + complaint.getFullName());
        usn_view.setText("USN: " + complaint.getUsn());
        batch_view.setText("Batch: " + complaint.getBatchYear());
        descriptionTextView.setText("Description: " + complaint.getDescription());
        datetimeTextView.setText(complaint.getDate() + " | " + complaint.getTime());
        statusTextView.setText(complaint.getStatus());
        responseTextView.setText("Response: " + complaint.getResponse());
        updatedTimeTextView.setText(complaint.getUpdatedTime());
        updatedDateTextView.setText("Responded: " + complaint.getUpdatedDate());
        // Set background drawable based on complaint status
        if (complaint.getStatus().equals("Active")) {
            statusTextView.setBackgroundResource(R.drawable.corner_green); // Set green complaint_corner drawable
        } else if (complaint.getStatus().equals("Closed")) {
            statusTextView.setBackgroundResource(R.drawable.corner_red); // Set red complaint_corner drawable
        }
    }

    private void clearComplaintDetails() {
        referenceNumberTextView.setText("");
        subjectTextView.setText("");
        descriptionTextView.setText("");
        datetimeTextView.setText("");
        statusTextView.setText("");
        updatedDateTextView.setText("");
        updatedTimeTextView.setText("");
        responseTextView.setText("");
    }

}
