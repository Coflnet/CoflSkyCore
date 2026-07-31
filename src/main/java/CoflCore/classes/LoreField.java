package CoflCore.classes;

/**
 * Known lore field names sourced from the backend DescriptionField enum.
 * Each entry carries its server-side description so UIs built on top of
 * CoflCore can show users what every field does.
 * <p>
 * The backend can add new fields dynamically and unknown values must be
 * preserved as-is — this enum serves as documentation, not validation.
 */
public enum LoreField {
    NONE("NONE", "Placeholder for empty line"),
    LBIN("LBIN", "Best matching lowest bin"),
    LBIN_KEY("LBIN_KEY", "List of modifiers used to get the lowest bin"),
    MEDIAN("MEDIAN", "Median price of items with all modifiers in MEDIAN_KEY"),
    MEDIAN_KEY("MEDIAN_KEY", "List of modifiers used to get the median price"),
    VOLUME("VOLUME", "Sales per day of items with all modifiers in MEDIAN_KEY"),
    TAG("TAG", "The hypixel internal item id"),
    CRAFT_COST("CRAFT_COST", "Craft cost of clean item"),
    BazaarBuy("BazaarBuy", "The price you can buy an item on bazaar\nGets hidden if not on bazaar"),
    BazaarSell("BazaarSell", "The price you can sell an item on bazaar\nGets hidden if not sellable on bazaar"),
    PRICE_PAID("PRICE_PAID", "The last price this item sold for\nAny sell counts, not just your own"),
    ITEM_KEY("ITEM_KEY", "List of valuable attributes used to estimate value"),
    EnchantCost("EnchantCost", "Sum of all enchantment costs"),
    GemValue("GemValue", "Sum of gemstone value"),
    SpentOnAhFees("SpentOnAhFees", "Summary of past list attempts"),
    KatUpgradeCost("KatUpgradeCost", "How much Kat takes in coins and materials to upgrade"),
    InstaSellPrice("InstaSellPrice", "Estimated price the item insta-sells for"),
    ModifierCost("ModifierCost", "Summary of the cost of all modifiers applied\nMay be skewed by manipulated bazaar prices"),
    FullCraftCost("FullCraftCost", "Full craft cost including modifiers"),
    ModifierCostList("ModifierCostList", "Modifiers included in cost\nLets you see what was summed up"),
    FinderEstimates("FinderEstimates", "List of flip finders which deemed the last purchase a flip\nIncludes their estimated value\n§cThis can noticeably slow description loading"),
    Volatility("Volatility", "How much the median estimate fluctuates\nUses different time-interval medians"),
    LastSoldFor("LastSoldFor", "The price the last reference with same valuable attributes sold for\n§cNot necessarily the same item — watch out if very low"),
    TimeToSell("TimeToSell", "How long on average it takes to sell an item with the same valuable attributes"),
    NpcSellPrice("NpcSellPrice", "How much the item/stack will sell for in an NPC shop"),
    ColorCode("ColorCode", "Color codes with their source\nHighlights exotics like iTEM"),
    DefaultLore("DefaultLore", "Placeholder for the skyblock item description\nUseful when you want data above the description, directly below the item name"),
    AiEstimate("AiEstimate", "Complex items may be better valued by our self-learning price model\nEnable its estimate with this"),
    BAZAAR_COST("BAZAAR_COST", "Bazaar cost (internal, hidden by default)");

    public final String fieldName;
    public final String description;

    LoreField(String fieldName, String description) {
        this.fieldName = fieldName;
        this.description = description;
    }

    /** Look up a LoreField by its case-insensitive name. Returns null if unknown. */
    public static LoreField fromName(String name) {
        for (LoreField value : values()) {
            if (value.fieldName.equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Builds a human-readable description block listing every known field
     * and its description. Suitable for use in tooltips or help text.
     */
    public static String buildFieldDescriptions() {
        StringBuilder sb = new StringBuilder();
        for (LoreField field : values()) {
            sb.append("§e").append(field.fieldName).append("§r: ")
                    .append(field.description.replace("\n", " "))
                    .append("\n");
        }
        return sb.toString();
    }
}
