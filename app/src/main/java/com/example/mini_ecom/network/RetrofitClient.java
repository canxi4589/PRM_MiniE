package com.example.mini_ecom.network;

import com.example.mini_ecom.config.ApiConfig;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient() {
        // Create logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (ApiConfig.isDebugMode()) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        // Create OkHttp client with timeouts
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS);

        if (ApiConfig.isDebugMode()) {
            clientBuilder.addInterceptor(logging);
            // Add SSL bypass for development
            clientBuilder = getUnsafeOkHttpClient(clientBuilder);
        }

        OkHttpClient client = clientBuilder.build();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    // Method to update base URL if needed (for different environments)
    public static void updateBaseUrl(String newBaseUrl) {
        instance = null; // Reset instance to force recreation with new URL
        // Update BASE_URL and recreate instance
    }

    /**
     * Create an unsafe OkHttpClient that trusts all certificates.
     * ONLY USE FOR DEVELOPMENT - NEVER IN PRODUCTION!
     */
    private static OkHttpClient.Builder getUnsafeOkHttpClient(OkHttpClient.Builder builder) {
        try {
            // Create a trust manager that accepts all certificates
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return builder
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true; // Trust all hostnames
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 