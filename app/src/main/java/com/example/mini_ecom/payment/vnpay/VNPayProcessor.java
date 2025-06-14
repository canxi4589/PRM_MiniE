package com.example.mini_ecom.payment.vnpay;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.mini_ecom.network.RetrofitClient;
import com.example.mini_ecom.payment.PaymentProcessor;
import com.example.mini_ecom.payment.PaymentRequest;
import com.example.mini_ecom.payment.PaymentResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VNPayProcessor implements PaymentProcessor {
    private static final String TAG = "VNPayProcessor";

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public void processPayment(Context context, PaymentRequest request, PaymentCallback callback) {
        if (!(request instanceof VNPayRequest)) {
            callback.onPaymentFailure(new PaymentResult(
                PaymentResult.Status.FAILED,
                "Invalid request type for VNPay payment"
            ));
            return;
        }

        VNPayRequest vnpayRequest = (VNPayRequest) request;
        
        // Call API to create VNPay payment
        Call<VNPayResponse> call = RetrofitClient.getInstance()
                .getApiService()
                .createVNPayPayment(vnpayRequest);

        call.enqueue(new Callback<VNPayResponse>() {
            @Override
            public void onResponse(Call<VNPayResponse> call, Response<VNPayResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VNPayResponse vnpayResponse = response.body();
                    
                    if (vnpayResponse.isSuccess() && vnpayResponse.getPaymentUrl() != null) {
                        // Open VNPay payment URL in WebView
                        Intent intent = new Intent(context, com.example.mini_ecom.PaymentWebViewActivity.class);
                        intent.putExtra("payment_url", vnpayResponse.getPaymentUrl());
                        intent.putExtra("order_id", vnpayRequest.getOrderId());
                        intent.putExtra("amount", vnpayRequest.getAmount());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        
                        // Payment initiated successfully, result will be handled in WebView
                        Log.d(TAG, "VNPay payment initiated successfully via WebView");
                    } else {
                        callback.onPaymentFailure(new PaymentResult(
                            PaymentResult.Status.FAILED,
                            vnpayResponse.getMessage() != null ? vnpayResponse.getMessage() : "Failed to create VNPay payment"
                        ));
                    }
                } else {
                    callback.onPaymentFailure(new PaymentResult(
                        PaymentResult.Status.FAILED,
                        "Failed to communicate with payment server"
                    ));
                }
            }

            @Override
            public void onFailure(Call<VNPayResponse> call, Throwable t) {
                Log.e(TAG, "VNPay payment creation failed", t);
                callback.onPaymentFailure(new PaymentResult(
                    PaymentResult.Status.FAILED,
                    "Network error: " + t.getMessage()
                ));
            }
        });
    }

    @Override
    public boolean isAvailable() {
        // VNPay is available if we have network connection
        // You can add more sophisticated checks here
        return true;
    }

    @Override
    public String getDisplayName() {
        return getPaymentMethod().getDisplayName();
    }

    @Override
    public void handlePaymentResult(String data, PaymentCallback callback) {
        try {
            Uri uri = Uri.parse(data);
            String responseCode = uri.getQueryParameter("vnp_ResponseCode");
            String transactionStatus = uri.getQueryParameter("vnp_TransactionStatus");
            String txnRef = uri.getQueryParameter("vnp_TxnRef");
            String amount = uri.getQueryParameter("vnp_Amount");
            String transactionNo = uri.getQueryParameter("vnp_TransactionNo");

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                // Payment successful
                PaymentResult result = new PaymentResult(
                    PaymentResult.Status.SUCCESS,
                    "Payment completed successfully",
                    transactionNo,
                    txnRef,
                    amount != null ? Double.parseDouble(amount) / 100 : 0 // VNPay returns amount in cents
                );
                callback.onPaymentSuccess(result);
            } else {
                // Payment failed
                PaymentResult result = new PaymentResult(
                    PaymentResult.Status.FAILED,
                    "Payment failed with code: " + responseCode
                );
                result.setTransactionId(transactionNo);
                result.setOrderId(txnRef);
                callback.onPaymentFailure(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling VNPay payment result", e);
            callback.onPaymentFailure(new PaymentResult(
                PaymentResult.Status.FAILED,
                "Error processing payment result: " + e.getMessage()
            ));
        }
    }
} 