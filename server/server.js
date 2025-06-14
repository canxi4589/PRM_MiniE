const express = require('express');
const stripe = require('stripe')('sk_test_51RZQtlRpw5sw2xnalWMscpCULRzSPboYAjpXy2HMl1sO7T5z8kKALrlNX3hxkKsdlShdu53MnWcMujVO3zOlnmH9009frbtXBh'); // Replace with your Stripe secret key
const app = express();

app.use(express.json());

// Enable CORS for all routes
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
    if (req.method === 'OPTIONS') {
        res.sendStatus(200);
    } else {
        next();
    }
});

// Create payment intent endpoint
app.post('/create-payment-intent', async (req, res) => {
    try {
        const { amount, currency, orderId, description } = req.body;

        // Validate request
        if (!amount || !currency) {
            return res.status(400).json({
                error: 'Missing required fields: amount and currency'
            });
        }

        // Create payment intent
        const paymentIntent = await stripe.paymentIntents.create({
            amount: Math.round(amount), // Amount should be in cents for USD
            currency: currency.toLowerCase(),
            automatic_payment_methods: {
                enabled: true,
            },
            metadata: {
                orderId: orderId || 'N/A',
                description: description || 'Payment'
            }
        });

        res.json({
            clientSecret: paymentIntent.client_secret,
            paymentIntentId: paymentIntent.id
        });

    } catch (error) {
        console.error('Error creating payment intent:', error);
        res.status(500).json({
            error: 'Failed to create payment intent',
            message: error.message
        });
    }
});

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({ 
        status: 'OK', 
        message: 'Stripe payment server is running',
        timestamp: new Date().toISOString()
    });
});

// Get payment intent status (optional - for checking payment status)
app.get('/payment-intent/:id', async (req, res) => {
    try {
        const paymentIntent = await stripe.paymentIntents.retrieve(req.params.id);
        res.json({
            status: paymentIntent.status,
            amount: paymentIntent.amount,
            currency: paymentIntent.currency,
            metadata: paymentIntent.metadata
        });
    } catch (error) {
        console.error('Error retrieving payment intent:', error);
        res.status(500).json({
            error: 'Failed to retrieve payment intent',
            message: error.message
        });
    }
});

const PORT = process.env.PORT || 4242;

app.listen(PORT, () => {
    console.log(`🚀 Stripe payment server running on port ${PORT}`);
    console.log(`📍 Health check: http://localhost:${PORT}/health`);
    console.log(`💳 Payment endpoint: http://localhost:${PORT}/create-payment-intent`);
}); 