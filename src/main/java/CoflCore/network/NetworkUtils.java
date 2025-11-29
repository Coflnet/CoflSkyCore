package CoflCore.network;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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

            // Use TLSv1.2 explicitly for Java 8 compatibility - required by modern servers
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
            // Wrap the socket factory to enable SNI for Java 8 compatibility
            sslSocketFactory = new SNISocketFactory(sslContext.getSocketFactory());
            
            System.out.println("[NetworkUtils] SSL context initialized successfully with " + keyStore.size() + " certificates");
            
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
            insecureSSLContext = SSLContext.getInstance("TLSv1.2");
            insecureSSLContext.init(null, trustAllCerts, new java.security.SecureRandom());
            insecureSSLSocketFactory = new SNISocketFactory(insecureSSLContext.getSocketFactory());
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
    
    /**
     * Custom SSLSocketFactory that enables SNI (Server Name Indication) for Java 8 compatibility.
     * This is required for connecting to servers behind Cloudflare and other CDNs that use SNI
     * to determine which certificate to present.
     */
    private static class SNISocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        
        public SNISocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }
        
        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }
        
        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
            enableSNI(socket, host);
            return socket;
        }
        
        @Override
        public Socket createSocket(String host, int port) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
            enableSNI(socket, host);
            return socket;
        }
        
        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
            enableSNI(socket, host);
            return socket;
        }
        
        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
            // Cannot enable SNI without hostname
            return socket;
        }
        
        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
            // Cannot enable SNI without hostname
            return socket;
        }
        
        /**
         * Enable SNI extension and configure TLS parameters for the socket.
         * Also ensures TLS 1.2 is enabled and proper cipher suites are available.
         */
        private void enableSNI(SSLSocket socket, String hostname) {
            if (hostname == null || hostname.isEmpty()) {
                return;
            }
            
            try {
                // Explicitly enable TLS 1.2 - very old Java 8 versions may not have it enabled by default
                String[] supportedProtocols = socket.getSupportedProtocols();
                List<String> enabledProtocols = new ArrayList<>();
                for (String protocol : supportedProtocols) {
                    if (protocol.equals("TLSv1.2") || protocol.equals("TLSv1.1") || protocol.equals("TLSv1")) {
                        enabledProtocols.add(protocol);
                    }
                }
                if (!enabledProtocols.isEmpty()) {
                    socket.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
                }
                
                // Enable additional cipher suites that may be needed for ECDSA certificates
                // Some Java 8 versions don't enable all EC cipher suites by default
                String[] supportedCiphers = socket.getSupportedCipherSuites();
                List<String> enabledCiphers = new ArrayList<>();
                
                // Prioritize strong modern ciphers with EC support
                String[] preferredCiphers = {
                    "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_RSA_WITH_AES_256_GCM_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_RSA_WITH_AES_128_CBC_SHA256"
                };
                
                // Add preferred ciphers first (if supported)
                for (String preferredCipher : preferredCiphers) {
                    for (String supportedCipher : supportedCiphers) {
                        if (supportedCipher.equals(preferredCipher)) {
                            enabledCiphers.add(preferredCipher);
                            break;
                        }
                    }
                }
                
                // Add remaining supported ciphers (excluding weak ones)
                for (String cipher : supportedCiphers) {
                    if (!enabledCiphers.contains(cipher) && 
                        !cipher.contains("_NULL_") && 
                        !cipher.contains("_anon_") && 
                        !cipher.contains("_EXPORT_") &&
                        !cipher.contains("_DES_") &&
                        !cipher.contains("_RC4_") &&
                        !cipher.contains("_MD5")) {
                        enabledCiphers.add(cipher);
                    }
                }
                
                if (!enabledCiphers.isEmpty()) {
                    socket.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
                }
                
                // Get current SSL parameters and set SNI
                SSLParameters params = socket.getSSLParameters();
                
                // Create SNI host name using reflection to support Java 8
                // SNIHostName was added in Java 8, so we use it directly
                List<SNIServerName> serverNames = new ArrayList<>();
                serverNames.add(new SNIHostName(hostname));
                params.setServerNames(serverNames);
                
                // Apply the parameters back to the socket
                socket.setSSLParameters(params);
                
                System.out.println("[NetworkUtils] SNI enabled for host: " + hostname + 
                    ", protocols: " + enabledProtocols.size() + ", ciphers: " + enabledCiphers.size());
            } catch (Exception e) {
                System.err.println("[NetworkUtils] Failed to enable SNI for " + hostname + ": " + e.getMessage());
            }
        }
    }
}
