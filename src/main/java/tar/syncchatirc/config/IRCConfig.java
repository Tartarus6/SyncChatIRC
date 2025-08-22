package tar.syncchatirc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tar.syncchatirc.SyncChatIRC;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class IRCConfig {
    public String server = "localhost";
    public int port = 6667;
    public String nickname = "MinecraftBot";
    public String username = "MinecraftBot";
    public String realname = "Minecraft IRC Bot";
    public String channel = "#minecraft";
    public boolean enabled = true;
    public boolean useSSL = false;
    public String serverPassword = "";
    public String userPassword = ""; // Password for user authentication (NickServ IDENTIFY)
    public String channelPassword = "";
    public boolean relayServerMessages = true;
    public boolean relayJoinLeave = true;
    public boolean relayDeathMessages = true;
    public boolean relayAdvancements = true;
    public String messageFormat = "[MC] <%s> %s";
    public String joinFormat = "[MC] * %s joined the game";
    public String leaveFormat = "[MC] * %s left the game";
    public String deathFormat = "[MC] * %s";
    public String advancementFormat = "[MC] * %s has made the advancement [%s]";
    
    private static final String CONFIG_FILE = "config/syncchatirc.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public static IRCConfig load() {
        File configFile = new File(CONFIG_FILE);
        
        if (!configFile.exists()) {
            IRCConfig defaultConfig = new IRCConfig();
            defaultConfig.save();
            SyncChatIRC.LOGGER.info("Created default IRC config at {}", CONFIG_FILE);
            return defaultConfig;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            IRCConfig config = GSON.fromJson(reader, IRCConfig.class);
            SyncChatIRC.LOGGER.info("Loaded IRC config from {}", CONFIG_FILE);
            return config;
        } catch (IOException e) {
            SyncChatIRC.LOGGER.error("Failed to load IRC config", e);
            return new IRCConfig();
        }
    }
    
    public void save() {
        File configFile = new File(CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
            SyncChatIRC.LOGGER.info("Saved IRC config to {}", CONFIG_FILE);
        } catch (IOException e) {
            SyncChatIRC.LOGGER.error("Failed to save IRC config", e);
        }
    }
}
