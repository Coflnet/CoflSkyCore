package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Flip {

    public List<ChatMessage> getMessages() {
        return Messages;
    }

    public String getId() {
        return Id;
    }

    public int getWorth() {
        return Worth;
    }

    public Sound getSound() {
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
    private List<ChatMessage> Messages;
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

    public Flip(List<ChatMessage> messages, String id, int worth, Sound soundData ,AuctionItem auctionData, String render, String target) {
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
