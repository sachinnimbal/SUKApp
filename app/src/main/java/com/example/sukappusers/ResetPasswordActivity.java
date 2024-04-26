package com.example.sukappusers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ResetPasswordActivity extends BaseActivity {

    private EditText reset_email_field;
    private String email;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        auth = FirebaseAuth.getInstance();
        reset_email_field = findViewById(R.id.reset_email_field);
        Button reset_button = findViewById(R.id.reset_button);

        reset_button.setOnClickListener(v -> validateEmail());
    }

    private void validateEmail() {
        email = reset_email_field.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            reset_email_field.setError("Email is required");
            reset_email_field.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            reset_email_field.setError("Invalid email format");
            reset_email_field.requestFocus();
        }else{
            forgotten();
        }
    }


    private void forgotten() {
        progressDialog = ProgressDialog.show(ResetPasswordActivity.this, "", "Sending password reset email...", true);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(ResetPasswordActivity.this, "Check your Email", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, UserLoginActivity.class));
                        finish();
                    }else{
                        Toast.makeText(ResetPasswordActivity.this, "Error :"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        reset_email_field.setText("");
                        reset_email_field.requestFocus();
                    }
                });
    }
    public void openBackToLoginPage(View view) {
        startActivity(new Intent(this, UserLoginActivity.class));
        finish();
    }
}