package com.example.smartjobrecommendation.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;


public class LoginActivity extends BaseActivity {
    EditText email, password;
    Button loginBtn, registerBtn;
    DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginButton);
        registerBtn = findViewById(R.id.registerButton);

        db = new DatabaseHelper(this);

        loginBtn.setOnClickListener(v -> loginUser());

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
    private void loginUser() {

        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Invalid email");
            return;
        }

        if (TextUtils.isEmpty(userPass)) {
            password.setError("Password required");
            return;
        }

        Cursor cursor = db.loginUser(userEmail, userPass);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("role", role);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }
}