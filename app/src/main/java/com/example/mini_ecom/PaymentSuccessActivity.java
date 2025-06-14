package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentSuccessActivity extends AppCompatActivity {
    private TextView transactionIdText;
    private TextView orderIdText;
    private TextView amountText;
    private Button continueShoppingButton;
    private Button viewOrdersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        initViews();
        displayPaymentInfo();
        setupClickListeners();
    }

    private void initViews() {
        transactionIdText = findViewById(R.id.transactionIdText);
        orderIdText = findViewById(R.id.orderIdText);
        amountText = findViewById(R.id.amountText);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);
        viewOrdersButton = findViewById(R.id.viewOrdersButton);
    }

    private void displayPaymentInfo() {
        Intent intent = getIntent();
        String transactionId = intent.getStringExtra("transaction_id");
        String orderId = intent.getStringExtra("order_id");
        double amount = intent.getDoubleExtra("amount", 0.0);

        transactionIdText.setText("Transaction ID: " + (transactionId != null ? transactionId : "N/A"));
        orderIdText.setText("Order ID: " + (orderId != null ? orderId : "N/A"));
        amountText.setText(String.format("Amount Paid: $%.2f", amount));
    }

    private void setupClickListeners() {
        continueShoppingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        viewOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to payment processing
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
} 