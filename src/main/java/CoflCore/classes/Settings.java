package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("key")
    private String SettingKey;

    @SerializedName("name")
    private String SettingName;

    @SerializedName("value")
    private Object SettingValue;

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

    public Object getSettingValue() {
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

    public void setSettingKey(String settingKey) {
        this.SettingKey = settingKey;
    }

    public void setSettingName(String settingName) {
        this.SettingName = settingName;
    }

    public void setSettingValue(Object settingValue) {
        this.SettingValue = settingValue;
    }

    public void setSettingInfo(String settingInfo) {
        this.SettingInfo = settingInfo;
    }

    public void setSettingType(String settingType) {
        this.SettingType = settingType;
    }

    public void setSettingCategory(String settingCategory) {
        this.SettingCategory = settingCategory;
    }

    public Settings() {

    }

    public Settings(String settingKey, String settingName, Object settingValue, String settingInfo, String settingType, String settingCategory) {
        this.SettingKey = settingKey;
        this.SettingName = settingName;
        this.SettingValue = settingValue;
        this.SettingInfo = settingInfo;
        this.SettingType = settingType;
        this.SettingCategory = settingCategory;
    }
}
