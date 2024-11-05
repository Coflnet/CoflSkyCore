package CoflCore.events;

import CoflCore.classes.ChatMessage;

public class OnWriteToChatReceive {
    public final ChatMessage ChatMessage;

    public OnWriteToChatReceive(ChatMessage chatMessage) {
        this.ChatMessage = chatMessage;
    }
}

