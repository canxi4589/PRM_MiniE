package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_ecom.payment.PaymentManager;
import com.example.mini_ecom.payment.PaymentProcessor;
import com.example.mini_ecom.payment.PaymentResult;
import com.example.mini_ecom.payment.vnpay.VNPayRequest;
import com.example.mini_ecom.payment.stripe.StripeRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentProcessingActivity extends AppCompatActivity {
    private static final String TAG = "PaymentProcessing";
    
    private TextView statusText;
    private ProgressBar progressBar;
    private Button retryButton;
    
    private PaymentProcessor.PaymentMethod paymentMethod;
    private double totalAmount;
    private String customerName, customerEmail, customerPhone, customerAddress;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        initViews();
        getIntentData();
        processPayment();
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);
        retryButton = findViewById(R.id.retryButton);
        
        retryButton.setOnClickListener(v -> processPayment());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String methodName = intent.getStringExtra("payment_method");
        paymentMethod = PaymentProcessor.PaymentMethod.valueOf(methodName);
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        customerName = intent.getStringExtra("customer_name");
        customerEmail = intent.getStringExtra("customer_email");
        customerPhone = intent.getStringExtra("customer_phone");
        customerAddress = intent.getStringExtra("customer_address");
        
        // Generate unique order ID
        orderId = "ORDER_" + System.currentTimeMillis();
    }

    private void processPayment() {
        showLoading();
        
        PaymentManager paymentManager = PaymentManager.getInstance();
        
        // Create payment request based on method
        switch (paymentMethod) {
            case VNPAY:
                processVNPayPayment(paymentManager);
                break;
            case STRIPE:
                processStripePayment(paymentManager);
                break;
            default:
                showError("Payment method not implemented yet");
                break;
        }
    }

    private void processVNPayPayment(PaymentManager paymentManager) {
        VNPayRequest request = new VNPayRequest(
            totalAmount,
            orderId,
            "Payment for order " + orderId
        );
        
        paymentManager.processPayment(
            paymentMethod,
            this,
            request,
            new PaymentProcessor.PaymentCallback() {
                @Override
                public void onPaymentSuccess(PaymentResult result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        showSuccess(result);
                    });
                }

                @Override
                public void onPaymentFailure(PaymentResult result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        showError(result.getMessage());
                    });
                }

                @Override
                public void onPaymentCancelled() {
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Payment was cancelled");
                    });
                }
            }
        );
    }

    private void processStripePayment(PaymentManager paymentManager) {
        StripeRequest request = new StripeRequest(
            totalAmount,
            "usd", // You can make this configurable
            orderId,
            "Payment for order " + orderId,
            customerName,
            customerEmail,
            customerPhone,
            customerAddress
        );
        
        paymentManager.processPayment(
            paymentMethod,
            this,
            request,
            new PaymentProcessor.PaymentCallback() {
                @Override
                public void onPaymentSuccess(PaymentResult result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        // Update result with correct order info
                        result.setOrderId(orderId);
                        result.setAmount(totalAmount);
                        showSuccess(result);
                    });
                }

                @Override
                public void onPaymentFailure(PaymentResult result) {
                    runOnUiThread(() -> {
                        hideLoading();
                        showError(result.getMessage());
                    });
                }

                @Override
                public void onPaymentCancelled() {
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Payment was cancelled");
                    });
                }
            }
        );
    }

    private void showLoading() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        statusText.setText("Processing payment...");
        retryButton.setVisibility(Button.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    private void showSuccess(PaymentResult result) {
        statusText.setText("Payment successful!\nTransaction ID: " + result.getTransactionId());
        
        // Save order to history
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        List<Product> products = new ArrayList<>();
        for (CartItem item : cartItems) {
            products.add(item.getProduct());
        }
        
        OrderHistoryManager.getInstance(this).saveOrder(
            result.getTransactionId(),
            result.getOrderId(),
            result.getAmount(),
            paymentMethod.toString(), // Payment method
            products
        );
        
        // Clear cart after successful payment
        CartManager.getInstance().clearCart();
        
        // Navigate to success page after delay
        statusText.postDelayed(() -> {
            Intent intent = new Intent(this, PaymentSuccessActivity.class);
            intent.putExtra("transaction_id", result.getTransactionId());
            intent.putExtra("order_id", result.getOrderId());
            intent.putExtra("amount", result.getAmount());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void showError(String message) {
        statusText.setText("Payment failed: " + message);
        retryButton.setVisibility(Button.VISIBLE);
        
        Toast.makeText(this, "Payment failed: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePaymentCallback(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check if we're returning from a payment browser session
        // by polling the callback endpoint
        checkPaymentStatus();
    }
    
    private void checkPaymentStatus() {
        // This method can be used to periodically check payment status
        // when returning from browser-based payment
        if (orderId != null && !orderId.isEmpty()) {
            // Poll the backend for payment status
            // Implementation depends on your backend API structure
        }
    }

    private void handlePaymentCallback(Intent intent) {
        if (intent != null && intent.getData() != null) {
            String callbackData = intent.getData().toString();
            Log.d(TAG, "Received payment callback: " + callbackData);
            
            PaymentManager paymentManager = PaymentManager.getInstance();
            paymentManager.handlePaymentResult(
                paymentMethod,
                callbackData,
                new PaymentProcessor.PaymentCallback() {
                    @Override
                    public void onPaymentSuccess(PaymentResult result) {
                        runOnUiThread(() -> {
                            hideLoading();
                            showSuccess(result);
                        });
                    }

                    @Override
                    public void onPaymentFailure(PaymentResult result) {
                        runOnUiThread(() -> {
                            hideLoading();
                            showError(result.getMessage());
                        });
                    }

                    @Override
                    public void onPaymentCancelled() {
                        runOnUiThread(() -> {
                            hideLoading();
                            showError("Payment was cancelled");
                        });
                    }
                }
            );
        }
    }
} 