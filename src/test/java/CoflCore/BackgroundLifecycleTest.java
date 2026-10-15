package CoflCore;

import CoflCore.commands.Command;
import CoflCore.commands.models.FlipData;
import CoflCore.configuration.Configuration;
import CoflCore.events.OnCloseGUI;
import CoflCore.events.OnModChatMessage;
import CoflCore.handlers.EventRegistry;
import CoflCore.network.WSClientWrapper;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class BackgroundLifecycleTest {
    @Test public void coreInitializationDoesNotKeepTheJvmAlive() throws Exception { runProbe("init"); }

    @Test public void backgroundWorkRemainsUsableAfterDisconnectAndAllowsNaturalExit() throws Exception { runProbe("active"); }

    private static void runProbe(String mode) throws Exception {
        Path output = Files.createTempFile("coflcore-lifecycle", ".log");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("coflcore.test.classpath", System.getProperty("java.class.path")),
                Probe.class.getName(), mode).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            boolean exited = process.waitFor(8, TimeUnit.SECONDS);
            assertTrue("Core background work prevented natural JVM exit:\n" + Files.readString(output), exited);
            assertEquals(Files.readString(output), 0, process.exitValue());
        } finally {
            if (process.isAlive()) process.destroyForcibly().waitFor();
            Files.deleteIfExists(output);
        }
    }

    /** Runs in its own JVM: it must return naturally, without System.exit or shutting down shared pools. */
    public static class Probe {
        final LinkedBlockingQueue<Boolean> commandWorkers = new LinkedBlockingQueue<>();
        @Subscribe public void closed(OnCloseGUI event) { commandWorkers.add(Thread.currentThread().isDaemon()); }
        @Subscribe public void chat(OnModChatMessage event) { commandWorkers.add(Thread.currentThread().isDaemon()); }

        public static void main(String[] args) throws Exception {
            Class.forName(CoflCore.class.getName());
            if (args[0].equals("init")) return;
            assertTrue(EventRegistry.chatThreadPool.submit(() -> Thread.currentThread().isDaemon()).get());
            assertTrue(EventRegistry.tickThreadPool.submit(() -> Thread.currentThread().isDaemon()).get());

            FlipData flip = new FlipData();
            flip.Id = "lifecycle";
            CoflCore.flipHandler.fds.Insert(flip);
            assertSame(flip, CoflCore.flipHandler.fds.GetLastFlip());
            CoflCore.flipHandler.fds.InvalidateFlip(flip);
            assertEquals(0, CoflCore.flipHandler.fds.CurrentFlips());

            var wrapper = new RecordingWrapper();
            CoflCore.Wrapper = wrapper;
            Configuration.getInstance().collectChat = true;
            Configuration.getInstance().chatRegex = ".*";
            // The very first collected chat and a concurrent burst must schedule exactly one batch.
            String[] expected = new String[20];
            for (int message = 0; message < expected.length; message++) {
                expected[message] = "first burst " + message;
                EventRegistry.onChatMessage(expected[message]);
            }
            String[] firstBatch = wrapper.batches.poll(2, TimeUnit.SECONDS);
            assertNotNull("First chat must send without needing a later message", firstBatch);
            Arrays.sort(expected);
            Arrays.sort(firstBatch);
            assertArrayEquals(expected, firstBatch);
            EventRegistry.onDisconnectedFromServer();
            assertFalse(wrapper.isRunning);
            assertTrue(EventRegistry.chatThreadPool.submit(() -> true).get());
            assertTrue(EventRegistry.tickThreadPool.submit(() -> true).get());
            wrapper.isRunning = true; // A reconnect must reuse the same schedulers and worker pools.
            EventRegistry.onChatMessage("second batch");
            assertArrayEquals(new String[]{"second batch"}, wrapper.batches.poll(2, TimeUnit.SECONDS));
            assertEquals(Arrays.asList(true, true), wrapper.batchWorkers);

            var subscriber = new Probe();
            EventBus.getDefault().register(subscriber);
            CoflSkyCommand.processCommand(new String[]{"closegui"}, "probe");
            new FlipperChatCommand().processCommand(new String[]{"toggle"}, "probe");
            assertEquals(Boolean.TRUE, subscriber.commandWorkers.poll(2, TimeUnit.SECONDS));
            assertEquals(Boolean.TRUE, subscriber.commandWorkers.poll(2, TimeUnit.SECONDS));
            EventBus.getDefault().unregister(subscriber);
            EventRegistry.onDisconnectedFromServer();
            CoflCore.flipHandler.fds.Insert(flip); // Leave housekeeping pending when main returns.
        }
    }

    private static class RecordingWrapper extends WSClientWrapper {
        final LinkedBlockingQueue<String[]> batches = new LinkedBlockingQueue<>();
        final java.util.List<Boolean> batchWorkers = new java.util.concurrent.CopyOnWriteArrayList<>();
        RecordingWrapper() { super(new String[0]); isRunning = true; }
        @Override public void SendMessage(Command command) {
            batchWorkers.add(Thread.currentThread().isDaemon());
            batches.add((String[]) command.getData());
        }
    }
}
