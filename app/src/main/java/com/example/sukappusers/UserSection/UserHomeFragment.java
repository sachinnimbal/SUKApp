package com.example.sukappusers.UserSection;

import android.app.AlertDialog;
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
import com.example.sukappusers.UserLoginActivity;
import com.example.sukappusers.databinding.UserFragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserHomeFragment extends Fragment {
    private UserFragmentHomeBinding binding;
    private TextView fullNameTextView;
    private TextView usnTextView;
    private FirebaseFirestore db;
    CircleImageView profile_image;
    private TextView greetingTextView;
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
        binding = UserFragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fullNameTextView = root.findViewById(R.id.fullNameTextView);
        usnTextView = root.findViewById(R.id.usnTextView);
        profile_image = root.findViewById(R.id.profileImage);
        db = FirebaseFirestore.getInstance();
        greetingTextView = root.findViewById(R.id.greetingTextView);

        // Retrieve data from Fire_store and set it to the TextViews
        retrieveUserData();
        profile_image.setOnClickListener(view -> {
            // Handle click event for the profile image
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            startActivity(intent);
        });
        binding.OfficialSite.setOnClickListener(v -> {
            // Handle click event for the "Academic" button
            Intent intent = new Intent(getActivity(), UserOfficialSiteActivity.class);
            startActivity(intent);
        });
        binding.Academic.setOnClickListener(v -> {
            // Handle click event for the "Academic" button
            Intent intent = new Intent(getActivity(), UserAcademicActivity.class);
            startActivity(intent);
        });
        binding.Faculties.setOnClickListener(v -> {
            // Handle click event for the "Faculty" button
            Intent intent = new Intent(getActivity(), UserFacultiesActivity.class);
            startActivity(intent);
        });
        binding.documents.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserOldqpActivity.class);
            startActivity(intent);
        });
        binding.EditProfile.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), UserEditProfileActivity.class);
            startActivity(intent);
        });
        binding.Notes.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), UserStudyActivity.class);
            startActivity(intent);
        });
        binding.Developer.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), DeveloperActivity.class);
            startActivity(intent);
        });
        binding.Logout.setOnClickListener(view -> {
            // Perform logout
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), UserLoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        return root;
    }

    private void retrieveUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String usn = documentSnapshot.getString("usn");
                    String batch = documentSnapshot.getString("batchYear");
                    String parentName = documentSnapshot.getString("parentName");
                    String phoneNumber = documentSnapshot.getString("phoneNumber");
                    String address = documentSnapshot.getString("address");

                    // Check if any of the required fields are empty or null
                    if (fullName == null || fullName.isEmpty() || usn == null || usn.isEmpty() ||
                            batch == null || batch.isEmpty() || parentName == null || parentName.isEmpty() || phoneNumber == null || phoneNumber.isEmpty() ||
                            address == null || address.isEmpty()) {
                        // Show alert dialog indicating missing details
                        showMissingDetailsAlertDialog();
                    }

                    usnTextView.setText(usn);
                    String profileImage = documentSnapshot.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImage)
                                .placeholder(R.drawable.loading_image)
                                .error(R.drawable.error_loading_image)
                                .into(profile_image);
                    } else {
                        // If no profile image is available, you can set a default image
                        showNoImageAlertDialog();
                        profile_image.setImageResource(R.drawable.profile);
                    }

                    // Get the current time
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);

                    // Display appropriate greeting based on the time of day
                    String greeting;
                    if (hour < 4) {
                        greeting = "Good Night";
                    } else if (hour < 12) {
                        greeting = "Good Morning";
                    } else if (hour < 18) {
                        greeting = "Good Afternoon";
                    } else {
                        greeting = "Good Evening";
                    }

                    // Append the greeting to the name
                    String personalizedGreeting = "Hi, " + greeting;
                    fullNameTextView.setText(fullName);
                    greetingTextView.setText(personalizedGreeting);
                }
            }).addOnFailureListener(e -> {
                // Handle the failure to retrieve user data
            });
        }
    }

    private void showMissingDetailsAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Missing Profile Details")
                .setMessage("You have not provided all the required profile details (batch, parents details, address). Please edit and update your profile.")
                .setPositiveButton("Edit Profile", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), UserEditProfileActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showNoImageAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Profile Details")
                .setMessage("You have not set an image for your profile and not filled the parent details..")
                .setPositiveButton("Edit Profile", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), UserEditProfileActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveUserData();
    }
}


