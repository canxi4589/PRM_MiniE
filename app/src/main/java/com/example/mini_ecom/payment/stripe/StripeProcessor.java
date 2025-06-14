package com.example.mini_ecom.payment.stripe;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.example.mini_ecom.payment.PaymentProcessor;
import com.example.mini_ecom.payment.PaymentRequest;
import com.example.mini_ecom.payment.PaymentResult;
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

public class StripeProcessor implements PaymentProcessor {
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51RZQtlRpw5sw2xnalWMscpCULRzSPboYAjpXy2HMl1sO7T5z8kKALrlNX3hxkKsdlShdu53MnWcMujVO3zOlnmH9009frbtXBh";
    private static final String BACKEND_URL = "http://10.0.2.2:4242/create-payment-intent";
    
    private PaymentSheet paymentSheet;
    private PaymentCallback currentCallback;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.STRIPE;
    }

    @Override
    public void processPayment(Context context, PaymentRequest request, PaymentCallback callback) {
        this.currentCallback = callback;
        
        // Initialize Stripe
        PaymentConfiguration.init(context, STRIPE_PUBLISHABLE_KEY);

        // Create PaymentSheet if not exists
        if (paymentSheet == null && context instanceof androidx.fragment.app.FragmentActivity) {
            paymentSheet = new PaymentSheet(
                (androidx.fragment.app.FragmentActivity) context, 
                this::onPaymentSheetResult
            );
        }

        // Fetch client secret and present payment sheet
        fetchClientSecretAndPay(request);
    }

    @Override
    public boolean isAvailable() {
        return true; // Stripe is always available
    }

    @Override
    public String getDisplayName() {
        return "Stripe";
    }

    @Override
    public void handlePaymentResult(String data, PaymentCallback callback) {
        // Handle any external payment result if needed
        // For Stripe, results are handled through PaymentSheet callback
    }

    private void fetchClientSecretAndPay(PaymentRequest request) {
        OkHttpClient client = new OkHttpClient();
        
        try {
            JSONObject json = new JSONObject();
            json.put("amount", (int)(request.getAmount() * 100)); // Stripe expects amount in cents
            json.put("currency", request.getCurrency().toLowerCase());
            json.put("orderId", request.getOrderId());
            json.put("description", request.getDescription());

            RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );

            Request httpRequest = new Request.Builder()
                .url(BACKEND_URL)
                .post(body)
                .build();

            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject resJson = new JSONObject(responseBody);
                        String clientSecret = resJson.getString("clientSecret");
                        
                        // Switch to UI thread to present payment sheet
                        new Handler(Looper.getMainLooper()).post(() -> {
                            presentPaymentSheet(clientSecret);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (currentCallback != null) {
                                PaymentResult result = new PaymentResult(
                                    PaymentResult.Status.FAILED,
                                    "Error parsing payment response",
                                    null,
                                    request.getOrderId(),
                                    request.getAmount()
                                );
                                currentCallback.onPaymentFailure(result);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (currentCallback != null) {
                            PaymentResult result = new PaymentResult(
                                PaymentResult.Status.FAILED,
                                "Network error: " + e.getMessage(),
                                null,
                                request.getOrderId(),
                                request.getAmount()
                            );
                            currentCallback.onPaymentFailure(result);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (currentCallback != null) {
                PaymentResult result = new PaymentResult(
                    PaymentResult.Status.FAILED,
                    "Error preparing payment data",
                    null,
                    request.getOrderId(),
                    request.getAmount()
                );
                currentCallback.onPaymentFailure(result);
            }
        }
    }

    private void presentPaymentSheet(String clientSecret) {
        if (paymentSheet != null) {
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Demo Store")
                .build();
                
            paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (currentCallback == null) return;

        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            PaymentResult result = new PaymentResult(
                PaymentResult.Status.SUCCESS,
                "Payment completed successfully",
                "stripe_" + System.currentTimeMillis(),
                null, // orderId will be set by the caller
                0.0   // amount will be set by the caller
            );
            currentCallback.onPaymentSuccess(result);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            currentCallback.onPaymentCancelled();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failed = (PaymentSheetResult.Failed) paymentSheetResult;
            PaymentResult result = new PaymentResult(
                PaymentResult.Status.FAILED,
                "Payment failed: " + failed.getError().getLocalizedMessage(),
                null,
                null,
                0.0
            );
            currentCallback.onPaymentFailure(result);
        }
    }
} 