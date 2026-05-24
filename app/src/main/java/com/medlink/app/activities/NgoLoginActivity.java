package com.medlink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.medlink.app.R;
import com.medlink.app.repository.AuthRepository;

public class NgoLoginActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_login);

        authRepository = new AuthRepository();

        EditText etEmail = findViewById(R.id.et_email_ngo);
        EditText etPassword = findViewById(R.id.et_password_ngo);
        TextView btnLogin = findViewById(R.id.btn_login_ngo);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(NgoLoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setText("Signing In...");
            btnLogin.setEnabled(false);

            authRepository.login(email, password, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(NgoLoginActivity.this, "NGO Login Successful", Toast.LENGTH_SHORT).show();
                    // We will route to NgoActivity here
                    Intent intent = new Intent(NgoLoginActivity.this, NgoActivity.class);
                    // To prevent going back to login
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(NgoLoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnLogin.setText("Sign In");
                    btnLogin.setEnabled(true);
                }
            });
        });

        findViewById(R.id.tv_register_ngo_link).setOnClickListener(v -> {
            startActivity(new Intent(NgoLoginActivity.this, NgoRegisterActivity.class));
        });

        findViewById(R.id.tv_user_login_link).setOnClickListener(v -> {
            finish(); // Go back to LoginActivity
        });
    }
}
