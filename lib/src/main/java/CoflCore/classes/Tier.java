package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Tier {

    @SerializedName("tier")
    private String RankType;

    @SerializedName("expires")
    private String ExpiresOn;

    public String getRankType() {
        return RankType;
    }

    public String getExpiresOn() {
        return ExpiresOn;
    }

    public Tier() {

    }

    public Tier(String rankType, String expiresOn) {
        super();
        RankType = rankType;
        ExpiresOn = expiresOn;
    }
}
