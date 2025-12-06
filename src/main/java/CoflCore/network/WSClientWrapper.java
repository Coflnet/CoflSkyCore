package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.commands.Command;
import CoflCore.commands.RawCommand;
import CoflCore.events.OnModChatMessage;
import CoflCore.misc.SessionManager;
import com.neovisionaries.ws.client.WebSocketException;
import org.greenrobot.eventbus.EventBus;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


public class WSClientWrapper {
    public WSClient socket;
   // public Thread thread;
    public boolean isRunning;
    
    private String[] uris;
    private String connectionId;
    private boolean sslHandshakeFailed = false;

    
    public WSClientWrapper(String[] uris) {
    	this.uris = uris;
    	this.connectionId = UUID.randomUUID().toString();
    }
    
    public void restartWebsocketConnection() {
    	socket.stop();
    	
    	System.out.println("Lost connection to Coflnet, trying to reestablish the connection in 2 Seconds...");

    	socket = new WSClient(socket.uri);
    	isRunning = false;   
		while(isRunning == false) {
    		start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		socket.shouldRun = true;
    }
    
    
    public boolean startConnection(String username) {
    	if(isRunning)
    		return false;
    	
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
    
    private synchronized boolean start() {
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
    
    public synchronized void stop() {
    	if(isRunning) {
    		socket.shouldRun = false;
    		socket.stop();
    		isRunning = false;
    		socket = null;
    	}
    }
    
    public synchronized void SendMessage(RawCommand cmd){
		if (this.isRunning) {
    		this.socket.SendCommand(cmd);
		}
    }

    public synchronized void SendMessage(Command cmd){
    	if(this.isRunning) {
    		this.socket.SendCommand(cmd);
    	} else {
			System.err.println("Tried sending a callback to coflnet but failed. The connection must be closed. cmd: " + cmd.getType());
		}
    }

	public String GetStatus() {
		return "" + isRunning + " " +  
	    (this.socket!=null ? this.socket.currentState.toString() : "NOT_INITIALIZED");
	}
}
