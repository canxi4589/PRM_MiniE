package com.example.mini_ecom;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private Product product;
    private ImageView productImageView;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView priceTextView;
    private TextView categoryTextView;
    private Button addToCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        setupToolbar();
        loadProduct();
        setupAddToCartButton();
    }

    private void initViews() {
        productImageView = findViewById(R.id.productDetailImageView);
        nameTextView = findViewById(R.id.productDetailNameTextView);
        descriptionTextView = findViewById(R.id.productDetailDescriptionTextView);
        priceTextView = findViewById(R.id.productDetailPriceTextView);
        categoryTextView = findViewById(R.id.productDetailCategoryTextView);
        addToCartButton = findViewById(R.id.productDetailAddToCartButton);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadProduct() {
        int productId = getIntent().getIntExtra("product_id", -1);
        product = findProductById(productId);

        if (product != null) {
            productImageView.setImageResource(product.getImageResource());
            nameTextView.setText(product.getName());
            descriptionTextView.setText(product.getDescription());
            priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
            categoryTextView.setText(product.getCategory());
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(product.getName());
            }
        }
    }

    private Product findProductById(int productId) {
        // This is a simple implementation. In a real app, you'd get this from a database or API
        Product[] allProducts = {
            new Product(1, "Smartphone", "Latest Android smartphone with great features. This device comes with a high-resolution display, powerful processor, and long-lasting battery life.", 299.99, android.R.drawable.ic_menu_camera, "Electronics"),
            new Product(2, "Laptop", "High-performance laptop for work and gaming. Features include fast SSD storage, dedicated graphics card, and excellent keyboard.", 799.99, android.R.drawable.ic_menu_edit, "Electronics"),
            new Product(3, "Headphones", "Wireless noise-canceling headphones with premium sound quality and comfortable fit for extended use.", 149.99, android.R.drawable.ic_btn_speak_now, "Electronics"),
            new Product(4, "T-Shirt", "Comfortable cotton t-shirt made from 100% organic cotton. Available in multiple sizes and colors.", 19.99, android.R.drawable.ic_menu_gallery, "Clothing"),
            new Product(5, "Jeans", "Classic blue jeans with a comfortable fit and durable construction. Perfect for everyday wear.", 49.99, android.R.drawable.ic_menu_gallery, "Clothing"),
            new Product(6, "Sneakers", "Comfortable running sneakers with excellent cushioning and breathable material.", 79.99, android.R.drawable.ic_menu_gallery, "Footwear"),
            new Product(7, "Coffee Mug", "Ceramic coffee mug with heat-resistant handle. Dishwasher and microwave safe.", 12.99, android.R.drawable.ic_menu_gallery, "Home"),
            new Product(8, "Book", "Bestselling novel with engaging storyline and compelling characters. Perfect for leisure reading.", 14.99, android.R.drawable.ic_menu_sort_by_size, "Books")
        };

        for (Product p : allProducts) {
            if (p.getId() == productId) {
                return p;
            }
        }
        return null;
    }

    private void setupAddToCartButton() {
        addToCartButton.setOnClickListener(v -> {
            if (product != null) {
                CartManager.getInstance().addToCart(product, 1);
                Toast.makeText(this, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 