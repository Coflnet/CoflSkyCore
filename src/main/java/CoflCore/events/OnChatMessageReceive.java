package CoflCore.events;

import CoflCore.commands.models.ChatMessageData;

public class OnChatMessageReceive {
    public final ChatMessageData[] ChatMessages;

    public OnChatMessageReceive(ChatMessageData[] chatMessages) {
        this.ChatMessages = chatMessages;
    }
}
