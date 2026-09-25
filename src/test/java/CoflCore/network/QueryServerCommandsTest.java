package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.misc.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class QueryServerCommandsTest {
    @Before public void setUp() throws IOException {
        SessionManager.setMainPath(Files.createTempDirectory("cofl-description-sessions"));
        QueryServerCommands.preferredDescriptionEndpoint = -1;
    }

    @After public void tearDown() {
        QueryServerCommands.preferredDescriptionEndpoint = -1;
    }

    @Test public void serverFailureFallsBackOnceAndSuccessfulHostSticks() throws Exception {
        List<String> order = new ArrayList<>();
        HttpServer sky = stub("sky", 503, order);
        HttpServer commands = stub("commands", 503, order);
        HttpServer mod = stub("mod", 200, order);
        try {
            String[] urls = {url(sky), url(commands), url(mod)};
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", -1));
            assertEquals(List.of("sky", "commands", "mod"), order);
            assertEquals(2, QueryServerCommands.preferredDescriptionEndpoint);
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", -1));
            assertEquals(List.of("sky", "commands", "mod", "mod"), order);
            QueryServerCommands.preferredDescriptionEndpoint = -1;
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", 2));
            assertEquals(List.of("sky", "commands", "mod", "mod", "mod"), order);
        } finally {
            sky.stop(0); commands.stop(0); mod.stop(0);
        }
    }

    @Test public void failedPreferenceReturnsToDefaultOrder() throws Exception {
        List<String> order = new ArrayList<>();
        HttpServer sky = stub("sky", 503, order);
        AtomicInteger commandsStatus = new AtomicInteger(200);
        HttpServer commands = stub("commands", commandsStatus, order);
        HttpServer mod = stub("mod", 200, order);
        try {
            String[] urls = {url(sky), url(commands), url(mod)};
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", -1));
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", -1));
            commandsStatus.set(503);
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", -1));
            assertEquals(List.of("sky", "commands", "commands", "commands", "sky", "mod"), order);
            assertEquals(2, QueryServerCommands.preferredDescriptionEndpoint);
        } finally {
            sky.stop(0); commands.stop(0); mod.stop(0);
        }
    }

    @Test public void allServerFailuresAreAttemptedOnceAndDoNotStick() throws Exception {
        List<String> order = new ArrayList<>();
        HttpServer sky = stub("sky", 503, order);
        HttpServer commands = stub("commands", 503, order);
        HttpServer mod = stub("mod", 503, order);
        try {
            assertNull(QueryServerCommands.postDescriptionWithFallback(
                    new String[]{url(sky), url(commands), url(mod)}, "{}", "tester", -1));
            assertEquals(List.of("sky", "commands", "mod"), order);
            assertEquals(-1, QueryServerCommands.preferredDescriptionEndpoint);
        } finally {
            sky.stop(0); commands.stop(0); mod.stop(0);
        }
    }

    @Test public void rateLimitAndRedirectStopWithoutLeakingBody() throws Exception {
        for (int status : new int[]{429, 302}) {
            List<String> order = new ArrayList<>();
            HttpServer sky = stub("sky", 503, order);
            HttpServer commands = stub("commands", status, order);
            HttpServer mod = stub("mod", 200, order);
            try {
                commands.removeContext("/api/mod/description/modifications");
                commands.createContext("/api/mod/description/modifications", exchange -> {
                    order.add("commands");
                    exchange.getRequestBody().readAllBytes();
                    exchange.getResponseHeaders().add("Location", url(mod));
                    respond(exchange, status);
                });
                assertNull(QueryServerCommands.postDescriptionWithFallback(new String[]{url(sky), url(commands), url(mod)}, "{}", "tester", -1));
                assertEquals(List.of("sky", "commands"), order);
                assertEquals(-1, QueryServerCommands.preferredDescriptionEndpoint);
            } finally {
                sky.stop(0); commands.stop(0); mod.stop(0);
            }
        }
    }

    @Test public void transportFailureKeepsBodyAndSessionHeaders() throws Exception {
        List<String> order = new ArrayList<>();
        HttpServer refused = stub("refused", 200, order);
        HttpServer commands = stub("commands", 200, order);
        HttpServer mod = stub("mod", 200, order);
        AtomicReference<String> body = new AtomicReference<>(), session = new AtomicReference<>(), uuid = new AtomicReference<>();
        commands.removeContext("/api/mod/description/modifications");
        commands.createContext("/api/mod/description/modifications", exchange -> {
            order.add("commands");
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            session.set(exchange.getRequestHeaders().getFirst("conId"));
            uuid.set(exchange.getRequestHeaders().getFirst("uuid"));
            respond(exchange, 200);
        });
        String[] urls = {url(refused), url(commands), url(mod)};
        refused.stop(0);
        try {
            assertEquals("[[]]", QueryServerCommands.postDescriptionWithFallback(urls, "{\"version\":4}", "tester", -1));
            assertEquals(List.of("commands"), order);
            assertEquals("{\"version\":4}", body.get());
            assertEquals("tester", uuid.get());
            assertEquals(SessionManager.GetCoflSession("tester").SessionUUID, session.get());
        } finally {
            commands.stop(0); mod.stop(0);
        }
        QueryServerCommands.preferredDescriptionEndpoint = -1;
        assertNull(QueryServerCommands.postDescriptionWithFallback(urls, "{}", "tester", -1));
        assertEquals(-1, QueryServerCommands.preferredDescriptionEndpoint);
    }

    @Test public void customPostUsesOnlySpecifiedEndpoint() throws Exception {
        List<String> order = new ArrayList<>();
        HttpServer server = stub("custom", 200, order);
        try {
            assertEquals("[[]]", QueryServerCommands.PostRequest(url(server), "{}", "tester"));
            assertEquals(List.of("custom"), order);
            assertEquals(-1, QueryServerCommands.preferredDescriptionEndpoint);
        } finally { server.stop(0); }
    }

    @Test public void websocketDefaultsAreEncrypted() {
        for (String uri : CoflCore.webSocketURIPrefix) assertTrue(uri, uri.startsWith("wss://"));
    }

    @Test public void failedWssConnectDoesNotRetryOverPlainWs() throws Exception {
        List<String> attempted = new ArrayList<>();
        WSClientWrapper wrapper = new WSClientWrapper(new String[0]) {
            @Override public boolean initializeNewSocket(String uri, String username) {
                attempted.add(uri);
                return false;
            }
        };
        Field failed = WSClientWrapper.class.getDeclaredField("sslHandshakeFailed");
        failed.setAccessible(true);
        failed.setBoolean(wrapper, true);
        assertFalse(wrapper.initializeNewSocketWithFallback("wss://sky.coflnet.com/modsocket", "tester"));
        assertEquals(List.of("wss://sky.coflnet.com/modsocket"), attempted);
    }

    private static HttpServer stub(String name, int status, List<String> order) throws IOException {
        return stub(name, new AtomicInteger(status), order);
    }

    private static HttpServer stub(String name, AtomicInteger status, List<String> order) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/mod/description/modifications", exchange -> {
            order.add(name);
            exchange.getRequestBody().readAllBytes();
            respond(exchange, status.get());
        });
        server.start();
        return server;
    }

    private static void respond(HttpExchange exchange, int status) throws IOException {
        if (status == 200) {
            byte[] response = "[[]]".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response);
        } else exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private static String url(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/api/mod/description/modifications";
    }
}
