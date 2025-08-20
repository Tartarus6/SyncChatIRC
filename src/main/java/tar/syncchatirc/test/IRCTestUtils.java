package tar.syncchatirc.test;

import tar.syncchatirc.SyncChatIRC;
import tar.syncchatirc.config.IRCConfig;

/**
 * Test utilities for SyncChatIRC mod.
 * Use these methods to test IRC functionality without requiring a live IRC server.
 */
public class IRCTestUtils {
    private static MockIRCClient mockClient;
    
    /**
     * Enable test mode - replaces the real IRC client with a mock one.
     */
    public static void enableTestMode() {
        IRCConfig config = SyncChatIRC.getConfig();
        if (config == null) {
            config = new IRCConfig();
        }
        
        mockClient = new MockIRCClient(config);
        SyncChatIRC.LOGGER.info("IRC test mode enabled - using mock client");
    }
    
    /**
     * Get the mock IRC client for testing.
     */
    public static MockIRCClient getMockClient() {
        return mockClient;
    }
    
    /**
     * Run a simple test to verify IRC message sending works.
     */
    public static boolean runBasicTest() {
        if (mockClient == null) {
            SyncChatIRC.LOGGER.error("Test mode not enabled - call enableTestMode() first");
            return false;
        }
        
        try {
            // Test connection
            mockClient.connect().join();
            if (!mockClient.isConnected()) {
                SyncChatIRC.LOGGER.error("Test failed: Mock client not connected");
                return false;
            }
            
            // Test message sending
            String testMessage = "Test message from Minecraft";
            mockClient.sendMessage(testMessage);
            
            if (!mockClient.hasMessage(testMessage)) {
                SyncChatIRC.LOGGER.error("Test failed: Message not sent properly");
                return false;
            }
            
            SyncChatIRC.LOGGER.info("Basic IRC test passed!");
            return true;
            
        } catch (Exception e) {
            SyncChatIRC.LOGGER.error("Test failed with exception", e);
            return false;
        }
    }
    
    /**
     * Test various message formats.
     */
    public static boolean runFormatTest() {
        if (mockClient == null) {
            SyncChatIRC.LOGGER.error("Test mode not enabled - call enableTestMode() first");
            return false;
        }
        
        try {
            IRCConfig config = SyncChatIRC.getConfig();
            mockClient.clearSentMessages();
            
            // Test chat message format
            String chatMsg = String.format(config.messageFormat, "TestPlayer", "Hello world!");
            mockClient.sendMessage(chatMsg);
            
            // Test join message format
            String joinMsg = String.format(config.joinFormat, "TestPlayer");
            mockClient.sendMessage(joinMsg);
            
            // Test leave message format
            String leaveMsg = String.format(config.leaveFormat, "TestPlayer");
            mockClient.sendMessage(leaveMsg);
            
            // Test death message format
            String deathMsg = String.format(config.deathFormat, "TestPlayer was killed by a zombie");
            mockClient.sendMessage(deathMsg);
            
            // Test advancement message format
            String advancementMsg = String.format(config.advancementFormat, "TestPlayer", "Stone Age");
            mockClient.sendMessage(advancementMsg);
            
            if (mockClient.getMessageCount() != 5) {
                SyncChatIRC.LOGGER.error("Format test failed: Expected 5 messages, got {}", mockClient.getMessageCount());
                return false;
            }
            
            SyncChatIRC.LOGGER.info("Format test passed! Sent {} messages:", mockClient.getMessageCount());
            for (String msg : mockClient.getSentMessages()) {
                SyncChatIRC.LOGGER.info("  - {}", msg);
            }
            
            return true;
            
        } catch (Exception e) {
            SyncChatIRC.LOGGER.error("Format test failed with exception", e);
            return false;
        }
    }
    
    /**
     * Test connection failure handling.
     */
    public static boolean runConnectionFailureTest() {
        if (mockClient == null) {
            SyncChatIRC.LOGGER.error("Test mode not enabled - call enableTestMode() first");
            return false;
        }
        
        try {
            // Simulate connection failure
            mockClient.setConnectionFailure(true);
            mockClient.connect().join();
            
            if (mockClient.isConnected()) {
                SyncChatIRC.LOGGER.error("Connection failure test failed: Should not be connected");
                return false;
            }
            
            // Try to send a message while disconnected
            int initialCount = mockClient.getMessageCount();
            mockClient.sendMessage("This should not be sent");
            
            if (mockClient.getMessageCount() != initialCount) {
                SyncChatIRC.LOGGER.error("Connection failure test failed: Message sent while disconnected");
                return false;
            }
            
            SyncChatIRC.LOGGER.info("Connection failure test passed!");
            return true;
            
        } catch (Exception e) {
            SyncChatIRC.LOGGER.error("Connection failure test failed with exception", e);
            return false;
        }
    }
    
    /**
     * Run all tests.
     */
    public static boolean runAllTests() {
        SyncChatIRC.LOGGER.info("Running all IRC tests...");
        
        enableTestMode();
        
        boolean basicTest = runBasicTest();
        boolean formatTest = runFormatTest();
        boolean connectionTest = runConnectionFailureTest();
        
        boolean allPassed = basicTest && formatTest && connectionTest;
        
        if (allPassed) {
            SyncChatIRC.LOGGER.info("All IRC tests passed! ✓");
        } else {
            SyncChatIRC.LOGGER.error("Some IRC tests failed! ✗");
        }
        
        return allPassed;
    }
}
