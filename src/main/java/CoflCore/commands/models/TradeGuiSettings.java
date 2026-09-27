package CoflCore.commands.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class TradeGuiSettings {
    private TradeGuiSettings() {
    }

    public static Boolean parseEnabled(String json) {
        JsonElement payload = JsonParser.parseString(json);
        if (!payload.isJsonObject()) {
            throw new IllegalArgumentException("Trade GUI response must be an object");
        }
        JsonObject object = payload.getAsJsonObject();
        JsonElement enabled = object.get("enabled");
        if (enabled == null) {
            throw new IllegalArgumentException("Trade GUI response must contain enabled");
        }
        if (enabled.isJsonNull()) {
            return null;
        }
        if (!enabled.isJsonPrimitive() || !enabled.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Trade GUI enabled must be a boolean or null");
        }
        return enabled.getAsBoolean();
    }
}
