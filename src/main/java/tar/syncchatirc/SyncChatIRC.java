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
}