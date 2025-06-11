package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class FailureActivity extends AppCompatActivity {

    private static final int REDIRECT_DELAY = 3000; // 3 seconds delay (slightly longer for failure)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.failure_popup);

        // Auto redirect after delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                redirectToCheckout();
            }
        }, REDIRECT_DELAY);

        // Optional: Allow manual redirect by tapping
        findViewById(R.id.failureCard).setOnClickListener(v -> redirectToCheckout());
    }

    private void redirectToCheckout() {
        Intent intent = new Intent(FailureActivity.this, CheckoutActivity.class);
        // Clear the activity stack so user can't go back to failure screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Optional: Pass error message or reason
        intent.putExtra("payment_failed", true);
        intent.putExtra("error_message", "Payment processing failed. Please try again.");

        startActivity(intent);
        finish(); // Close the failure activity

        // Optional: Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Prevent going back, redirect to checkout instead
        super.onBackPressed();
        redirectToCheckout();
    }
}
