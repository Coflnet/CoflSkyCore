package CoflCore;

import CoflCore.events.OnModChatMessage;
import org.greenrobot.eventbus.EventBus;

public class FlipperChatCommand {

    public static boolean useChatOnlyMode = false;

    public void processCommand(String[] args, String username) {
        new Thread(() -> {
            if (args.length == 1 && args[0].equals("toggle")) {
                FlipperChatCommand.useChatOnlyMode = !FlipperChatCommand.useChatOnlyMode;
                EventBus.getDefault().post(new OnModChatMessage("[§1C§6oflnet§f]§7: §7Set §bChat only mode §7to: §f" + (FlipperChatCommand.useChatOnlyMode ? "true" : "false")));
            } else {
                String[] newArgs = new String[args.length + 1];
                System.arraycopy(args, 0, newArgs, 1, args.length);
                newArgs[0] = "chat";
                CoflSkyCommand.sendCommandToServer(newArgs, username);
            }
        }).start();
    }
}
