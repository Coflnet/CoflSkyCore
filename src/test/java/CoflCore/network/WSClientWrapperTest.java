package CoflCore.network;

import CoflCore.commands.Command;
import CoflCore.commands.CommandType;
import CoflCore.commands.RawCommand;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the SendMessage/start()/stop() locking hazard: SendMessage used to be
 * synchronized on the same monitor as start()/stop(), so a caller could block for up to the
 * 10s connect timeout whenever a reconnect attempt was in progress. SendMessage must never
 * wait on the connection lifecycle lock.
 */
public class WSClientWrapperTest {

    /**
     * A fake WSClient that never touches the network - SendCommand just records the call.
     */
    private static class FakeWSClient extends WSClient {
        final AtomicInteger rawCommandCount = new AtomicInteger();
        final AtomicInteger commandCount = new AtomicInteger();

        FakeWSClient() {
            super(URI.create("ws://localhost:1/test"));
        }

        @Override
        public void SendCommand(RawCommand cmd) {
            rawCommandCount.incrementAndGet();
        }

        @Override
        public void SendCommand(Command cmd) {
            commandCount.incrementAndGet();
        }
    }

    @Test
    public void sendMessageDoesNotBlockWhileConnectionLockIsHeld() throws InterruptedException {
        WSClientWrapper wrapper = new WSClientWrapper(new String[0]);
        FakeWSClient fake = new FakeWSClient();
        wrapper.socket = fake;
        wrapper.isRunning = true;

        CountDownLatch lockHeld = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        // Simulate a long-running connect() by holding the connection lock from another thread,
        // exactly like start() would while blocked in socket.start()/connect().
        Thread lockHolder = new Thread(() -> {
            synchronized (wrapper.connectionLock) {
                lockHeld.countDown();
                try {
                    releaseLock.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        lockHolder.setDaemon(true);
        lockHolder.start();

        assertTrue("connection lock should have been acquired by the simulated connect thread",
                lockHeld.await(2, TimeUnit.SECONDS));

        CountDownLatch sendCompleted = new CountDownLatch(1);
        Thread sender = new Thread(() -> {
            wrapper.SendMessage(new RawCommand("ping", null));
            sendCompleted.countDown();
        });
        sender.setDaemon(true);
        sender.start();

        try {
            assertTrue("SendMessage must not block while the connection lock is held elsewhere",
                    sendCompleted.await(2, TimeUnit.SECONDS));
            assertEquals(1, fake.rawCommandCount.get());
        } finally {
            releaseLock.countDown();
            lockHolder.join(2000);
        }
    }

    @Test
    public void sendMessageIsNoOpWhenNotRunning() {
        WSClientWrapper wrapper = new WSClientWrapper(new String[0]);
        FakeWSClient fake = new FakeWSClient();
        wrapper.socket = fake;
        wrapper.isRunning = false;

        wrapper.SendMessage(new RawCommand("ping", null));
        wrapper.SendMessage(new Command<>(CommandType.Ping, null));

        assertEquals(0, fake.rawCommandCount.get());
        assertEquals(0, fake.commandCount.get());
    }

    @Test
    public void sendMessageDoesNotThrowWhenSocketIsNulledConcurrently() {
        WSClientWrapper wrapper = new WSClientWrapper(new String[0]);
        wrapper.isRunning = true;
        wrapper.socket = null;

        // Must not throw NPE even though isRunning is true.
        wrapper.SendMessage(new RawCommand("ping", null));
        wrapper.SendMessage(new Command<>(CommandType.Ping, null));

        assertTrue("SendMessage must not mutate isRunning", wrapper.isRunning);
    }

    /**
     * This is THE regression test for the render-thread freeze bug: on the old
     * all-synchronized-on-`this` WSClientWrapper, SendMessage() could not even be entered while
     * start() was in the middle of a real (up to 10s) blocking socket.connect() - both methods
     * shared the same monitor. A caller sending from the Minecraft render thread would then
     * freeze the whole client for the remainder of the connect attempt.
     *
     * Do NOT "simplify" away the ~3s blocking sleep in the fake start() below - a fast/no-op
     * fake would not actually exercise the bug. The sleep must genuinely overlap with the
     * SendMessage call for this test to mean anything.
     */
    @Test
    public void sendMessageDoesNotBlockWhileStartIsConnecting() throws InterruptedException {
        WSClientWrapper wrapper = new WSClientWrapper(new String[0]);

        CountDownLatch connectEntered = new CountDownLatch(1);
        class SlowConnectWSClient extends WSClient {
            SlowConnectWSClient() {
                super(URI.create("ws://localhost:1/test"));
            }

            @Override
            public void start() {
                // Simulates the real WSClient.start() blocking in socket.connect()
                // (CONNECT_TIMEOUT_MS = 10s) while a connection attempt is in flight.
                connectEntered.countDown();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        SlowConnectWSClient fake = new SlowConnectWSClient();
        wrapper.socket = fake;
        wrapper.isRunning = false;

        Thread starter = new Thread(wrapper::start, "test-wrapper-start");
        starter.setDaemon(true);
        starter.start();

        try {
            assertTrue("wrapper.start() should have entered the simulated connect",
                    connectEntered.await(2, TimeUnit.SECONDS));

            // At this point, start() is (on the old code) holding the shared monitor for
            // ~3 more seconds. SendMessage must return promptly regardless - isRunning is
            // still false here, so it takes the not-running branch, but on the old code it
            // couldn't even reach that branch check because it blocked acquiring the monitor.
            long beginNanos = System.nanoTime();
            wrapper.SendMessage(new RawCommand("ping", null));
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beginNanos);

            assertTrue("SendMessage blocked for " + elapsedMs + "ms while start() was connecting "
                            + "- this is the render-thread freeze regression",
                    elapsedMs < 1000);
        } finally {
            starter.join(5000);
            assertFalse("start thread should not be leaked past the test", starter.isAlive());
        }
    }
}
