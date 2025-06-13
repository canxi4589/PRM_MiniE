# Payment Integration Guide

This document explains how the modular payment system is integrated into the Mini Ecommerce Android app.

## Architecture Overview

The payment system is designed with a modular architecture that allows easy addition of new payment methods:

```
payment/
├── PaymentProcessor.java          # Interface for all payment processors
├── PaymentRequest.java           # Base payment request model
├── PaymentResult.java            # Payment result model
├── PaymentManager.java           # Manages all payment processors
└── vnpay/
    ├── VNPayProcessor.java       # VNPay implementation
    ├── VNPayRequest.java         # VNPay specific request
    └── VNPayResponse.java        # VNPay API response
```

## Key Components

### 1. PaymentProcessor Interface
- Defines the contract for all payment methods
- Provides callback interface for payment results
- Supports payment method enumeration

### 2. PaymentManager
- Singleton pattern for managing payment processors
- Handles payment method selection and processing
- Extensible for adding new payment methods

### 3. VNPay Integration
- Implements PaymentProcessor interface
- Handles VNPay API communication
- Supports deep link callbacks

## Configuration

### API Configuration
Update `app/src/main/java/com/example/mini_ecom/config/ApiConfig.java`:

```java
public static final String BASE_URL = "https://your-backend-api.com/api/";
```

### VNPay Backend API
Your backend should implement these endpoints:

#### Create Payment
```
POST /vnpay/create
Content-Type: application/json

{
    "amount": 100000,
    "currency": "VND",
    "orderId": "ORDER_123456789",
    "description": "Payment for order ORDER_123456789",
    "orderInfo": "Payment for order ORDER_123456789",
    "orderType": "billpayment",
    "locale": "vn",
    "returnUrl": "vnpay://return",
    "ipAddr": "127.0.0.1"
}
```

Response:
```json
{
    "success": true,
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "message": "Payment URL created successfully",
    "orderId": "ORDER_123456789",
    "transactionId": "TXN_123456789"
}
```

#### Handle Callback
```
GET /vnpay/callback?vnp_Amount=...&vnp_ResponseCode=...&...
```

## Payment Flow

1. **Checkout**: User fills checkout form
2. **Payment Method Selection**: User selects VNPay
3. **Payment Processing**: App calls backend API to create payment URL
4. **External Payment**: User redirected to VNPay website/app
5. **Callback Handling**: VNPay redirects back to app with result
6. **Success/Failure**: App shows appropriate result screen

## Adding New Payment Methods

### 1. Create Payment Processor
```java
public class PayPalProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.PAYPAL;
    }
    
    @Override
    public void processPayment(Context context, PaymentRequest request, PaymentCallback callback) {
        // Implement PayPal payment logic
    }
    
    // ... other methods
}
```

### 2. Register in PaymentManager
```java
private void initializeProcessors() {
    processors.put(PaymentProcessor.PaymentMethod.VNPAY, new VNPayProcessor());
    processors.put(PaymentProcessor.PaymentMethod.PAYPAL, new PayPalProcessor()); // Add this
}
```

### 3. Add to PaymentMethod Enum
```java
enum PaymentMethod {
    VNPAY("VNPay"),
    PAYPAL("PayPal"), // Add this
    STRIPE("Stripe"),
    BANK_TRANSFER("Bank Transfer");
}
```

## Deep Link Configuration

The app is configured to handle VNPay callbacks via deep links:

```xml
<activity
    android:name=".PaymentProcessingActivity"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="vnpay" />
    </intent-filter>
</activity>
```

## Testing

### Test VNPay Integration
1. Set up VNPay sandbox account
2. Configure backend with VNPay credentials
3. Update `ApiConfig.BASE_URL` with your backend URL
4. Test payment flow with sandbox data

### Mock Payment for Development
You can create a mock payment processor for testing:

```java
public class MockPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(Context context, PaymentRequest request, PaymentCallback callback) {
        // Simulate payment success after 2 seconds
        new Handler().postDelayed(() -> {
            PaymentResult result = new PaymentResult(
                PaymentResult.Status.SUCCESS,
                "Mock payment successful",
                "MOCK_TXN_" + System.currentTimeMillis(),
                request.getOrderId(),
                request.getAmount()
            );
            callback.onPaymentSuccess(result);
        }, 2000);
    }
}
```

## Security Considerations

1. **API Security**: Use HTTPS for all API calls
2. **Validation**: Validate payment callbacks on backend
3. **Sensitive Data**: Never store payment credentials in the app
4. **Deep Links**: Validate deep link data before processing

## Dependencies Added

```kotlin
// Network dependencies
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
```

## Activities Added

1. **PaymentMethodActivity**: Payment method selection
2. **PaymentProcessingActivity**: Handles payment processing and callbacks
3. **PaymentSuccessActivity**: Shows payment success result

## Next Steps

1. Set up your backend API with VNPay integration
2. Update `ApiConfig.BASE_URL` with your actual API URL
3. Test the payment flow
4. Add additional payment methods as needed
5. Implement order history functionality 