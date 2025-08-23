package tar.syncchatirc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tar.syncchatirc.commands.IRCCommands;
import tar.syncchatirc.config.IRCConfig;
import tar.syncchatirc.events.ChatEventHandler;
import tar.syncchatirc.irc.IRCClient;

public class SyncChatIRC implements ModInitializer {
	public static final String MOD_ID = "syncchatirc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	private static IRCConfig config;
	private static IRCClient ircClient;
	private static ChatEventHandler chatEventHandler;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing SyncChatIRC...");
		
		// Load configuration
		config = IRCConfig.load();
		
		// Register commands
		IRCCommands.register();
		
		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
		
		LOGGER.info("SyncChatIRC initialized!");
	}
	
	private void onServerStarted(MinecraftServer server) {
		if (config.enabled) {
			LOGGER.info("Starting IRC client...");
			
			// Initialize IRC client
			ircClient = new IRCClient(config, server);
			
			// Initialize chat event handler
			chatEventHandler = new ChatEventHandler(ircClient, config);
			chatEventHandler.register();
			
			// Connect to IRC
			ircClient.connect().exceptionally(throwable -> {
				LOGGER.error("Failed to connect to IRC", throwable);
				return null;
			});
		} else {
			LOGGER.info("IRC integration disabled in config");
		}
	}
	
	private void onServerStopping(MinecraftServer server) {
		if (ircClient != null) {
			LOGGER.info("Disconnecting from IRC...");
			ircClient.disconnect();
		}
	}
	
	public static IRCConfig getConfig() {
		return config;
	}
	
	public static IRCClient getIrcClient() {
		return ircClient;
	}
	
	public static ChatEventHandler getChatEventHandler() {
		return chatEventHandler;
	}
	
	public static void reloadConfig(MinecraftServer server) {
		LOGGER.info("Reloading IRC configuration...");
		
		// Stop current IRC client if running
		if (ircClient != null && ircClient.isConnected()) {
			LOGGER.info("Disconnecting current IRC client...");
			ircClient.disconnect();
		}
		
		// Reload configuration from file
		config = IRCConfig.load();
		LOGGER.info("Configuration reloaded from file");
		
		// Reinitialize IRC client and event handler if enabled
		if (config.enabled && server != null) {
			LOGGER.info("Reinitializing IRC client with new configuration...");
			
			// Initialize new IRC client
			ircClient = new IRCClient(config, server);
			
			// Update or initialize chat event handler
			if (chatEventHandler == null) {
				chatEventHandler = new ChatEventHandler(ircClient, config);
				chatEventHandler.register();
			} else {
				chatEventHandler.updateConfig(ircClient, config);
			}
			
			// Connect to IRC with new configuration
			ircClient.connect().exceptionally(throwable -> {
				LOGGER.error("Failed to connect to IRC after reload", throwable);
				return null;
			});
		} else if (!config.enabled) {
			LOGGER.info("IRC integration disabled in reloaded config");
			ircClient = null;
			// Keep chatEventHandler but update it to use null references
			if (chatEventHandler != null) {
				chatEventHandler.updateConfig(null, config);
			}
		} else {
			LOGGER.warn("Cannot initialize IRC client: server is null");
		}
		
		LOGGER.info("IRC configuration reload completed");
	}
}