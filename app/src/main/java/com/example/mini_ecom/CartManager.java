package com.example.mini_ecom;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public void removeFromCart(int productId) {
        cartItems.removeIf(item -> item.getProduct().getId() == productId);
    }

    public void updateQuantity(int productId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == productId) {
                if (quantity <= 0) {
                    removeFromCart(productId);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getTotalItems() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
} 