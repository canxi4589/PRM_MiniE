package com.example.mini_ecom;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView transactionsRecyclerView;

    private TransactionAdapter transactionAdapter;
    private List<TransactionItem> allTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_history);

        initViews();
        setupToolbar();

        // Initialize data
        allTransactions = new ArrayList<>();
        setupRecyclerView();

        // Fetch transactions
        fetchTransactions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Debug option - long click toolbar to clear order history
        toolbar.setOnLongClickListener(v -> {
            OrderHistoryManager.getInstance(this).clearOrderHistory();
            fetchTransactions(); // Refresh the list
            return true;
        });
    }

    private void setupRecyclerView() {
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(this, allTransactions);
        transactionsRecyclerView.setAdapter(transactionAdapter);
    }

    private void fetchTransactions() {
        new Thread(() -> {
            // Load real order history
            List<TransactionItem> transactions = OrderHistoryManager.getInstance(this).getOrderHistory();
            
            // If no real orders exist, show sample data for demo
            if (transactions.isEmpty()) {
                transactions = generateSampleTransactions();
            }

            // Make final copy for lambda
            final List<TransactionItem> finalTransactions = transactions;

            // Update UI on main thread
            runOnUiThread(() -> {
                allTransactions.clear();
                allTransactions.addAll(finalTransactions);
                transactionAdapter.notifyDataSetChanged();

                Log.d("TransactionActivity", "Updated adapter with " + allTransactions.size() + " transactions");
            });

        }).start();
    }

    private List<TransactionItem> generateSampleTransactions() {
        List<TransactionItem> transactions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Transaction 1 - Today
        List<Product> products1 = new ArrayList<>();
        products1.add(new Product(1, "Smartphone", "Latest Android smartphone with great features", 299.99, android.R.drawable.ic_menu_camera, "Electronics"));
        products1.add(new Product(2, "Laptop", "High-performance laptop for work and gaming", 799.99, android.R.drawable.ic_menu_edit, "Electronics"));
        products1.add(new Product(3, "Headphones", "Wireless noise-canceling headphones", 149.99, android.R.drawable.ic_btn_speak_now, "Electronics"));

        transactions.add(new TransactionItem(
                "TXN-001",
                calendar.getTime(),
                "Success",
                "Credit Card",
                products1
        ));

        // Transaction 2 - Yesterday
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        List<Product> products2 = new ArrayList<>();
        products2.add(new Product(4, "T-Shirt", "Comfortable cotton t-shirt", 19.99, android.R.drawable.ic_menu_gallery, "Clothing"));
        products2.add(new Product(5, "Jeans", "Classic blue jeans", 49.99, android.R.drawable.ic_menu_gallery, "Clothing"));

        transactions.add(new TransactionItem(
                "TXN-002",
                calendar.getTime(),
                "Success",
                "VNPay",
                products2
        ));
        // Transaction 3 - 3 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        List<Product> products3 = new ArrayList<>();
        products3.add(new Product(6, "Sneakers", "Comfortable running sneakers", 79.99, android.R.drawable.ic_menu_gallery, "Footwear"));

        transactions.add(new TransactionItem(
                "TXN-003",
                calendar.getTime(),
                "Success",
                "PayPal",
                products3
        ));

        // Transaction 4 - 1 week ago
        calendar.add(Calendar.DAY_OF_MONTH, -4);
        List<Product> products4 = new ArrayList<>();
        products4.add(new Product(7, "Coffee Mug", "Ceramic coffee mug", 12.99, android.R.drawable.ic_menu_gallery, "Home"));
        products4.add(new Product(8, "Book", "Bestselling novel", 14.99, android.R.drawable.ic_menu_sort_by_size, "Books"));

        transactions.add(new TransactionItem(
                "TXN-004",
                calendar.getTime(),
                "Success",
                "Credit Card",
                products4
        ));

        Log.d("TransactionFetch", "Added transaction bitch: " + transactions);
        return transactions;
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from detail activity
        if (transactionAdapter != null) {
            transactionAdapter.notifyDataSetChanged();
        }
    }
}