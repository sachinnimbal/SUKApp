package com.example.sukappusers.UserSection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;

import java.util.Objects;

public class UserDocumentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_documents);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Documents");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    public void openOldQP(View view) {
        startActivity(new Intent(this, UserOldqpActivity.class));
    }

}