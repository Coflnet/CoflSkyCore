package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.classes.*;
import CoflCore.commands.models.*;
import CoflCore.commands.Command;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.RawCommand;
import CoflCore.configuration.Configuration;
import CoflCore.configuration.ConfigurationManager;
import CoflCore.configuration.LocalConfig;
import CoflCore.events.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.*;
import org.greenrobot.eventbus.EventBus;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class WSClient extends WebSocketAdapter {

	public static Gson gson;	
	
	static {
		gson = new GsonBuilder()
			.disableHtmlEscaping()  // Prevent corruption of special characters like §
			.create();
	}
	public URI uri;
	private WebSocket socket;
	public boolean shouldRun = false;
	public WebSocketState currentState = WebSocketState.CLOSED;
	
	public WSClient(URI uri) {
		this.uri = uri;
		
	}
	
	public void start() throws IOException, WebSocketException, NoSuchAlgorithmException {
		WebSocketFactory factory = new WebSocketFactory();
		String scheme = uri.getScheme();
		String host = uri.getHost();
		boolean isSecure = "wss".equalsIgnoreCase(scheme);
		
		// Only configure SSL for secure WebSocket connections
		if (isSecure) {
			if (!NetworkUtils.isSSLInitialized()) {
				System.out.println("[WSClient] SSL not initialized, cannot connect securely to: " + host);
				throw new IOException("SSL keystore failed to load. Cannot establish WebSocket connection.");
			}
			
			boolean allowInsecure = NetworkUtils.allowInsecureConnection(host);
			
			if (allowInsecure) {
				// Use insecure SSL context for localhost connections
				System.out.println("[WSClient] Using insecure SSL context for connection: " + host);
				factory.setSSLContext(NetworkUtils.getInsecureSSLContext());
				factory.setSSLSocketFactory(NetworkUtils.getInsecureSSLSocketFactory());
			} else {
				System.out.println("[WSClient] Using secure SSL context for connection: " + host);
				factory.setSSLContext(NetworkUtils.getSSLContext());
				// IMPORTANT: Must also set SSLSocketFactory for neovisionaries to use our custom trust manager
				// and SNI-enabled socket factory. Without this, the library may use default certificates.
				factory.setSSLSocketFactory(NetworkUtils.getSSLSocketFactory());
			}
			// Disable neovisionaries library's built-in hostname verification 
			// as it has compatibility issues with Java 8 and some certificate configurations.
			// SSL/TLS certificate validation is still performed by the SSLContext's TrustManager.
			// The certificate chain is validated against our embedded root certificates.
			factory.setVerifyHostname(false);
		} else {
			System.out.println("[WSClient] Using plain WebSocket connection (no SSL) to: " + host);
		}
		
		factory.setConnectionTimeout(10*1000);
		this.socket = factory.createSocket(uri);
		this.socket.addListener(this);
		this.socket.connect();
	}
	
	public void stop() {
		System.out.println("Closing Socket");
		if(socket == null)
			return;
		socket.clearListeners();
		socket.disconnect();
		System.out.println("Socket closed");

	}
	
	@Override
	public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
		System.out.println("WebSocket Changed state to: " + newState);
		currentState = newState;
		
		if(newState == WebSocketState.CLOSED && shouldRun) {
			CoflCore.Wrapper.restartWebsocketConnection();
		}
		
		super.onStateChanged(websocket, newState);
	}

	 @Override
	 public void onTextMessage(WebSocket websocket, String text) throws Exception{
		 System.out.println("Received: "+ text);
		 JsonStringCommand body = gson.fromJson(text, JsonStringCommand.class);
		 if (body.getType() == null) {
			 System.out.println("Received a unknown command: " + text);
			 return;
		 }
		 try {
		 	HandleCommand(body);
		 } catch (Exception e) {
			 System.out.println("Error while handling command: " + body.getType() + " with data: " + body.getData());
			 e.printStackTrace();
		 }

	}

	public static void HandleCommand(JsonStringCommand body) {
		EventBus.getDefault().post(new ReceiveCommand(body));
		switch (body.getType()) {
			case Flip:
				EventBus.getDefault().post(new OnFlipReceive(body.GetAs(new TypeToken<FlipData>() {
				}).getData()));
				break;
			case ChatMessage:
				EventBus.getDefault().post(new OnChatMessageReceive(body.GetAs(new TypeToken<ChatMessageData[]>() {
				}).getData()));
				break;
			case PrivacySettings:
				new ConfigurationManager().UpdateConfiguration(body.getData());
				break;
			case WriteToChat:
				EventBus.getDefault().post(new OnWriteToChatReceive(body.GetAs(new TypeToken<ChatMessageData>() {
				}).getData()));
				break;
			case Execute:
				EventBus.getDefault().post(new OnExecuteCommand(body.GetAs(new TypeToken<String>(){
				}).getData()));
				break;
			case Countdown:
				EventBus.getDefault().post(new OnCountdownReceive(body.GetAs(new TypeToken<Countdown>() {
				}).getData()));
				break;
			case GetMods:
				EventBus.getDefault().post(new OnModRequestReceive());
				break;
			case PlaySound:
				EventBus.getDefault().post(new OnPlaySoundReceive(body.GetAs(new TypeToken<Sound>() {
				}).getData()));
				break;
			case HighlightBlocks:
				EventBus.getDefault().post(new OnHighlightBlocks(body.GetAs(new TypeToken<List<Position>>(){
				}).getData()));
				break;
            case RegisterKeybind:
                EventBus.getDefault().post(body.GetAs(new TypeToken<HotkeyRegister[]>() {
                }).getData());
                break;
            case OpenUrl:
                openUrl(body.GetAs(new TypeToken<String>() {
				}).getData());
                break;
			case ProxyRequest:
				EventBus.getDefault().post(new OnWriteToChatReceive(
					new ChatMessageData("This feature is no longer supported.", null, null)));
				break;
            case Ping:
                // nothing to do on ping, just sent to keep connection alive
                break;
            case CommandUpdate:
				CoflCore.config.knownCommands.clear();
                java.util.HashMap<String, String> receivedCommands = gson.fromJson(
					body.getData(), 
					new TypeToken<java.util.HashMap<String, String>>(){}.getType()
				);
				// Sanitize and limit command descriptions to prevent config file corruption
				if (receivedCommands != null) {
					for (java.util.Map.Entry<String, String> entry : receivedCommands.entrySet()) {
						String description = entry.getValue();
						if (description != null && description.length() > 200) {
							// Truncate to 200 characters max
							description = description.substring(0, 200);
							System.err.println("Warning: Truncated description for command '" + entry.getKey() + 
								"' from " + entry.getValue().length() + " to 200 characters");
						}
						CoflCore.config.knownCommands.put(entry.getKey(), description);
					}
				}
				System.out.println("Updated commands: " + CoflCore.config.knownCommands.size());
                break;
            case Settings:
                // Parse the JSON string from the data field
                String settingsJsonString = body.getData();
                ArrayList<Settings> settings = gson.fromJson(settingsJsonString, new TypeToken<ArrayList<Settings>>(){}.getType());
                CoflCore.config.updateSettings(settings);
                EventBus.getDefault().post(new OnSettingsReceive(settings));
                System.out.println("Updated settings: " + settings.size());
                break;
			default:
				break;
		}
	}

	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
		EventBus.getDefault().post(new SocketClose());
		CoflCore.Wrapper = null;
	}

	@Override
	public void onError(WebSocket websocket, WebSocketException cause)	 {
		EventBus.getDefault().post(new SocketError(cause));
	}

	public static void openUrl(String url) {
		try {
			// Validate URL format and domain
			URL parsedUrl = new URL(url);
			String host = parsedUrl.getHost();
			
			if (host == null) {
				System.err.println("Cannot open URL: invalid URL format - " + url);
				return;
			}
			
			// Require domain to be *.coflnet.com
			if (!isValidCoflnetDomain(host)) {
				System.err.println("Cannot open URL: domain must be *.coflnet.com - " + host);
				return;
			}
			
			// Try Desktop API first for secure browser handling
			boolean opened = false;
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.BROWSE)) {
					try {
						desktop.browse(new URI(url));
						opened = true;
					} catch (Exception e) {
						System.err.println("Desktop.browse() failed: " + e.getMessage());
					}
				}
			}
			
			// Fallback to platform-specific commands if Desktop API failed or not supported
			if (!opened) {
				String os = System.getProperty("os.name").toLowerCase();
				ProcessBuilder pb;
				
				if (os.contains("win")) {
					pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
				} else if (os.contains("mac")) {
					pb = new ProcessBuilder("open", url);
				} else if (os.contains("nix") || os.contains("nux")) {
					pb = new ProcessBuilder("xdg-open", url);
				} else {
					System.err.println("Cannot open URL: unsupported operating system.");
					return;
				}
				
				pb.start();
			}
		} catch (Exception e) {
			System.err.println("Error opening URL: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static boolean isValidCoflnetDomain(String host) {
		// Accept coflnet.com or any subdomain of coflnet.com
		return host.equals("coflnet.com") || host.endsWith(".coflnet.com");
	}

	public void SendCommand(Command cmd) {
		SendCommand(new RawCommand(cmd.getType().ToJson(),gson.toJson(cmd.getData())));
	}
	public void SendCommand(RawCommand cmd) {
		Send(cmd);
	}
	
	public void Send(Object obj) {
		String json = gson.toJson(obj);
		System.out.println("###Sending message of json value " + json);
		if(this.socket == null)
			try 
			{
				start();
			} catch(Exception e)
			{
		 		System.out.println("Ran into an error on implicit start for send: "+ e);
			}
		this.socket.sendText(json);
	}
	
}