package com.example.mynotes;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateNoteActivity extends AppCompatActivity {

    // UI elements for creating a note
    private EditText mCreateTitleOfNote, mCreateContentOfNote;
    private FloatingActionButton mSaveNote;

    // Firebase components
    public FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // Initialize UI elements
        mSaveNote = findViewById(R.id.savenote);
        mCreateContentOfNote = findViewById(R.id.createcontentofnote);
        mCreateTitleOfNote = findViewById(R.id.createtitleofnote);
        ProgressBar progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar

        // Set up the toolbar with a back button
        Toolbar toolbar = findViewById(R.id.toolbarofcreatenote);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase instances for authentication and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Set up a click listener on the save button
        mSaveNote.setOnClickListener(view -> {
            // Get the title and content from the EditText fields
            String title = mCreateTitleOfNote.getText().toString().trim();
            String content = mCreateContentOfNote.getText().toString().trim();

            // Check if either field is empty and show a toast message if so
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Both fields are required.", Toast.LENGTH_SHORT).show();
            } else {
                // Show ProgressBar and hide the button while saving
                progressBar.setVisibility(View.VISIBLE);
                mSaveNote.setVisibility(View.INVISIBLE);
                saveNoteToFirebase(title, content, progressBar); // Call method to save note
            }
        });
    }

    // Method to save the note to Firebase Firestore
    private void saveNoteToFirebase(String title, String content, ProgressBar progressBar) {
        // Create a new document reference for the note
        DocumentReference documentReference = firebaseFirestore.collection("notes")
                .document(firebaseUser.getUid())
                .collection("myNotes")
                .document();

        // Create a map to hold the note data
        Map<String, Object> note = new HashMap<>();
        note.put("title", title);   // Add title to the note map
        note.put("content", content); // Add content to the note map

        // Save the note in Firestore
        documentReference.set(note)
                .addOnSuccessListener(unused -> {
                    // Hide ProgressBar and show the save button upon success
                    progressBar.setVisibility(View.GONE);
                    mSaveNote.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Note Created.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class)); // Navigate to NotesActivity
                    finish(); // Close CreateNoteActivity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create note", e); // Log the error
                    // Hide ProgressBar and show the button upon failure
                    progressBar.setVisibility(View.GONE);
                    mSaveNote.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Failed to Create Note.", Toast.LENGTH_SHORT).show();
                });
    }

    // Handle back navigation when the toolbar back button is pressed
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Use OnBackPressedDispatcher to handle back press
            OnBackPressedDispatcher onBackPressedDispatcher = new OnBackPressedDispatcher();
            onBackPressedDispatcher.onBackPressed();
            return true; // Indicate that the back press was handled
        }
        return super.onOptionsItemSelected(item); // Handle other menu items normally
    }
}