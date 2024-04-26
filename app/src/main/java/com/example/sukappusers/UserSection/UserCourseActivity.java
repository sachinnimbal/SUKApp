package com.example.sukappusers.UserSection;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserCourseActivity extends BaseActivity {

    private DatabaseReference databaseRef;
    private Spinner semesterSpinner;
    private TextView semesterTextView;
    private TableLayout headerTableLayout;
    private CircularProgressIndicator circularProgressIndicator;
    private LinearProgressIndicator toolbarProgressBar;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_course);
        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Course");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("Courses");

        // Initialize storage reference
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference("SyllabusPDFs");

        // Initialize views
        headerTableLayout = findViewById(R.id.headerTableLayout);
        semesterSpinner = findViewById(R.id.spinnerOption);
        circularProgressIndicator = findViewById(R.id.circularProgress);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);
        semesterTextView = findViewById(R.id.semesterTextView);

        // Load semesters into the spinner
        loadSemesters();
    }

    private void loadSemesters() {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                List<String> semesterList = new ArrayList<>();

                for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                    String semester = semesterSnapshot.getKey();
                    semesterList.add(semester);
                }

                if (semesterList.isEmpty()) {
                    semesterList.add("No data found"); // Add a message when no data is found
                }

                // Create an ArrayAdapter and set it to the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(UserCourseActivity.this, android.R.layout.simple_spinner_item, semesterList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(adapter);

                semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        String selectedSemester = (String) parent.getItemAtPosition(position);
                        if (selectedSemester.equals("No data found")) {
                            semesterTextView.setText(selectedSemester);
                            showNoDataAlertDialog();
                        } else {
                            circularProgressIndicator.setVisibility(View.INVISIBLE);
                            semesterTextView.setText(selectedSemester);
                            fetchAndDisplayData(selectedSemester);
                        }
                        semesterTextView.setText(selectedSemester);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        showNoDataAlertDialog();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                // Handle database error if needed
                Toast.makeText(UserCourseActivity.this, "Failed to load semesters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayData(String selectedSemester) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        databaseRef.child(selectedSemester).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                headerTableLayout.removeAllViews();
                createTableHeader();
                int index = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    DataItem item = data.getValue(DataItem.class);
                    if (item != null) {
                        addTableRow(index, item);
                        index++;
                    }
                }

                if (index == 0) {
                    showNoDataAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                showNoDataAlertDialog();
                Toast.makeText(UserCourseActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTableHeader() {
        TableRow headerRow = new TableRow(UserCourseActivity.this);
        headerRow.setBackgroundColor(Color.MAGENTA);
        headerRow.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        headerRow.setPadding(15, 15, 15, 15);

        TextView slNoHeader = createHeaderTextView("Sl No");
        headerRow.addView(slNoHeader);

        TextView courseCodeHeader = createHeaderTextView("Course Code");
        headerRow.addView(courseCodeHeader);

        TextView courseTitleHeader = createHeaderTextView("Course Title");
        headerRow.addView(courseTitleHeader);

        TextView downloadHeader = createHeaderTextView("Download");
        headerRow.addView(downloadHeader);

        headerTableLayout.addView(headerRow);
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(UserCourseActivity.this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(15, 15, 15, 15);
        return textView;
    }

    private void addTableRow(int index, DataItem item) {
        TableRow row = new TableRow(UserCourseActivity.this);
        row.setPadding(10, 10, 10, 10);

        TextView slNoTextView = createTextView(String.valueOf(index + 1));
        row.addView(slNoTextView);

        TextView courseCodeTextView = createTextView(item.getCourseCode());
        courseCodeTextView.setGravity(Gravity.START);
        row.addView(courseCodeTextView);

        TextView courseTitleTextView = createTextView(item.getCourseTitle());
        courseTitleTextView.setGravity(Gravity.START);
        row.addView(courseTitleTextView);

        TextView downloadTextView = createDownloadTableCell(item.getPdfUrl(), item.getCourseTitle());
        row.addView(downloadTextView);

        headerTableLayout.addView(row);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(UserCourseActivity.this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(15, 15, 15, 15);
        return textView;
    }

    private void showNoDataAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserCourseActivity.this);
        builder.setTitle("No Course Found");
        builder.setMessage("There is no data available here.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed(); // Go back
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private TextView createDownloadTableCell(final String pdfUrl,String courseTitle) {
        TextView textView = new TextView(UserCourseActivity.this);
        textView.setText(R.string.pdf);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 10, 10, 10);
        textView.setTextColor(Color.BLUE);
        textView.setOnClickListener(v -> {
            // Add your code to handle download here
            showDownloadDialog(pdfUrl,courseTitle);
        });
        return textView;
    }

    private void showDownloadDialog(String pdfUrl, String courseTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserCourseActivity.this);
        builder.setTitle(courseTitle);
        builder.setMessage("Download will start soon...\nPress Ok to continue...");
        builder.setPositiveButton("OK", (dialog, which) -> startPdfDownload(pdfUrl, courseTitle));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void startPdfDownload(String pdfUrl, String courseTitle) {
        // Here's an example of how you can use the DownloadManager to start the download:
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(courseTitle);
        request.setDescription("Please wait while the PDF is being downloaded...");
        String fileName = courseTitle +"_sc.pdf"; // Set the desired file name here
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class DataItem {

        private String courseTitle;
        private String courseCode;
        private String pdfName;
        private String pdfUrl;
        private String CourseKey;
        private String semester;

        public DataItem() {
        }

        public DataItem(String courseTitle, String courseCode, String pdfName, String pdfUrl, String courseKey, String semester) {
            this.courseTitle = courseTitle;
            this.courseCode = courseCode;
            this.pdfName = pdfName;
            this.pdfUrl = pdfUrl;
            CourseKey = courseKey;
            this.semester = semester;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

        public String getPdfName() {
            return pdfName;
        }

        public void setPdfName(String pdfName) {
            this.pdfName = pdfName;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }

        public void setPdfUrl(String pdfUrl) {
            this.pdfUrl = pdfUrl;
        }

        public String getCourseKey() {
            return CourseKey;
        }

        public void setCourseKey(String courseKey) {
            CourseKey = courseKey;
        }

        public String getSemester() {
            return semester;
        }

        public void setSemester(String semester) {
            this.semester = semester;
        }
    }
}
