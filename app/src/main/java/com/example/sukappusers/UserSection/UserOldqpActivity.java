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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserOldqpActivity extends BaseActivity {
    private DatabaseReference qpRef;
    private Spinner semesterSpinner;
    private Spinner examYearSpinner;
    private ArrayAdapter<String> examYearAdapter;
    private TextView semesterTextView;
    private TextView examYearTextView;
    private TableLayout tableLayout;
    private CircularProgressIndicator circularProgressIndicator;
    private LinearProgressIndicator toolbarProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_oldqp);

        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Old Question Paper");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize database reference
        qpRef = FirebaseDatabase.getInstance().getReference("OldquestionPaper");

        // Get references to Spinners and TextViews in the layout
        semesterSpinner = findViewById(R.id.spinner_Sem);
        examYearSpinner = findViewById(R.id.spinner_examYear);
        semesterTextView = findViewById(R.id.semesterTextView);
        examYearTextView = findViewById(R.id.examYearTextView);
        circularProgressIndicator = findViewById(R.id.circularProgress);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);
        tableLayout = findViewById(R.id.TableLayout);

        // Populate the semester Spinner
        populateSemesterSpinner();
        examYearSpinner.setVisibility(View.GONE);

    }

    private void populateSemesterSpinner() {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        circularProgressIndicator.setVisibility(View.VISIBLE);
        qpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                List<String> semesters = new ArrayList<>();
                for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {
                    String semester = yearSnapshot.getKey();
                    semesters.add(semester);
                }

                if (semesters.isEmpty()) {
                    semesters.add("No data found"); // Add a message when no data is found
                }

                // Create an ArrayAdapter for the semester Spinner
                ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(UserOldqpActivity.this, android.R.layout.simple_spinner_item, semesters);

                // Set the adapter for the semester Spinner
                semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(semesterAdapter);

                // Set the selection listener for the semester Spinner
                semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        String selectedSemester = parent.getItemAtPosition(position).toString();
                        if (selectedSemester.equals("No data found")) {
                            semesterTextView.setText(selectedSemester);
                            showNoDataAlertDialog();
                            // Hide the exam year Spinner when no data is found in the semester Spinner
                            examYearSpinner.setVisibility(View.GONE);
                            examYearTextView.setVisibility(View.GONE);
                        } else {
                            semesterTextView.setText(selectedSemester); // Set the selected semester in the TextView
                            // Fetch and populate the exam years for the selected semester
                            populateExamYearSpinner(selectedSemester);
                            // Show the exam year Spinner when data is available in the semester Spinner
                            examYearSpinner.setVisibility(View.VISIBLE);
                            examYearTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        // Do nothing
                        showNoDataAlertDialog();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                showNoDataAlertDialog();
                Toast.makeText(UserOldqpActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateExamYearSpinner(final String selectedSemester) {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        circularProgressIndicator.setVisibility(View.VISIBLE);
        qpRef.child(selectedSemester).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> examYears = new ArrayList<>();
                for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {
                    String examYear = yearSnapshot.getKey();
                    examYears.add(examYear);
                }

                if (examYears.isEmpty()) {
                    showNoDataAlertDialog();
                    return;
                }

                // Create an ArrayAdapter for the exam year Spinner
                examYearAdapter = new ArrayAdapter<>(UserOldqpActivity.this, android.R.layout.simple_spinner_item, examYears);

                // Set the adapter for the exam year Spinner
                examYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                examYearSpinner.setAdapter(examYearAdapter);

                // Set the selection listener for the exam year Spinner
                examYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        String selectedExamYear = parent.getItemAtPosition(position).toString();
                        examYearTextView.setText(selectedExamYear); // Set the selected exam year in the TextView
                        // Fetch and display the old question papers for the selected semester and exam year
                        displayOldQuestionPapers(selectedSemester, selectedExamYear);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        // Do nothing
                        showNoDataAlertDialog();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                showNoDataAlertDialog();
                Toast.makeText(UserOldqpActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOldQuestionPapers(String selectedSemester, String selectedExamYear) {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        circularProgressIndicator.setVisibility(View.VISIBLE);
        qpRef.child(selectedSemester).child(selectedExamYear).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                tableLayout.removeAllViews();
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
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                showNoDataAlertDialog();
                Toast.makeText(UserOldqpActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createTableHeader() {
        TableRow headerRow = new TableRow(UserOldqpActivity.this);
        headerRow.setBackgroundColor(Color.MAGENTA);
        headerRow.setPadding(15, 15, 15, 15);

        TextView slNoHeader = createHeaderTextView("Sl No");
        headerRow.addView(slNoHeader);

        TextView courseTitleHeader = createHeaderTextView("Course Title");
        headerRow.addView(courseTitleHeader);

        TextView downloadHeader = createHeaderTextView("Download");
        headerRow.addView(downloadHeader);

        tableLayout.addView(headerRow);
    }

    private void addTableRow(int index, DataItem item) {
        TableRow row = new TableRow(UserOldqpActivity.this);
        row.setPadding(20, 20, 20, 20);
        TextView slNoTextView = createTextView(String.valueOf(index + 1));
        row.addView(slNoTextView);
        TextView courseTitleTextView = createTextView(item.getCourseTitle());
        row.addView(courseTitleTextView);
        TextView downloadTextView = createDownloadTableCell(item.getPdfUrl(), item.getCourseTitle());
        row.addView(downloadTextView);

        tableLayout.addView(row);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(UserOldqpActivity.this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(15, 15, 15, 15);
        return textView;
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = createTextView(text);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        return textView;
    }

    private TextView createDownloadTableCell(final String pdfUrl,String courseTitle) {
        TextView textView = new TextView(UserOldqpActivity.this);
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


    private void showDownloadDialog(final String pdfUrl, String courseTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserOldqpActivity.this);
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
        String fileName = courseTitle+"_qp.pdf"; // Set the desired file name here
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }


    private void showNoDataAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserOldqpActivity.this);
        builder.setTitle("No Question Paper Found");
        builder.setMessage("There is no data available here.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed(); // Go back
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class DataItem {

        private String courseTitle;
        private String examYear;
        private String pdfName;
        private String pdfUrl;
        private String qpKey;
        private String semester;

        public DataItem() {
        }

        public DataItem(String courseTitle, String examYear, String pdfName, String pdfUrl, String qpKey, String semester) {
            this.courseTitle = courseTitle;
            this.examYear = examYear;
            this.pdfName = pdfName;
            this.pdfUrl = pdfUrl;
            this.qpKey = qpKey;
            this.semester = semester;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }

        public String getExamYear() {
            return examYear;
        }

        public void setExamYear(String examYear) {
            this.examYear = examYear;
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

        public String getQpKey() {
            return qpKey;
        }

        public void setQpKey(String qpKey) {
            this.qpKey = qpKey;
        }

        public String getSemester() {
            return semester;
        }

        public void setSemester(String semester) {
            this.semester = semester;
        }
    }
}

