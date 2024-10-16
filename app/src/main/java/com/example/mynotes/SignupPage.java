package com.example.mynotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button; // Change this to Button
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupPage extends AppCompatActivity {

    private static final String TAG = "SignupPage";

    private EditText msignupemail, msignuppassword;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar; // Declare the ProgressBar
    public Button msignup; // Change to Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        // Initialize the ProgressBar
        progressBar = findViewById(R.id.progressBar);

        // Other initializations
        msignupemail = findViewById(R.id.sigupemail);
        msignuppassword = findViewById(R.id.siguppassword);
        msignup = findViewById(R.id.signup); // Corrected initialization
        TextView mgotologin = findViewById(R.id.gotologin);
        firebaseAuth = FirebaseAuth.getInstance();

        mgotologin.setOnClickListener(view -> {
            Intent intent = new Intent(SignupPage.this, MainActivity.class);
            startActivity(intent);
        });

        // Use button click listener for signup button
        msignup.setOnClickListener(view -> {
            String mail = msignupemail.getText().toString().trim();
            String password = msignuppassword.getText().toString().trim();

            if (mail.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "All fields are Required.", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 7) {
                Toast.makeText(getApplicationContext(), "Password must be greater than 7 chars or digits.", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE); // Show progress bar

                // Create user
                firebaseAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                        sendEmailVerification();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to Register", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "createUserWithEmailAndPassword failed", task.getException());
                    }
                });
            }
        });
    }

    private void sendEmailVerification() {
        // Sending email verification
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE); // Hide progress bar
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Email sent, Verify it to LogIn Again", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    startActivity(new Intent(SignupPage.this, MainActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to send Email, Try Again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE); // Hide progress bar
            Toast.makeText(getApplicationContext(), "Failed to send Email, Try Again", Toast.LENGTH_SHORT).show();
        }
    }
}