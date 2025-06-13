package com.example.mini_ecom.payment;

public class PaymentResult {
    public enum Status {
        SUCCESS,
        FAILED,
        CANCELLED,
        PENDING
    }

    private Status status;
    private String message;
    private String transactionId;
    private String orderId;
    private double amount;
    private Object additionalData;

    public PaymentResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public PaymentResult(Status status, String message, String transactionId, String orderId, double amount) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
    }

    // Getters
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public String getTransactionId() { return transactionId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public Object getAdditionalData() { return additionalData; }

    // Setters
    public void setStatus(Status status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setAdditionalData(Object additionalData) { this.additionalData = additionalData; }

    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }
} 