package com.example.mini_ecom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// import androidx.activity.EdgeToEdge; // Nếu không dùng thì comment lại

import com.google.android.material.button.MaterialButton;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class PaymentGateway extends AppCompatActivity {
    private View progressBar;
    private TextView tvAmount;
    private double totalAmount;
    private String currency;
    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_gateway);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
        currency = getIntent().getStringExtra("currency");
        if (currency == null) currency = "usd";
        initializeViews();

        PaymentConfiguration.init(
            getApplicationContext(),
            "pk_test_51RZQtlRpw5sw2xnalWMscpCULRzSPboYAjpXy2HMl1sO7T5z8kKALrlNX3hxkKsdlShdu53MnWcMujVO3zOlnmH9009frbtXBh"
        );
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        MaterialButton btnStripePay = findViewById(R.id.btnStripePay);
        btnStripePay.setOnClickListener(v -> fetchClientSecretAndPay(totalAmount, currency));
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        tvAmount = findViewById(R.id.tvAmount);
        tvAmount.setText(String.format("Tổng tiền: %.0f %s", totalAmount, currency.toUpperCase()));
    }

    private void fetchClientSecretAndPay(double totalAmount, String currency) {
        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("amount", totalAmount);
            json.put("currency", currency);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:4242/create-payment-intent")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject resJson = new JSONObject(responseBody);
                        String clientSecret = resJson.getString("clientSecret");
                        runOnUiThread(() -> presentPaymentSheet(clientSecret));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> showMessage("Lỗi khi lấy client_secret!"));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> showMessage("Không kết nối được backend!"));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Lỗi khi chuẩn bị dữ liệu thanh toán!");
        }
    }

    private void presentPaymentSheet(String clientSecret) {
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            new PaymentSheet.Configuration("Demo Store")
        );
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            showMessage("Thanh toán Stripe thành công!");
            Intent intent = new Intent(PaymentGateway.this, SuccessActivity.class);
            startActivity(intent);
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            showMessage("Đã hủy thanh toán Stripe");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            showMessage("Thanh toán Stripe thất bại");
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
