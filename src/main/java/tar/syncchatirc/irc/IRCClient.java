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
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean connected = false;
    private volatile boolean shouldReconnect = true;

    public IRCClient(IRCConfig config, MinecraftServer server) {
        this.config = config;
        this.server = server;
    }

    public CompletableFuture<Void> connect() {
        // Create a new executor if the current one is shut down
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        
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
                
                // Add user authentication if password is provided
                if (config.userPassword != null && !config.userPassword.isEmpty()) {
                    SyncChatIRC.LOGGER.info("User password configured - will authenticate as {} after connection", config.username);
                }
                
                
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
        
        
        // Perform user authentication if password is provided
        if (config.userPassword != null && !config.userPassword.isEmpty()) {
            SyncChatIRC.LOGGER.info("Authenticating with NickServ...");
            // Send IDENTIFY command to NickServ for user authentication
            client.sendRawLine("PRIVMSG NickServ :IDENTIFY " + config.userPassword);
        }

        
        
        // Join the channel after connecting and authentication
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
        String sourceType = "[IRC] ";
        
        // Don't relay our own messages back
        if (nickname.equals(config.nickname)) {
            return;
        }
        
        // This is specifically to mesh with the "GN" bot that is the partner project also titled
        // "SyncChatIRC", bridging Synchronet BBS multinode chat to IRC. It uses the nickname "GN".
        // IF nickname is "GN", parse message and check for text inside "<>", set that to the nickname
        if (nickname.equals("GN")) {
            sourceType = "";
            int start = message.indexOf('<');
            int end = message.indexOf('>');
            if (start != -1 && end != -1 && end > start) {
                nickname = message.substring(start + 1, end).trim();
                message = message.substring(end + 1).trim();
                sourceType = "[GN] ";
            }
        }

        final String nicknameFinal = nickname;
        final String messageFinal = message;
        final String sourceTypeFinal = sourceType;

        // Send to Minecraft server
        if (server != null) {
            server.execute(() -> {
                Text ircMessage = Text.literal(String.format("<%s> %s%s", nicknameFinal, sourceTypeFinal, messageFinal));
                server.getPlayerManager().broadcast(ircMessage, false);
                SyncChatIRC.LOGGER.info("IRC -> MC: <{}> {}", nicknameFinal, messageFinal);
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
