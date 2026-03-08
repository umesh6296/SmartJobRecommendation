package com.example.smartjobrecommendation.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.adapters.JobAdapter;
import com.example.smartjobrecommendation.database.DatabaseHelper;

import java.util.ArrayList;

public class HRProfileActivity extends BaseActivity {

    TextView profileInfo, jobCountText;
    RecyclerView recyclerView;
    DatabaseHelper db;
    int userId;

    ArrayList<String> jobTitles = new ArrayList<>();
    ArrayList<Integer> jobIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrprofile);

        try {
            initViews();
            loadProfile();
            loadPostedJobs();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        profileInfo = findViewById(R.id.hrProfileInfo);
        jobCountText = findViewById(R.id.jobCountText);
        recyclerView = findViewById(R.id.hrPostedJobsRecycler);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProfile() {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT name, companyName, aboutCompany, companyType FROM users WHERE id=?",
                    new String[]{String.valueOf(userId)}
            );

            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                String company = cursor.getString(1);
                String about = cursor.getString(2);
                String type = cursor.getString(3);

                profileInfo.setText(
                        "👤 HR: " + name +
                                "\n🏢 Company: " + company +
                                "\n📊 Type: " + type +
                                "\n📝 About: " + about
                );
            } else {
                profileInfo.setText("No profile data found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            profileInfo.setText("Error loading profile");
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadPostedJobs() {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT jobId, jobTitle, jobDesc, requiredSkills, requiredExp, salary FROM jobs WHERE hrId=?",
                    new String[]{String.valueOf(userId)}
            );

            jobTitles.clear();
            jobIds.clear();

            // Create ArrayLists for all job details
            ArrayList<String> jobDescList = new ArrayList<>();
            ArrayList<String> jobSkillsList = new ArrayList<>();
            ArrayList<String> jobExpList = new ArrayList<>();
            ArrayList<String> jobSalaryList = new ArrayList<>();


            while (cursor.moveToNext()) {
                jobIds.add(cursor.getInt(0));
                jobTitles.add(cursor.getString(1));
                jobDescList.add(cursor.getString(2) != null ? cursor.getString(2) : "");
                jobSkillsList.add(cursor.getString(3) != null ? cursor.getString(3) : "");
                jobExpList.add(cursor.getString(4) != null ? cursor.getString(4) : "");
                jobSalaryList.add(cursor.getString(5) != null ? cursor.getString(5) : "");
            }

            jobCountText.setText("📋 Total Jobs Posted: " + jobTitles.size());

            if (jobTitles.size() > 0) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                JobAdapter adapter = new JobAdapter(
                        this,
                        jobTitles,
                        jobIds,
                        jobDescList,
                        jobSkillsList,
                        jobExpList,
                        jobSalaryList,
                        "HR",
                        userId
                );
                recyclerView.setAdapter(adapter);
            } else {

                TextView emptyView = new TextView(this);
                emptyView.setText("No jobs posted yet");
                emptyView.setTextSize(16);
                emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                emptyView.setPadding(0, 20, 0, 20);
                emptyView.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));


                recyclerView.setAdapter(null);

            }

        } catch (Exception e) {
            e.printStackTrace();
            jobCountText.setText("Error loading jobs");
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}