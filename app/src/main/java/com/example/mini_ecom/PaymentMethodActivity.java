package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_ecom.payment.PaymentManager;
import com.example.mini_ecom.payment.PaymentProcessor;

import java.util.List;

public class PaymentMethodActivity extends AppCompatActivity {
    private RadioGroup paymentMethodGroup;
    private Button proceedButton;
    private TextView totalAmountText;
    
    private double totalAmount;
    private String customerName, customerEmail, customerPhone, customerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        initViews();
        getIntentData();
        setupPaymentMethods();
        setupClickListeners();
    }

    private void initViews() {
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        proceedButton = findViewById(R.id.proceedButton);
        totalAmountText = findViewById(R.id.totalAmountText);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        customerName = intent.getStringExtra("customer_name");
        customerEmail = intent.getStringExtra("customer_email");
        customerPhone = intent.getStringExtra("customer_phone");
        customerAddress = intent.getStringExtra("customer_address");

        totalAmountText.setText(String.format("Total: $%.2f", totalAmount));
    }

    private void setupPaymentMethods() {
        PaymentManager paymentManager = PaymentManager.getInstance();
        List<PaymentProcessor.PaymentMethod> availableMethods = paymentManager.getAvailablePaymentMethods();

        for (PaymentProcessor.PaymentMethod method : availableMethods) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(method.getDisplayName());
            radioButton.setTag(method);
            radioButton.setTextSize(16);
            radioButton.setPadding(16, 16, 16, 16);
            
            paymentMethodGroup.addView(radioButton);
        }

        // Select first method by default
        if (paymentMethodGroup.getChildCount() > 0) {
            ((RadioButton) paymentMethodGroup.getChildAt(0)).setChecked(true);
        }
    }

    private void setupClickListeners() {
        proceedButton.setOnClickListener(v -> {
            int selectedId = paymentMethodGroup.getCheckedRadioButtonId();
            
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = findViewById(selectedId);
            PaymentProcessor.PaymentMethod selectedMethod = 
                (PaymentProcessor.PaymentMethod) selectedRadioButton.getTag();

            // Navigate to payment processing
            Intent intent = new Intent(this, PaymentProcessingActivity.class);
            intent.putExtra("payment_method", selectedMethod.name());
            intent.putExtra("total_amount", totalAmount);
            intent.putExtra("customer_name", customerName);
            intent.putExtra("customer_email", customerEmail);
            intent.putExtra("customer_phone", customerPhone);
            intent.putExtra("customer_address", customerAddress);
            
            startActivity(intent);
        });
    }
} 