package com.example.mini_ecom;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class TransactionItem {
    private String transactionId;
    private double totalAmount;
    private Date transactionDate;
    private String status; // "Success", "Failed", "Pending"
    private String paymentMethod;
    private List<Product> products;
    private int productCount;

    // Constructor
    public TransactionItem() {
    }

    public TransactionItem(String transactionId, Date transactionDate,
                           String status, String paymentMethod, List<Product> products) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.products = products;
        this.productCount = products != null ? products.size() : 0;
        this.totalAmount = calculateTotalAmount();
    }

    private double calculateTotalAmount() {
        if (products == null) return 0;
        double total = 0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        this.productCount = products != null ? products.size() : 0;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int itemCount) {
        this.productCount = itemCount;
    }

    // Helper methods
    public boolean isSuccessful() {
        return "Success".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "Failed".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public String getFormattedAmount() {
        return String.format("$%.2f", totalAmount);
    }

    public String getProductCountText() {
        if (productCount == 1) {
            return "1 product";
        } else {
            return productCount + " Products";
        }
    }


}