package com.example.mynotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;  // Import the SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Random;

public class NotesActivity extends AppCompatActivity {

    private static final String TAG = "NotesActivity"; // Tag for logging
    private LinearLayout notesContainer; // Container for displaying notes
    private FirebaseAuth firebaseAuth; // Firebase authentication instance
    private FirebaseUser firebaseUser; // Currently authenticated user
    private FirebaseFirestore firebaseFirestore; // Firestore database instance
    private ProgressBar progressBar; // ProgressBar to indicate loading
    private SwipeRefreshLayout swipeRefreshLayout;  // SwipeRefreshLayout for refreshing notes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes); // Set the content view

        // Initialize UI and Firebase components
        FloatingActionButton mCreateNotesFab = findViewById(R.id.createnotefab);
        Button mLogoutButton = findViewById(R.id.logoutbtn);
        notesContainer = findViewById(R.id.notes_container);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);  // Initialize SwipeRefreshLayout

        firebaseAuth = FirebaseAuth.getInstance(); // Get FirebaseAuth instance
        firebaseFirestore = FirebaseFirestore.getInstance(); // Get Firestore instance
        firebaseUser = firebaseAuth.getCurrentUser(); // Get current user

        // Check if user is authenticated
        if (firebaseUser == null) {
            startActivity(new Intent(this, MainActivity.class)); // Navigate to MainActivity if not authenticated
            finish(); // Close the current activity
            return;
        }

        // Set up UI actions
        mCreateNotesFab.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar while starting CreateNoteActivity
            startActivity(new Intent(this, CreateNoteActivity.class));
            progressBar.setVisibility(View.GONE); // Hide progress bar
        });

        mLogoutButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar while logging out
            firebaseAuth.signOut(); // Sign out the user
            startActivity(new Intent(this, MainActivity.class)); // Navigate to MainActivity
            finish(); // Close the current activity
        });

        // Set up SwipeRefreshLayout for refreshing notes
        swipeRefreshLayout.setOnRefreshListener(this::fetchAndDisplayNotes);  // Fetch notes on refresh

        // Fetch and display notes when the activity is created
        fetchAndDisplayNotes();
    }

    private void fetchAndDisplayNotes() {
        progressBar.setVisibility(View.VISIBLE);  // Show progress bar while fetching notes
        swipeRefreshLayout.setRefreshing(true);   // Show refresh indicator

        // Fetch notes from Firestore
        firebaseFirestore.collection("notes")
                .document(firebaseUser.getUid()) // Get user's notes document
                .collection("myNotes") // Get the user's notes collection
                .orderBy("title", Query.Direction.ASCENDING) // Order notes by title
                .addSnapshotListener((snapshots, e) -> {
                    // Hide progress bar and refresh indicator
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (e != null) {
                        Log.e(TAG, "Error fetching notes", e); // Log the error
                        Toast.makeText(this, "Error fetching notes", Toast.LENGTH_SHORT).show(); // Show error message
                        return;
                    }

                    // Check if snapshots is not null
                    if (snapshots != null) {
                        notesContainer.removeAllViews();  // Clear existing notes

                        // Iterate through the documents and create note views
                        for (QueryDocumentSnapshot document : snapshots) {
                            View noteView = LayoutInflater.from(this).inflate(R.layout.notes_layout, notesContainer, false);
                            TextView noteTitle = noteView.findViewById(R.id.notetitle);
                            TextView noteContent = noteView.findViewById(R.id.notecontent);

                            // Set the title and content of the note
                            noteTitle.setText(document.getString("title"));
                            noteContent.setText(document.getString("content"));

                            // Get a random color and apply it to the note's background
                            int randomColor = getResources().getColor(getRandomColor(), null);
                            noteView.setBackgroundColor(randomColor);

                            // Set the click listener for the three-dot menu
                            ImageView threeDotMenu = noteView.findViewById(R.id.menupopbutton);
                            threeDotMenu.setOnClickListener(v -> showPopupMenu(v, document.getId())); // Show popup menu for note actions

                            notesContainer.addView(noteView);  // Add the note view to the container
                        }
                    }
                });
    }

    private void showPopupMenu(View view, String noteId) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.note_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_update) {
                // If update action is selected, navigate to UpdateNoteActivity
                Intent intent = new Intent(NotesActivity.this, UpdateNoteActivity.class);
                intent.putExtra("NOTE_ID", noteId); // Pass the note ID to update activity
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                // If delete action is selected, prompt for deletion confirmation
                deleteNote(noteId);
                return true;
            } else {
                return false; // Unrecognized menu item
            }
        });
        popupMenu.show(); // Show the popup menu
    }

    private void deleteNote(String noteId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> firebaseFirestore.collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("myNotes")
                        .document(noteId) // Get the note to delete
                        .delete() // Delete the note
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(NotesActivity.this, "Note deleted successfully", Toast.LENGTH_SHORT).show(); // Show success message
                            fetchAndDisplayNotes();  // Refresh notes after deletion
                        })
                        .addOnFailureListener(e -> Toast.makeText(NotesActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show())) // Show error message
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Dismiss the dialog on cancel
                .setIcon(android.R.drawable.ic_dialog_alert) // Set dialog icon
                .show(); // Show the dialog
    }

    private int getRandomColor() {
        // Define an array of colors to choose from
        int[] colors = new int[]{
                R.color.pastelPink, R.color.pastelBlue,
                R.color.pastelGreen, R.color.pastelYellow,
                R.color.brightRed, R.color.vibrantOrange,
                R.color.limeGreen, R.color.vibrantBlue,
                R.color.forestGreen, R.color.clayRed,
                R.color.warmYellow, R.color.oceanBlue,
                R.color.darkBackground, R.color.darkPrimary,
                R.color.darkSecondary, R.color.deepPurple,
                R.color.charcoal, R.color.warmTaupe,
                R.color.lightCream, R.color.coolGray,
                R.color.sunsetOrange, R.color.softLavender,
                R.color.horizonBlue, R.color.nightSky,
                R.color.platinum, R.color.seafoamGreen,
        };

        Random random = new Random(); // Create a Random instance
        int randomColorIndex = random.nextInt(colors.length); // Get a random index
        return colors[randomColorIndex]; // Return the color at the random index
    }
}