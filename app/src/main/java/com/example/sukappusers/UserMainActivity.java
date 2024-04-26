package com.example.sukappusers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sukappusers.UserSection.UserProfileActivity;
import com.example.sukappusers.databinding.UserActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserMainActivity extends BaseActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private long backPressedTime;
    NavController navController;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isCameraPermission = false;
    private boolean isNotificationPermission = false;
    FirebaseFirestore fFirestore;
    private DatabaseReference notificationRef, noticeRef;
    BadgeDrawable notifyBadge, noticeBadge;
    BottomNavigationView bottomNavigationView;
    NavigationView navigationView;
    private boolean isFirstVisit = true;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserActivityMainBinding binding = UserActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fFirestore = FirebaseFirestore.getInstance();
        setSupportActionBar(binding.appBarUserMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;


        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.get(Manifest.permission.CAMERA) != null) {
                isCameraPermission = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
            }

            if (result.get(Manifest.permission.POST_NOTIFICATIONS) != null) {
                isNotificationPermission = Boolean.TRUE.equals(result.get(Manifest.permission.POST_NOTIFICATIONS));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission();
        }

        // Get the reference to the BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set up the AppBarConfiguration with both the DrawerLayout and BottomNavigationView
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_notice, R.id.nav_notify, R.id.nav_help, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();

        // Set up the NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_main);

        // Link the NavController with the AppBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Set up the NavigationView with the NavController
        NavigationUI.setupWithNavController(navigationView, navController);

        // Set up the BottomNavigationView with the NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        notificationRef = FirebaseDatabase.getInstance().getReference("notifications");
        noticeRef = FirebaseDatabase.getInstance().getReference("Notice");

        // Add badges to the menu items
        Menu menu = bottomNavigationView.getMenu();
        MenuItem notifyMenuItem = menu.findItem(R.id.nav_notify);
        MenuItem noticeMenuItem = menu.findItem(R.id.nav_notice);

        // Add badge dots to the menu items
        notifyBadge = bottomNavigationView.getOrCreateBadge(notifyMenuItem.getItemId());
        noticeBadge = bottomNavigationView.getOrCreateBadge(noticeMenuItem.getItemId());

        notifyBadge.setVisible(false);
        noticeBadge.setVisible(false);

        // Set the item selection listener for the bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Handle bottom navigation item selection
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Navigate to the "Home" fragment
                navController.navigate(R.id.nav_home);
                navigationView.setCheckedItem(R.id.nav_home); // Sync the Drawer menu selection
                return true;
            } else if (itemId == R.id.nav_notify) {
                // Navigate to the "Notify" fragment
                notifyBadge.setVisible(false);
                navController.navigate(R.id.nav_notify);
                navigationView.setCheckedItem(R.id.nav_notify); // Sync the Drawer menu selection
                return true;
            } else if (itemId == R.id.nav_notice) {
                // Navigate to the "Notice" fragment
                noticeBadge.setVisible(false);
                navController.navigate(R.id.nav_notice);
                navigationView.setCheckedItem(R.id.nav_notice); // Sync the Drawer menu selection
                return true;
            } else if (itemId == R.id.nav_help) {
                // Navigate to the "Help" fragment
                navController.navigate(R.id.nav_help);
                navigationView.setCheckedItem(R.id.nav_help); // Sync the Drawer menu selection
                return true;
            }
            return false;
        });

        // Add a listener to the Drawer menu to sync with the BottomNavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle Drawer menu item selection
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home); // Sync the BottomNavigationView selection
                drawer.closeDrawer(GravityCompat.START); // Close the drawer
                return true;
            } else if (itemId == R.id.nav_notify) {
                bottomNavigationView.setSelectedItemId(R.id.nav_notify); // Sync the BottomNavigationView selection
                drawer.closeDrawer(GravityCompat.START); // Close the drawer
                return true;
            } else if (itemId == R.id.nav_notice) {
                bottomNavigationView.setSelectedItemId(R.id.nav_notice);
                drawer.closeDrawer(GravityCompat.START); // Close the drawer
                return true;
            } else if (itemId == R.id.nav_help) {
                bottomNavigationView.setSelectedItemId(R.id.nav_help);
                drawer.closeDrawer(GravityCompat.START); // Close the drawer
                return true;
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, UserLoginActivity.class));
                finish();
                return true;
            }
            return false;
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermission() {
        isCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        isNotificationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;

        if (!isCameraPermission || !isNotificationPermission) {
            List<String> permissionRequest = new ArrayList<>();

            if (!isCameraPermission) {
                permissionRequest.add(Manifest.permission.CAMERA);
            }

            if (!isNotificationPermission) {
                permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }

            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, UserLoginActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Check for device ID changes here
        checkDeviceIdChanges();
        checkUserStatus();
    }

    private void checkDeviceIdChanges() {
        @SuppressLint("HardwareIds") String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseFirestore fFirestore = FirebaseFirestore.getInstance();
            fFirestore.collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String storedDeviceId = documentSnapshot.getString("deviceId");
                            if (!currentDeviceId.equals(storedDeviceId)) {
                                showDeviceIdChangedDialog();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors while fetching data from Firestore
                    });
        }
    }

    private void showDeviceIdChangedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        builder.setView(view);
        builder.setCancelable(false); // Prevent dismiss on outside touch or back button

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Show the AlertDialog
        playNotificationSound();
        alertDialog.show();

        // Perform the task that requires progress dialog, e.g., logging out the user
        new Thread(() -> {
            // Simulate some background task
            try {
                Thread.sleep(2000); // Replace this with your actual task
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Dismiss the AlertDialog after the task is complete
            runOnUiThread(() -> {
                alertDialog.dismiss();

                // Log out the user
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserMainActivity.this, UserLoginActivity.class));
                finish();
            });
        }).start();
    }

    private void playNotificationSound() {
        try {
            Uri notificationSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSoundUri);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchNotices() {
        noticeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int newNoticesCount = 0; // Counter for new Notices

                for (DataSnapshot noticesSnapshot : dataSnapshot.getChildren()) {
                    // Assuming each notification has a "status" field
                    String status = noticesSnapshot.child("status").getValue(String.class);

                    // Check if the status is "new"
                    if (status != null && status.equals("new")) {
                        newNoticesCount++;
                    }
                }

                // Update the badge with the number of new notifications
                if (newNoticesCount > 0) {
                    navController.navigate(R.id.nav_notice);
                    noticeBadge.setVisible(true); // Show the badge when there are new notifications
                } else {
                    noticeBadge.setVisible(false); // Hide the badge when there are no new notifications
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyBadge.setVisible(false);
            }
        });
    }

    private void fetchNotifications() {
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int newNotificationsCount = 0; // Counter for new notifications

                for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren()) {
                    // Assuming each notification has a "status" field
                    String status = notificationSnapshot.child("status").getValue(String.class);

                    // Check if the status is "new"
                    if (status != null && status.equals("new")) {
                        newNotificationsCount++;
                    }
                }

                // Update the badge with the number of new notifications
                if (newNotificationsCount > 0) {
                    navController.navigate(R.id.nav_notify);
                    notifyBadge.setVisible(true); // Show the badge when there are new notifications
                } else {
                    notifyBadge.setVisible(false); // Hide the badge when there are no new notifications
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyBadge.setVisible(false);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFirstVisit) {
            fetchNotices();
            fetchNotifications();
            // Check if there are new notifications or notices
            if (notifyBadge.isVisible()) {
                // If there are new notifications, navigate to the "Notify" fragment

                navigationView.setCheckedItem(R.id.nav_notify); // Sync the Drawer menu selection
            } else if (noticeBadge.isVisible()) {
                // If there are new notices, navigate to the "Notice" fragment
                navController.navigate(R.id.nav_notice);
                navigationView.setCheckedItem(R.id.nav_notice); // Sync the Drawer menu selection
            }
            isFirstVisit = false;
        }
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore fFirestore = FirebaseFirestore.getInstance();

            // Check if the user is in the UserBlocked collection
            fFirestore.collection("BlockedUsers")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // User is blocked, show blocked dialog
                            showUserBlockedDialog();
                        } else {
                            // Check if the user is in the UserRemoved collection
                            fFirestore.collection("RemovedUsers")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener(removedDocumentSnapshot -> {
                                        if (removedDocumentSnapshot.exists()) {
                                            // User is removed, show removed dialog
                                            showUserRemovedDialog();
                                        } else {
                                            // Check if the user status is "pending"
                                            fFirestore.collection("Users")
                                                    .document(userId)
                                                    .get()
                                                    .addOnSuccessListener(usersdocumentSnapshot -> {
                                                        if (usersdocumentSnapshot.exists()) {
                                                            String status = usersdocumentSnapshot.getString("status");
                                                            if ("pending".equals(status)) {
                                                                // Account is still being verified, show appropriate message
                                                                showVerificationPendingDialog();
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Handle any errors while fetching data from Firestore
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors while fetching data from Firestore
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors while fetching data from Firestore
                    });
        }
    }

    private void showVerificationPendingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verification Pending");
        builder.setMessage("Sorry, your account is still verifying by the admin.\n\nPlease wait for a 3 days. If this exceeds, please contact the admin for further assistance..");

        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserMainActivity.this, UserLoginActivity.class));
            finish();
        });

        // Prevent dismiss on outside touch or back button
        builder.setCancelable(false);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Play notification sound
        playNotificationSound();
    }

    private void showUserBlockedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Blocked");
        builder.setMessage("Sorry, your account has been blocked by the admin.\n\nIf you have any doubt related to this, then contact the admin for further assistance.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserMainActivity.this, UserLoginActivity.class));
            finish();
        });

        // Prevent dismiss on outside touch or back button
        builder.setCancelable(false);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Play notification sound
        playNotificationSound();
    }

    private void showUserRemovedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Removed");
        builder.setMessage("Sorry, your account has been removed by the admin.\n\nWe have found that your USN is not in our academic list, so we have permanently removed your account. Thank you.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserMainActivity.this, UserLoginActivity.class));
            finish();
        });

        // Prevent dismiss on outside touch or back button
        builder.setCancelable(false);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Play notification sound
        playNotificationSound();
    }

}
