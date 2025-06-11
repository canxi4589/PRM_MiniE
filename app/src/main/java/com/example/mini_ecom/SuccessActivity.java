package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    private static final int REDIRECT_DELAY = 2000; // 2 seconds delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success_popup);

        // Auto redirect after delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                redirectToMain();
            }
        }, REDIRECT_DELAY);

        // Optional: Allow manual redirect by tapping
        findViewById(R.id.successCard).setOnClickListener(v -> redirectToMain());
    }

    private void redirectToMain() {
        Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
        // Clear the activity stack so user can't go back to success screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the success activity

        // Optional: Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Prevent going back, redirect to main instead
        super.onBackPressed();
        redirectToMain();
    }
}