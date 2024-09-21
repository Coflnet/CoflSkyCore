package CoflCore.handlers;

import CoflCore.CoflCore;
import CoflCore.commands.Command;
import CoflCore.commands.CommandType;
import CoflCore.commands.JsonStringCommand;
import CoflCore.commands.models.FlipData;
import CoflCore.CoflSkyCommand;
import CoflCore.configuration.Configuration;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventRegistry {
    public static long LastClick = System.currentTimeMillis();
    public static final ExecutorService chatThreadPool = Executors.newFixedThreadPool(2);
    public static final ExecutorService tickThreadPool = Executors.newFixedThreadPool(2);

    public static long LastViewAuctionInvocation = Long.MIN_VALUE;
    public static String LastViewAuctionUUID = null;
    public static Pattern chatpattern = Pattern.compile("a^", Pattern.CASE_INSENSITIVE);
    private static LinkedBlockingQueue<String> chatBatch = new LinkedBlockingQueue<String>();
    private static LocalDateTime lastBatchStart = LocalDateTime.now();

    public static void onDisconnectedFromServer(){
        if (CoflCore.Wrapper.isRunning) {
            System.out.println("Disconnected from server");
            CoflCore.Wrapper.stop();
            System.out.println("CoflSky stopped");
        }
    }

    public static void onOpenLastFlip(String username){
        FlipData f = CoflCore.flipHandler.fds.GetLastFlip();
        if (f != null) {
            String[] args = new String[]{"/cofl", "openauctiongui", f.Id, "false"};
            CoflSkyCommand.processCommand(args, username);
        }
    }

    public static void onOpenBestFlip(String username, boolean isInitialKeypress){
        if ((System.currentTimeMillis() - LastClick) >= 300) {

            FlipData f = CoflCore.flipHandler.fds.GetHighestFlip();

            if (f != null) {
                CoflSkyCommand.processCommand(new String[]{"/cofl", "openauctiongui", f.Id, "true"}, username);
                EventRegistry.LastViewAuctionUUID = f.Id;
                EventRegistry.LastViewAuctionInvocation = System.currentTimeMillis();
                LastClick = System.currentTimeMillis();
                String command = new Gson().toJson("/viewauction " + f.Id);

                CoflCore.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, command));
                CoflSkyCommand.processCommand(new String[]{"/cofl", "track", "besthotkey", f.Id, username}, username);
            } else {
                // only display message once (if this is the key down event)
                if (isInitialKeypress) {
                    CoflSkyCommand.processCommand(new String[]{"/cofl", "dialog", "nobestflip", username}, username);
                }
            }
        }
    }

    public static void onChatMessage(String msg) {
        if (!CoflCore.Wrapper.isRunning || !Configuration.getInstance().collectChat)
            return;
        chatThreadPool.submit(() -> {
            try {
                Matcher matcher = chatpattern.matcher(msg);
                boolean matchFound = matcher.find();
                if (!matchFound)
                    return;

                chatBatch.add(msg);
                // add 500ms to the last batch start time
                long nanoSeconds = 500_000_000;
                if (!lastBatchStart.plusNanos(nanoSeconds).isBefore(LocalDateTime.now())) {
                    System.out.println(msg + " was not sent because it was too soon");
                    return;
                }
                lastBatchStart = LocalDateTime.now();

                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Sending batch of " + chatBatch.size() + " messages");
                        Command<String[]> data = new Command<>(CommandType.chatBatch, chatBatch.toArray(new String[0]));
                        chatBatch.clear();
                        CoflCore.Wrapper.SendMessage(data);
                    }
                }, 500);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
