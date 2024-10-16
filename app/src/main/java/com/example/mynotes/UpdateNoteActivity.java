package com.example.mynotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ProgressBar; // Import ProgressBar
import java.util.Objects;

public class UpdateNoteActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent; // EditText fields for title and content
    private String noteId; // ID of the note to be updated
    private FirebaseFirestore firebaseFirestore; // Firestore instance
    private ProgressBar progressBar; // ProgressBar for indicating loading state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_note); // Set the content view

        // Initialize UI elements
        editTextTitle = findViewById(R.id.editTextTitle); // Title EditText
        editTextContent = findViewById(R.id.editTextContent); // Content EditText
        Button buttonUpdate = findViewById(R.id.buttonUpdate); // Update button
        progressBar = findViewById(R.id.progressBar); // Initialize the ProgressBar
        firebaseFirestore = FirebaseFirestore.getInstance(); // Get Firestore instance

        // Retrieve note ID from intent
        noteId = getIntent().getStringExtra("NOTE_ID");
        if (noteId != null) {
            loadNoteData(noteId); // Load note data if ID is present
        } else {
            // Show error and finish the activity if no note ID is provided
            Toast.makeText(this, "Error: Note ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set OnClickListener for the update button
        buttonUpdate.setOnClickListener(v -> updateNote());
    }

    private void loadNoteData(String noteId) {
        showProgressBar(true); // Show progress bar while loading note data

        // Fetch note data from Firestore
        firebaseFirestore.collection("notes")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()) // Get user document
                .collection("myNotes") // Access user's notes collection
                .document(noteId) // Reference the specific note document
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showProgressBar(false); // Hide progress bar after fetching
                    if (documentSnapshot.exists()) { // Check if the note exists
                        String title = documentSnapshot.getString("title"); // Get title
                        String content = documentSnapshot.getString("content"); // Get content
                        editTextTitle.setText(title); // Set title in EditText
                        editTextContent.setText(content); // Set content in EditText
                    } else {
                        // Show error if the note doesn't exist
                        Toast.makeText(UpdateNoteActivity.this, "Note not found", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    }
                })
                .addOnFailureListener(e -> {
                    showProgressBar(false); // Hide progress bar on failure
                    // Show error message
                    Toast.makeText(UpdateNoteActivity.this, "Error loading note", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNote() {
        // Get text from EditText fields
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        // Check if title or content is empty
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Title and Content cannot be empty", Toast.LENGTH_SHORT).show();
            return; // Exit method if validation fails
        }

        showProgressBar(true); // Show progress bar while updating note

        // Update note in Firestore
        firebaseFirestore.collection("notes")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()) // Get user document
                .collection("myNotes") // Access user's notes collection
                .document(noteId) // Reference the specific note document
                .update("title", title, "content", content) // Update title and content
                .addOnSuccessListener(aVoid -> {
                    showProgressBar(false); // Hide progress bar after update
                    Toast.makeText(UpdateNoteActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after successful update
                })
                .addOnFailureListener(e -> {
                    showProgressBar(false); // Hide progress bar on failure
                    // Show error message
                    Toast.makeText(UpdateNoteActivity.this, "Error updating note", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressBar(boolean show) {
        // Show or hide the progress bar based on the parameter
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}