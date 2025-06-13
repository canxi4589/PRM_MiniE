package com.example.mini_ecom.network;

import com.example.mini_ecom.payment.vnpay.VNPayRequest;
import com.example.mini_ecom.payment.vnpay.VNPayResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    
    @POST("Payment/vnpay/create")
    Call<VNPayResponse> createVNPayPayment(@Body VNPayRequest request);
    
    @GET("Payment/vnpay/callback")
    Call<VNPayResponse> handleVNPayCallback(
        @Query("vnp_Amount") String amount,
        @Query("vnp_BankCode") String bankCode,
        @Query("vnp_BankTranNo") String bankTranNo,
        @Query("vnp_CardType") String cardType,
        @Query("vnp_OrderInfo") String orderInfo,
        @Query("vnp_PayDate") String payDate,
        @Query("vnp_ResponseCode") String responseCode,
        @Query("vnp_TmnCode") String tmnCode,
        @Query("vnp_TransactionNo") String transactionNo,
        @Query("vnp_TransactionStatus") String transactionStatus,
        @Query("vnp_TxnRef") String txnRef,
        @Query("vnp_SecureHash") String secureHash
    );
} 