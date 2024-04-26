package com.example.sukappusers.UserSection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserPassingCriteria extends BaseActivity {

    private DatabaseReference passingCriteriaRef;
    private TextView degreeNameTextView;
    private TextView passingCriteriaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_passing_criteria);

        // Initialize DatabaseReference
        passingCriteriaRef = FirebaseDatabase.getInstance().getReference("PassingCriteria");

        // Initialize TextViews
        degreeNameTextView = findViewById(R.id.degreeName);
        passingCriteriaTextView = findViewById(R.id.PassingCriteria);

        degreeNameTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        passingCriteriaTextView.setTextColor(ContextCompat.getColor(this, R.color.black));

        //Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Passing Criteria");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Fetch and display data
        fetchPassingCriteria();
    }

    private void fetchPassingCriteria() {
        passingCriteriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the degree and passing criteria values
                    String degreeName = dataSnapshot.child("degreeName").getValue(String.class);
                    String passingCriteria = dataSnapshot.child("passingCriteria").getValue(String.class);

                    // Update the TextViews with the retrieved data
                    degreeNameTextView.setText(degreeName);
                    passingCriteriaTextView.setText(passingCriteria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error occurred while fetching data
                Toast.makeText(getApplicationContext(), "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
