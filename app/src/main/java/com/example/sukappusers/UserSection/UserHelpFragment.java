package com.example.sukappusers.UserSection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.sukappusers.BaseActivity;
import com.example.sukappusers.R;
import com.example.sukappusers.databinding.UserFragmentHelpBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserHelpFragment extends Fragment {

    private UserFragmentHelpBinding binding;
    FloatingActionButton floatingActionButton;
    TextView collegeTitleTextView, deptNameTextView, addressTextView, phoneNoTextView1, phoneNoTextView2,
            phoneNoTextView3, phoneNoTextView4;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    CircleImageView collegeLogoImageView;
    private LinearProgressIndicator toolbarProgressBar;
    public BaseActivity baseActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = UserFragmentHelpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Set the text values in TextView views
        databaseReference = FirebaseDatabase.getInstance().getReference("ContactDetails");
        storageReference = FirebaseStorage.getInstance().getReference("collegeLOGO");

        collegeTitleTextView = root.findViewById(R.id.collegeTitleTextView);
        deptNameTextView = root.findViewById(R.id.deptNameTextView);
        addressTextView = root.findViewById(R.id.addressTextView);
        phoneNoTextView1 = root.findViewById(R.id.phoneNoTextView1);
        phoneNoTextView2 = root.findViewById(R.id.phoneNoTextView2);
        phoneNoTextView3 = root.findViewById(R.id.phoneNoTextView3);
        phoneNoTextView4 = root.findViewById(R.id.phoneNoTextView4);
        collegeLogoImageView = root.findViewById(R.id.collegeLogo);
        toolbarProgressBar = root.findViewById(R.id.toolbarProgressBar);
        toolbarProgressBar.setVisibility(View.VISIBLE);

        fetchContactData();

        floatingActionButton = root.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(view -> startActivity(new Intent(requireContext(), UserCheckStatusActivity.class)));
        return root;
    }

    private void fetchContactData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);

                if (dataSnapshot.exists()) {
                    // Get the contact details from the dataSnapshot
                    ContactDetails contactDetails = dataSnapshot.getValue(ContactDetails.class);
                    if (contactDetails != null) {
                        // Set the retrieved data in the TextView views
                        collegeTitleTextView.setText(contactDetails.getCollegeTitle());
                        deptNameTextView.setText(contactDetails.getDeptName());
                        addressTextView.setText(contactDetails.getAddress());
                        phoneNoTextView1.setText(contactDetails.getPhoneNo1());
                        phoneNoTextView2.setText(contactDetails.getPhoneNo2());
                        phoneNoTextView3.setText(contactDetails.getPhoneNo3());
                        phoneNoTextView4.setText(contactDetails.getPhoneNo4());

                        String imageUrl = contactDetails.getImageURL();
                        if (imageUrl != null) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.logo)
                                    .error(R.drawable.logo)
                                    .into(collegeLogoImageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                // Toast.makeText(requireContext(), "Failed to fetch contact details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
