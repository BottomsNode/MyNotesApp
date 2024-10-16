package com.example.mynotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton; // Updated import for MaterialButton
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

public class ForgetPassword extends AppCompatActivity {

    private static final String TAG = "ForgetPassword"; // Tag for logging

    // Declare UI elements and FirebaseAuth instance
    private EditText mforgotpassword; // EditText for user to input their email
    private FirebaseAuth firebaseAuth; // Firebase authentication instance
    private ProgressBar progressBar; // ProgressBar to show loading status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Set the content view to the forget password layout
            setContentView(R.layout.activity_forget_password);

            // Ensure the ActionBar is visible
            Objects.requireNonNull(getSupportActionBar()).show();

            // Initialize UI elements
            mforgotpassword = findViewById(R.id.forgetpassword); // EditText for email input
            MaterialButton mpasswordrecoverbutton = findViewById(R.id.passwordrecoverbutton); // Button to recover password
            TextView mgobacktologin = findViewById(R.id.gobacktologin); // TextView to go back to login
            progressBar = findViewById(R.id.progressBar); // ProgressBar for loading status
            firebaseAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

            // Set an OnClickListener to navigate back to the MainActivity (Login screen)
            mgobacktologin.setOnClickListener(view -> {
                Intent intent = new Intent(ForgetPassword.this, MainActivity.class);
                startActivity(intent);
            });

            // Set an OnClickListener for the password recovery button
            mpasswordrecoverbutton.setOnClickListener(view -> {
                String mail = mforgotpassword.getText().toString().trim(); // Get the email input

                // Check if the email field is empty
                if (mail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter your email first", Toast.LENGTH_SHORT).show();
                } else {
                    // Show ProgressBar and hide the button while the task is in progress
                    progressBar.setVisibility(View.VISIBLE);
                    mpasswordrecoverbutton.setVisibility(View.INVISIBLE);

                    // Send password recovery email using FirebaseAuth
                    firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(task -> {
                        // After the task is completed, hide ProgressBar and show the button again
                        progressBar.setVisibility(View.GONE);
                        mpasswordrecoverbutton.setVisibility(View.VISIBLE);

                        // Check if the email was sent successfully
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Mail sent. You can recover your password using the email.", Toast.LENGTH_SHORT).show();
                            finish(); // Close the current activity
                            startActivity(new Intent(ForgetPassword.this, MainActivity.class)); // Navigate to MainActivity
                        } else {
                            // Handle errors and show an appropriate message
                            Toast.makeText(getApplicationContext(), "Email is wrong or account does not exist.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "sendPasswordResetEmail failed", task.getException());
                        }
                    });
                }
            });
        } catch (Exception e) {
            // Log any exceptions and show a general error message
            Log.e(TAG, "Exception in onCreate", e);
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }
}