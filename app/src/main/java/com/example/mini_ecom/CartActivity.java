package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private LinearLayout emptyCartTextView;
    private TextView totalPriceTextView;
    private MaterialButton checkoutButton;
    private MaterialButton clearCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        updateCartDisplay();
    }

    private void initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        clearCartButton = findViewById(R.id.clearCartButton);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.cartToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Shopping Cart");
        }
    }

    private void setupRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupButtons() {
        checkoutButton.setOnClickListener(v -> {
            if (CartManager.getInstance().getTotalItems() > 0) {
                Intent intent = new Intent(this, CheckoutActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        clearCartButton.setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            cartAdapter.notifyDataSetChanged();
            updateCartDisplay();
            Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCartDisplay() {
        CartManager cartManager = CartManager.getInstance();
        List<CartItem> cartItems = cartManager.getCartItems();
        
        if (cartItems.isEmpty()) {
            cartRecyclerView.setVisibility(View.GONE);
            emptyCartTextView.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(false);
            clearCartButton.setEnabled(false);
            totalPriceTextView.setText("Total: $0.00");
        } else {
            cartRecyclerView.setVisibility(View.VISIBLE);
            emptyCartTextView.setVisibility(View.GONE);
            checkoutButton.setEnabled(true);
            clearCartButton.setEnabled(true);
            
            double totalPrice = cartManager.getTotalPrice();
            totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", totalPrice));
        }
    }

    @Override
    public void onQuantityChanged() {
        updateCartDisplay();
        cartAdapter = new CartAdapter(this, CartManager.getInstance().getCartItems(), this);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    @Override
    public void onItemRemoved() {
        updateCartDisplay();
        cartAdapter = new CartAdapter(this, CartManager.getInstance().getCartItems(), this);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 