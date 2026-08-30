package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.commands.Command;
import CoflCore.commands.RawCommand;
import CoflCore.configuration.Config;
import CoflCore.events.OnModChatMessage;
import CoflCore.misc.SessionManager;
import com.neovisionaries.ws.client.WebSocketException;
import org.greenrobot.eventbus.EventBus;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class WSClientWrapper {
    public volatile WSClient socket;
   // public Thread thread;
    public volatile boolean isRunning;

    // Guards ONLY the connection lifecycle (start/stop/socket replacement).
    // SendMessage() must never acquire this - it must never wait on an in-progress
    // (blocking, up to 10s) connect attempt.
    final Object connectionLock = new Object();

    private String[] uris;
    private String connectionId;
    private boolean sslHandshakeFailed = false;


    private volatile boolean wantToStop = false;
		private final AtomicBoolean reconnectInProgress = new AtomicBoolean(false);
		private volatile Thread reconnectThread;
    
    public WSClientWrapper(String[] uris) {
    	this.uris = uris;
    	this.connectionId = UUID.randomUUID().toString();
    }
    
    public void restartWebsocketConnection() {
    	if(socket == null)
    		return;
	    if (wantToStop) {
	    	return;
	    }
	    if (!reconnectInProgress.compareAndSet(false, true)) {
	    	System.out.println("Reconnect already in progress, ignoring duplicate request.");
	    	return;
	    }
    	URI lastUri = socket.uri;
    	socket.stop();
    	
    	System.out.println("Lost connection to Coflnet, trying to reestablish...");

    	// Run retry in a separate thread so it doesn't block the WebSocket event thread forever
	    Thread thread = new Thread(() -> {
	    	try {
	    		socket = new WSClient(lastUri);
	    		isRunning = false;
	    		
				// Retry indefinitely until successful or explicitly stopped via stop()
				while(!isRunning && !wantToStop) {
	    			start();
	    			if (isRunning || wantToStop) break;
	    			
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
				}
				if(socket != null && !wantToStop && isRunning) {
					socket.shouldRun = true;
				}
	    	} finally {
	    		reconnectThread = null;
	    		reconnectInProgress.set(false);
	    	}
	    }, "CoflSky-Reconnect");
	    thread.setDaemon(true);
	    reconnectThread = thread;
	    thread.start();
    }
    
    
    public boolean startConnection(String username) {
    	if(isRunning)
    		return false;
	    if (reconnectInProgress.get())
	    	return false;
    	
    	wantToStop = false;
    	// Generate new connection ID for user-initiated start
    	this.connectionId = UUID.randomUUID().toString();
    	
    	for(String s : uris) {
    		System.out.println("Trying connection with uri=" + s);
    		
    		if(initializeNewSocket(s, username)) {
    			return true;
    		}
    	}

		EventBus.getDefault().post("Cofl could not establish a connection to any server!\n"
				+ "If you use a vpn/proxy please try connecting without it.\n"
				+ "If that does not work please contact us on our ");
    	
    	return false;
    }
    
    
    
    public boolean initializeNewSocket(String uriPrefix, String username) {
    	
    	
    	String uri = uriPrefix;
    	uri += "?version=" + CoflCore.VERSION;
    	uri += "&player=" + username;
    	
    	//Generate a CoflSession
    	
    	try {
			SessionManager.UpdateCoflSessions();
			String coflSessionID = SessionManager.GetCoflSession(username).SessionUUID;
			
			uri += "&SId=" + coflSessionID;
			uri += "&cid=" + this.connectionId;	
			if (Config.ServerContext != null && !Config.ServerContext.isBlank()) {
				uri += "&server=" + Config.ServerContext;
			}

			if(socket != null)
				socket.stop();
			socket = new WSClient(URI.create(uri));
			isRunning = false;
			boolean successfull = start();
			if(successfull) {
				socket.shouldRun = true;
			}
			return successfull;
    	} catch(IOException e) {
    		e.printStackTrace();
    	}			

		return false;   	
    	
    }
    
    public boolean initializeNewSocketWithFallback(String uriPrefix, String username) {
    	// First try with the original URI (might be wss://)
    	if (initializeNewSocket(uriPrefix, username)) {
    		return true;
    	}
    	
    	// If SSL handshake failed and this is a WSS URL, retry with WS
    	if (sslHandshakeFailed && uriPrefix.startsWith("wss://")) {
    		System.out.println("SSL connection failed, retrying with ws:// (insecure)");
    		String insecureUri = uriPrefix.replace("wss://", "ws://");
    		sslHandshakeFailed = false; // Reset flag for the retry
    		if (initializeNewSocket(insecureUri, username)) {
    			EventBus.getDefault().post(new OnModChatMessage(
    				"§eWarning: Connected using insecure WebSocket (ws://) due to SSL issues.\n" +
    				"§ePlease update your Java version for secure connections."
    			));
    			return true;
    		}
    	}
    	
    	// If all attempts failed, show appropriate error message
    	if (sslHandshakeFailed) {
    		EventBus.getDefault().post(new OnModChatMessage(
    			"§cSSL connection failed. This is likely due to an outdated Java version.\n" +
    			"§cPlease update your Java to the latest version (Java 8u141 or newer is recommended).\n" +
    			"§cYou can also try using a different Minecraft launcher that bundles a newer Java runtime."
    		));
    	}
    	
    	return false;
    }
    
    boolean start() {
    	synchronized (connectionLock) {
	    	if(!isRunning) {
	    		try {

					socket.start();
					isRunning = true;

					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WebSocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// Track SSL handshake failures
					Throwable cause = e.getCause();
					while (cause != null) {
						if (cause instanceof SSLHandshakeException) {
							sslHandshakeFailed = true;
							break;
						}
						cause = cause.getCause();
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		return false;
	    	}
			return false;
    	}
    }

    public void stop() {
    	synchronized (connectionLock) {
	        wantToStop = true;
		    Thread currentReconnectThread = reconnectThread;
		    reconnectThread = null;
		    reconnectInProgress.set(false);
		    if (currentReconnectThread != null) {
		    	currentReconnectThread.interrupt();
		    }
	    	if(socket != null) {
	    		socket.shouldRun = false;
	    		socket.stop();
	    	}
	    	isRunning = false;
	    	socket = null;
    	}
    }

    public void SendMessage(RawCommand cmd){
		WSClient currentSocket = this.socket;
		if (this.isRunning && currentSocket != null) {
    		currentSocket.SendCommand(cmd);
		}
    }

    public void SendMessage(Command cmd){
    	WSClient currentSocket = this.socket;
    	if(this.isRunning && currentSocket != null) {
    		currentSocket.SendCommand(cmd);
    	} else {
			System.err.println("Tried sending a callback to coflnet but failed. The connection must be closed. cmd: " + cmd.getType());
		}
    }

	public String GetStatus() {
		return "" + isRunning + " " +  
	    (this.socket!=null ? this.socket.currentState.toString() : "NOT_INITIALIZED");
	}
}
