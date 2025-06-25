package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.classes.ChatMessage;
import CoflCore.classes.Countdown;
import CoflCore.classes.Flip;
import CoflCore.classes.Sound;
import CoflCore.commands.Command;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.RawCommand;
import CoflCore.commands.models.ProxyRequest;
import CoflCore.configuration.Configuration;
import CoflCore.configuration.ConfigurationManager;
import CoflCore.configuration.LocalConfig;
import CoflCore.events.*;
import CoflCore.proxy.ProxyManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.*;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

public class WSClient extends WebSocketAdapter {

	public static Gson gson;
	private static final ProxyManager proxyManager = new ProxyManager();
	
	
	static {
		gson = new GsonBuilder()/*.setFieldNamingStrategy(new FieldNamingStrategy() {
			@Override
			public String translateName(Field f) {
				
				String name = f.getName();
				char firstChar = name.charAt(0);
				return Character.toLowerCase(firstChar) + name.substring(1);
			}
		})*/.create();
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
		
		/*// Create a custom SSL context.
		SSLContext context = NaiveSSLContext.getInstance("TLS");

		// Set the custom SSL context.
		factory.setSSLContext(context);

		// Disable manual hostname verification for NaiveSSLContext.
		//
		// Manual hostname verification has been enabled since the
		// version 2.1. Because the verification is executed manually
		// after Socket.connect(SocketAddress, int) succeeds, the
		// hostname verification is always executed even if you has
		// passed an SSLContext which naively accepts any server
		// certificate. However, this behavior is not desirable in
		// some cases and you may want to disable the hostname
		// verification. You can disable the hostname verification
		// by calling WebSocketFactory.setVerifyHostname(false).
		factory.setVerifyHostname(false);
		factory.*/
		factory.setVerifyHostname(false);
		factory.setSSLContext(NaiveSSLContext.getInstance("TLSv1.2"));
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
		 HandleCommand(body);

	}

	public static void HandleCommand(JsonStringCommand body) {
		EventBus.getDefault().post(new ReceiveCommand(body));
		switch (body.getType()) {
			case Flip -> {
				EventBus.getDefault().post(new OnFlipReceive(body.GetAs(new TypeToken<Flip>() {
				}).getData()));
			}
			case ChatMessage -> {
				EventBus.getDefault().post(new OnChatMessageReceive(body.GetAs(new TypeToken<ChatMessage[]>() {
				}).getData()));
			}
			case PrivacySettings -> {
				new ConfigurationManager().UpdateConfiguration(body.getData());
			}
			case WriteToChat -> {
				EventBus.getDefault().post(new OnWriteToChatReceive(body.GetAs(new TypeToken<ChatMessage>() {
				}).getData()));
			}
			case Execute -> {
				EventBus.getDefault().post(new OnExecuteCommand(body.GetAs(new TypeToken<String>(){
				}).getData()));
			}
			case Countdown -> {
				EventBus.getDefault().post(new OnCountdownReceive(body.GetAs(new TypeToken<Countdown>() {
				}).getData()));
			}
			case GetMods -> {
				EventBus.getDefault().post(new OnModRequestReceive());
			}
			case PlaySound -> {
				EventBus.getDefault().post(new OnPlaySoundReceive(body.GetAs(new TypeToken<Sound>() {
				}).getData()));
			}
			case ProxyRequest -> {
				ProxyRequest[] proxyRequests = body.GetAs(new TypeToken<ProxyRequest[]>() {
				}).getData();

				for (ProxyRequest req : proxyRequests) {
					proxyManager.handleRequestAsync(req);
				}
			}
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