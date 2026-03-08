package com.example.smartjobrecommendation.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartjobrecommendation.R;


public class RegisterActivity extends BaseActivity {

    Button jobSeekerBtn, hrBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        jobSeekerBtn = findViewById(R.id.jobSeekerBtn);
        hrBtn = findViewById(R.id.hrBtn);

        jobSeekerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, JobSeekerRegisterActivity.class)));

        hrBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HRRegisterActivity.class)));
    }
}