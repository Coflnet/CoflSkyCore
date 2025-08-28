package CoflCore.configuration;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class LocalConfig {
    public boolean autoStart;
    public boolean extendedtooltips;
    public GUIType purchaseOverlay;
    public HashMap<String,String> knownCommands;

    public LocalConfig(boolean autoStart, boolean extendedtooltips, GUIType purchaseOverlay, HashMap<String,String> knownCommands) {
        this.knownCommands = knownCommands != null ? knownCommands : new HashMap<>();
        this.autoStart = autoStart;
        this.extendedtooltips = extendedtooltips;
        this.purchaseOverlay = purchaseOverlay;

        initCommands();
    }

    public void initCommands() {
        if(this.knownCommands == null)
            this.knownCommands = new HashMap<>();
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

    public static void saveConfig(File file, LocalConfig Config) {
        Gson gson = new Gson();
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
        return new LocalConfig(true, true, null, null);
    }
}
