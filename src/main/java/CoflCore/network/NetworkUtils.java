package CoflCore.network;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Utility for HTTPS connections with proper SSL/TLS certificate validation.
 * Loads a custom keystore with modern root CA certificates for Java 1.8 compatibility.
 * 
 * Reference: https://moddev.nea.moe/https/#bringing-your-own-certificates
 */
public class NetworkUtils {
    
    private static SSLContext sslContext;
    private static SSLSocketFactory sslSocketFactory;
    
    static {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream keystoreStream = NetworkUtils.class.getResourceAsStream("/ssl/coflkeystore.jks")) {
                if (keystoreStream == null) {
                    throw new RuntimeException("Failed to load coflkeystore.jks from resources");
                }
                keyStore.load(keystoreStream, "changeit".toCharArray());
            }
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, null);
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
            
            System.out.println("CoflCore: SSL keystore loaded successfully");
            
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | 
                 UnrecoverableKeyException | IOException | CertificateException e) {
            System.err.println("CoflCore: Failed to load SSL keystore");
            e.printStackTrace();
            sslContext = null;
            sslSocketFactory = null;
        }
    }
    
    public static SSLContext getSSLContext() {
        return sslContext;
    }
    
    public static SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }
    
    public static HttpsURLConnection setupConnection(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        
        if (sslSocketFactory != null) {
            connection.setSSLSocketFactory(sslSocketFactory);
        }
        
        connection.setRequestProperty("User-Agent", "CoflMod");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        return connection;
    }
    
    public static boolean isSSLInitialized() {
        return sslContext != null && sslSocketFactory != null;
    }
}
