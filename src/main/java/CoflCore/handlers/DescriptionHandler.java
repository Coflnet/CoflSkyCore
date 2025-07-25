package CoflCore.handlers;

import CoflCore.classes.Position;
import CoflCore.configuration.Config;
import CoflCore.network.QueryServerCommands;
import CoflCore.network.WSClient;
import com.google.gson.JsonObject;
import java.util.HashMap;

public class DescriptionHandler {

    public class DescModification {
        public String type;
        public String value;
        public int line;
    }

    public static HashMap<String, DescModification[]> tooltipItemIdMap = new HashMap<>();
    private static DescModification[] infoDisplay = new DescModification[0];

    public static void loadDescriptionForInventory(String[] itemIdList, String chestName, String fullInventoryNBT, String username, Position position) {
        JsonObject body = new JsonObject();
        body.addProperty("chestName", chestName);
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
    }

    public static void loadDescriptionForInventory(String[] itemIdList, String chestName, String fullInventoryNBT, String username) {
        loadDescriptionForInventory(itemIdList, chestName, fullInventoryNBT, username, null);
    }

    public static DescModification[] getTooltipData(String id) {
        DescModification[] EMPTY_ARRAY = new DescModification[]{};

        if (tooltipItemIdMap.containsKey(id)) {
            return tooltipItemIdMap.getOrDefault(id, EMPTY_ARRAY);
        }

        return EMPTY_ARRAY;
    }

    public static DescModification[] getInfoDisplay() {
        return infoDisplay;
    }

    /**
     * Called when the inventory is closed
     */
    public static void emptyTooltipData() {
        tooltipItemIdMap.clear();
    }
}
