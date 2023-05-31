package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("text")
    private String Text;

    @SerializedName("onClick")
    private String OnClick;

    public String getText() {
        return Text;
    }

    public String getOnClick() {
        return OnClick;
    }

    public String getHover() {
        return Hover;
    }

    @SerializedName("hover")
    private String Hover;

    public ChatMessage(String text, String onClick, String hover) {
        Text = text;
        OnClick = onClick;
        Hover = hover;
    }
}
