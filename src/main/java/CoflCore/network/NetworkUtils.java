package CoflCore.network;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

/**
 * Utility for HTTPS connections with proper SSL/TLS certificate validation.
 * Embedded JKS keystore (Java 1.8 compatible format) with root certificates from Java 21.
 * 
 * Keystore generation:
 * - Import root certificates from Java 21 JDK's cacerts using keytool
 * - Store in JKS format for compatibility with Java 1.8 Minecraft clients
 * - Encode to Base64 for embedding in source code
 * 
 * Reference: https://moddev.nea.moe/https/#bringing-your-own-certificates
 */
public class NetworkUtils {
    
    // JKS keystore (Java 1.8 compatible) with root certificates from Java 21
    // Location: src/main/resources/ssl/coflkeystore.jks
    // Password: changeit (default for all Java keystores)
    //
    // Loaded via classpath resource: /ssl/coflkeystore.jks
    
    private static final String KEYSTORE_RESOURCE_PATH = "/ssl/coflkeystore.jks";
    private static final String KEYSTORE_PASSWORD = "changeit";
    
    private static SSLContext sslContext;
    private static SSLSocketFactory sslSocketFactory;
    private static SSLContext insecureSSLContext;
    private static SSLSocketFactory insecureSSLSocketFactory;
    
    static {
        try {
            // Load keystore from JAR resource at: /ssl/coflkeystore.jks
            // This keystore is generated with Java 21 root certificates but stored in JKS format for Java 1.8 compatibility
            InputStream keystoreInputStream = NetworkUtils.class.getResourceAsStream(KEYSTORE_RESOURCE_PATH);
            
            if (keystoreInputStream == null) {
                throw new IOException("Keystore resource not found at: " + KEYSTORE_RESOURCE_PATH);
            }

            // Load JKS keystore (Java 1.8 compatible format)
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keystoreInputStream, KEYSTORE_PASSWORD.toCharArray());
            keystoreInputStream.close();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
            
            // Create insecure SSL context for localhost development
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };
            insecureSSLContext = SSLContext.getInstance("TLS");
            insecureSSLContext.init(null, trustAllCerts, new java.security.SecureRandom());
            insecureSSLSocketFactory = insecureSSLContext.getSocketFactory();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
            System.err.println("Failed to initialize SSL context: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static SSLContext getSSLContext() {
        return sslContext;
    }
    
    public static SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }
    
    public static HttpURLConnection setupConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Apply SSL settings only for HTTPS connections
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            String host = url.getHost();
            
            if (allowInsecureConnection(host)) {
                // Use insecure SSL for localhost/coflnet.com connections
                if (insecureSSLSocketFactory != null) {
                    httpsConnection.setSSLSocketFactory(insecureSSLSocketFactory);
                    httpsConnection.setHostnameVerifier((hostname, session) -> true);
                }
            } else {
                // Use secure SSL for remote connections
                if (sslSocketFactory != null) {
                    httpsConnection.setSSLSocketFactory(sslSocketFactory);
                }
            }
        }
        
        connection.setRequestProperty("User-Agent", "SkyCoflMod/1.7.9");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        return connection;
    }
    
    public static boolean isSSLInitialized() {
        return sslContext != null && sslSocketFactory != null;
    }
    
    /**
     * Get insecure SSL context for localhost development connections.
     * WARNING: Only use this for localhost connections during development!
     */
    public static SSLContext getInsecureSSLContext() {
        return insecureSSLContext;
    }
    
    /**
     * Get insecure SSL socket factory for localhost development connections.
     * WARNING: Only use this for localhost connections during development!
     */
    public static SSLSocketFactory getInsecureSSLSocketFactory() {
        return insecureSSLSocketFactory;
    }
    
    /**
     * Check if the given host allows insecure connections.
     * This includes localhost addresses and coflnet.com subdomains (for direct connections).
     */
    public static boolean allowInsecureConnection(String host) {
        if (host == null) return false;
        return host.equals("localhost") || 
               host.equals("127.0.0.1") || 
               host.equals("[::1]") ||
               host.startsWith("192.168.") ||
               host.startsWith("10.");
    }
}
