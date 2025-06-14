package com.example.mini_ecom.payment.stripe;

import com.example.mini_ecom.payment.PaymentRequest;

public class StripeRequest extends PaymentRequest {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    public StripeRequest(double amount, String currency, String orderId, String description) {
        super(amount, currency, orderId, description);
    }

    public StripeRequest(double amount, String currency, String orderId, String description,
                        String customerName, String customerEmail, String customerPhone, String customerAddress) {
        super(amount, currency, orderId, description);
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
    }

    // Getters
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }

    // Setters
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
} 