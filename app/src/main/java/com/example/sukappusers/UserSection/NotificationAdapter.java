package com.example.sukappusers.UserSection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sukappusers.UserSection.Notification;
import com.example.sukappusers.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Check if the TextView references are null
        if (holder.titleTextView != null) {
            holder.titleTextView.setText(notification.getTitle());
        }
        if (holder.bodyTextView != null) {
            holder.bodyTextView.setText(notification.getBody());
        }
        // Set date and time
        if (holder.dateTextView != null && holder.timeTextView != null) {

            String dateTimeString = notification.getDate() + " " + notification.getTime();

            holder.dateTextView.setText(dateTimeString);
            holder.timeTextView.setVisibility(View.GONE);
        }

        holder.status.setText(notification.getStatus());
        holder.status.setAllCaps(true);

        // Set text color based on complaint status
        switch (notification.getStatus()) {
            case "new":
                holder.status.setBackgroundResource(R.drawable.corner_green);
                break;
            case "old":
                holder.status.setBackgroundResource(R.drawable.corner_red);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView bodyTextView;
        TextView timeTextView, dateTextView;
        TextView status;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            bodyTextView = itemView.findViewById(R.id.textViewBody);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            status = itemView.findViewById(R.id.status_view);

        }

    }
}
