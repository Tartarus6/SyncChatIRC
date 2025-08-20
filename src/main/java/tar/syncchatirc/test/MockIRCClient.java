package tar.syncchatirc.test;

import tar.syncchatirc.SyncChatIRC;
import tar.syncchatirc.config.IRCConfig;
import tar.syncchatirc.irc.IRCClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Mock IRC client for testing purposes.
 * This allows testing the IRC integration without requiring a live IRC server.
 */
public class MockIRCClient extends IRCClient {
    private final List<String> sentMessages = new ArrayList<>();
    private boolean connected = false;
    private boolean shouldFailConnection = false;

    public MockIRCClient(IRCConfig config) {
        super(config, null);
    }

    @Override
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            if (shouldFailConnection) {
                SyncChatIRC.LOGGER.info("[MOCK] Connection failed (simulated)");
                return;
            }
            
            connected = true;
            SyncChatIRC.LOGGER.info("[MOCK] Connected to IRC (simulated)");
        });
    }

    @Override
    public void disconnect() {
        connected = false;
        SyncChatIRC.LOGGER.info("[MOCK] Disconnected from IRC (simulated)");
    }

    @Override
    public void sendMessage(String message) {
        if (connected) {
            sentMessages.add(message);
            SyncChatIRC.LOGGER.info("[MOCK] IRC message sent: {}", message);
        } else {
            SyncChatIRC.LOGGER.warn("[MOCK] Cannot send message - not connected");
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    // Test helper methods
    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public void clearSentMessages() {
        sentMessages.clear();
    }

    public void simulateIRCMessage(String user, String message) {
        if (connected) {
            SyncChatIRC.LOGGER.info("[MOCK] Simulated IRC message from {}: {}", user, message);
            // In a real test, this would trigger the onMessage event
        }
    }

    public void setConnectionFailure(boolean shouldFail) {
        this.shouldFailConnection = shouldFail;
    }

    public int getMessageCount() {
        return sentMessages.size();
    }

    public boolean hasMessage(String message) {
        return sentMessages.contains(message);
    }

    public boolean hasMessageContaining(String substring) {
        return sentMessages.stream().anyMatch(msg -> msg.contains(substring));
    }
}
