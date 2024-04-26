package com.example.sukappusers.UserSection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserNoticeFragment extends Fragment {

    private NoticeAdapter noticeAdapter;
    private List<Notice> noticeItemList;
    private CircularProgressIndicator circularProgressIndicator;
    private LinearProgressIndicator toolbarProgressBar;
    private DatabaseReference noticeRef;
    private ImageView backToTopImageView;
    LottieAnimationView noDataAnimationView;
    public BaseActivity baseActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Storage and Realtime Database references
        FirebaseStorage.getInstance().getReference("noticeImages");
        noticeRef = FirebaseDatabase.getInstance().getReference("Notice");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_fragment_notice, container, false);

        backToTopImageView = view.findViewById(R.id.backToTopImageView);
        RecyclerView noticeRecyclerView = view.findViewById(R.id.noticeRecyclerView);
        noticeItemList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(requireContext(), noticeItemList);
        noDataAnimationView = view.findViewById(R.id.noDataAnimationView);
        toolbarProgressBar = view.findViewById(R.id.toolbarProgressBar);
        circularProgressIndicator = view.findViewById(R.id.circularProgress);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        noticeRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        noticeRecyclerView.setLayoutManager(layoutManager);
        noticeRecyclerView.setAdapter(noticeAdapter);

        // Scroll listener to show/hide the "back to top" icon based on scroll position
        noticeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        backToTopImageView.setOnClickListener(v -> noticeRecyclerView.smoothScrollToPosition(0));

        // Call a method to fetch the notice data from a data source
        fetchNoticeData();

        return view;
    }


    private void fetchNoticeData() {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        // Set the style programmatically for a horizontal progress indicator
        toolbarProgressBar.setVisibility(View.VISIBLE);
        noticeRef.orderByChild("noticeDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noticeItemList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notice notice = snapshot.getValue(Notice.class);
                        if (notice != null) {
                            noticeItemList.add(notice);
                        }
                    }
                    // Sort the noticeItemList based on date and time in descending order
                    noticeItemList.sort(new Comparator<Notice>() {
                        @Override
                        public int compare(Notice notice1, Notice notice2) {
                            int dateComparison = notice2.getNoticeDate().compareTo(notice1.getNoticeDate());
                            if (dateComparison == 0) {
                                // If the dates are the same, compare the time
                                return compareTime(notice1.getNoticeTime(), notice2.getNoticeTime());
                            }
                            return dateComparison;
                        }

                        private int compareTime(String time1, String time2) {
                            SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                            try {
                                Date date1 = format.parse(time1);
                                Date date2 = format.parse(time2);
                                assert date2 != null;
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });

                    noticeAdapter.notifyDataSetChanged();
                    // Inside onDataChange or onCancelled methods:
                } else {
                    showNoDataImageView();
                    // Inside onDataChange or onCancelled methods:
                }
                circularProgressIndicator.setVisibility(View.GONE);
                toolbarProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                circularProgressIndicator.setVisibility(View.GONE);
                // Inside onDataChange or onCancelled methods:
                toolbarProgressBar.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), "Failed to fetch notice data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showNoDataImageView() {
        noDataAnimationView.setVisibility(View.VISIBLE);
    }

}
