package tar.syncchatirc.events;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import tar.syncchatirc.SyncChatIRC;
import tar.syncchatirc.config.IRCConfig;
import tar.syncchatirc.irc.IRCClient;

public class ChatEventHandler {
    private IRCClient ircClient;
    private IRCConfig config;
    private static boolean registered = false;

    public ChatEventHandler(IRCClient ircClient, IRCConfig config) {
        this.ircClient = ircClient;
        this.config = config;
    }
    
    public void updateConfig(IRCClient ircClient, IRCConfig config) {
        this.ircClient = ircClient;
        this.config = config;
    }

    public void register() {
        // Only register the event handlers once to avoid duplicates
        if (!registered) {
            registerEventHandlers();
            registered = true;
        }
    }
    
    private void registerEventHandlers() {
        // Handle chat messages
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (config != null && config.enabled && ircClient != null && ircClient.isConnected()) {
                String playerName = sender.getDisplayName().getString();
                String chatMessage = message.getContent().getString();
                String formattedMessage = String.format(config.messageFormat, playerName, chatMessage);
                
                ircClient.sendMessage(formattedMessage);
                SyncChatIRC.LOGGER.debug("MC -> IRC: <{}> {}", playerName, chatMessage);
            }
        });

        // Handle player join/leave events
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (config != null && config.enabled && config.relayJoinLeave && ircClient != null && ircClient.isConnected()) {
                String playerName = handler.getPlayer().getDisplayName().getString();
                String joinMessage = String.format(config.joinFormat, playerName);
                
                ircClient.sendMessage(joinMessage);
                SyncChatIRC.LOGGER.debug("MC -> IRC: {} joined", playerName);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (config != null && config.enabled && config.relayJoinLeave && ircClient != null && ircClient.isConnected()) {
                String playerName = handler.getPlayer().getDisplayName().getString();
                String leaveMessage = String.format(config.leaveFormat, playerName);
                
                ircClient.sendMessage(leaveMessage);
                SyncChatIRC.LOGGER.debug("MC -> IRC: {} left", playerName);
            }
        });
    }
}
