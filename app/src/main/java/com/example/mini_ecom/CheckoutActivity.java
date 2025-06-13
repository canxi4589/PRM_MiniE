package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private TextView orderSummaryTextView;
    private TextView totalPriceTextView;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText addressEditText;
    private EditText phoneEditText;
    private MaterialButton placeOrderButton;
    private static final double USD_TO_VND = 25000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupToolbar();
        loadOrderSummary();
        setupPlaceOrderButton();
    }

    private void initViews() {
        orderSummaryTextView = findViewById(R.id.orderSummaryTextView);
        totalPriceTextView = findViewById(R.id.checkoutTotalPriceTextView);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addressEditText = findViewById(R.id.addressEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        placeOrderButton = findViewById(R.id.placeOrderButton);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.checkoutToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Checkout");
        }
    }

    private void loadOrderSummary() {
        CartManager cartManager = CartManager.getInstance();
        List<CartItem> cartItems = cartManager.getCartItems();
        
        StringBuilder summary = new StringBuilder();
        for (CartItem item : cartItems) {
            summary.append(item.getProduct().getName())
                   .append(" x")
                   .append(item.getQuantity())
                   .append(" - $")
                   .append(String.format(Locale.getDefault(), "%.2f", item.getTotalPrice()))
                   .append("\n");
        }
        
        orderSummaryTextView.setText(summary.toString());
        totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", cartManager.getTotalPrice()));
    }

    private void setupPlaceOrderButton() {
        placeOrderButton.setOnClickListener(v -> {
            if (validateForm()) {
                placeOrder();
            }
        });
    }

    private boolean validateForm() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (address.isEmpty()) {
            addressEditText.setError("Address is required");
            addressEditText.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void placeOrder() {
        // In a real app, you would send this data to a server
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        
        double totalAmountUSD = CartManager.getInstance().getTotalPrice();
        if (totalAmountUSD < 0.25) { // Stripe yêu cầu tối thiểu 0.25 USD
            Toast.makeText(this, "Số tiền thanh toán phải từ $0.25 trở lên!", Toast.LENGTH_LONG).show();
            return;
        }
        // Simulate order processing
        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
        
        // Clear the cart
        CartManager.getInstance().clearCart();
        
        // Navigate to PaymentGateway (Stripe)
        Intent intent = new Intent(this, PaymentGateway.class);
        intent.putExtra("total_amount", totalAmountUSD); // Truyền vào số tiền thanh toán
        intent.putExtra("currency", "usd"); // Truyền currency động
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 