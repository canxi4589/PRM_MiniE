# VNPay Integration Guide

A comprehensive guide explaining how VNPay payment gateway integration works in the Mini Ecommerce Android application.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Component Details](#component-details)
4. [Payment Flow](#payment-flow)
5. [Configuration](#configuration)
6. [Backend API](#backend-api)
7. [Deep Link Handling](#deep-link-handling)
8. [Testing](#testing)
9. [Error Handling](#error-handling)
10. [Security Considerations](#security-considerations)

## Overview

VNPay is Vietnam's leading electronic payment platform. Our integration allows customers to pay using:
- Vietnamese banks (ATM cards, Internet Banking)
- International cards (Visa, MasterCard, JCB, etc.)
- VNPay QR code
- Mobile banking apps

### Key Features
- **Web-based Payment**: Uses WebView for secure payment processing
- **Deep Link Support**: Automatic return to app after payment
- **Multiple Payment Methods**: Supports all VNPay payment options
- **Real-time Callback**: Instant payment status updates
- **Transaction History**: Automatic order tracking and history

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Android App   │    │  Backend API    │    │   VNPay API     │
│                 │    │                 │    │                 │
│ PaymentMethod   │───▶│ /vnpay/create   │───▶│ Payment Gateway │
│ Activity        │    │                 │    │                 │
│                 │    │                 │    │                 │
│ PaymentWeb      │◀───│ /vnpay/callback │◀───│ Payment Result  │
│ ViewActivity    │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Integration follows the modular PaymentProcessor pattern:

```
payment/
├── PaymentProcessor.java          # Interface for all payment methods
├── PaymentRequest.java           # Base payment request model
├── PaymentResult.java            # Payment result model
├── PaymentManager.java           # Central payment coordinator
└── vnpay/
    ├── VNPayProcessor.java       # VNPay implementation
    ├── VNPayRequest.java         # VNPay-specific request model
    └── VNPayResponse.java        # VNPay API response model
```

## Component Details

### 1. VNPayProcessor.java

The main processor implementing the `PaymentProcessor` interface.

**Key Methods:**
- `processPayment()`: Initiates VNPay payment by calling backend API
- `handlePaymentResult()`: Processes callback data from VNPay
- `isAvailable()`: Checks if VNPay is available (network connectivity)

**Flow:**
1. Creates API call to backend with payment details
2. Receives payment URL from backend
3. Opens `PaymentWebViewActivity` with payment URL
4. Handles payment result via deep links or JavaScript interface

```java
@Override
public void processPayment(Context context, PaymentRequest request, PaymentCallback callback) {
    // 1. Validate request type
    if (!(request instanceof VNPayRequest)) {
        callback.onPaymentFailure(/* error */);
        return;
    }
    
    // 2. Call backend API to create payment
    Call<VNPayResponse> call = RetrofitClient.getInstance()
            .getApiService()
            .createVNPayPayment(vnpayRequest);
            
    // 3. Handle API response and open WebView
    call.enqueue(/* callback handling */);
}
```

### 2. VNPayRequest.java

Extends `PaymentRequest` with VNPay-specific fields:

```java
public class VNPayRequest extends PaymentRequest {
    private String orderInfo;      // Payment description
    private String orderType;      // "billpayment" for purchases
    private String locale;         // "vn" for Vietnamese
    private String returnUrl;      // Callback URL after payment
    private String ipAddr;         // Customer IP (set by backend)
}
```

**Default Values:**
- `currency`: "VND" (Vietnamese Dong)
- `orderType`: "billpayment"
- `locale`: "vn"
- `returnUrl`: From `ApiConfig.VNPAY_RETURN_URL`

### 3. VNPayResponse.java

Response model from backend API:

```java
public class VNPayResponse {
    private boolean success;       // API call success status
    private String paymentUrl;     // VNPay payment URL
    private String message;        // Response message
    private String orderId;        // Order reference
    private String transactionId;  // Transaction reference
}
```

### 4. PaymentWebViewActivity.java

Handles the web-based payment process:

**Features:**
- Full-screen WebView with VNPay payment page
- Progress indicator during loading
- JavaScript interface for payment result detection
- Automatic callback URL detection
- Error handling for network issues

**Key Components:**
- `WebViewClient`: Handles URL loading and page events
- `WebChromeClient`: Manages progress updates
- `PaymentJavaScriptInterface`: Enables communication between web page and app
- Callback detection via URL monitoring and JavaScript injection

## Payment Flow

### Step-by-Step Process

1. **User Initiates Payment**
   ```
   CartActivity → CheckoutActivity → PaymentMethodActivity
   ```
   - User selects items and proceeds to checkout
   - Enters customer information
   - Selects "VNPay" as payment method

2. **Payment Processing Begins**
   ```
   PaymentMethodActivity → PaymentProcessingActivity
   ```
   - Creates `VNPayRequest` with order details
   - Calls `PaymentManager.processPayment()`
   - `VNPayProcessor.processPayment()` executes

3. **Backend API Call**
   ```
   Android App → Backend API → VNPay API
   ```
   - POST to `/api/Payment/vnpay/create`
   - Backend creates VNPay payment URL
   - Returns `VNPayResponse` with payment URL

4. **WebView Payment**
   ```
   PaymentProcessingActivity → PaymentWebViewActivity
   ```
   - Opens WebView with VNPay payment URL
   - User completes payment on VNPay website
   - Supports multiple payment methods (bank transfer, cards, QR)

5. **Payment Callback**
   ```
   VNPay → Backend Callback → App Deep Link
   ```
   - VNPay redirects to backend callback URL
   - Backend processes VNPay response and validates signature
   - Backend redirects to app deep link with result

6. **Result Processing**
   ```
   PaymentWebViewActivity → PaymentProcessingActivity → Success/Failure
   ```
   - App receives callback via deep link or JavaScript
   - `VNPayProcessor.handlePaymentResult()` processes result
   - Shows success/failure screen and updates order history

## Configuration

### Android App Configuration

#### 1. ApiConfig.java
```java
public class ApiConfig {
    // Backend API base URL
    public static final String BASE_URL = "http://10.0.2.2:5096/api/";
    
    // VNPay return URL (backend callback endpoint)
    public static final String VNPAY_RETURN_URL = 
        "http://10.0.2.2:5096/api/payment/vnpay/callback";
}
```

#### 2. Deep Link Configuration (AndroidManifest.xml)
```xml
<activity
    android:name=".PaymentProcessingActivity"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="vnpay" android:host="return" />
    </intent-filter>
</activity>
```

### Backend Configuration Requirements

Your backend API must implement these endpoints:

#### 1. Create Payment Endpoint
```
POST /api/Payment/vnpay/create
Content-Type: application/json

Request Body:
{
    "amount": 100000,
    "currency": "VND",
    "orderId": "ORDER_123456789",
    "description": "Payment for order ORDER_123456789",
    "orderInfo": "Payment for order ORDER_123456789",
    "orderType": "billpayment",
    "locale": "vn",
    "returnUrl": "http://10.0.2.2:5096/api/payment/vnpay/callback",
    "ipAddr": "127.0.0.1"
}

Response:
{
    "success": true,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "message": "Payment URL created successfully",
    "orderId": "ORDER_123456789",
    "transactionId": "TXN_123456789"
}
```

#### 2. Callback Handling Endpoint
```
GET /api/payment/vnpay/callback?vnp_Amount=...&vnp_ResponseCode=...&...
```

**VNPay Callback Parameters:**
- `vnp_Amount`: Payment amount (in cents)
- `vnp_ResponseCode`: Payment result code ("00" = success)
- `vnp_TransactionStatus`: Transaction status ("00" = success)
- `vnp_TxnRef`: Order reference
- `vnp_TransactionNo`: VNPay transaction number
- `vnp_SecureHash`: Security signature for verification

## Testing

### Test Environment Setup

1. **VNPay Sandbox**
   - Use VNPay sandbox environment for testing
   - Sandbox URL: `https://sandbox.vnpayment.vn/`
   - Test merchant credentials from VNPay

2. **Test Cards**
   ```
   Bank: NCB
   Card Number: 9704198526191432198
   Card Name: NGUYEN VAN A
   Issue Date: 07/15
   OTP: 123456
   ```

### Response Code Mapping

| VNPay Code | Status | Description |
|------------|--------|-------------|
| 00 | SUCCESS | Payment successful |
| 07 | FAILED | Transaction deducted, awaiting confirmation |
| 09 | FAILED | Transaction failed, pending reversal |
| 10 | FAILED | Payment failed |
| 11 | FAILED | Payment timeout |
| 12 | FAILED | Account locked |
| 13 | FAILED | Incorrect OTP |
| 24 | CANCELLED | User cancelled |
| 51 | FAILED | Insufficient balance |
| 65 | FAILED | Daily limit exceeded |

## Security Considerations

### 1. Data Protection
- Never store VNPay credentials in the app
- Use HTTPS for all API communications
- Validate all callback parameters server-side
- Implement secure hash verification

### 2. Payment Security
- All sensitive operations performed on backend
- Signature validation prevents callback tampering
- Order information encrypted during transmission
- Payment amounts validated on both client and server

---

This integration provides a robust, secure, and user-friendly VNPay payment experience within your Mini Ecommerce application. 