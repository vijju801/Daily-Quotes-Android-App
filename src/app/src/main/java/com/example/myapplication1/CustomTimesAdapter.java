package com.example.myapplication1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomTimesAdapter extends RecyclerView.Adapter<CustomTimesAdapter.ViewHolder> {

    private List<String> customTimes;
    private final OnTimeDeleteListener deleteListener;

    public interface OnTimeDeleteListener {
        void onTimeDeleted(String time);
    }

    public CustomTimesAdapter(List<String> customTimes, OnTimeDeleteListener deleteListener) {
        this.customTimes = customTimes;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_time, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = customTimes.get(position);
        holder.timeTextView.setText(time);
        holder.deleteButton.setOnClickListener(v -> deleteListener.onTimeDeleted(time));
    }

    @Override
    public int getItemCount() {
        return customTimes.size();
    }

    public void setCustomTimes(List<String> newTimes) {
        this.customTimes = newTimes;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            deleteButton = itemView.findViewById(R.id.delete_time_button);
        }
    }
}