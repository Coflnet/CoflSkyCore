package CoflCore;


import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import CoflCore.configuration.Config;
import CoflCore.configuration.LocalConfig;
import CoflCore.misc.SessionManager;
import CoflCore.network.WSClientWrapper;
import CoflCore.proxy.APIKeyManager;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;

public class CoflCore {
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.7.7";

    public static FlipHandler flipHandler = new FlipHandler();

    public static File configFile;
    private File coflDir;
    public static LocalConfig config;

    private static final long MAX_CONFIG_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final DateTimeFormatter BACKUP_FILE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final String[] webSocketURIPrefix = new String[]{
            "wss://sky.coflnet.com/modsocket",
            // fallback for old java versions not supporting new tls certificates
            "ws://sky-mod.coflnet.com/modsocket",
    };
    public static WSClientWrapper Wrapper = new WSClientWrapper(webSocketURIPrefix);

    public static String CommandUri = Config.BaseUrl + "/api/mod/commands";
    private final static APIKeyManager apiKeyManager = new APIKeyManager();

    public void registerEventFile(Object target) {
        EventBus.getDefault().register(target);
    }

    public void init(Path configPath) {
        Gson gson = new com.google.gson.GsonBuilder()
            .disableHtmlEscaping()  // Prevent corruption of special characters like §
            .create();

        coflDir = new File(configPath.toFile(), "CoflSky");
        coflDir.mkdirs();
        SessionManager.setMainPath(Paths.get(coflDir.toString() + "/sessions"));
        configFile = new File(coflDir, "config.json");
        if (configFile.isFile()) {
            config = loadConfig(gson);
        }
        if (config == null) {
            config = LocalConfig.createDefaultConfig();
        }

        try {
            this.apiKeyManager.loadIfExists(configPath);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        //CoflCore.Wrapper = new WSClientWrapper(webSocketURIPrefix);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.saveConfig(configFile, config);
            try {
                apiKeyManager.saveKey(configPath);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }));
    }
    public static APIKeyManager getAPIKeyManager() {
        return apiKeyManager;
    }

    private LocalConfig loadConfig(Gson gson) {
        try {
            long fileSize = Files.size(configFile.toPath());
            if (fileSize > MAX_CONFIG_FILE_SIZE_BYTES) {
                return handleInvalidConfig("Config file is larger than " + MAX_CONFIG_FILE_SIZE_BYTES + " bytes (" + fileSize + ")", null);
            }

            try (Reader reader = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
                LocalConfig loadedConfig = gson.fromJson(reader, LocalConfig.class);
                if (loadedConfig != null) {
                    loadedConfig.initCommands();
                    loadedConfig.initSettings();
                    return loadedConfig;
                }
                return handleInvalidConfig("Config file could not be parsed (null result)", null);
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            return handleInvalidConfig("Out of memory while loading config", outOfMemoryError);
        } catch (Exception exception) {
            exception.printStackTrace();
            return handleInvalidConfig("Exception while loading config: " + exception.getMessage(), exception);
        }
    }

    private LocalConfig handleInvalidConfig(String reason, Throwable cause) {
        System.err.println("[CoflCore] " + reason + " - resetting config to defaults.");
        if (cause != null) {
            cause.printStackTrace();
        }

        if (configFile != null && configFile.isFile()) {
            Path backupPath = configFile.toPath().resolveSibling(
                    "config.json." + BACKUP_FILE_SUFFIX_FORMATTER.format(LocalDateTime.now()) + ".bak");
            try {
                Files.move(configFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.err.println("[CoflCore] Previous config backed up to " + backupPath.toAbsolutePath());
            } catch (IOException ioException) {
                System.err.println("[CoflCore] Failed to back up invalid config: " + ioException.getMessage());
            }
        }

        LocalConfig defaultConfig = LocalConfig.createDefaultConfig();
        LocalConfig.saveConfig(configFile, defaultConfig);
        return defaultConfig;
    }
}

