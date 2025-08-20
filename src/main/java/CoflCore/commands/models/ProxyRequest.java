package CoflCore.commands.models;

import com.google.gson.annotations.SerializedName;

public class ProxyRequest {
    @SerializedName("uploadTo")
    private String uploadTo;

    @SerializedName("id")
    private String id;

    @SerializedName("url")
    private String url;
    @SerializedName("userAgent")
    private String userAgent;
    @SerializedName("regex")
    private String regex;


    public String getId(){
        return id;
    }

    public String getUrl(){
        return url;
    }

    public String getUserAgent() {
        return userAgent;
    }
    public String getUploadTo() {
        return uploadTo;
    }
    public String getRegex() {
        return regex;
    }
}
