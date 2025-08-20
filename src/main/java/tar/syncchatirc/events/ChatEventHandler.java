package tar.syncchatirc.events;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import tar.syncchatirc.SyncChatIRC;
import tar.syncchatirc.config.IRCConfig;
import tar.syncchatirc.irc.IRCClient;

public class ChatEventHandler {
    private final IRCClient ircClient;
    private final IRCConfig config;

    public ChatEventHandler(IRCClient ircClient, IRCConfig config) {
        this.ircClient = ircClient;
        this.config = config;
    }

    public void register() {
        // Handle chat messages
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (config.enabled && ircClient.isConnected()) {
                String playerName = sender.getDisplayName().getString();
                String chatMessage = message.getContent().getString();
                String formattedMessage = String.format(config.messageFormat, playerName, chatMessage);
                
                ircClient.sendMessage(formattedMessage);
                SyncChatIRC.LOGGER.debug("MC -> IRC: <{}> {}", playerName, chatMessage);
            }
        });

        // Handle player join/leave events
        if (config.relayJoinLeave) {
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                if (config.enabled && ircClient.isConnected()) {
                    String playerName = handler.getPlayer().getDisplayName().getString();
                    String joinMessage = String.format(config.joinFormat, playerName);
                    
                    ircClient.sendMessage(joinMessage);
                    SyncChatIRC.LOGGER.debug("MC -> IRC: {} joined", playerName);
                }
            });

            ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
                if (config.enabled && ircClient.isConnected()) {
                    String playerName = handler.getPlayer().getDisplayName().getString();
                    String leaveMessage = String.format(config.leaveFormat, playerName);
                    
                    ircClient.sendMessage(leaveMessage);
                    SyncChatIRC.LOGGER.debug("MC -> IRC: {} left", playerName);
                }
            });
        }
    }
}
