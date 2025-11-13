package CoflCore.network;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    
    public static HttpsURLConnection setupConnection(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        
        if (sslSocketFactory != null) {
            connection.setSSLSocketFactory(sslSocketFactory);
        }
        
        connection.setRequestProperty("User-Agent", "SkyCoflMod/1.7.8");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        return connection;
    }
    
    public static boolean isSSLInitialized() {
        return sslContext != null && sslSocketFactory != null;
    }
}
