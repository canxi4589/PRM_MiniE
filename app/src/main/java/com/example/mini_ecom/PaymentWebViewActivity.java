package com.example.mini_ecom;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mini_ecom.payment.PaymentResult;

public class PaymentWebViewActivity extends AppCompatActivity {
    private static final String TAG = "PaymentWebView";
    
    private WebView webView;
    private ProgressBar progressBar;
    private String paymentUrl;
    private String orderId;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);
        
        initViews();
        setupWebView();
        loadPaymentUrl();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        
        // Get data from intent
        Intent intent = getIntent();
        paymentUrl = intent.getStringExtra("payment_url");
        orderId = intent.getStringExtra("order_id");
        amount = intent.getDoubleExtra("amount", 0.0);
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Invalid payment URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupWebView() {
        // Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        
        // Add JavaScript interface for communication
        webView.addJavascriptInterface(new PaymentJavaScriptInterface(), "Android");
        
        // Set WebView client to handle page loading and URL changes
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                
                Log.d(TAG, "Page started loading: " + url);
                if (isCallbackUrl(url)) {
                    handlePaymentCallback(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                
                Log.d(TAG, "Page finished loading: " + url);
                injectCallbackDetectionScript();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "URL loading: " + url);
                if (isCallbackUrl(url)) {
                    handlePaymentCallback(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "WebView error: " + description + " for URL: " + failingUrl);
                Toast.makeText(PaymentWebViewActivity.this, 
                    "Failed to load payment page: " + description, 
                    Toast.LENGTH_LONG).show();
            }
        });
        
        // Set WebChrome client for progress updates
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadPaymentUrl() {
        Log.d(TAG, "Loading payment URL: " + paymentUrl);
        webView.loadUrl(paymentUrl);
    }

    private boolean isCallbackUrl(String url) {
        return url.contains("/api/payment/vnpay/callback") || 
               url.contains("vnp_ResponseCode") ||
               url.contains("vnp_TransactionStatus");
    }

    private void injectCallbackDetectionScript() {
        String script = 
            "javascript:(function() {" +
            "    if (window.location.href.indexOf('callback') !== -1 || " +
            "        window.location.search.indexOf('vnp_ResponseCode') !== -1) {" +
            "        var urlParams = new URLSearchParams(window.location.search);" +
            "        var paymentData = {" +
            "            success: urlParams.get('success') || (urlParams.get('vnp_ResponseCode') === '00')," +
            "            message: urlParams.get('message') || 'Payment completed'," +
            "            orderId: urlParams.get('orderId') || urlParams.get('vnp_TxnRef')," +
            "            transactionId: urlParams.get('transactionId') || urlParams.get('vnp_TransactionNo')," +
            "            amount: urlParams.get('amount') || urlParams.get('vnp_Amount')," +
            "            payDate: urlParams.get('payDate') || urlParams.get('vnp_PayDate')," +
            "            responseCode: urlParams.get('responseCode') || urlParams.get('vnp_ResponseCode')" +
            "        };" +
            "        Android.onPaymentResult(JSON.stringify(paymentData));" +
            "        try {" +
            "            var bodyText = document.body.innerText || document.body.textContent;" +
            "            if (bodyText.indexOf('success') !== -1 && bodyText.indexOf('{') !== -1) {" +
            "                var jsonMatch = bodyText.match(/\\{[^}]+\\}/g);" +
            "                if (jsonMatch && jsonMatch.length > 0) {" +
            "                    var jsonData = JSON.parse(jsonMatch[0]);" +
            "                    Android.onPaymentResult(JSON.stringify(jsonData));" +
            "                }" +
            "            }" +
            "        } catch(e) {" +
            "            console.log('Could not parse JSON from page:', e);" +
            "        }" +
            "    }" +
            "})()";
        
        webView.loadUrl(script);
    }

    private void handlePaymentCallback(String url) {
        Log.d(TAG, "Handling payment callback: " + url);
        
        try {
            Uri uri = Uri.parse(url);
            
            String responseCode = uri.getQueryParameter("vnp_ResponseCode");
            String transactionStatus = uri.getQueryParameter("vnp_TransactionStatus");
            String txnRef = uri.getQueryParameter("vnp_TxnRef");
            String amount = uri.getQueryParameter("vnp_Amount");
            String transactionNo = uri.getQueryParameter("vnp_TransactionNo");
            
            String success = uri.getQueryParameter("success");
            String message = uri.getQueryParameter("message");
            String orderId = uri.getQueryParameter("orderId");
            String transactionId = uri.getQueryParameter("transactionId");
            
            PaymentResult result;
            
            boolean isSuccess = false;
            if (success != null) {
                isSuccess = "true".equalsIgnoreCase(success);
            } else if (responseCode != null && transactionStatus != null) {
                isSuccess = "00".equals(responseCode) && "00".equals(transactionStatus);
            }
            
            if (isSuccess) {
                result = new PaymentResult(
                    PaymentResult.Status.SUCCESS,
                    message != null ? message : "Payment completed successfully",
                    transactionId != null ? transactionId : transactionNo,
                    orderId != null ? orderId : txnRef,
                    amount != null ? Double.parseDouble(amount) / 100 : this.amount
                );
                handlePaymentSuccess(result);
            } else {
                result = new PaymentResult(
                    PaymentResult.Status.FAILED,
                    message != null ? message : "Payment failed with code: " + responseCode
                );
                result.setTransactionId(transactionId != null ? transactionId : transactionNo);
                result.setOrderId(orderId != null ? orderId : txnRef);
                handlePaymentFailure(result);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling payment callback", e);
            PaymentResult result = new PaymentResult(
                PaymentResult.Status.FAILED,
                "Error processing payment result: " + e.getMessage()
            );
            handlePaymentFailure(result);
        }
    }

    private void handlePaymentSuccess(PaymentResult result) {
        Log.d(TAG, "Payment successful: " + result.getMessage());
        CartManager.getInstance().clearCart();
        
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("transaction_id", result.getTransactionId());
        intent.putExtra("order_id", result.getOrderId());
        intent.putExtra("amount", result.getAmount());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void handlePaymentFailure(PaymentResult result) {
        Log.d(TAG, "Payment failed: " + result.getMessage());
        
        Intent intent = new Intent(this, FailureActivity.class);
        intent.putExtra("error_message", result.getMessage());
        intent.putExtra("order_id", result.getOrderId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class PaymentJavaScriptInterface {
        @JavascriptInterface
        public void onPaymentResult(String paymentDataJson) {
            Log.d(TAG, "Received payment result from JavaScript: " + paymentDataJson);
            
            runOnUiThread(() -> {
                try {
                    handlePaymentResultFromJs(paymentDataJson);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling payment result from JavaScript", e);
                }
            });
        }
    }

    private void handlePaymentResultFromJs(String paymentDataJson) {
        try {
            boolean isSuccess = paymentDataJson.contains("\"success\":true") || 
                               paymentDataJson.contains("\"success\":\"true\"");
            
            if (isSuccess) {
                PaymentResult result = new PaymentResult(
                    PaymentResult.Status.SUCCESS,
                    "Payment completed successfully",
                    extractJsonValue(paymentDataJson, "transactionId"),
                    extractJsonValue(paymentDataJson, "orderId"),
                    this.amount
                );
                handlePaymentSuccess(result);
            } else {
                PaymentResult result = new PaymentResult(
                    PaymentResult.Status.FAILED,
                    extractJsonValue(paymentDataJson, "message")
                );
                handlePaymentFailure(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing payment result from JavaScript", e);
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchPattern = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchPattern);
            if (startIndex != -1) {
                startIndex += searchPattern.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting JSON value for key: " + key, e);
        }
        return "";
    }
} 