package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private TextView cartSummaryTextView;
    private MaterialButton viewCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadProducts();
        setupCartButton();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        cartSummaryTextView = findViewById(R.id.cartSummaryTextView);
        viewCartButton = findViewById(R.id.viewCartButton);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        products = new ArrayList<>();
        productAdapter = new ProductAdapter(this, products);
        productAdapter.setOnCartChangedListener(this::updateCartSummary);
        recyclerView.setAdapter(productAdapter);
    }

    private void loadProducts() {
        // Sample products with placeholder images
        products.add(new Product(1, "Smartphone", "Latest Android smartphone with great features", 299.99, android.R.drawable.ic_menu_camera, "Electronics"));
        products.add(new Product(2, "Laptop", "High-performance laptop for work and gaming", 799.99, android.R.drawable.ic_menu_edit, "Electronics"));
        products.add(new Product(3, "Headphones", "Wireless noise-canceling headphones", 149.99, android.R.drawable.ic_btn_speak_now, "Electronics"));
        products.add(new Product(4, "T-Shirt", "Comfortable cotton t-shirt", 19.99, android.R.drawable.ic_menu_gallery, "Clothing"));
        products.add(new Product(5, "Jeans", "Classic blue jeans", 49.99, android.R.drawable.ic_menu_gallery, "Clothing"));
        products.add(new Product(6, "Sneakers", "Comfortable running sneakers", 79.99, android.R.drawable.ic_menu_gallery, "Footwear"));
        products.add(new Product(7, "Coffee Mug", "Ceramic coffee mug", 12.99, android.R.drawable.ic_menu_gallery, "Home"));
        products.add(new Product(8, "Book", "Bestselling novel", 14.99, android.R.drawable.ic_menu_sort_by_size, "Books"));

        productAdapter.notifyDataSetChanged();
    }

    private void setupCartButton() {
        viewCartButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartSummary();
    }

    private void updateCartSummary() {
        CartManager cartManager = CartManager.getInstance();
        int itemCount = cartManager.getTotalItems();
        double totalPrice = cartManager.getTotalPrice();
        
        cartSummaryTextView.setText(String.format(Locale.getDefault(), 
            "Cart: %d items - $%.2f", itemCount, totalPrice));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_search) {
            // TODO: Implement search functionality
            return true;
        } else if (id == R.id.action_transaction_history) {
            Intent intent = new Intent(this, TransactionActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}