# Stripe Payment Server

A simple Node.js server to handle Stripe payment intents for the Mini Ecommerce Android app.

## Setup

1. **Install dependencies:**
   ```bash
   cd server
   npm install
   ```

2. **Configure Stripe keys:**
   - Replace the Stripe secret key in `server.js` with your actual Stripe secret key
   - The publishable key is already configured in the Android app

3. **Start the server:**
   ```bash
   npm start
   ```
   
   Or for development with auto-reload:
   ```bash
   npm run dev
   ```

## API Endpoints

### Create Payment Intent
- **POST** `/create-payment-intent`
- **Body:**
  ```json
  {
    "amount": 100,
    "currency": "usd",
    "orderId": "ORDER_123456789",
    "description": "Payment for order ORDER_123456789"
  }
  ```
- **Response:**
  ```json
  {
    "clientSecret": "pi_xxx_secret_xxx",
    "paymentIntentId": "pi_xxx"
  }
  ```

### Health Check
- **GET** `/health`
- **Response:**
  ```json
  {
    "status": "OK",
    "message": "Stripe payment server is running",
    "timestamp": "2024-01-01T00:00:00.000Z"
  }
  ```

### Get Payment Intent Status
- **GET** `/payment-intent/:id`
- **Response:**
  ```json
  {
    "status": "succeeded",
    "amount": 100,
    "currency": "usd",
    "metadata": {
      "orderId": "ORDER_123456789",
      "description": "Payment for order ORDER_123456789"
    }
  }
  ```

## Testing

The server will run on `http://localhost:4242` by default.

You can test the health endpoint:
```bash
curl http://localhost:4242/health
```

Test payment intent creation:
```bash
curl -X POST http://localhost:4242/create-payment-intent \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100,
    "currency": "usd",
    "orderId": "TEST_ORDER_123",
    "description": "Test payment"
  }'
```

## Android App Configuration

The Android app is configured to connect to `http://10.0.2.2:4242` which is the localhost address accessible from Android emulator. If you're testing on a real device, update the `BACKEND_URL` in `StripeProcessor.java` to your computer's IP address.

## Security Notes

- **Never commit your actual Stripe secret key to version control**
- Use environment variables for production:
  ```javascript
  const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);
  ```
- Implement proper authentication and validation for production use
- Add request rate limiting and other security measures 