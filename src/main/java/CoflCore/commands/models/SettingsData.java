package CoflCore.commands.models;

import CoflCore.classes.Settings;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class SettingsData {
    @SerializedName("data")
    public String Data;
    
    public SettingsData() {
        this.Data = "[]";
    }

    public SettingsData(String data) {
        super();
        this.Data = data;
    }

    public SettingsData(ArrayList<Settings> settings) {
        // Convert settings list to JSON string
        com.google.gson.Gson gson = new com.google.gson.Gson();
        this.Data = gson.toJson(settings);
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        this.Data = data;
    }
}
