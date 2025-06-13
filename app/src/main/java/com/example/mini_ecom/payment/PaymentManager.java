package com.example.mini_ecom.payment;

import android.content.Context;
import android.util.Log;

import com.example.mini_ecom.payment.vnpay.VNPayProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentManager {
    private static final String TAG = "PaymentManager";
    private static PaymentManager instance;
    private Map<PaymentProcessor.PaymentMethod, PaymentProcessor> processors;

    private PaymentManager() {
        processors = new HashMap<>();
        initializeProcessors();
    }

    public static synchronized PaymentManager getInstance() {
        if (instance == null) {
            instance = new PaymentManager();
        }
        return instance;
    }

    private void initializeProcessors() {
        // Initialize VNPay processor
        processors.put(PaymentProcessor.PaymentMethod.VNPAY, new VNPayProcessor());
        
        // Add other payment processors here as you implement them
        // processors.put(PaymentProcessor.PaymentMethod.PAYPAL, new PayPalProcessor());
        // processors.put(PaymentProcessor.PaymentMethod.STRIPE, new StripeProcessor());
    }

    /**
     * Get all available payment methods
     */
    public List<PaymentProcessor.PaymentMethod> getAvailablePaymentMethods() {
        List<PaymentProcessor.PaymentMethod> availableMethods = new ArrayList<>();
        
        for (Map.Entry<PaymentProcessor.PaymentMethod, PaymentProcessor> entry : processors.entrySet()) {
            if (entry.getValue().isAvailable()) {
                availableMethods.add(entry.getKey());
            }
        }
        
        return availableMethods;
    }

    /**
     * Process payment with the specified method
     */
    public void processPayment(
            PaymentProcessor.PaymentMethod method,
            Context context,
            PaymentRequest request,
            PaymentProcessor.PaymentCallback callback
    ) {
        PaymentProcessor processor = processors.get(method);
        
        if (processor == null) {
            Log.e(TAG, "No processor found for payment method: " + method);
            callback.onPaymentFailure(new PaymentResult(
                PaymentResult.Status.FAILED,
                "Payment method not supported: " + method.getDisplayName()
            ));
            return;
        }

        if (!processor.isAvailable()) {
            Log.e(TAG, "Payment processor not available: " + method);
            callback.onPaymentFailure(new PaymentResult(
                PaymentResult.Status.FAILED,
                "Payment method not available: " + method.getDisplayName()
            ));
            return;
        }

        Log.d(TAG, "Processing payment with method: " + method);
        processor.processPayment(context, request, callback);
    }

    /**
     * Handle payment result from external sources (like deep links)
     */
    public void handlePaymentResult(
            PaymentProcessor.PaymentMethod method,
            String data,
            PaymentProcessor.PaymentCallback callback
    ) {
        PaymentProcessor processor = processors.get(method);
        
        if (processor == null) {
            Log.e(TAG, "No processor found for handling result from: " + method);
            callback.onPaymentFailure(new PaymentResult(
                PaymentResult.Status.FAILED,
                "Cannot handle payment result for: " + method.getDisplayName()
            ));
            return;
        }

        processor.handlePaymentResult(data, callback);
    }

    /**
     * Get processor for a specific payment method
     */
    public PaymentProcessor getProcessor(PaymentProcessor.PaymentMethod method) {
        return processors.get(method);
    }

    /**
     * Register a new payment processor (for future extensibility)
     */
    public void registerProcessor(PaymentProcessor.PaymentMethod method, PaymentProcessor processor) {
        processors.put(method, processor);
        Log.d(TAG, "Registered new payment processor: " + method);
    }

    /**
     * Check if a payment method is supported
     */
    public boolean isPaymentMethodSupported(PaymentProcessor.PaymentMethod method) {
        return processors.containsKey(method) && processors.get(method).isAvailable();
    }
} 