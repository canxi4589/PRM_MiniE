package com.example.mini_ecom.payment;

import android.content.Context;

public interface PaymentProcessor {
    
    interface PaymentCallback {
        void onPaymentSuccess(PaymentResult result);
        void onPaymentFailure(PaymentResult result);
        void onPaymentCancelled();
    }
    
    enum PaymentMethod {
        VNPAY("VNPay"),
        PAYPAL("PayPal"),
        STRIPE("Stripe"),
        BANK_TRANSFER("Bank Transfer");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Get the payment method type
     */
    PaymentMethod getPaymentMethod();
    
    /**
     * Process the payment
     */
    void processPayment(Context context, PaymentRequest request, PaymentCallback callback);
    
    /**
     * Check if this payment method is available
     */
    boolean isAvailable();
    
    /**
     * Get display name for this payment method
     */
    String getDisplayName();
    
    /**
     * Handle payment result from external sources (like deep links)
     */
    void handlePaymentResult(String data, PaymentCallback callback);
} 