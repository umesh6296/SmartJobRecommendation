package com.example.smartjobrecommendation.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.database.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class JobSeekerRegisterActivity extends BaseActivity {

    ImageView profileImage;
    Button selectPhotoBtn, addQualificationBtn, selectResumeBtn, registerBtn;
    EditText name, mobile, email, password, skills, experience;
    LinearLayout qualificationContainer;
    TextView passwordLabel;
    ScrollView scrollView;

    Uri imageUri, resumeUri;
    DatabaseHelper db;
    ArrayList<QualificationItem> qualificationItems = new ArrayList<>();
    boolean isEditMode = false;
    int userId = -1;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_RESUME_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_seeker_register);

        initViews();
        setupClickListeners();
        setupKeyboardAndScrolling();

        db = new DatabaseHelper(this);
        isEditMode = getIntent().getBooleanExtra("editMode", false);

        if (isEditMode) {
            userId = getIntent().getIntExtra("userId", -1);
            if (userId != -1) {
                loadUserData();
            } else {
                Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // Check permissions
        checkPermissions();

        // Add initial qualification field
        addQualificationField();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        selectPhotoBtn = findViewById(R.id.selectPhotoBtn);
        addQualificationBtn = findViewById(R.id.addQualificationBtn);
        selectResumeBtn = findViewById(R.id.selectResumeBtn);
        registerBtn = findViewById(R.id.registerBtn);
        scrollView = findViewById(R.id.scrollView); // Initialize ScrollView

        name = findViewById(R.id.name);
        mobile = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        skills = findViewById(R.id.skills);
        experience = findViewById(R.id.experience);
        qualificationContainer = findViewById(R.id.qualificationContainer);
        passwordLabel = findViewById(R.id.passwordLabel);
    }

    private void setupClickListeners() {
        addQualificationBtn.setOnClickListener(v -> addQualificationField());

        selectPhotoBtn.setOnClickListener(v -> {
            if (checkPermission()) {
                openImagePicker();
            } else {
                requestPermissions();
            }
        });

        selectResumeBtn.setOnClickListener(v -> {
            if (checkPermission()) {
                openResumePicker();
            } else {
                requestPermissions();
            }
        });

        registerBtn.setOnClickListener(v -> registerUser());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select files.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openResumePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Resume"), PICK_RESUME_REQUEST);
    }

    // Add this method to handle add qualification button properly
    private void addQualificationField() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View qualView = inflater.inflate(R.layout.qualification_item, qualificationContainer, false);

        QualificationItem item = new QualificationItem(qualView);

        // Add focus listeners
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && scrollView != null) {
                v.postDelayed(() -> {
                    int y = v.getTop() - 100;
                    scrollView.smoothScrollTo(0, y);
                }, 200);
            }
        };

        item.etDegree.setOnFocusChangeListener(focusListener);
        item.etStartYear.setOnFocusChangeListener(focusListener);
        item.etEndYear.setOnFocusChangeListener(focusListener);

        item.btnRemove.setOnClickListener(v -> {
            qualificationContainer.removeView(item.view);
            qualificationItems.remove(item);
        });

        qualificationContainer.addView(qualView);
        qualificationItems.add(item);

        // Scroll to the new field after adding
        if (scrollView != null) {
            qualView.postDelayed(() -> {
                int y = qualView.getTop() - 100;
                scrollView.smoothScrollTo(0, y);
            }, 300);
        }
    }



    private void registerUser() {
        if (!validateInputs()) {
            return;
        }

        String fullName = name.getText().toString().trim();
        String phone = mobile.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();
        String skillSet = skills.getText().toString().trim();
        String exp = experience.getText().toString().trim();

        // Build qualifications string with years
        StringBuilder qualificationBuilder = new StringBuilder();
        StringBuilder qualificationDetailsBuilder = new StringBuilder();

        for (QualificationItem item : qualificationItems) {
            if (item.isValid()) {
                String degree = item.etDegree.getText().toString().trim();
                String startYear = item.etStartYear.getText().toString().trim();
                String endYear = item.etEndYear.getText().toString().trim();

                // For simple qualification field (backward compatibility)
                if (qualificationBuilder.length() > 0) {
                    qualificationBuilder.append(", ");
                }
                qualificationBuilder.append(degree);

                // For detailed qualification with years
                if (qualificationDetailsBuilder.length() > 0) {
                    qualificationDetailsBuilder.append(";;");
                }
                qualificationDetailsBuilder.append(degree).append("|")
                        .append(startYear).append("|")
                        .append(endYear);
            }
        }
        boolean success;

        if (isEditMode) {
            success = db.updateUser(
                    userId,
                    fullName,
                    phone,
                    userEmail,
                    userPass,
                    qualificationBuilder.toString(),
                    qualificationDetailsBuilder.toString(),
                    skillSet,
                    exp,
                    imageUri != null ? imageUri.toString() : "",
                    resumeUri != null ? resumeUri.toString() : ""
            );

            if (success) {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to Update Profile", Toast.LENGTH_SHORT).show();
            }

        } else {
            success = db.registerUser(
                    "JobSeeker",
                    fullName,
                    "",
                    "",
                    phone,
                    userEmail,
                    userPass,
                    qualificationBuilder.toString(),
                    qualificationDetailsBuilder.toString(),
                    skillSet,
                    exp,
                    imageUri != null ? imageUri.toString() : "",
                    resumeUri != null ? resumeUri.toString() : "",
                    ""
            );

            if (success) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Registration Failed - Email may already exist", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInputs() {
        String fullName = name.getText().toString().trim();
        String phone = mobile.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            name.setError("Name is required");
            name.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            mobile.setError("Mobile number is required");
            mobile.requestFocus();
            return false;
        }

        if (phone.length() != 10) {
            mobile.setError("Enter valid 10 digit mobile number");
            mobile.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Enter valid email address");
            email.requestFocus();
            return false;
        }

        if (!isEditMode) {
            if (TextUtils.isEmpty(userPass)) {
                password.setError("Password is required");
                password.requestFocus();
                return false;
            }

            if (userPass.length() < 6) {
                password.setError("Password must be at least 6 characters");
                password.requestFocus();
                return false;
            }

            if (imageUri == null) {
                Toast.makeText(this, "Please select a profile photo", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (resumeUri == null) {
                Toast.makeText(this, "Please upload your resume", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                if (requestCode == PICK_IMAGE_REQUEST) {
                    // Take persistable permission for future use
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }

                    imageUri = uri;

                    // Load image with Glide
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(profileImage);

                    Toast.makeText(this, "Photo selected successfully", Toast.LENGTH_SHORT).show();

                } else if (requestCode == PICK_RESUME_REQUEST) {
                    // Take persistable permission for future use
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }

                    resumeUri = uri;
                    selectResumeBtn.setText("Resume Selected ✓");
                    Toast.makeText(this, "Resume selected: " + getFileName(uri), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
    private String getFileName(Uri uri) {
        String fileName = "Unknown";
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private void loadUserData() {
        Cursor cursor = null;
        try {
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT * FROM users WHERE id=?",
                    new String[]{String.valueOf(userId)}
            );

            if (cursor != null && cursor.moveToFirst()) {
                name.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                mobile.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                email.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                skills.setText(cursor.getString(cursor.getColumnIndexOrThrow("skills")));
                experience.setText(cursor.getString(cursor.getColumnIndexOrThrow("experience")));

                // Load qualifications with years
                String qualificationDetails = cursor.getString(cursor.getColumnIndexOrThrow("qualification_details"));
                if (qualificationDetails != null && !qualificationDetails.isEmpty()) {
                    String[] qualItems = qualificationDetails.split(";;");
                    for (String qualItem : qualItems) {
                        String[] parts = qualItem.split("\\|");
                        if (parts.length == 3) {
                            addQualificationFieldWithData(parts[0], parts[1], parts[2]);
                        }
                    }
                } else {
                    // Purane data ke liye backup
                    String qualifications = cursor.getString(cursor.getColumnIndexOrThrow("qualification"));
                    if (qualifications != null && !qualifications.isEmpty()) {
                        String[] qualArray = qualifications.split(",");
                        for (String qual : qualArray) {
                            if (!qual.trim().isEmpty()) {
                                addQualificationFieldWithData(qual.trim(), "Not Set", "Not Set");
                            }
                        }
                    }
                }

                // Load photo
                String photo = cursor.getString(cursor.getColumnIndexOrThrow("photo"));
                if (photo != null && !photo.isEmpty()) {
                    try {
                        imageUri = Uri.parse(photo);
                        Glide.with(this)
                                .load(imageUri)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(profileImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        profileImage.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }

                // Load resume
                String resume = cursor.getString(cursor.getColumnIndexOrThrow("resume"));
                if (resume != null && !resume.isEmpty()) {
                    resumeUri = Uri.parse(resume);
                    selectResumeBtn.setText("Resume Selected ✓");
                }

                // Hide password field in edit mode
                if (passwordLabel != null) {
                    passwordLabel.setVisibility(View.GONE);
                }
                password.setVisibility(View.GONE);

                registerBtn.setText("Update Profile");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void addQualificationFieldWithData(String degree, String startYear, String endYear) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View qualView = inflater.inflate(R.layout.qualification_item, qualificationContainer, false);

        QualificationItem item = new QualificationItem(qualView);
        item.etDegree.setText(degree);
        item.etStartYear.setText(startYear);
        item.etEndYear.setText(endYear);

        // Add focus listeners
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && scrollView != null) {
                v.postDelayed(() -> {
                    int y = v.getTop() - 100;
                    scrollView.smoothScrollTo(0, y);
                }, 200);
            }
        };

        item.etDegree.setOnFocusChangeListener(focusListener);
        item.etStartYear.setOnFocusChangeListener(focusListener);
        item.etEndYear.setOnFocusChangeListener(focusListener);

        item.btnRemove.setOnClickListener(v -> {
            qualificationContainer.removeView(item.view);
            qualificationItems.remove(item);
        });

        qualificationContainer.addView(qualView);
        qualificationItems.add(item);
    }


    private void setupKeyboardAndScrolling() {
        if (scrollView == null) return;

        // Get all EditTexts
        ArrayList<View> editTexts = new ArrayList<>();
        editTexts.add(name);
        editTexts.add(mobile);
        editTexts.add(email);
        editTexts.add(password);
        editTexts.add(skills);
        editTexts.add(experience);

        // Set focus change listener for main EditTexts
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && scrollView != null) {
                v.postDelayed(() -> {
                    int y = v.getTop() - 100;
                    scrollView.smoothScrollTo(0, y);
                }, 200);
            }
        };

        for (View view : editTexts) {
            if (view != null) {
                view.setOnFocusChangeListener(focusListener);
            }
        }
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            View focusedChild = scrollView.findFocus();
            if (focusedChild != null) {
                int y = focusedChild.getTop() - 100;
                scrollView.smoothScrollTo(0, y);
            }
        });
    }



}

class QualificationItem {
    EditText etDegree, etStartYear, etEndYear;
    Button btnRemove;
    View view;

    QualificationItem(View view) {
        this.view = view;
        etDegree = view.findViewById(R.id.etDegree);
        etStartYear = view.findViewById(R.id.etStartYear);
        etEndYear = view.findViewById(R.id.etEndYear);
        btnRemove = view.findViewById(R.id.btnRemoveQualification);
    }

    boolean isValid() {
        return !TextUtils.isEmpty(etDegree.getText()) &&
                !TextUtils.isEmpty(etStartYear.getText()) &&
                !TextUtils.isEmpty(etEndYear.getText());
    }

    String getQualificationString() {
        return etDegree.getText().toString().trim() + "|" +
                etStartYear.getText().toString().trim() + "|" +
                etEndYear.getText().toString().trim();
    }
}