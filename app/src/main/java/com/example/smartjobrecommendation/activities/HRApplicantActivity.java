package com.example.smartjobrecommendation.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;
import java.util.ArrayList;

public class HRApplicantActivity extends BaseActivity {

    TextView jobTitle, jobDesc;
    RecyclerView recyclerView;
    DatabaseHelper db;
    int jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrapplicant);

        jobTitle = findViewById(R.id.jobTitle);
        jobDesc = findViewById(R.id.jobDesc);
        recyclerView = findViewById(R.id.applicantRecycler);

        db = new DatabaseHelper(this);
        jobId = getIntent().getIntExtra("jobId", -1);

        loadJobDetails();
        loadApplicants();
    }

    private void loadJobDetails() {

        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT jobTitle, jobDesc FROM jobs WHERE jobId=?",
                new String[]{String.valueOf(jobId)}
        );

        if (cursor.moveToFirst()) {
            jobTitle.setText(cursor.getString(0));
            jobDesc.setText(cursor.getString(1));
        }
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(
                new com.example.smartjobrecommendation.adapters.HRApplicantAdapter(
                        this, jobId, userIds, names, skills, resumes
                ));
    }
}