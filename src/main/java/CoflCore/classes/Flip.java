package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Flip {

    public ChatMessage[] getMessages() {
        return Messages;
    }

    public String getId() {
        return Id;
    }

    public int getWorth() {
        return Worth;
    }

    public CoflCore.classes.Sound getSound() {
        return Sound;
    }

    public AuctionItem getAuctionData() {
        return AuctionData;
    }

    public String getRender() {
        return Render;
    }

    public String getTargetPrice() {
        return TargetPrice;
    }

    @SerializedName("messages")
    private ChatMessage[] Messages;
    @SerializedName("id")
    private String Id;
    @SerializedName("worth")
    private int Worth;
    @SerializedName("sound")
    private Sound Sound;
    @SerializedName("auction")
    private AuctionItem AuctionData;
    @SerializedName("render")
    private String Render;

    @SerializedName("target")
    private String TargetPrice;

    public Flip() {
    }

    public Flip(ChatMessage[] messages, String id, int worth, Sound soundData ,AuctionItem auctionData, String render, String target) {
        super();
        Messages = messages;
        Id = id;
        Worth = worth;
        AuctionData = auctionData;
        Sound = soundData;
        Render = render;
        TargetPrice = target;
    }
}
