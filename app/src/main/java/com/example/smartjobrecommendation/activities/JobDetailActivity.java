package com.example.smartjobrecommendation.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;
import com.example.smartjobrecommendation.utils.SkillMatcher;

public class JobDetailActivity extends BaseActivity {

    TextView title, desc, skills, salary, matchView, degreeView;
    Button applyBtn;
    DatabaseHelper db;

    int jobId, userId;
    boolean isAlreadyApplied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        initViews();

        db = new DatabaseHelper(this);

        jobId = getIntent().getIntExtra("jobId", -1);
        userId = getIntent().getIntExtra("userId", -1);


        loadJobDetails();
        checkIfAlreadyApplied();

        applyBtn.setOnClickListener(v -> applyForJob());
    }

    private void initViews() {
        title = findViewById(R.id.detailTitle);
        desc = findViewById(R.id.detailDesc);
        skills = findViewById(R.id.detailSkills);
        salary = findViewById(R.id.detailSalary);
        matchView = findViewById(R.id.detailMatch);
        degreeView = findViewById(R.id.detailDegree);
        applyBtn = findViewById(R.id.applyBtn);
    }

    private void loadJobDetails() {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT * FROM jobs WHERE jobId=?",
                new String[]{String.valueOf(jobId)}
        );

        if (cursor.moveToFirst()) {
            String jobTitle = cursor.getString(cursor.getColumnIndexOrThrow("jobTitle"));
            String jobDesc = cursor.getString(cursor.getColumnIndexOrThrow("jobDesc"));
            String jobSkills = cursor.getString(cursor.getColumnIndexOrThrow("requiredSkills"));
            String jobSalary = cursor.getString(cursor.getColumnIndexOrThrow("salary"));
            String mandatoryDegree = cursor.getString(cursor.getColumnIndexOrThrow("mandatoryDegree"));

            title.setText(jobTitle);
            desc.setText("Description: " + jobDesc);
            skills.setText("Required Skills: " + jobSkills);
            salary.setText("Salary: ₹" + jobSalary);
            degreeView.setText("Required Degree: " + (mandatoryDegree != null ? mandatoryDegree : "Any Degree"));

            String userSkills = db.getUserSkills(userId);
            int match = SkillMatcher.calculateMatch(userSkills, jobSkills);

            matchView.setText("Match: " + match + "% "
                    + (match >= 70 ? "(High Match)" : "(Low Match)"));
        }
        cursor.close();
    }

    private void checkIfAlreadyApplied() {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT * FROM applications WHERE jobId=? AND userId=?",
                new String[]{String.valueOf(jobId), String.valueOf(userId)}
        );

        isAlreadyApplied = cursor.getCount() > 0;
        cursor.close();

        if (isAlreadyApplied) {
            applyBtn.setText("Already Applied");
            applyBtn.setEnabled(false);
            applyBtn.setAlpha(0.5f);
        }
    }

    private void applyForJob() {
        if (isAlreadyApplied) return;

        applyBtn.setEnabled(false);

        boolean applied = db.applyJob(jobId, userId);

        if (applied) {
            Toast.makeText(this, "Applied Successfully!", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("jobApplied", true);
            resultIntent.putExtra("jobId", jobId);
            setResult(RESULT_OK, resultIntent);

            finish();
        } else {
            Toast.makeText(this, "Failed to apply!", Toast.LENGTH_SHORT).show();
            applyBtn.setEnabled(true);
        }
    }
}