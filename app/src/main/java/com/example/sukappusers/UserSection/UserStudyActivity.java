package com.example.sukappusers.UserSection;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class UserStudyActivity extends BaseActivity {
    private DatabaseReference notesRef;
    private Spinner semesterSpinner;
    private Spinner optionsSpinner;
    private TextView semesterTextView;
    private TextView optionTextView;
    private TableLayout tableLayout;
    private StorageReference storageRef;
    private CircularProgressIndicator circularProgressIndicator;
    private LinearProgressIndicator toolbarProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_study);
        // Toolbar Title with back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Study Materials");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize database reference
        notesRef = FirebaseDatabase.getInstance().getReference("Notes");

        // Initialize storage reference
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference("notesPDFs");

        // Get references to Spinners and TextViews in the layout
        semesterSpinner = findViewById(R.id.spinner_Sem);
        optionsSpinner = findViewById(R.id.spinner_Options);
        semesterTextView = findViewById(R.id.semesterTextView);
        optionTextView = findViewById(R.id.optionTextView);
        circularProgressIndicator = findViewById(R.id.circularProgress);
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar);

        tableLayout = findViewById(R.id.NotesTableLayout);

        // Populate the semester Spinner
        populateSemesterSpinner();

    }

    private void populateSemesterSpinner() {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        // Hide the options spinner and optionTextView initially
        optionsSpinner.setVisibility(View.GONE);
        optionTextView.setVisibility(View.GONE);

        notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                List<String> semesters = new ArrayList<>();
                for (DataSnapshot semesterSnapshot : dataSnapshot.getChildren()) {
                    String semester = semesterSnapshot.getKey();
                    semesters.add(semester);
                }

                if (semesters.isEmpty()) {
                    semesters.add("No data found"); // Add a message when no data is found
                }

                // Create an ArrayAdapter for the semester spinner
                ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(UserStudyActivity.this, android.R.layout.simple_spinner_item, semesters);

                // Set the adapter for the semester spinner
                semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                semesterSpinner.setAdapter(semesterAdapter);

                // Set the selection listener for the semester spinner
                semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        String selectedSemester = parent.getItemAtPosition(position).toString();
                        if (selectedSemester.equals("No data found")) {
                            showNoDataAlertDialog();
                            semesterTextView.setText(selectedSemester);
                            optionsSpinner.setVisibility(View.GONE);
                            optionTextView.setVisibility(View.GONE);
                        } else {
                            semesterTextView.setText(selectedSemester); // Set the selected semester in the TextView
                            // Fetch and populate the options for the selected semester
                            populateOptionSpinner(selectedSemester);
                            // Show the options spinner and optionTextView
                            optionsSpinner.setVisibility(View.VISIBLE);
                            optionTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                        showNoDataAlertDialog();
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showNoDataAlertDialog();
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UserStudyActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateOptionSpinner(String selectedSemester) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        notesRef.child(selectedSemester).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<String> options = new ArrayList<>();
                for (DataSnapshot optionSnapshot : dataSnapshot.getChildren()) {
                    String option = optionSnapshot.getKey();
                    options.add(option);
                }

                if (options.isEmpty()) {
                    options.add("No data found"); // Add a message when no data is found
                }

                // Create an ArrayAdapter for the options spinner
                ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(UserStudyActivity.this, android.R.layout.simple_spinner_item, options);

                // Set the adapter for the options spinner
                optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                optionsSpinner.setAdapter(optionsAdapter);

                // Set the selection listener for the options spinner
                optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        circularProgressIndicator.setVisibility(View.INVISIBLE);
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                        String selectedOption = parent.getItemAtPosition(position).toString();
                        if (selectedOption.equals("No data found")) {
                            optionTextView.setText(""); // Clear the TextView if no data is found
                            showNoDataAlertDialog();
                        } else {
                            optionTextView.setText(selectedOption); // Set the selected option in the TextView
                            // Fetch and display the notes for the selected semester and option
                            displayNotes(selectedSemester, selectedOption);
                        }
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
                // Handle any errors
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UserStudyActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNotes(String selectedSemester, String selectedOption) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        toolbarProgressBar.setVisibility(View.VISIBLE);
        notesRef.child(selectedSemester).child(selectedOption).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                circularProgressIndicator.setVisibility(View.INVISIBLE);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                tableLayout.removeAllViews();
                createTableHeader();
                int index = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Notes notes = data.getValue(Notes.class);
                    if (notes != null) {
                        addTableRow(index, notes);
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
                Toast.makeText(UserStudyActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTableHeader() {
        TableRow headerRow = new TableRow(UserStudyActivity.this);
        headerRow.setBackgroundColor(Color.MAGENTA);
        headerRow.setPadding(15, 15, 15, 15);

        TextView slNoHeader = createHeaderTextView("Sl No");
        headerRow.addView(slNoHeader);

        TextView courseTitleHeader = createHeaderTextView("Course Title");
        headerRow.addView(courseTitleHeader);

        TextView notesHeader = createHeaderTextView("Notes");
        headerRow.addView(notesHeader);

        TextView downloadHeader = createHeaderTextView("Download");
        headerRow.addView(downloadHeader);

        tableLayout.addView(headerRow);
    }

    private void addTableRow(int index, Notes notes) {
        TableRow row = new TableRow(UserStudyActivity.this);
        row.setPadding(10, 10, 10, 10);

        TextView slNoTextView = createTextView(String.valueOf(index + 1));
        row.addView(slNoTextView);

        TextView courseTitleTextView = createTextView(notes.getCourseTitle());
        row.addView(courseTitleTextView);

        TextView notesTextView = createTextView(notes.getNotes());
        row.addView(notesTextView);

        TextView downloadTextView = createDownloadTableCell(notes.getPdfUrl(), notes.getNotes());
        row.addView(downloadTextView);

        tableLayout.addView(row);
    }

    private TextView createDownloadTableCell(final String pdfUrl, String courseTitle) {
        TextView textView = new TextView(UserStudyActivity.this);
        textView.setText(R.string.pdf);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 10, 10, 10);
        textView.setTextColor(Color.BLUE);
        textView.setOnClickListener(v -> {
            // Add your code to handle download here
            showDownloadDialog(pdfUrl, courseTitle);
        });
        return textView;
    }

    private void showDownloadDialog(final String pdfUrl, String courseTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserStudyActivity.this);
        builder.setTitle(courseTitle);
        builder.setMessage("Download will start soon...\nPress Ok to continue...");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPdfDownload(pdfUrl, courseTitle);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void startPdfDownload(String pdfUrl, String courseTitle) {

        // Here's an example of how you can use the DownloadManager to start the download:
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(courseTitle);
        request.setDescription("Please wait while the PDF is being downloaded...");
        String fileName = courseTitle + "notes.pdf"; // Set the desired file name here
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }


    private TextView createTextView(String text) {
        TextView textView = new TextView(UserStudyActivity.this);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(10, 10, 10, 10);
        return textView;
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = createTextView(text);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        return textView;
    }

    private void showNoDataAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserStudyActivity.this);
        builder.setTitle("No Notes Found");
        builder.setMessage("There is no data available here.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed(); // Go back
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void onDestroy() {
        super.onDestroy();
    }


    static class Notes {
        private String notesKey;
        private String notes;
        private String courseTitle;
        private String option;
        private String pdfUrl;
        private String pdfName;
        private String semester;

        public Notes() {
            // Default constructor required for Firebase Realtime Database
        }

        public Notes(String notesKey, String notes, String courseTitle, String option, String pdfUrl, String pdfName, String semester) {
            this.notesKey = notesKey;
            this.notes = notes;
            this.courseTitle = courseTitle;
            this.option = option;
            this.pdfUrl = pdfUrl;
            this.pdfName = pdfName;
            this.semester = semester;
        }

        public String getSemester() {
            return semester;
        }

        public void setSemester(String semester) {
            this.semester = semester;
        }

        public String getNotesKey() {
            return notesKey;
        }

        public void setNotesKey(String notesKey) {
            this.notesKey = notesKey;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }

        public void setPdfUrl(String pdfUrl) {
            this.pdfUrl = pdfUrl;
        }

        public String getPdfName() {
            return pdfName;
        }

        public void setPdfName(String pdfName) {
            this.pdfName = pdfName;
        }
    }
}