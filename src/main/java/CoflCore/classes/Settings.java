package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("key")
    private String SettingKey;

    @SerializedName("name")
    private String SettingName;

    @SerializedName("value")
    private Integer SettingValue;

    @SerializedName("info")
    private String SettingInfo;

    @SerializedName("type")
    private String SettingType;

    @SerializedName("category")
    private String SettingCategory;

    public String getSettingKey() {
        return SettingKey;
    }

    public String getSettingName() {
        return SettingName;
    }

    public Integer getSettingValue() {
        return SettingValue;
    }

    public String getSettingInfo() {
        return SettingInfo;
    }

    public String getSettingType() {
        return SettingType;
    }

    public String getSettingCategory() {
        return SettingCategory;
    }

    public Settings() {

    }

    public Settings(String settingKey, String settingName, Integer settingValue, String settingInfo, String settingType, String settingCategory) {
        this.SettingKey = settingKey;
        this.SettingName = settingName;
        this.SettingValue = settingValue;
        this.SettingInfo = settingInfo;
        this.SettingType = settingType;
        this.SettingCategory = settingCategory;
    }
}
