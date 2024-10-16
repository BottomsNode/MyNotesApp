package com.example.mynotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;
import android.widget.ProgressBar; // Import ProgressBar

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag for logging

    private EditText mloginemail, mloginpassword; // EditText fields for user input
    private FirebaseAuth firebaseAuth; // Firebase authentication instance
    private ProgressBar progressBar; // ProgressBar to indicate loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Set the content view to the main activity layout
            setContentView(R.layout.activity_main);

            // Ensure the ActionBar is visible
            Objects.requireNonNull(getSupportActionBar()).show();

            // Initialize views
            mloginemail = findViewById(R.id.loginemail); // EditText for email input
            mloginpassword = findViewById(R.id.loginpassword); // EditText for password input
            RelativeLayout mlogin = findViewById(R.id.login); // Layout for login button
            TextView mgotoforgotpassword = findViewById(R.id.gotoforgotpassword); // TextView for forgot password
            RelativeLayout mgotosignup = findViewById(R.id.gotosignup); // Layout for signup button
            progressBar = findViewById(R.id.progressBar); // ProgressBar for loading status
            firebaseAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

            // Check if the user is already logged in
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                // If logged in, navigate to NotesActivity
                finish();
                startActivity(new Intent(MainActivity.this, NotesActivity.class));
            }

            // Set onClickListener for the signup button to navigate to SignupPage
            mgotosignup.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SignupPage.class)));

            // Set onClickListener for the forgot password text to navigate to ForgetPassword
            mgotoforgotpassword.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ForgetPassword.class)));

            // Set onClickListener for the login button
            mlogin.setOnClickListener(view -> {
                // Get user input from EditTexts
                String mail = mloginemail.getText().toString().trim();
                String password = mloginpassword.getText().toString().trim();

                // Check if email or password fields are empty
                if (mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All Fields Are Required", Toast.LENGTH_SHORT).show();
                } else {
                    // Show ProgressBar while authenticating
                    progressBar.setVisibility(View.VISIBLE);
                    // Authenticate user with Firebase
                    firebaseAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar after authentication
                        if (task.isSuccessful()) {
                            // Check if the email is verified after login
                            checkMailVerification();
                        } else {
                            // Show error message if authentication fails
                            Toast.makeText(getApplicationContext(), "Account does not exist", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "signInWithEmailAndPassword failed", task.getException());
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

    // Check if the email is verified after login
    private void checkMailVerification() {
        try {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); // Get the current user
            if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                // If email is verified, navigate to NotesActivity
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(MainActivity.this, NotesActivity.class));
            } else {
                // If email is not verified, show a message and sign out
                Toast.makeText(getApplicationContext(), "Verify your Mail First", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
            }
        } catch (Exception e) {
            // Log any exceptions that occur
            Log.e(TAG, "Exception in checkMailVerification", e);
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }
}