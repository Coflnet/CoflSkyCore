package CoflCore.configuration;

import CoflCore.classes.Settings;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalConfig {
    public boolean autoStart;
    public boolean extendedtooltips;
    public GUIType purchaseOverlay;
    public HashMap<String,String> knownCommands;
    public ArrayList<Settings> knownSettings;

    public LocalConfig(boolean autoStart, boolean extendedtooltips, GUIType purchaseOverlay, HashMap<String,String> knownCommands, ArrayList<Settings> knownSettings) {
        this.knownCommands = knownCommands;
        this.knownSettings = knownSettings;
        this.autoStart = autoStart;
        this.extendedtooltips = extendedtooltips;
        this.purchaseOverlay = purchaseOverlay;

        initCommands();
        initSettings();
    }

    public void initCommands() {
        if(this.knownCommands == null)
            this.knownCommands = new HashMap<>();
        
        // Sanitize existing commands - remove any with corrupted/oversized descriptions
        if (!this.knownCommands.isEmpty()) {
            java.util.Iterator<java.util.Map.Entry<String, String>> iterator = this.knownCommands.entrySet().iterator();
            while (iterator.hasNext()) {
                java.util.Map.Entry<String, String> entry = iterator.next();
                String description = entry.getValue();
                if (description != null && description.length() > 200) {
                    System.err.println("Warning: Removing corrupted command '" + entry.getKey() + 
                        "' with description length " + description.length() + " (max 200)");
                    iterator.remove();
                }
            }
        }
        
        if (!this.knownCommands.isEmpty()) {
            return;
        }
        this.knownCommands.putAll(new HashMap<String, String>() {{
            put("report", "Reports an issue to the developer\n/cofl report <message>\nWhen executed returns you a case id.\nPlease use that id to post into the bug report channel on discord.\nIsses can be fixed very quickly if\nyou include as much information as possible.");
            put("online", "Shows the number of players online");
            put("delay", "Shows your current delay\nTo allow everyone to get some flips, each\nuser gets delayed when he is found to buy too fast\nThe delay decreases over time\nand is not fully applied to all flips\nYou can reduce this by buying slower\nVery high profit flips are excepted from this");
            put("blacklist", "Manage your blacklist\nto add use /cl bl add <item> [filterName=Value]\nExample /cl bl add Hyperion sharpness=1-5");
            put("whitelist", "Manage your whitelist\nto add use /cl wl add <item> [filterName=Value]\nAllows you to skip entries on your blacklist\nWhitelist only things you definetly want to see\nExample /cl wl add Hyperion StartingBid=<50m");
            put("mute", "Mutes a user\nMuting an user will hide their chat messages from you\nThis does not prevent their auctions from showing up as flips\nFor that use /cl bl add Seller=PlayerName");
            put("blocked", "Shows you which flips were blocked and why\nUsage: /cofl blocked [search]\nUse this to find out why you don't get any flips\nor didn't get a specific flip\nExample: /cofl blocked Hyperion\nAlso supports 'profit' to show sorted by profit\nAnd /cofl blocked <uuid> for specific auctions");
            put("chat", "Writes a message to the chat\nAlias /fc <msg>\nWrites a message to the cofl chat\nBe nice and don't advertise or you may get muted");
            put("nickname", "Set your account nickname\nThis will be displayed in chat instead of your minecraft name\nYou can clear it by typing /cofl nickname clear\nNote that if your nickname contains inappropriate\nwords your account may be suspended");
            put("profit", "How much profit you made through flipping\nUsage: /cl profit {days}\nThe default is 7 days\nFlip tracking includes modifications to items and craft flips");
            put("worstflips", "Worst flips of x days");
            put("bestflips", "Best flips");
            put("leaderboard", "Flippers with the most profit\nMost profit in the current week\nSupports pagination with /cl lb <page>");
            put("loserboard", "Flippers with the highest loss\nThe flippers who lost the most coins in the last week\nSupports pagination with /cl lb <page>");
            put("buyspeedboard", "Fastest buying players\nRanked by milliseconds after grace period\nresets weekly\nyou can opt out of showing up\nwith §b/cl buyspeedboard disable");
            put("trades", "Recorded item movements (WIP)\nShows you item movements the mod detected\nTargets recoginizing more kinds of flips\nEg. lowballing through trade menu");
            put("flips", "Shows your flips for the last x days\nUsage /flips [sort] [days] [page]\nWhere sort is one of: profit, best, time, recent, name, price\nDays is the number of days to look back\nPage is the page to show (default 1)");
            put("set", "Sets a setting\nUsage: /cl set <setting> <value>\nSuggests corrections in case you have a typo\nUse only /cl set to get a list of all settings\nThe default view allows you to change settings\nby clicking on the options next to them");
            put("purchase", "Start purchase of a paid plan\nTo buy a plan use /cofl buy <plan> [count]\nAllows you to buy premium and other plans\nBuy premium to support the server <3\nExample /cofl buy premium+ 3");
            put("transactions", "Past /cofl buy transactions\nA list of transactions of CoflCoins\nAllows you to check where they came from and went to");
            put("balance", "Check how many CoflCoins you have");
            put("help", "Prints help for the mod\nUsage: /cofl help [topic]");
            put("logout", "logout all minecraft accounts\nSecurity command in case you think\nsomeone else has access to your account");
            put("backup", "Create a backup of your settings\nto create use /cofl backup add <name>\nto restore use /cofl restore <name>\nYou can create 3 to 10 backups");
            put("restore", "Restore settings from backup\nYou probably want to use the restore option in\n/cofl backup list instead of this one directly");
            put("captcha", "Solve a captcha\nYou will be asked to solve a captcha if you are afk for too long\nYou can also use this command to get a new captcha\nExample: /cl captcha another\nUse /cl captcha vertical to letters below each other\nWhich helps if you have a mod with different font\nCaptchas are necesary to prevent bots from using the flipper");
            put("importtfm", "Import blacklists from tfm\nUsage: /cofl importtfm <identifier> <userName>\nwhere <identifier> is one of user, enchant or item (counter part to /tfm export <identifier>)");
            put("replayactive", "Replay all active auctions against your filter\nUseful to recheck auctions that have been listed\nwhile you were offline\nThis will take a while\nto dearchive all active auctions");
            put("reminder", "Manage your reminders\nReminders are messages that will be sent to you after a certain time\nto add use /cofl reminder add 1h30m <message>\nto remove use /cofl reminder remove <message>");
            put("filters", "List filters and their options\nSupports pages and search\nExample: /cl filters sharpness");
            put("emoji", "Lists available emojis for §b/fc");
            put("addremindertime", "Add time to a reminder\nUsage: /cl reminder add <reminder> <time>");
            put("lore", "Change whats appended to item lore\nDisplays a chat menu to modify whats put in what line\nSome options may take longer to load than others");
            put("fact", "Gives you a random fact");
            put("flip", "Toggles flipping on or off\nUsage: /cl flip <never|always>");
            put("preapi", "Pre api submenu\nUsage: /cofl preapi <notify|profit>");
            put("transfercoins", "no help yet");
            put("ping", "Checks your ping to the SkyCofl server");
            put("setgui", "Sets a custom ah gui overlay\nUsage: /cofl setgui <gui>");
            put("bazaar", "A list of the top bazaar flips\nAllows you to see the most profitable\nbazaar flips currently available\nIt assumes that you make buy and sellers\nand includes the §b1.25% bazaar fee\nfrom the free bazaar community upgrade");
            put("switchregion", "Switches your region\ncurrently supported: eu, us");
            put("craftbreakdown", "Shows breakdown of cost for items applied to the main item.\nThis command allows you to see the total cost of crafting an item\nIt will show you the total cost and the individual costs of each component\nThis represents the induvidual costs in TotalCraftCost in lore");
            put("cheapattrib", "Shows you the cheapest attributes to upgrade or unlock");
            put("attributeupgrade", "Lists the cheapest upgrade path for some attribute\nattributeupgrade <item_name> <attrib2> {start_level} {end_level}");
            put("ownconfigs", "Lists configs you purchased from /cofl configs\nThis command allows you to see the configs you own\nYou can load them with /cl loadconfig <ownerId> <name>\nor by clicking on the output of the command");
            put("configs", "A list of the top configs\nAllows you to see the most popular configs\nYou can upvote and downvote configs\nYou can also see stats for each config\nand buy them if you don't own them yet\nNote that configs are not required to use the flipper");
            put("licenses", "Allows you to manage your account licenses\nLicenses allow you to open multiple connections at once\nIf you have multiple accounts and want to configure\nwhich one takes the premium on your email first use\n/cofl license default <userName>");
            put("verify", "Helps you verify your minecraft account\nThis command checks if your minecraft account is verified\nIf it is not, it will prompt you to verify it\nYou can also use this command to check if you are verified");
            put("unverify", "Allows you to unverify one of your minecraft accounts\nYou can only unverify accounts that fufill requirements\nEg. blacklisted accounts can't be unverfied\nUnless you shared an account with someone else\nthere is no reason to unverify an account");
            put("attributeflip", "Lists flips that a modifier can be applied to for profit\nThis command is experimental and not all modifiers\nlist correctly. It uses the median sniper flip finder\nto find price differences between modifiers on the ah");
            put("forge", "Displays forge flips you can do based on hotM level\nRecognizes your quick forge level and adjusts time accordingly");
            put("crafts", "Displays craft flips you can do.\nBased on unlocked collectionsAnd slayer level");
            put("upgradeplan", "Start purchase of a paid plan\nTo buy a plan use /cofl buy <plan> [count]\nAllows you to buy premium and other plans\nBuy premium to support the server <3\nExample /cofl buy premium+ 3");
            put("updatecurrentconfig", "Updates the current config to the latest version\nThis command applies all changes from the config seller\nThis is done by checking what the seller changed and applying\n those changes. This keeps any filter changes you made in tact.\nYou can skip settings by using /cl updateconfig skipSettings=true");
            put("settimezone", "Set the timezone offset for the current user");
            put("cheapmuseum", "Lists the cheapest items per exp for museum donations\nHonors tierd donations and tries to suggest the biggest\nexp donation first so you don't double spend.\nAlso respects armor set requirements");
            put("replayflips", "Replay all flips from the last x hours\nMeant for config creators to test their config");
            put("ahtax", "Calculates the auction house tax for a given sell amount\nHonors fee changes of Derpy\nUsage: /cofl ahtax <sellAmount>");
            put("networth", "Get a breakdown of networth\nBased on current market prices");
            put("ananke", "Lists the top potential flips for\nRNG meter progress granted by Ananke Feathers");
            put("task", "Lists tasks that can be done for profit\nTasks are calculated based on your current progress\nand try to self adjust based on how many items\nyou managed to collect recently (active tasks)\nPassive tasks include flips from other commands");
            put("lowball", "Offer items to or register as lowballer\nSimplifies lowballing by not requiring\nyou to advertise anymore as a buyer.\nAnd allows you to compare multiple offers\nand be visited by the highest as a seller");
            put("bzmove", "Lists the top bazaar movers in the last 24 hours\nSorts by biggest price increase by default\nYou can use /cofl bzmove asc to sort by biggest drop\nYou can also search for items by name or id\nUse /cofl bzmove help to see usage");
            put("minion", "no help yet");
            put("fusionflip", "Lists flips that can be made with fusionmachine\nAssumes you have top buy order, fuse it and\nthen have top sell order to sell the shard");
            put("search", "no help yet");
            put("ahflips", "Shows not yet sold auction house flips\nFlips might still be unavailable due\nto update lag, just try the next one\nThis command eases the compatitive \nnature of ah flipping");
        }});
    }

    public void initSettings() {
        if(this.knownSettings == null)
            this.knownSettings = new ArrayList<>();
        if (!this.knownSettings.isEmpty()) {
            return;
        }
        
        // Add default settings based on the JSON example
        this.knownSettings.add(new Settings("filters", "Filters", null, null, "Dictionary`2", "general"));
        this.knownSettings.add(new Settings("lbin", "BasedOnLBin", false, "Calculate profit based on lowest bin", "Boolean", "general"));
        this.knownSettings.add(new Settings("finders", "AllowedFinders", "FLIPPER_AND_SNIPERS", "Which algorithms are selected for price estimation", "FinderType", "general"));
        this.knownSettings.add(new Settings("onlyBin", "OnlyBin", true, "Hide all auctions (not buy item now)", "Boolean", "general"));
        this.knownSettings.add(new Settings("whitelistAftermain", "WhitelistAfterMain", false, "whitelisted items will only show if they also meet main filters (min profit etc)", "Boolean", "general"));
        this.knownSettings.add(new Settings("DisableFlips", "DisableFlips", false, "Stop receiving any flips (just use other features) also stops the timer", "Boolean", "general"));
        this.knownSettings.add(new Settings("DebugMode", "DebugMode", false, "Outputs more information to help with debugging issues", "Boolean", "general"));
        this.knownSettings.add(new Settings("blockHighCompetition", "BlockHighCompetitionFlips", true, "Block flips that are probably not purchaseable manually", "Boolean", "general"));
        this.knownSettings.add(new Settings("minProfit", "MinProfit", 2500000L, "Minimum profit of flips", "Int64", "general"));
        this.knownSettings.add(new Settings("minProfitPercent", "MinProfitPercent", 20, "Minimum profit Percentage", "Int32", "general"));
        this.knownSettings.add(new Settings("minVolume", "MinVolume", 0.1, "The minimum sales per 24 hours (has decimals)", "Double", "general"));
        this.knownSettings.add(new Settings("maxCost", "MaxCost", 1000000000L, "Maximium cost of flips", "Int64", "general"));
        
        // Mod category settings
        this.knownSettings.add(new Settings("modjustProfit", "DisplayJustProfit", true, "Display just the profit", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modsoundOnFlip", "PlaySoundOnFlip", true, "Play a sound when a flip is received", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modsoundOnOutbid", "PlaySoundOnOutbid", false, "Play a sound when a bazaar order is outbid", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modshortNumbers", "ShortNumbers", true, "Use M and k to shorten numbers", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modshortNames", "ShortNames", false, "Remove reforges etc from item names", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modblockTenSecMsg", "BlockTenSecondsMsg", false, "Hide the flips in 10 seconds message", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modformat", "Format", "(FLIP) {0}: {1}{2} {3}{4}  -> {5} Volume: {10}", "Custom flip message format", "String", "mod"));
        this.knownSettings.add(new Settings("modblockedFormat", "BlockedFormat", null, "Custom flip message format for blocked flips", "String", "mod"));
        this.knownSettings.add(new Settings("modchat", "Chat", false, "Is the chat enabled", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modcountdown", "DisplayTimer", true, "Show the timer", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modhideNoBestFlip", "HideNoBestFlip", false, "Hides the message from the hotkey", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modtimerX", "TimerX", 0, "<---> position in percent", "Int32", "mod"));
        this.knownSettings.add(new Settings("modtimerY", "TimerY", 0, "up/down position in percent", "Int32", "mod"));
        this.knownSettings.add(new Settings("modtimerSeconds", "TimerSeconds", 0, "how many seconds before the update the timer should be shown", "Int32", "mod"));
        this.knownSettings.add(new Settings("modtimerScale", "TimerScale", 0.0f, "What scale the timer should be displayed with", "Single", "mod"));
        this.knownSettings.add(new Settings("modtimerPrefix", "TimerPrefix", null, "Custom text to put in front of the timer", "String", "mod"));
        this.knownSettings.add(new Settings("modtimerPrecision", "TimerPrecision", 0, "How many digits the timer should target (3)", "Int32", "mod"));
        this.knownSettings.add(new Settings("modblockedMsg", "MinutesBetweenBlocked", (byte)0, "How many minutes to have pass before showing the x amounts of flips blocked message again, max is 127", "SByte", "mod"));
        this.knownSettings.add(new Settings("modmaxPercentOfPurse", "MaxPercentOfPurse", (short)0, "The maximum amount of your purse you are willing to spend on a single flip", "Int16", "mod"));
        this.knownSettings.add(new Settings("modnoBedDelay", "NoBedDelay", false, "Don't delay bed flips, send them imediately instead", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modstreamerMode", "StreamerMode", false, "Hide any personal data and reduce sounds", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modautoStartFlipper", "AutoStartFlipper", false, "Start showing flips automatically when joining skyblock", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modnormalSoldFlips", "NormalSoldFlips", false, "Don't add [SOLD] to sold flips, send them normally instead", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modtempBlacklistSpam", "TempBlacklistSpam", false, "Autmatically add items to the blacklist for 8 hours if they show up more than 5 times in 2 minutes", "Boolean", "mod"));
        this.knownSettings.add(new Settings("moddataOnlyMode", "AhDataOnlyMode", false, "don't show flips only add useful data", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modahListHours", "AhListTimeTarget", 0, "Ah list time target in hours", "Int32", "mod"));
        this.knownSettings.add(new Settings("modquickSell", "QuickSell", false, "Sell items as fast as possible", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modmaxItemsInInventory", "MaxFlipItemsInInventory", 0, "The maximum amount of flips to buy and store in inventory", "Int32", "mod"));
        this.knownSettings.add(new Settings("moddisableSpamProtection", "DisableSpamProtection", false, "Disables spam protection. By default only ~5 most valuable flips are shown that fit the settings. CAUTION: This can lead to spam flips", "Boolean", "mod"));
        this.knownSettings.add(new Settings("modtempBlacklistThreshold", "TempBlacklistThreshold", 20, "Purchasing more than this percenatage of flips on an item will temp blacklist the item, eg if you see 8 and buy 4 the rate is 50", "Int32", "mod"));
        
        // Visibility category settings
        this.knownSettings.add(new Settings("showcost", "Cost", true, "Show the cost of a flip", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showestProfit", "EstimatedProfit", true, "Estimated profit, based on estimated sell -(ah tax)", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showlbin", "LowestBin", false, "Show closest lowest bin (adds a few ms)", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showslbin", "SecondLowestBin", false, "Second lowest bin (adds a few ms)", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showmedPrice", "MedianPrice", false, "Show median/target price, equals lbin if sniper", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showseller", "Seller", true, "Show the sellers name (adds a few ms)", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showvolume", "Volume", true, "Show the average sell volume in 24 hours", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showextraFields", "ExtraInfoMax", 0, "How many extra information fields to display below the flip", "Int32", "visibility"));
        this.knownSettings.add(new Settings("showprofitPercent", "ProfitPercentage", true, "Show profit percentage", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showprofit", "Profit", false, "Show absolute amount of profit", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showsellerOpenBtn", "SellerOpenButton", true, "Display a button to open the sellers ah", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showlore", "Lore", true, "Show the item description in hover text", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showhideSold", "HideSoldAuction", false, "Prevents sold auctions from showing", "Boolean", "visibility"));
        this.knownSettings.add(new Settings("showhideManipulated", "HideManipulated", false, "Prevents manipulated bazaar items from showing up", "Boolean", "visibility"));
        
        // Privacy category settings
        this.knownSettings.add(new Settings("privacyCollectChat", "CollectChat", true, "Allow collection of limited amount of chat content to track eg. trades, drops, ah and bazaar events ", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyCollectInventory", "CollectInventory", true, "Upload chest and inventory content (required for trade tracking)", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyDisableTradeStoring", "DisableTradeStoring", false, "Stop trades from being stored", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyDisableKuudraTracking", "DisableKuudraTracking", false, "Stop kuudra profit from being calculated", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyCollectTab", "CollectTab", true, "Read and upload tab contents when joining server (detect profile type, server and island location)", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyCollectScoreboard", "CollectScoreboard", true, "Read and upload scoreboard peridicly to detect purse", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyCollectChatClicks", "CollectChatClicks", true, "Collect clicks on chat messages", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyExtendDescriptions", "ExtendDescriptions", true, "Extend item descriptions (configure with /cofl lore)", "Boolean", "privacy"));
        this.knownSettings.add(new Settings("privacyAutoStart", "AutoStart", true, "Autostart when joining skyblock", "Boolean", "privacy"));
        
        // Lore category settings
        this.knownSettings.add(new Settings("loreHighlightFilterMatch", "HighlightFilterMatch", true, "Highlight items in ah and trade windows when matching black or whitelist filter", "Boolean", "lore"));
        this.knownSettings.add(new Settings("loreMinProfitForHighlight", "MinProfitForHighlight", 5000000L, "What is the minimum profit for highlighting best flip on page", "Int64", "lore"));
        this.knownSettings.add(new Settings("loreDisableHighlighting", "DisableHighlighting", false, "Disable all highlighting", "Boolean", "lore"));
        this.knownSettings.add(new Settings("loreDisableSuggestions", "DisableSuggestions", true, "Disable all sign input suggestions", "Boolean", "lore"));
        this.knownSettings.add(new Settings("loreDisableInfoIn", "DisableInfoIn", null, "Disable side info display in these menus, will add any menu you type into this setting, to remove prefix with `rm `, `clear` is also an option", "HashSet`1", "lore"));
        this.knownSettings.add(new Settings("loreDisabled", "Disabled", false, "If the extra lore should be displayed or not", "Boolean", "lore"));
        this.knownSettings.add(new Settings("loreLowballMedUndercut", "LowballMedUndercut", (byte)0, "Mow many percent to undercut the median price when lowballing, the lower of median and lbin will be used, setting this setting to 1 or more will hide the note in the lowballing info", "Byte", "lore"));
        this.knownSettings.add(new Settings("loreLowballLbinUndercut", "LowballLbinUndercut", (byte)10, "Mow many percent to undercut the lbin price when lowballing, for items below 10m this is increased by 2% for items above 100m this is decreased by 2%, under 1 volume will also increase this by another 3%", "Byte", "lore"));
        this.knownSettings.add(new Settings("lorePreferLbinInSuggestions", "PreferLbinInSuggestions", true, "Prefer current lbin for suggestions over stable median", "Boolean", "lore"));
        this.knownSettings.add(new Settings("loreSuggestQuicksell", "SuggestQuicksell", false, "Suggest quicksell prices on listing", "Boolean", "lore"));
    }

    public void updateSettings(ArrayList<Settings> newSettings) {
        if (newSettings != null) {
            this.knownSettings = newSettings;
        }
    }

    public ArrayList<Settings> getKnownSettings() {
        return knownSettings;
    }

    public static void saveConfig(File file, LocalConfig Config) {
        Gson gson = new com.google.gson.GsonBuilder()
            .disableHtmlEscaping()  // Prevent corruption of special characters like §
            .create();
        try {
            if (!file.isFile()) {
                file.createNewFile();
            }
            Files.write(Paths.get(file.getAbsolutePath()),
                    gson.toJson(Config).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalConfig createDefaultConfig() {
        return new LocalConfig(true, true, null, null, null);
    }
}
