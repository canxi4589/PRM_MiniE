package com.example.mini_ecom;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderHistoryManager {
    private static final String TAG = "OrderHistoryManager";
    private static final String PREF_NAME = "order_history";
    private static final String KEY_ORDERS = "orders";
    
    private static OrderHistoryManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    private OrderHistoryManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized OrderHistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrderHistoryManager(context);
        }
        return instance;
    }
    
    /**
     * Save a successful order to history
     */
    public void saveOrder(String transactionId, String orderId, double amount, 
                         String paymentMethod, List<Product> products) {
        try {
            // Get existing orders
            List<TransactionItem> orders = getOrderHistory();
            
            // Create new transaction
            TransactionItem newOrder = new TransactionItem(
                transactionId,
                new Date(),
                "Success",
                paymentMethod,
                new ArrayList<>(products) // Create copy to avoid reference issues
            );
            
            // Add to beginning of list (most recent first)
            orders.add(0, newOrder);
            
            // Limit to last 50 orders to prevent storage bloat
            if (orders.size() > 50) {
                orders = orders.subList(0, 50);
            }
            
            // Save to SharedPreferences
            String ordersJson = gson.toJson(orders);
            sharedPreferences.edit()
                .putString(KEY_ORDERS, ordersJson)
                .apply();
            
            Log.d(TAG, "Order saved successfully: " + transactionId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving order to history", e);
        }
    }
    
    /**
     * Get all order history
     */
    public List<TransactionItem> getOrderHistory() {
        try {
            String ordersJson = sharedPreferences.getString(KEY_ORDERS, "[]");
            Type listType = new TypeToken<List<TransactionItem>>(){}.getType();
            List<TransactionItem> orders = gson.fromJson(ordersJson, listType);
            
            // Return empty list if null
            return orders != null ? orders : new ArrayList<>();
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading order history", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get order by transaction ID
     */
    public TransactionItem getOrderByTransactionId(String transactionId) {
        List<TransactionItem> orders = getOrderHistory();
        for (TransactionItem order : orders) {
            if (order.getTransactionId().equals(transactionId)) {
                return order;
            }
        }
        return null;
    }
    
    /**
     * Get recent orders (last N orders)
     */
    public List<TransactionItem> getRecentOrders(int count) {
        List<TransactionItem> allOrders = getOrderHistory();
        if (allOrders.size() <= count) {
            return allOrders;
        }
        return allOrders.subList(0, count);
    }
    
    /**
     * Clear all order history (for testing/debugging)
     */
    public void clearOrderHistory() {
        sharedPreferences.edit()
            .remove(KEY_ORDERS)
            .apply();
        Log.d(TAG, "Order history cleared");
    }
    
    /**
     * Get total number of orders
     */
    public int getOrderCount() {
        return getOrderHistory().size();
    }
    
    /**
     * Check if there are any orders
     */
    public boolean hasOrders() {
        return getOrderCount() > 0;
    }
} 