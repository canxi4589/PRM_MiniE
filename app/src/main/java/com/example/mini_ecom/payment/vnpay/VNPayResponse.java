package com.example.mini_ecom.payment.vnpay;

import com.google.gson.annotations.SerializedName;

public class VNPayResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("paymentUrl")
    private String paymentUrl;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("orderId")
    private String orderId;
    
    @SerializedName("transactionId")
    private String transactionId;

    // Getters
    public boolean isSuccess() { return success; }
    public String getPaymentUrl() { return paymentUrl; }
    public String getMessage() { return message; }
    public String getOrderId() { return orderId; }
    public String getTransactionId() { return transactionId; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    public void setMessage(String message) { this.message = message; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
} 