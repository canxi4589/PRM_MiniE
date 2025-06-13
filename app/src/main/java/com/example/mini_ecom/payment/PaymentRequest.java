package com.example.mini_ecom.payment;

public abstract class PaymentRequest {
    protected double amount;
    protected String currency;
    protected String orderId;
    protected String description;

    public PaymentRequest(double amount, String currency, String orderId, String description) {
        this.amount = amount;
        this.currency = currency;
        this.orderId = orderId;
        this.description = description;
    }

    // Getters
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getOrderId() { return orderId; }
    public String getDescription() { return description; }

    // Setters
    public void setAmount(double amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setDescription(String description) { this.description = description; }
} 