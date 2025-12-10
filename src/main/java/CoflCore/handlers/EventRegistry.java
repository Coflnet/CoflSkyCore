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
    public static Pattern chatBlockPattern = Pattern.compile("a^", Pattern.CASE_INSENSITIVE);

    /**
     * Checks if a message should be blocked based on `Configuration.getInstance().chatBlockRegex`.
     * This is a standalone helper so other code can query the block decision without invoking
     * the batching pipeline.
     */
    public static boolean shouldBlockChatMessage(String message) {
        if (message == null)
            return false;

        String msg = message;
        if (msg.contains("§")) {
            msg = msg.replaceAll("§.", "");
        }

        String chatBlockRegex = Configuration.getInstance().chatBlockRegex;
        if (chatBlockRegex == null || chatBlockRegex.isEmpty())
            return false;

        try {
            if (chatBlockPattern.pattern().compareTo(chatBlockRegex) != 0) {
                chatBlockPattern = Pattern.compile(chatBlockRegex, Pattern.CASE_INSENSITIVE);
            }
            if (chatBlockPattern.matcher(msg).find()) {
                System.out.println("Blocking chat message: " + msg);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error compiling chatBlockRegex: " + e.getMessage());
        }

        return false;
    }
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
            String[] args = new String[]{"openauctiongui", f.Id, "false"};
            CoflSkyCommand.processCommand(args, username);
        }
    }

    public static void onOpenBestFlip(String username, boolean isInitialKeypress){
        if ((System.currentTimeMillis() - LastClick) >= 300) {

            FlipData f = CoflCore.flipHandler.fds.GetHighestFlip();

            if (f != null) {
                CoflSkyCommand.processCommand(new String[]{"openauctiongui", f.Id, "true"}, username);
                EventRegistry.LastViewAuctionUUID = f.Id;
                EventRegistry.LastViewAuctionInvocation = System.currentTimeMillis();
                LastClick = System.currentTimeMillis();
                String command = new Gson().toJson("/viewauction " + f.Id);

                CoflCore.Wrapper.SendMessage(new JsonStringCommand(CommandType.Clicked, command));
                CoflSkyCommand.processCommand(new String[]{"track", "besthotkey", f.Id, username}, username);
            } else {
                // only display message once (if this is the key down event)
                if (isInitialKeypress) {
                    CoflSkyCommand.processCommand(new String[]{"dialog", "nobestflip", username}, username);
                }
            }
        }
    }

    /**
     * Should be invoked with every chat mesesage received.
     * Expects unformatted chat messages (without color codes).
     * It will batch messages that match the regex defined in the configuration.
     * Returns whether the message should be blocked based on chatBlockRegex.
     */
    public static boolean onChatMessage(String message) {
        if (CoflCore.Wrapper == null || !CoflCore.Wrapper.isRunning || !Configuration.getInstance().collectChat)
            return false;
        
        final String msg;
        if (message.contains("§")) {
            msg = message.replaceAll("§.", "");
        } else {
            msg = message;
        }
        
        chatThreadPool.submit(() -> {
            try {
                if (chatpattern.pattern().compareTo(Configuration.getInstance().chatRegex) != 0){
                    chatpattern = Pattern.compile(Configuration.getInstance().chatRegex, Pattern.CASE_INSENSITIVE);
                }
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
        
        return false; // Message was not blocked
    }
}
