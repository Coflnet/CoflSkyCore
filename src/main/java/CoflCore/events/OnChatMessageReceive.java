package CoflCore.events;

import CoflCore.classes.ChatMessage;

public class OnChatMessageReceive {
    public final ChatMessage[] ChatMessages;

    public OnChatMessageReceive(ChatMessage[] chatMessages) {
        this.ChatMessages = chatMessages;
    }
}
