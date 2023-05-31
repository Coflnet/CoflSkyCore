package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;

public class AuctionItem {

    public Array getEnchantments() {
        return Enchantments;
    }

    public String getItemId() {
        return ItemId;
    }

    public Integer getCount() {
        return Count;
    }

    public String getStartingPrice() {
        return StartingPrice;
    }

    public String getItemTag() {
        return ItemTag;
    }

    public String getItemName() {
        return ItemName;
    }

    public String getAuctionStart() {
        return AuctionStart;
    }

    public String getAuctionEnd() {
        return AuctionEnd;
    }

    public String getAuctioneerId() {
        return AuctioneerId;
    }

    public String getProfileId() {
        return ProfileId;
    }

    public Array getCoop() {
        return Coop;
    }

    public Array getCoopMembers() {
        return CoopMembers;
    }

    public Integer getHighestBid() {
        return HighestBid;
    }

    public Array getBids() {
        return Bids;
    }

    public Integer getAnvilUses() {
        return AnvilUses;
    }

    public Object getNbtData() {
        return NbtData;
    }

    public String getItemCreatedAt() {
        return ItemCreatedAt;
    }

    public String getItemReforge() {
        return ItemReforge;
    }

    public String getItemCategory() {
        return ItemCategory;
    }

    public String getItemRarity() {
        return ItemRarity;
    }

    public Boolean getBinAuction() {
        return BinAuction;
    }

    public Object getFlatNbtData() {
        return FlatNbtData;
    }

    @SerializedName("enchantments")
    private Array Enchantments;

    @SerializedName("uuid")
    private String ItemId;

    @SerializedName("count")
    private Integer Count;

    @SerializedName("startingBid")
    private String StartingPrice;

    @SerializedName("tag")
    private String ItemTag;

    @SerializedName("itemName")
    private String ItemName;

    @SerializedName("start")
    private String AuctionStart;

    @SerializedName("end")
    private String AuctionEnd;

    @SerializedName("auctioneerId")
    private String AuctioneerId;

    @SerializedName("profileId")
    private String ProfileId;

    @SerializedName("coop")
    private Array Coop;

    @SerializedName("coopMembers")
    private Array CoopMembers;

    @SerializedName("highestBidAmount")
    private Integer HighestBid;

    @SerializedName("bids")
    private Array Bids;

    @SerializedName("anvilUses")
    private Integer AnvilUses;

    @SerializedName("nbtData")
    private Object NbtData;

    @SerializedName("itemCreatedAt")
    private String ItemCreatedAt;

    @SerializedName("reforge")
    private String ItemReforge;

    @SerializedName("category")
    private String ItemCategory;

    @SerializedName("tier")
    private String ItemRarity;

    @SerializedName("bin")
    private Boolean BinAuction;

    @SerializedName("flatNbt")
    private Object FlatNbtData;

    public AuctionItem() {
    }

    public AuctionItem(Array enchantments, String itemId, Integer count, String startingPrice, String itemTag, String itemName, String start, String end, String auctioneerId, String profileId, Array coop, Array coopMembers, Integer highestBid, Array bids, Integer anvilUses, Object nbtData, String itemCreatedAt, String reforge, String category, String tier, Boolean bin, Object flatNbt) {
        super();
        this.Enchantments = enchantments;
        this.ItemId = itemId;
        this.Count = count;
        this.StartingPrice = startingPrice;
        this.ItemTag = itemTag;
        this.ItemName = itemName;
        this.AuctionStart = start;
        this.AuctionEnd = end;
        this.AuctioneerId = auctioneerId;
        this.ProfileId = profileId;
        this.Coop = coop;
        this.CoopMembers = coopMembers;
        this.HighestBid = highestBid;
        this.Bids = bids;
        this.AnvilUses = anvilUses;
        this.NbtData = nbtData;
        this.ItemCreatedAt = itemCreatedAt;
        this.ItemReforge = reforge;
        this.ItemCategory = category;
        this.ItemRarity = tier;
        this.BinAuction = bin;
        this.FlatNbtData = flatNbt;
    }
}
