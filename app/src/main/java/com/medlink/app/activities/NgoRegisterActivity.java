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

public class NgoRegisterActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo_register);

        authRepository = new AuthRepository();

        EditText etNgoName = findViewById(R.id.et_ngo_name_reg);
        EditText etEmail = findViewById(R.id.et_email_ngo_reg);
        EditText etPassword = findViewById(R.id.et_password_ngo_reg);
        TextView btnRegister = findViewById(R.id.btn_register_ngo);

        btnRegister.setOnClickListener(v -> {
            String ngoName = etNgoName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (ngoName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(NgoRegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(NgoRegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setText("Creating NGO Account...");
            btnRegister.setEnabled(false);

            authRepository.registerNgo(ngoName, email, password, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(NgoRegisterActivity.this, "NGO Registration Successful", Toast.LENGTH_SHORT).show();
                    // Route to NgoActivity
                    Intent intent = new Intent(NgoRegisterActivity.this, NgoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(NgoRegisterActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnRegister.setText("Register NGO");
                    btnRegister.setEnabled(true);
                }
            });
        });

        findViewById(R.id.tv_login_ngo_link).setOnClickListener(v -> {
            finish(); // Go back to NgoLoginActivity
        });
    }
}
