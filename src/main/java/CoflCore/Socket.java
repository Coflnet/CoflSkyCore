package CoflCore;

import CoflCore.classes.*;
import CoflCore.events.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class Socket extends WebSocketClient {
    public static Gson gson = new Gson();
    public Socket(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        EventBus.getDefault().post(new SocketOpen());
    }

    @Override
    public void onMessage(String message) {
        JsonStringCommand body = gson.fromJson(message, JsonStringCommand.class);
        EventBus.getDefault().post(new ReceiveCommand(body));
        switch (body.getType()) {
            case Flip -> {
                EventBus.getDefault().post(new OnFlipReceive(body.GetAs(new TypeToken<Flip>() {
                }).getData()));
            }
            case ChatMessage -> {
                EventBus.getDefault().post(new OnChatMessageReceive(body.GetAs(new TypeToken<ChatMessage>() {
                }).getData()));
            }
            case PrivacySettings -> {
//                TODO: Update so that it is all handled library side
            }
            case Error -> {
                EventBus.getDefault().post(new OnCoflError(body.getData()));
            }
            case WriteToChat -> {
                EventBus.getDefault().post(new OnWriteToChatReceive(body.GetAs(new TypeToken<ChatMessage[]>() {
                }).getData()));
            }
            case Execute -> {
                EventBus.getDefault().post(new OnExecuteCommand(body.getData()));
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
            case Log -> {
                EventBus.getDefault().post(new OnLogReceive(body.getData()));
            }
            case Settings -> {
                EventBus.getDefault().post(new OnSettingsReceive(body.GetAs(new TypeToken<Settings>() {
                }).getData()));
            }
            case Tier -> {
                EventBus.getDefault().post(new OnTierRequestReceive(body.GetAs(new TypeToken<Tier>() {
                }).getData()));
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        EventBus.getDefault().post(new SocketClose());
        CoflCore.setSocket(null);
    }

    @Override
    public void onError(Exception ex) {
        EventBus.getDefault().post(new SocketError(ex));
    }
}


