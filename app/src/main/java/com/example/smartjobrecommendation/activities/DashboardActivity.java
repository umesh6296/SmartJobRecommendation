package com.example.smartjobrecommendation.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.adapters.JobAdapter;
import com.example.smartjobrecommendation.database.DatabaseHelper;

import java.util.ArrayList;

public class DashboardActivity extends BaseActivity {

    RecyclerView recyclerView;
    Button postJobBtn;
    ImageView profileMenu;
    TextView profileName;


    EditText searchEditText;
    ImageView searchButton;
    LinearLayout searchLayout;

    DatabaseHelper db;

    String role;
    int userId;
    String userName = "";

    ArrayList<String> jobList = new ArrayList<>();
    ArrayList<Integer> jobIds = new ArrayList<>();
    ArrayList<String> jobDescList = new ArrayList<>();
    ArrayList<String> jobSkillsList = new ArrayList<>();
    ArrayList<String> jobExpList = new ArrayList<>();
    ArrayList<String> jobSalaryList = new ArrayList<>();

    JobAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        loadUserData();
        setupClickListeners();
        setupSearch();
        loadJobs();
    }
    @Override
    public void onBackPressed() {
        // Navigate to LoginActivity instead of exiting app
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.jobRecycler);
        postJobBtn = findViewById(R.id.postJobBtn);
        profileMenu = findViewById(R.id.profileMenu);
        profileName = findViewById(R.id.profileName);

        // Initialize search views
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchLayout = findViewById(R.id.searchLayout);

        db = new DatabaseHelper(this);

        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("userId", -1);

        if (role != null && role.equals("HR")) {
            postJobBtn.setVisibility(View.VISIBLE);

        }
    }

    private void loadUserData() {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT name FROM users WHERE id=?",
                    new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                userName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                profileName.setText("Welcome, " + userName + "!");
                profileName.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void setupClickListeners() {
        // Three dot menu click
        profileMenu.setOnClickListener(v -> {
            if (role != null && role.equals("JobSeeker")) {
                Intent intent = new Intent(this, JobSeekerProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, HRProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        // Profile name click - same as menu click
        profileName.setOnClickListener(v -> profileMenu.performClick());

        // Post job button
        postJobBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostJobActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void setupSearch() {
        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchJobs(query);
            } else {
                // Agar search empty hai to saari jobs dikhao
                loadAllJobs();
            }
        });

        // Clear search when text is empty
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    loadAllJobs();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchJobs(query);
                } else {
                    loadAllJobs();
                }
                return true;
            }
            return false;
        });
    }
    private void loadJobs() {
        loadAllJobs();
    }

    private void loadAllJobs() {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM jobs", null);

        jobList.clear();
        jobIds.clear();
        jobDescList.clear();
        jobSkillsList.clear();
        jobExpList.clear();
        jobSalaryList.clear();

        while (cursor.moveToNext()) {
            int jobId = cursor.getInt(cursor.getColumnIndexOrThrow("jobId"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("jobTitle"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("jobDesc"));
            String skills = cursor.getString(cursor.getColumnIndexOrThrow("requiredSkills"));
            String exp = cursor.getString(cursor.getColumnIndexOrThrow("requiredExp"));
            String salary = cursor.getString(cursor.getColumnIndexOrThrow("salary"));

            jobIds.add(jobId);
            jobList.add(title);
            jobDescList.add(desc);
            jobSkillsList.add(skills);
            jobExpList.add(exp);
            jobSalaryList.add(salary);
        }
        cursor.close();


        if (adapter != null) {
            adapter.updateData(jobList, jobIds, jobDescList, jobSkillsList, jobExpList, jobSalaryList);
        } else {
            setupAdapter();
        }
    }

    private void searchJobs(String keyword) {
        String searchQuery = "SELECT * FROM jobs WHERE jobTitle LIKE ? OR jobDesc LIKE ? OR requiredSkills LIKE ?";
        String[] searchArgs = new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};

        Cursor cursor = db.getReadableDatabase().rawQuery(searchQuery, searchArgs);

        jobList.clear();
        jobIds.clear();
        jobDescList.clear();
        jobSkillsList.clear();
        jobExpList.clear();
        jobSalaryList.clear();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No jobs found for '" + keyword + "'", Toast.LENGTH_SHORT).show();
        }

        while (cursor.moveToNext()) {
            int jobId = cursor.getInt(cursor.getColumnIndexOrThrow("jobId"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("jobTitle"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("jobDesc"));
            String skills = cursor.getString(cursor.getColumnIndexOrThrow("requiredSkills"));
            String exp = cursor.getString(cursor.getColumnIndexOrThrow("requiredExp"));
            String salary = cursor.getString(cursor.getColumnIndexOrThrow("salary"));

            jobIds.add(jobId);
            jobList.add(title);
            jobDescList.add(desc);
            jobSkillsList.add(skills);
            jobExpList.add(exp);
            jobSalaryList.add(salary);
        }
        cursor.close();

        if (adapter != null) {
            adapter.updateData(jobList, jobIds, jobDescList, jobSkillsList, jobExpList, jobSalaryList);
        } else {
            setupAdapter();
        }
    }
    private void clearLists() {
        jobList.clear();
        jobIds.clear();
        jobDescList.clear();
        jobSkillsList.clear();
        jobExpList.clear();
        jobSalaryList.clear();
    }

    private void addJobFromCursor(Cursor cursor) {
        int jobId = cursor.getInt(cursor.getColumnIndexOrThrow("jobId"));
        String title = cursor.getString(cursor.getColumnIndexOrThrow("jobTitle"));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow("jobDesc"));
        String skills = cursor.getString(cursor.getColumnIndexOrThrow("requiredSkills"));
        String exp = cursor.getString(cursor.getColumnIndexOrThrow("requiredExp"));
        String salary = cursor.getString(cursor.getColumnIndexOrThrow("salary"));

        jobIds.add(jobId);
        jobList.add(title);
        jobDescList.add(desc);
        jobSkillsList.add(skills);
        jobExpList.add(exp);
        jobSalaryList.add(salary);
    }

    private void setupAdapter() {
        adapter = new JobAdapter(this, jobList, jobIds, jobDescList, jobSkillsList,
                jobExpList, jobSalaryList, role, userId) {
            @Override
            public void onBindViewHolder(JobAdapter.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                int jobId = jobIds.get(position);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, JobDetailActivity.class);
                    intent.putExtra("jobId", jobId);
                    intent.putExtra("userId", userId);

                    // FIX: startActivity ki jagah startActivityForResult
                    startActivityForResult(intent, 1001);
                });
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("jobApplied", false)) {
                // Job apply hua hai, jobs reload karo
                loadAllJobs();

                // Optional: Applied jobs count update karne ke liye profile reload karo
                loadUserData();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadAllJobs(); // Refresh jobs when returning
    }
}