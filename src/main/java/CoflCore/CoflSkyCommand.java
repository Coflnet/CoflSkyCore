package CoflCore;

import CoflCore.commands.Command;
import CoflCore.commands.CommandType;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.RawCommand;
import CoflCore.commands.models.FlipData;
import CoflCore.configuration.Config;
import CoflCore.configuration.GUIType;
import CoflCore.events.OnCloseGUI;
import CoflCore.events.OnGetInventory;
import CoflCore.events.OnModChatMessage;
import CoflCore.events.OnOpenAuctionGUI;
import CoflCore.misc.SessionManager;
import CoflCore.network.QueryServerCommands;
import CoflCore.network.WSClient;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class CoflSkyCommand {

    public static final String HelpText = "Available local sub-commands:\n"
            + "§bstart: §7starts a new connection\n"
            + "§bstop: §7stops the connection\n"
            + "§bconnect: §7Connects to a different server\n"
            + "§breset: §7resets all local session information and stops the connection\n"
            + "§bstatus: §7Emits status information\n"
            + "§bsetgui: §7Changes the auction purchase GUI\nServer-Only Commands:";

    public static void processCommand(String[] args, String username) {
        new Thread(() -> {
            System.out.println(Arrays.toString(args));

            if (args.length >= 1) {
                switch (args[0].toLowerCase()) {
                    case "start":
                        start(username);
                        break;
                    case "stop":
                        stop();
                        break;
                    case "callback":
                        callback(args);
                        break;
                    case "dev":
                        dev(username);
                        break;
                    case "status":
                        status(username);
                        break;
                    case "reset":
                        reset(username);
                        break;
                    case "connect":
                        connect(args, username);
                        break;
                    case "openauctiongui":
                        openAuctionGUI(args);
                        break;
                    case "setgui":
                        setGUI(args);
                        break;
                    case "closegui":
                        closeGUI();
                        break;
                    case "getinventory":
                        getInventory();
                        break;
                    default:
                        sendCommandToServer(args, username);
                }
            } else {
                EventBus.getDefault().post(new OnModChatMessage(HelpText));
                EventBus.getDefault().post(new OnModChatMessage(QueryServerCommands.QueryCommands()));
            }
        }).start();
    }

    public static void start(String username){
        CoflCore.Wrapper.stop();
        EventBus.getDefault().post(new OnModChatMessage("Starting connection..."));
        CoflCore.Wrapper.startConnection(username);
    }

    public static void sendCommandToServer(String[] args, String username){
        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        //JsonStringCommand sc = new JsonStringCommand(args[0], WSClient.gson.toJson(command));
        RawCommand rc = new RawCommand(args[0], WSClient.gson.toJson(command));
        if (CoflCore.Wrapper.isRunning) {
            CoflCore.Wrapper.SendMessage(rc);
        } else {
            EventBus.getDefault().post(new OnModChatMessage("§cCoflSky wasn't active."));
            CoflCore.Wrapper.startConnection(username);
            CoflCore.Wrapper.SendMessage(rc);
        }
    }

    public static void stop(){
        CoflCore.Wrapper.stop();
        EventBus.getDefault().post(new OnModChatMessage("you stopped the connection to §1C§6oflnet§r.\n    To reconnect enter §b\"§r/cofl start§b\"§r or click this message\n"));
    }

    public static void callback(String[] args) {

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        System.out.println("CallbackData: " + command);
        //new Thread(()->{
        System.out.println("Callback: " + command);
        WSClient.HandleCommand(new JsonStringCommand(CommandType.Execute, WSClient.gson.toJson(command)));
        CoflCore.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, WSClient.gson.toJson(command)));

        System.out.println("Sent!");
        //}).start();
    }

    public static void dev(String username){
        if (Config.BaseUrl.contains("localhost")) {
            CoflCore.Wrapper.startConnection(username);
            Config.BaseUrl = "https://sky.coflnet.com";
        } else {
            CoflCore.Wrapper.initializeNewSocket("ws://localhost:8009/modsocket", username);
            Config.BaseUrl = "http://localhost:5005";
        }
        EventBus.getDefault().post(new OnModChatMessage("toggled dev mode, now using " + Config.BaseUrl));
    }

    public static void status(String username) {

        String vendor = System.getProperty("java.vm.vendor");
        String name = System.getProperty("java.vm.name");
        String version = System.getProperty("java.version");
        String detailedVersion = System.getProperty("java.vm.version");

        String status = vendor + " " + name + " " + version + " " + detailedVersion + "|Connection = " + (CoflCore.Wrapper != null ? CoflCore.Wrapper.GetStatus() : "UNINITIALIZED_WRAPPER");
        try {
            status += "  uri=" + CoflCore.Wrapper.socket.uri.toString();
        } catch (NullPointerException npe) {
        }


        try {
            SessionManager.CoflSession session = SessionManager.GetCoflSession(username);
            String sessionString = SessionManager.gson.toJson(session);
            status += "  session=" + sessionString;
        } catch (IOException e) {
        }

        EventBus.getDefault().post(new OnModChatMessage(status));
    }

    public static void reset(String username) {
        CoflCore.Wrapper.SendMessage(new Command(CommandType.Reset, ""));
        CoflCore.Wrapper.stop();
        EventBus.getDefault().post(new OnModChatMessage("Stopping Connection to Coflnet"));
        SessionManager.DeleteAllCoflSessions();
        EventBus.getDefault().post(new OnModChatMessage("Deleting Coflnet sessions..."));
        if (CoflCore.Wrapper.startConnection(username)) {
            EventBus.getDefault().post(new OnModChatMessage("Started the Connection to Coflnet"));
        }
    }

    public static void connect(String args[], String username){
        if (args.length == 2) {
            String destination = args[1];

            if (!destination.contains("://")) {
                destination = new String(Base64.getDecoder().decode(destination));
            }
            EventBus.getDefault().post(new OnModChatMessage("Stopping connection!"));
            CoflCore.Wrapper.stop();
            EventBus.getDefault().post(new OnModChatMessage("Opening connection to " + destination));
            if (CoflCore.Wrapper.initializeNewSocket(destination, username)) {
                EventBus.getDefault().post(new OnModChatMessage("Success"));
            } else {
                EventBus.getDefault().post(new OnModChatMessage("Could not open connection, please check the logs"));
            }
        } else {
            EventBus.getDefault().post(new OnModChatMessage("§cPleace specify a server to connect to"));
        }
    }

    public static void openAuctionGUI(String[] args) {
        FlipData flip = CoflCore.flipHandler.fds.getFlipById(args[1]);
        boolean shouldInvalidate = args.length >= 3 && args[2].equals("true");


        // Is not a stored flip -> just open the auction
        if (flip == null) {
            CoflCore.flipHandler.lastClickedFlipMessage = "";
            return;
        }

        String oneLineMessage = String.join(" ", flip.getMessageAsString()).replaceAll("\n", "").split(",§7 sellers ah")[0];

        if (shouldInvalidate) {
            CoflCore.flipHandler.fds.InvalidateFlip(flip);
        }

        CoflCore.flipHandler.lastClickedFlipMessage = oneLineMessage;

        EventBus.getDefault().post(new OnOpenAuctionGUI("/viewauction " + flip.Id, flip));
    }

    public static void setGUI(String[] args) {
        if (args.length != 2) {
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Available GUIs:"));
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Cofl"));
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7TFM"));
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Off"));
            return;
        }

        if (args[1].equalsIgnoreCase("cofl")) {
            CoflCore.config.purchaseOverlay = GUIType.COFL;
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fCofl"));
        }
        if (args[1].equalsIgnoreCase("tfm")) {
            CoflCore.config.purchaseOverlay = GUIType.TFM;
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fTFM"));
        }
        if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) {
            CoflCore.config.purchaseOverlay = null;
            EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Set §bPurchase Overlay §7to: §fOff"));
        }
    }

    public static void closeGUI(){
        EventBus.getDefault().post(new OnCloseGUI());
    }


    private static void getInventory() {
        EventBus.getDefault().post(new OnGetInventory());
    }
}
