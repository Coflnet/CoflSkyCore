package CoflCore.network;

import CoflCore.commands.CommandType;
import CoflCore.commands.RawCommand;
import CoflCore.events.ReceiveCommand;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InfoDisplayProtocolTest {
    public static class Receiver {
        ReceiveCommand received;

        @Subscribe
        public void receive(ReceiveCommand event) {
            received = event;
        }
    }

    @Test
    public void websocketDispatchesInfoDisplayPayloadUnchanged() throws Exception {
        Receiver receiver = new Receiver();
        EventBus.getDefault().register(receiver);
        try {
            WSClient client = new WSClient(URI.create("ws://localhost"));
            for (String payload : new String[] {
                    "{\"id\":1,\"lines\":[{\"text\":\"Clear\",\"onClick\":\"/cofl test display clear\",\"hover\":\"Clear panel\"}],\"ttl\":60}",
                    "{\"id\":1,\"clear\":true}"
            }) {
                receiver.received = null;
                client.onTextMessage(null, WSClient.gson.toJson(new RawCommand("infoDisplay", payload)));
                assertNotNull(receiver.received);
                assertEquals(CommandType.InfoDisplay, receiver.received.command.getType());
                assertEquals(payload, receiver.received.command.getData());
            }
            assertEquals("infoDisplay", CommandType.InfoDisplay.ToJson());
        } finally {
            EventBus.getDefault().unregister(receiver);
        }
    }
}
