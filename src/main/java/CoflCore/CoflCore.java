package CoflCore;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import CoflCore.configuration.Config;
import CoflCore.configuration.LocalConfig;
import CoflCore.misc.SessionManager;
import CoflCore.network.WSClientWrapper;
import CoflCore.proxy.APIKeyManager;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;

public class CoflCore {
    public static final String MODID = "CoflSky";
    public static final String VERSION = "1.6.3";

    public static FlipHandler flipHandler = new FlipHandler();

    public static File configFile;
    private File coflDir;
    public static LocalConfig config;

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
        String configString = null;
        Gson gson = new Gson();
        coflDir = new File(configPath.toFile(), "CoflSky");
        coflDir.mkdirs();
        SessionManager.setMainPath(Paths.get(coflDir.toString() + "/sessions"));
        configFile = new File(coflDir, "config.json");
        try {
            if (configFile.isFile()) {
                configString = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
                config = gson.fromJson(configString, LocalConfig.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

}

