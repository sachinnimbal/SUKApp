package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.example.sukappusers.databinding.UserFragmentNotificationBinding;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserNotificationFragment extends Fragment {

    private UserFragmentNotificationBinding binding;
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private ImageView backToTopImageView;
    private LottieAnimationView noDataAnimationView;
    private CircularProgressIndicator circularProgressIndicator;
    private LinearProgressIndicator toolbarProgressBar;
    public BaseActivity baseActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = UserFragmentNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(notificationAdapter);
        backToTopImageView = root.findViewById(R.id.backToTopImageView);
        noDataAnimationView = root.findViewById(R.id.noDataAnimationView);
        toolbarProgressBar = root.findViewById(R.id.toolbarProgressBar);
        circularProgressIndicator = root.findViewById(R.id.circularProgress);

        displayNotification();

        // Scroll listener to show/hide the "back to top" icon based on scroll position
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    // Scrolling downwards
                    backToTopImageView.setVisibility(View.VISIBLE);
                } else {
                    // Scrolling upwards
                    backToTopImageView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Set click listener for the "back to top" icon
        backToTopImageView.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        return root;
    }

    private void displayNotification() {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        // Set the style programmatically for a horizontal progress indicator
        toolbarProgressBar.setVisibility(View.VISIBLE);
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        Query query = notificationsRef.orderByChild("currentTimestamp").limitToLast(50);

        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                circularProgressIndicator.setVisibility(View.VISIBLE);
                // Set the style programmatically for a horizontal progress indicator
                toolbarProgressBar.setVisibility(View.VISIBLE);
                List<Notification> notifications = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String notificationKey = snapshot.getKey();
                    Notification notification = snapshot.getValue(Notification.class);
                    assert notification != null;
                    notification.setNotificationKey(notificationKey);
                    notifications.add(notification);
                }

                if (!notifications.isEmpty()) {
                    // Reverse the order of notifications
                    Collections.reverse(notifications);

                    // Notifications available
                    notificationAdapter.setNotifications(notifications);
                    recyclerView.setVisibility(View.VISIBLE);
                    noDataAnimationView.setVisibility(View.GONE);
                } else {
                    // No notifications available
                    showNoDataImageView();
                }

                notificationAdapter.notifyDataSetChanged();
                circularProgressIndicator.setVisibility(View.GONE);
                toolbarProgressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                circularProgressIndicator.setVisibility(View.GONE);
                toolbarProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showNoDataImageView() {
        noDataAnimationView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
