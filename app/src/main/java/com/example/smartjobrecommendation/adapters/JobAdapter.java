package com.example.smartjobrecommendation.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartjobrecommendation.R;
import com.example.smartjobrecommendation.activities.HRApplicantActivity;
import com.example.smartjobrecommendation.activities.JobDetailActivity;
import com.example.smartjobrecommendation.database.DatabaseHelper;
import com.example.smartjobrecommendation.utils.SkillMatcher;

import java.util.ArrayList;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.ViewHolder> {

    Context context;
    ArrayList<String> jobList;
    ArrayList<Integer> jobIds;
    ArrayList<String> jobDescList;
    ArrayList<String> jobSkillsList;
    ArrayList<String> jobExpList;
    ArrayList<String> jobSalaryList;
    String role;
    int userId;
    DatabaseHelper db;


    public JobAdapter(Context context,
                      ArrayList<String> jobList,
                      ArrayList<Integer> jobIds,
                      ArrayList<String> jobDescList,
                      ArrayList<String> jobSkillsList,
                      ArrayList<String> jobExpList,
                      ArrayList<String> jobSalaryList,
                      String role,
                      int userId) {
        this.context = context;
        this.jobList = jobList;
        this.jobIds = jobIds;
        this.jobDescList = jobDescList;
        this.jobSkillsList = jobSkillsList;
        this.jobExpList = jobExpList;
        this.jobSalaryList = jobSalaryList;
        this.role = role;
        this.userId = userId;
        db = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int jobId = jobIds.get(position);
        String title = jobList.get(position);


        String desc = (jobDescList != null && jobDescList.size() > position) ?
                jobDescList.get(position) : "";
        String skills = (jobSkillsList != null && jobSkillsList.size() > position) ?
                jobSkillsList.get(position) : "";
        String exp = (jobExpList != null && jobExpList.size() > position) ?
                jobExpList.get(position) : "";
        String salary = (jobSalaryList != null && jobSalaryList.size() > position) ?
                jobSalaryList.get(position) : "";

        if (role != null && role.equals("JobSeeker")) {
            // Job Seeker View - with match percentage
            String userSkills = db.getUserSkills(userId);
            String jobSkills = db.getJobSkills(jobId);
            int match = SkillMatcher.calculateMatch(userSkills, jobSkills);

            String displayText = "📌 " + title + "\n" +
                    "📝 " + (desc.length() > 30 ? desc.substring(0, 30) + "..." : desc) + "\n" +
                    "💡 Skills: " + skills + "\n" +
                    "⏳ Exp: " + exp + " years\n" +
                    "💰 Salary: ₹" + salary + "\n" +
                    "📊 Match: " + match + "% " +
                    (match >= 70 ? "⚡ (High Match)" : match >= 40 ? "📈 (Medium Match)" : "📉 (Low Match)");

            holder.jobText.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra("jobId", jobId);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            });

        } else if (role != null && role.equals("HR")) {

            String displayText = "📌 " + title + "\n" +
                    "📝 " + (desc.length() > 30 ? desc.substring(0, 30) + "..." : desc) + "\n" +
                    "💡 Skills: " + skills + "\n" +
                    "⏳ Exp: " + exp + " years\n" +
                    "💰 Salary: ₹" + salary;

            holder.jobText.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, HRApplicantActivity.class);
                intent.putExtra("jobId", jobId);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }


    public void updateData(ArrayList<String> newJobList,
                           ArrayList<Integer> newJobIds,
                           ArrayList<String> newJobDescList,
                           ArrayList<String> newJobSkillsList,
                           ArrayList<String> newJobExpList,
                           ArrayList<String> newJobSalaryList) {
        this.jobList = newJobList;
        this.jobIds = newJobIds;
        this.jobDescList = newJobDescList;
        this.jobSkillsList = newJobSkillsList;
        this.jobExpList = newJobExpList;
        this.jobSalaryList = newJobSalaryList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jobText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jobText = itemView.findViewById(R.id.jobText);
        }
    }
}