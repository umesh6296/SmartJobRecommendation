package com.example.smartjobrecommendation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartjobrecommendation.R;
import java.util.ArrayList;

public class AppliedJobAdapter extends RecyclerView.Adapter<AppliedJobAdapter.ViewHolder> {

    ArrayList<String> list;

    public AppliedJobAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_applied_job, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String fullText = list.get(position);
        String[] parts = fullText.split(" - ");

        String title = parts[0];
        String status = parts.length > 1 ? parts[1] : "Applied";

        holder.jobTitleText.setText(title);
        holder.statusText.setText(status);

        if (status.equals("Shortlisted")) {
            holder.statusText.setBackgroundResource(R.drawable.shortlisted_badge);
        } else if (status.equals("Rejected")) {
            holder.statusText.setBackgroundResource(R.drawable.rejected_badge);
        } else {
            holder.statusText.setBackgroundResource(R.drawable.applied_badge);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView jobTitleText, statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitleText = itemView.findViewById(R.id.jobTitleText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}