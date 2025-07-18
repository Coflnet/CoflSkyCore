package CoflCore.events;

import CoflCore.commands.models.ChatMessageData;

public class OnWriteToChatReceive {
    public final ChatMessageData ChatMessage;

    public OnWriteToChatReceive(ChatMessageData chatMessage) {
        this.ChatMessage = chatMessage;
    }
}

