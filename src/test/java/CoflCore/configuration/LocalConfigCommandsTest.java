package CoflCore.configuration;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class LocalConfigCommandsTest {
    @Test
    public void addsNewPublicCommandsWithoutReplacingSavedDescriptions() {
        HashMap<String, String> saved = new HashMap<>();
        saved.put("report", "Saved description");
        saved.put("vps", "Retired command");
        saved.put("loadfliphistory", "Internal command");

        LocalConfig config = new LocalConfig(true, true, null, saved, null);

        assertEquals("Saved description", config.knownCommands.get("report"));
        for (String command : new String[]{"rustaddon", "agreementterms", "settings", "proxy", "emblem"})
            assertTrue(command, config.knownCommands.containsKey(command));
        assertFalse(config.knownCommands.containsKey("vps"));
        assertFalse(config.knownCommands.containsKey("loadfliphistory"));
    }

    @Test
    public void repopulatesDefaultsWhenOnlyRetiredCommandsWereSaved() {
        HashMap<String, String> saved = new HashMap<>();
        saved.put("vps", "Retired command");
        saved.put("loadfliphistory", "Internal command");

        LocalConfig config = new LocalConfig(true, true, null, saved, null);

        assertTrue(config.knownCommands.containsKey("report"));
        assertFalse(config.knownCommands.containsKey("vps"));
        assertFalse(config.knownCommands.containsKey("loadfliphistory"));
    }
}
