package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.List;

public class PrivacySettings {

    @SerializedName("chatRegex")
    private String ChatRegex;

    @SerializedName("collectChat")
    private Boolean CollectChat;

    @SerializedName("collectInventory")
    private Boolean CollectInventory;

    @SerializedName("collectTab")
    private Boolean CollectTab;

    @SerializedName("collectScoreboard")
    private Boolean CollectScoreboard;

    public String getChatRegex() {
        return ChatRegex;
    }

    public Boolean getCollectChat() {
        return CollectChat;
    }

    public Boolean getCollectInventory() {
        return CollectInventory;
    }

    public Boolean getCollectTab() {
        return CollectTab;
    }

    public Boolean getCollectScoreboard() {
        return CollectScoreboard;
    }

    public Boolean getAllowProxy() {
        return AllowProxy;
    }

    public Boolean getCollectLobbyChanges() {
        return CollectLobbyChanges;
    }

    public Boolean getCollectEntities() {
        return CollectEntities;
    }

    public Boolean getCollectLocation() {
        return CollectLocation;
    }

    public Boolean getExtendDescriptions() {
        return ExtendDescriptions;
    }

    public List getCommandPrefixes() {
        return CommandPrefixes;
    }

    public Boolean getAutoStart() {
        return AutoStart;
    }

    @SerializedName("allowProxy")
    private Boolean AllowProxy;

    @SerializedName("collectLobbyChanges")
    private Boolean CollectLobbyChanges;

    @SerializedName("collectEntities")
    private Boolean CollectEntities;

    @SerializedName("collectLocation")
    private Boolean CollectLocation;

    @SerializedName("extendDescriptions")
    private Boolean ExtendDescriptions;

    @SerializedName("commandPrefixes")
    private List CommandPrefixes;

    @SerializedName("autoStart")
    private Boolean AutoStart;

    public PrivacySettings(String chatRegex, Boolean collectChat, Boolean collectInventory, Boolean collectTab, Boolean collectScoreboard, Boolean allowProxy, Boolean collectLobbyChanges, Boolean collectEntities, Boolean collectLocation, Boolean extendDescriptions, List commandPrefixes, Boolean autoStart) {
        super();
        this.ChatRegex = chatRegex;
        this.CollectChat = collectChat;
        this.CollectInventory = collectInventory;
        this.CollectTab = collectTab;
        this.CollectScoreboard = collectScoreboard;
        this.AllowProxy = allowProxy;
        this.CollectLobbyChanges = collectLobbyChanges;
        this.CollectEntities = collectEntities;
        this.CollectLocation = collectLocation;
        this.ExtendDescriptions = extendDescriptions;
        this.CommandPrefixes = commandPrefixes;
        this.AutoStart = autoStart;
    }

    public PrivacySettings() {

    }
}
