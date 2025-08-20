package tar.syncchatirc.irc;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent;
import org.kitteh.irc.client.library.feature.auth.NickServ;
import tar.syncchatirc.SyncChatIRC;
import tar.syncchatirc.config.IRCConfig;
import net.engio.mbassy.listener.Handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IRCClient {
    private final IRCConfig config;
    private final MinecraftServer server;
    private Client client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean connected = false;
    private volatile boolean shouldReconnect = true;

    public IRCClient(IRCConfig config, MinecraftServer server) {
        this.config = config;
        this.server = server;
    }

    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                SyncChatIRC.LOGGER.info("Starting IRC client...");
                
                Client.Builder builder = Client.builder()
                        .nick(config.nickname)
                        .user(config.username)
                        .realName(config.realname)
                        .server()
                            .host(config.server)
                            .port(config.port, config.useSSL ? Client.Builder.Server.SecurityType.SECURE : Client.Builder.Server.SecurityType.INSECURE)
                            .password(config.serverPassword.isEmpty() ? null : config.serverPassword)
                            .then();

                client = builder.build();
                
                // Add event listeners
                client.getEventManager().registerEventListener(this);
                
                SyncChatIRC.LOGGER.info("Connecting to IRC server {}:{}", config.server, config.port);
                client.connect();
                
            } catch (Exception e) {
                SyncChatIRC.LOGGER.error("Failed to connect to IRC", e);
                if (shouldReconnect) {
                    SyncChatIRC.LOGGER.info("Attempting to reconnect in 30 seconds...");
                    try {
                        Thread.sleep(30000);
                        if (shouldReconnect) {
                            connect();
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, executor);
    }

    public void disconnect() {
        shouldReconnect = false;
        if (client != null && connected) {
            SyncChatIRC.LOGGER.info("Disconnecting from IRC...");
            client.sendMessage(config.channel, "Minecraft server shutting down");
            client.shutdown("Minecraft server shutting down");
        }
        executor.shutdown();
    }

    public void sendMessage(String message) {
        if (client != null && connected) {
            executor.submit(() -> {
                try {
                    client.sendMessage(config.channel, message);
                } catch (Exception e) {
                    SyncChatIRC.LOGGER.error("Failed to send IRC message", e);
                }
            });
        }
    }

    @Handler
    public void onConnect(ClientConnectionEstablishedEvent event) {
        connected = true;
        SyncChatIRC.LOGGER.info("Connected to IRC server");
        
        // Join the channel after connecting
        if (config.channelPassword != null && !config.channelPassword.isEmpty()) {
            // Use raw IRC command for password-protected channels
            SyncChatIRC.LOGGER.info("Joining password-protected channel: {}", config.channel);
            client.sendRawLine("JOIN " + config.channel + " " + config.channelPassword);
        } else {
            client.addChannel(config.channel);
        }
    }

    @Handler
    public void onDisconnect(ClientConnectionEndedEvent event) {
        connected = false;
        SyncChatIRC.LOGGER.info("Disconnected from IRC server. Reason: {}", event.getCause().isPresent() ? event.getCause().get().getMessage() : "Unknown");
    }

    @Handler
    public void onChannelMessage(ChannelMessageEvent event) {
        if (!event.getChannel().getName().equals(config.channel)) {
            return;
        }

        String nickname = event.getActor().getNick();
        String message = event.getMessage();
        
        // Don't relay our own messages back
        if (nickname.equals(config.nickname)) {
            return;
        }

        // Send to Minecraft server
        if (server != null) {
            server.execute(() -> {
                Text ircMessage = Text.literal(String.format("[IRC] <%s> %s", nickname, message));
                server.getPlayerManager().broadcast(ircMessage, false);
                SyncChatIRC.LOGGER.info("IRC -> MC: <{}> {}", nickname, message);
            });
        }
    }
    
    @Handler
    public void onChannelJoin(ChannelJoinEvent event) {
        if (event.getActor().getNick().equals(config.nickname)) {
            SyncChatIRC.LOGGER.info("Successfully joined IRC channel: {}", event.getChannel().getName());
        }
    }
    
    @Handler
    public void onChannelPart(ChannelPartEvent event) {
        if (event.getActor().getNick().equals(config.nickname)) {
            String reason = event.getMessage() != null ? event.getMessage() : "No reason";
            SyncChatIRC.LOGGER.info("Left IRC channel: {} - Reason: {}", event.getChannel().getName(), reason);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
