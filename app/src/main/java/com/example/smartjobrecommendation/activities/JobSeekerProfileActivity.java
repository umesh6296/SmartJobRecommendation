package com.example.smartjobrecommendation.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class JobSeekerProfileActivity extends BaseActivity {

    DatabaseHelper db;
    int userId;

    RecyclerView recyclerView;
    TextView profileName, profileEmail, profileMobile, profileSkills, profileExperience;
    LinearLayout qualificationsLayout;
    de.hdodenhof.circleimageview.CircleImageView profileImage;
    Button openResumeBtn, editBtn;
    String resumeUriString = "";
    String imageUriString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_seeker_profile);

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new DatabaseHelper(this);

        initViews();
        loadProfile();
        loadApplications();
    }

    private void initViews() {
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileImage = findViewById(R.id.profileImage);

        profileMobile = findViewById(R.id.profileMobile);
        profileSkills = findViewById(R.id.profileSkills);
        profileExperience = findViewById(R.id.profileExperience);
        qualificationsLayout = findViewById(R.id.qualificationsLayout);

        openResumeBtn = findViewById(R.id.openResumeBtn);
        editBtn = findViewById(R.id.editProfileBtn);
        recyclerView = findViewById(R.id.appliedRecycler);

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobSeekerRegisterActivity.class);
            intent.putExtra("editMode", true);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        openResumeBtn.setOnClickListener(v -> openResume());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadApplications();
    }

    private void loadProfile() {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT * FROM users WHERE id=?",
                    new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {

                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String skills = cursor.getString(cursor.getColumnIndexOrThrow("skills"));
                String exp = cursor.getString(cursor.getColumnIndexOrThrow("experience"));


                String qualificationDetails = cursor.getString(cursor.getColumnIndexOrThrow("qualification_details"));

                String qualifications = cursor.getString(cursor.getColumnIndexOrThrow("qualification"));

                resumeUriString = cursor.getString(cursor.getColumnIndexOrThrow("resume"));
                imageUriString = cursor.getString(cursor.getColumnIndexOrThrow("photo"));

                // Set basic info
                profileName.setText(name != null && !name.isEmpty() ? name : "Not Set");
                profileEmail.setText(email != null && !email.isEmpty() ? email : "Not Set");


                profileMobile.setText("📞 " + (mobile != null && !mobile.isEmpty() ? mobile : "Not Set"));
                profileSkills.setText("💡 Skills: " + (skills != null && !skills.isEmpty() ? skills : "Not Set"));
                profileExperience.setText("⏳ Experience: " + (exp != null && !exp.isEmpty() ? exp + " years" : "Not Set"));

                // Handle qualifications with years
                qualificationsLayout.removeAllViews();

                if (qualificationDetails != null && !qualificationDetails.isEmpty()) {
                    String[] qualItems = qualificationDetails.split(";;");
                    for (String qualItem : qualItems) {
                        String[] parts = qualItem.split("\\|");
                        if (parts.length == 3) {
                            String degree = parts[0];
                            String startYear = parts[1];
                            String endYear = parts[2];

                            // Create a card for each qualification
                            androidx.cardview.widget.CardView qualCard = new androidx.cardview.widget.CardView(this);
                            qualCard.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            qualCard.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                            qualCard.setRadius(8);
                            qualCard.setCardElevation(4);
                            qualCard.setContentPadding(20, 15, 20, 15);
                            qualCard.setUseCompatPadding(true);
                            qualCard.setMaxCardElevation(8);

                            // Inner LinearLayout
                            LinearLayout innerLayout = new LinearLayout(this);
                            innerLayout.setOrientation(LinearLayout.VERTICAL);
                            innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));

                            // Degree name
                            TextView tvDegree = new TextView(this);
                            tvDegree.setText(degree);
                            tvDegree.setTextSize(16f);
                            tvDegree.setTextColor(getResources().getColor(android.R.color.black));
                            tvDegree.setTypeface(null, Typeface.BOLD);

                            // Years
                            TextView tvYears = new TextView(this);
                            String yearText = startYear + " - " + (endYear.equals("Not Set") ? "Present" : endYear);
                            tvYears.setText("📅 " + yearText);
                            tvYears.setTextSize(14f);
                            tvYears.setTextColor(getResources().getColor(android.R.color.darker_gray));

                            innerLayout.addView(tvDegree);
                            innerLayout.addView(tvYears);
                            qualCard.addView(innerLayout);

                            qualificationsLayout.addView(qualCard);

                            // Add space between cards
                            if (qualItems.length > 1) {
                                View space = new View(this);
                                space.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 10));
                                qualificationsLayout.addView(space);
                            }
                        }
                    }
                } else {
                    // Fallback to simple qualifications
                    String simpleQual = cursor.getString(cursor.getColumnIndexOrThrow("qualification"));
                    if (simpleQual != null && !simpleQual.isEmpty()) {
                        String[] qualArray = simpleQual.split(",");
                        for (String qual : qualArray) {
                            if (!qual.trim().isEmpty()) {
                                TextView tv = new TextView(this);
                                tv.setText("• " + qual.trim());
                                tv.setTextSize(14f);
                                tv.setTextColor(getResources().getColor(android.R.color.black));
                                tv.setPadding(16, 8, 16, 8);
                                qualificationsLayout.addView(tv);
                            }
                        }
                    } else {
                        TextView tv = new TextView(this);
                        tv.setText("No qualifications added");
                        tv.setTextSize(14f);
                        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tv.setPadding(16, 8, 16, 8);
                        qualificationsLayout.addView(tv);
                    }
                }


                loadProfileImage();

                // Profile completion logic
                int completion = calculateProfileCompletion(name, mobile, email, skills, exp,
                        qualificationDetails != null ? qualificationDetails : qualifications);
                ProgressBar progressBar = findViewById(R.id.profileProgress);
                progressBar.setProgress(completion);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private void loadProfileImage() {
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri uri = Uri.parse(imageUriString);
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
                profileImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private int calculateProfileCompletion(String name, String mobile, String email,
                                           String skills, String exp, String qualifications) {
        int completion = 0;
        if (name != null && !name.isEmpty()) completion += 15;
        if (mobile != null && !mobile.isEmpty()) completion += 15;
        if (email != null && !email.isEmpty()) completion += 15;
        if (skills != null && !skills.isEmpty()) completion += 15;
        if (exp != null && !exp.isEmpty()) completion += 15;
        if (qualifications != null && !qualifications.isEmpty()) completion += 15;
        if (resumeUriString != null && !resumeUriString.isEmpty()) completion += 5;
        if (imageUriString != null && !imageUriString.isEmpty()) completion += 5;
        return completion;
    }

    // FIXED openResume METHOD - This will definitely work
    private void openResume() {
        if (resumeUriString == null || resumeUriString.isEmpty()) {
            Toast.makeText(this, "No resume uploaded", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = Uri.parse(resumeUriString);

            // Method 1: Try to open directly with Intent
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                startActivity(intent);
                return; // Success, exit method
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            // Method 2: Copy file to app's cache and open
            File cacheDir = getExternalCacheDir();
            if (cacheDir == null) {
                cacheDir = getCacheDir();
            }

            File tempFile = new File(cacheDir, "resume_temp_" + System.currentTimeMillis() + ".pdf");

            try {
                // Copy file using ParcelFileDescriptor (more reliable)
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileInputStream inputStream = new FileInputStream(pfd.getFileDescriptor());
                    FileOutputStream outputStream = new FileOutputStream(tempFile);

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();
                    pfd.close();

                    // Open the copied file
                    Uri fileUri = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider", tempFile);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(intent);
                    return; // Success
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(this, "Permission denied. Please re-upload resume.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Method 3: Ask user to re-select the file
            Toast.makeText(this, "Cannot access resume. Please select again.", Toast.LENGTH_LONG).show();

            // Open file picker to reselect
            Intent selectIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            selectIntent.setType("application/pdf");
            selectIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(selectIntent, 1001);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri newUri = data.getData();

                // Save permission for future use
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        getContentResolver().takePersistableUriPermission(newUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Update database
                        updateResumeUriInDatabase(newUri.toString());

                        // Open the newly selected resume
                        openResume();

                    } catch (SecurityException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to get permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void updateResumeUriInDatabase(String newUri) {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT * FROM users WHERE id=?",
                    new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String qualification = cursor.getString(cursor.getColumnIndexOrThrow("qualification"));
                String qualificationDetails = cursor.getString(cursor.getColumnIndexOrThrow("qualification_details"));
                String skills = cursor.getString(cursor.getColumnIndexOrThrow("skills"));
                String experience = cursor.getString(cursor.getColumnIndexOrThrow("experience"));
                String photo = cursor.getString(cursor.getColumnIndexOrThrow("photo"));


                db.updateUser(userId, name, phone, email, password,
                        qualification,
                        qualificationDetails,
                        skills,
                        experience,
                        photo,
                        newUri);

                resumeUriString = newUri;
                Toast.makeText(this, "Resume updated successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadApplications() {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT jobs.jobTitle, applications.status FROM applications " +
                            "JOIN jobs ON applications.jobId = jobs.jobId " +
                            "WHERE applications.userId=?",
                    new String[]{String.valueOf(userId)});

            ArrayList<String> list = new ArrayList<>();

            if (cursor.getCount() == 0) {
                // No applications yet
                list.add("No jobs applied yet");
            } else {
                while (cursor.moveToNext()) {
                    String title = cursor.getString(0);
                    String status = cursor.getString(1);

                    // Add emoji based on status
                    String statusEmoji = "⏳"; // Default
                    if (status.equals("Applied")) statusEmoji = "📤";
                    else if (status.equals("Reviewed")) statusEmoji = "👀";
                    else if (status.equals("Shortlisted")) statusEmoji = "✅";
                    else if (status.equals("Rejected")) statusEmoji = "❌";

                    list.add(statusEmoji + " " + title + " - " + status);
                }
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            com.example.smartjobrecommendation.adapters.AppliedJobAdapter adapter =
                    new com.example.smartjobrecommendation.adapters.AppliedJobAdapter(list);

            recyclerView.setAdapter(adapter);



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}