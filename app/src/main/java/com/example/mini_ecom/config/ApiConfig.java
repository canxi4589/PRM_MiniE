package com.example.mini_ecom.config;

public class ApiConfig {
    // TODO: Replace with your actual API base URL
    // For Android Emulator, use 10.0.2.2 to access host machine's localhost
    // For physical device, use your computer's actual IP address
    public static final String BASE_URL = "http://10.0.2.2:5096/api/"; // For emulator (HTTP port)
    // public static final String BASE_URL = "http://192.168.2.74:7190/api/"; // For physical device (your IP)

    // VNPay Configuration
    // Option 1: Direct deep link (may not work with all payment providers)
    // public static final String VNPAY_RETURN_URL = "vnpay://return";
    
    // Option 2: Web callback that redirects to deep link (recommended)
    public static final String VNPAY_RETURN_URL = "http://10.0.2.2:5096/api/payment/vnpay/callback";
    
    // Timeout configurations (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // Debug mode
    public static final boolean DEBUG_MODE = true; // Set to false in production
    
    /**
     * Get the base URL for API calls
     * You can modify this method to return different URLs based on build variants
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }
    
    /**
     * Check if we're in debug mode
     */
    public static boolean isDebugMode() {
        return DEBUG_MODE;
    }
} 