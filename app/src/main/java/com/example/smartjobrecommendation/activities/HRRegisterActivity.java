package com.example.smartjobrecommendation.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;

public class HRRegisterActivity extends BaseActivity {

    EditText name, companyName, aboutCompany, phone, email, password;
    Spinner companyTypeSpinner;
    Button registerBtn;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrregister);

        name = findViewById(R.id.hrName);
        companyName = findViewById(R.id.companyName);
        aboutCompany = findViewById(R.id.aboutCompany);
        phone = findViewById(R.id.hrPhone);
        email = findViewById(R.id.hrEmail);
        password = findViewById(R.id.hrPassword);
        companyTypeSpinner = findViewById(R.id.companyTypeSpinner);
        registerBtn = findViewById(R.id.hrRegisterBtn);

        db = new DatabaseHelper(this);

        // Spinner Data
        String[] types = {"Select Company Type", "IT", "Non-IT", "Healthcare", "Education", "Finance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companyTypeSpinner.setAdapter(adapter);

        registerBtn.setOnClickListener(v -> registerHR());
    }

    private void registerHR() {

        String hrName = name.getText().toString().trim();
        String compName = companyName.getText().toString().trim();
        String about = aboutCompany.getText().toString().trim();
        String hrPhone = phone.getText().toString().trim();
        String hrEmail = email.getText().toString().trim();
        String hrPass = password.getText().toString().trim();
        String companyType = companyTypeSpinner.getSelectedItem().toString();

        // Validations
        if (TextUtils.isEmpty(hrName)) {
            name.setError("Name required");
            name.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(compName)) {
            companyName.setError("Company name required");
            companyName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(about)) {
            aboutCompany.setError("About company required");
            aboutCompany.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(hrPhone)) {
            phone.setError("Phone number required");
            phone.requestFocus();
            return;
        }

        if (hrPhone.length() != 10) {
            phone.setError("Enter valid 10 digit phone number");
            phone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(hrEmail)) {
            email.setError("Email required");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(hrEmail).matches()) {
            email.setError("Invalid email format");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(hrPass)) {
            password.setError("Password required");
            password.requestFocus();
            return;
        }

        if (hrPass.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return;
        }

        if (companyType.equals("Select Company Type")) {
            Toast.makeText(this, "Please select company type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Using the overloaded method (without qualificationDetails)
        boolean inserted = db.registerUser(
                "HR",                    // role
                hrName,                  // name
                compName,                // companyName
                about,                   // aboutCompany
                hrPhone,                 // phone
                hrEmail,                 // email
                hrPass,                  // password
                "",                      // qualification (empty for HR)
                "",                      // skills (empty for HR)
                "",                      // experience (empty for HR)
                "",                      // photo (empty for HR)
                "",                      // resume (empty for HR)
                companyType              // companyType
        );

        if (inserted) {
            Toast.makeText(this, "HR Registration Successful! Please login.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            // Check if email already exists
            Cursor cursor = db.loginUser(hrEmail, hrPass);
            if (cursor != null && cursor.getCount() > 0) {
                Toast.makeText(this, "Email already registered! Please use different email.", Toast.LENGTH_LONG).show();
                cursor.close();
            } else {
                Toast.makeText(this, "Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}