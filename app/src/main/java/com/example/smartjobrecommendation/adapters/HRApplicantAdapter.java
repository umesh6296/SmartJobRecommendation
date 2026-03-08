package com.example.smartjobrecommendation.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.activities.ChatActivity;
import com.example.smartjobrecommendation.activities.HRApplicantActivity;
import com.example.smartjobrecommendation.database.DatabaseHelper;
import com.example.smartjobrecommendation.utils.SkillMatcher;

import java.util.ArrayList;

public class HRApplicantAdapter extends RecyclerView.Adapter<HRApplicantAdapter.ViewHolder> {

    Context context;
    int jobId;
    ArrayList<Integer> userIds;
    ArrayList<String> names, skills, resumes;
    DatabaseHelper db;

    public HRApplicantAdapter(Context context, int jobId,
                              ArrayList<Integer> userIds,
                              ArrayList<String> names,
                              ArrayList<String> skills,
                              ArrayList<String> resumes) {

        this.context = context;
        this.jobId = jobId;
        this.userIds = userIds;
        this.names = names;
        this.skills = skills;
        this.resumes = resumes;
        db = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_hr_applicant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int userId = userIds.get(position);
        String name = names.get(position);
        String userSkills = skills.get(position);
        String resume = resumes.get(position);

        String jobSkills = db.getJobSkills(jobId);
        int match = SkillMatcher.calculateMatch(userSkills, jobSkills);

        holder.nameText.setText(name);

        holder.matchText.setText(match + "% - " + (match >= 70 ? "High Match" : "Low Match"));

        if (match >= 70) {
            holder.matchText.setTextColor(Color.parseColor("#2ECC71"));
        } else {
            holder.matchText.setTextColor(Color.parseColor("#E74C3C"));
        }

        // Resume Button
        holder.resumeBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(resume), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show();
            }
        });

        // Shortlist Button
        holder.shortlistBtn.setOnClickListener(v -> {
            db.updateStatus(jobId, userId, "Shortlisted");
            Toast.makeText(context, "Shortlisted", Toast.LENGTH_SHORT).show();
            holder.shortlistBtn.setEnabled(false);
            holder.rejectBtn.setEnabled(false);
        });

        // Reject Button
        holder.rejectBtn.setOnClickListener(v -> {
            db.updateStatus(jobId, userId, "Rejected");
            Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show();
            holder.shortlistBtn.setEnabled(false);
            holder.rejectBtn.setEnabled(false);
        });

        // Chat Button
        holder.chatBtn.setOnClickListener(v -> {

            int hrId = ((HRApplicantActivity)context).getUserId();

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("currentUserId", hrId);
            intent.putExtra("otherUserId", userId);
            intent.putExtra("otherUserName", name);
            intent.putExtra("jobId", jobId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, matchText;
        Button resumeBtn, shortlistBtn, rejectBtn;
        ImageButton chatBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.applicantName);
            matchText = itemView.findViewById(R.id.matchText);
            resumeBtn = itemView.findViewById(R.id.resumeBtn);
            shortlistBtn = itemView.findViewById(R.id.shortlistBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
            chatBtn = itemView.findViewById(R.id.chatBtn);
        }
    }
}