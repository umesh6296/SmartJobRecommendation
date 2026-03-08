package com.example.smartjobrecommendation.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.adapters.HRApplicantAdapter;
import com.example.smartjobrecommendation.database.DatabaseHelper;

import java.util.ArrayList;

public class HRJobDetailActivity extends BaseActivity {

    TextView jobTitle, jobDesc, jobSkills;
    RecyclerView recyclerView;
    DatabaseHelper db;
    int jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrjob_detail);

        jobTitle = findViewById(R.id.detailTitle);
        jobDesc = findViewById(R.id.detailDesc);
        jobSkills = findViewById(R.id.detailSkills);
        recyclerView = findViewById(R.id.applicantRecycler);

        db = new DatabaseHelper(this);
        jobId = getIntent().getIntExtra("jobId", -1);

        loadJobDetails();
        loadApplicants();
    }

    private void loadJobDetails() {

        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT jobTitle, jobDesc, jobSkills FROM jobs WHERE jobId=?",
                new String[]{String.valueOf(jobId)}
        );

        if (cursor.moveToFirst()) {
            jobTitle.setText(cursor.getString(0));
            jobDesc.setText(cursor.getString(1));
            jobSkills.setText("Skills: " + cursor.getString(2));
        }

        cursor.close();
    }

    private void loadApplicants() {

        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT users.id, users.name, users.skills, users.resume " +
                        "FROM applications " +
                        "JOIN users ON applications.userId = users.id " +
                        "WHERE applications.jobId=?",
                new String[]{String.valueOf(jobId)}
        );

        ArrayList<Integer> userIds = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> skills = new ArrayList<>();
        ArrayList<String> resumes = new ArrayList<>();

        while (cursor.moveToNext()) {

            userIds.add(cursor.getInt(0));
            names.add(cursor.getString(1));
            skills.add(cursor.getString(2));
            resumes.add(cursor.getString(3));
        }
        cursor.close();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(
                new HRApplicantAdapter(
                        this,
                        jobId,
                        userIds,
                        names,
                        skills,
                        resumes
                )
        );
    }
}