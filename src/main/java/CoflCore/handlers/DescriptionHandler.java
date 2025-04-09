package CoflCore.handlers;

import CoflCore.configuration.Config;
import CoflCore.network.QueryServerCommands;
import CoflCore.network.WSClient;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class DescriptionHandler {

    private static class DescModification {
        public String type;
        public String value;
        public int line;
    }

    public static HashMap<String, DescModification[]> tooltipItemIdMap = new HashMap<>();

    public static void loadDescriptionForInventory(String[] itemIdList, String chestName, String fullInventoryNBT, String username) {
        JsonObject body = new JsonObject();
        body.addProperty("chestName", chestName);
        body.addProperty("fullInventoryNbt", fullInventoryNBT);

        String info = QueryServerCommands.PostRequest(Config.BaseUrl + "/api/mod/description/modifications", body.toString(), username);
        DescModification[][] arr = WSClient.gson.fromJson(info, DescModification[][].class);

        for (int i = 0; i < itemIdList.length; i++) {
            final String id = itemIdList[i];
            if (id.length() > 0)
                tooltipItemIdMap.put(id, arr[i]);
        }
    }

    public DescModification[] getTooltipData(String id) {
        DescModification[] EMPTY_ARRAY = new DescModification[]{};

        if (tooltipItemIdMap.containsKey(id)) {
            return tooltipItemIdMap.getOrDefault(id, EMPTY_ARRAY);
        }

        return EMPTY_ARRAY;
    }

    /**
     * Called when the inventory is closed
     */
    public static void emptyTooltipData() {
        tooltipItemIdMap.clear();
    }
}
