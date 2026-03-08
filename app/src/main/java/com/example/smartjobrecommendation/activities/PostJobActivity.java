package com.example.smartjobrecommendation.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;

public class PostJobActivity extends BaseActivity {

    EditText title, desc, skills, exp, salary;
    Button postBtn;
    Spinner degreeSpinner;
    DatabaseHelper db;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_job);

        // Initialize all views
        initViews();

        // Setup spinner with data
        setupDegreeSpinner();

        // Initialize database
        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        // Set click listener
        postBtn.setOnClickListener(v -> postJob());
    }

    private void initViews() {
        title = findViewById(R.id.jobTitle);
        desc = findViewById(R.id.jobDesc);
        skills = findViewById(R.id.jobSkills);
        exp = findViewById(R.id.jobExp);
        salary = findViewById(R.id.jobSalary);
        degreeSpinner = findViewById(R.id.degreeSpinner);
        postBtn = findViewById(R.id.postBtn);
    }

    private void setupDegreeSpinner() {
        // List of common degrees
        String[] degrees = {
                "Any Degree",
                "10th Pass",
                "12th Pass",
                "Diploma",
                "B.Sc",
                "B.Com",
                "B.A",
                "BCA",
                "BBA",
                "B.Tech/B.E",
                "M.Sc",
                "M.Com",
                "M.A",
                "MCA",
                "MBA",
                "M.Tech",
                "PhD"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                degrees
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        degreeSpinner.setAdapter(adapter);
    }

    private void postJob() {
        // Get text from EditTexts
        String titleText = title.getText().toString().trim();
        String descText = desc.getText().toString().trim();
        String skillsText = skills.getText().toString().trim();
        String expText = exp.getText().toString().trim();
        String salaryText = salary.getText().toString().trim();

        // Validate all fields
        if (TextUtils.isEmpty(titleText)) {
            title.setError("Job title is required");
            title.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(descText)) {
            desc.setError("Job description is required");
            desc.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(skillsText)) {
            skills.setError("Required skills are required");
            skills.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(expText)) {
            exp.setError("Required experience is required");
            exp.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(salaryText)) {
            salary.setError("Salary is required");
            salary.requestFocus();
            return;
        }

        // Get selected degree
        String mandatoryDegree = degreeSpinner.getSelectedItem().toString();

        // Post job with all details
        boolean inserted = db.postJob(
                userId,
                titleText,
                descText,
                skillsText,
                expText,
                salaryText,
                mandatoryDegree
        );

        if (inserted) {
            Toast.makeText(this, "Job Posted Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to post job. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}