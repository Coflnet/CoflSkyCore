package CoflCore.handlers;

import CoflCore.classes.Position;
import CoflCore.configuration.Config;
import CoflCore.network.QueryServerCommands;
import CoflCore.network.WSClient;
import com.google.gson.JsonObject;
import java.util.HashMap;

public class DescriptionHandler {

    public interface DescriptionRefreshCallback {
        void onDescriptionRefresh(String[] itemIdList, String chestName);
    }

    public class DescModification {
        public String type;
        public String value;
        public int line;
    }

    public static HashMap<String, DescModification[]> tooltipItemIdMap = new HashMap<>();
    private static DescModification[] infoDisplay = new DescModification[0];
    private static DescriptionRefreshCallback refreshCallback;

    public static void loadDescriptionForInventory(String[] itemIdList, String chestName, String fullInventoryNBT, String username, Position position) {
        JsonObject body = new JsonObject();
        body.addProperty("chestName", chestName);
        body.addProperty("version", 3);
        body.addProperty("fullInventoryNbt", fullInventoryNBT);
        if (position != null) body.add("position", WSClient.gson.toJsonTree(position, Position.class));

        String info = QueryServerCommands.PostRequest(Config.BaseUrl + "/api/mod/description/modifications", body.toString(), username);
        DescModification[][] arr = WSClient.gson.fromJson(info, DescModification[][].class);

        for (int i = 0; i < itemIdList.length; i++) {
            final String id = itemIdList[i];
            if (id.length() > 0)
                tooltipItemIdMap.put(id, arr[i]);
        }

        if(itemIdList.length < arr.length)
            infoDisplay = arr[arr.length - 1];
        else 
            infoDisplay = new DescModification[0];

        if (refreshCallback != null) {
            refreshCallback.onDescriptionRefresh(itemIdList, chestName);
        }
    }

    public static void loadDescriptionForInventory(String[] itemIdList, String chestName, String fullInventoryNBT, String username) {
        loadDescriptionForInventory(itemIdList, chestName, fullInventoryNBT, username, null);
    }

    public static DescModification[] getTooltipData(String id) {
        return tooltipItemIdMap.getOrDefault(id, null);
    }

    public static DescModification[] getInfoDisplay() {
        return infoDisplay;
    }

    public static void setRefreshCallback(DescriptionRefreshCallback callback) {
        refreshCallback = callback;
    }

    /**
     * Called when the inventory is closed
     */
    public static void emptyTooltipData() {
        tooltipItemIdMap.clear();
    }
}
