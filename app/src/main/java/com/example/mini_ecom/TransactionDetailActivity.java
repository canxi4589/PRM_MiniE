package com.example.mini_ecom;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView detailTransactionStatusTextView;
    private TextView detailTransactionIdTextView;
    private TextView detailTransactionDateTextView;
    private TextView detailPaymentMethodTextView;
    private TextView detailTotalAmountTextView;
    private RecyclerView productsRecyclerView;

    private ProductAdapter productAdapter;
    private List<Product> transactionproducts;

    private String transactionId;
    private double transactionAmount;
    private long transactionDateLong;
    private String transactionStatus;
    private String paymentMethod;
    private int productCount;

    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        initViews();
        setupToolbar();
        getIntentData();
        setupRecyclerView();
        displayTransactionDetails();
        loadTransactionproducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        detailTransactionStatusTextView = findViewById(R.id.detailTransactionStatusTextView);
        detailTransactionIdTextView = findViewById(R.id.detailTransactionIdTextView);
        detailTransactionDateTextView = findViewById(R.id.detailTransactionDateTextView);
        detailPaymentMethodTextView = findViewById(R.id.detailPaymentMethodTextView);
        detailTotalAmountTextView = findViewById(R.id.detailTotalAmountTextView);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);

        dateFormat = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void getIntentData() {
        if (getIntent() != null) {
            transactionId = getIntent().getStringExtra("transaction_id");
            transactionAmount = getIntent().getDoubleExtra("transaction_amount", 0.0);
            transactionDateLong = getIntent().getLongExtra("transaction_date", System.currentTimeMillis());
            transactionStatus = getIntent().getStringExtra("transaction_status");
            paymentMethod = getIntent().getStringExtra("payment_method");
            productCount = getIntent().getIntExtra("product_count", 0);
        }
    }

    private void setupRecyclerView() {
            transactionproducts = new ArrayList<>();
            productAdapter = new ProductAdapter(this, transactionproducts);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(productAdapter);
        productsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void displayTransactionDetails() {
        // Transaction ID
        detailTransactionIdTextView.setText(transactionId);

        // Date and Time
        Date transactionDate = new Date(transactionDateLong);
        detailTransactionDateTextView.setText(dateFormat.format(transactionDate));

        // Payment Method
        detailPaymentMethodTextView.setText(paymentMethod);

        // Total Amount
        detailTotalAmountTextView.setText(String.format(Locale.getDefault(), "$%.2f", transactionAmount));

        // Status with styling
        detailTransactionStatusTextView.setText(transactionStatus);
    }

    private void loadTransactionproducts() {
        // Try to load real transaction data first
        TransactionItem realTransaction = OrderHistoryManager.getInstance(this).getOrderByTransactionId(transactionId);
        
        transactionproducts.clear();
        
        if (realTransaction != null && realTransaction.getProducts() != null) {
            // Load real products from stored order
            transactionproducts.addAll(realTransaction.getProducts());
        } else {
            // Fallback to sample data for demo transactions
            transactionproducts.addAll(generateSampleProductsForTransaction(transactionId));
        }
        
        productAdapter.notifyDataSetChanged();
    }

    private List<Product> generateSampleProductsForTransaction(String transactionId) {
        List<Product> products = new ArrayList<>();

        // Generate different products based on transaction ID
        switch (transactionId) {
            case "TXN-001":
                products.add(new Product(1, "Smartphone", "Latest Android smartphone with great features", 299.99, android.R.drawable.ic_menu_camera, "Electronics"));
                products.add(new Product(2, "Laptop", "High-performance laptop for work and gaming", 799.99, android.R.drawable.ic_menu_edit, "Electronics"));
                products.add(new Product(3, "Headphones", "Wireless noise-canceling headphones", 149.99, android.R.drawable.ic_btn_speak_now, "Electronics"));
                break;

            case "TXN-002":
                products.add(new Product(4, "T-Shirt", "Comfortable cotton t-shirt", 19.99, android.R.drawable.ic_menu_gallery, "Clothing"));
                products.add(new Product(5, "Jeans", "Classic blue jeans", 49.99, android.R.drawable.ic_menu_gallery, "Clothing"));
                break;

            case "TXN-003":
                products.add(new Product(6, "Sneakers", "Comfortable running sneakers", 79.99, android.R.drawable.ic_menu_gallery, "Footwear"));
                break;

            case "TXN-004":
                products.add(new Product(7, "Coffee Mug", "Ceramic coffee mug", 12.99, android.R.drawable.ic_menu_gallery, "Home"));
                products.add(new Product(8, "Book", "Bestselling novel", 14.99, android.R.drawable.ic_menu_sort_by_size, "Books"));
                break;
            default:
                break;
        }

        return products;
    }

    /**
     * Alternative method to load transaction details from database
     * Call this method if you want to fetch complete transaction data including products
     */
    private void loadTransactionFromDatabase(String transactionId) {
        // TODO: Implement database/API call to fetch complete transaction data
        // This would replace the sample data generation above

        /*
        // Example implementation:
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TransactionItem transaction = dbHelper.getTransactionById(transactionId);

        if (transaction != null) {
            // Update all fields with real data
            this.transactionAmount = transaction.getTotalAmount();
            this.transactionStatus = transaction.getStatus();
            this.paymentMethod = transaction.getPaymentMethod();
            this.transactionDateLong = transaction.getTransactionDate().getTime();

            // Update UI
            displayTransactionDetails();

            // Load products
            transactionproducts.clear();
            transactionproducts.addAll(transaction.getproducts());
            ProductAdapter.notifyDataSetChanged();
        }
        */
    }
}