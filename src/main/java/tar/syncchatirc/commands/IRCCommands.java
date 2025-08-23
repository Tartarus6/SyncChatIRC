package tar.syncchatirc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import tar.syncchatirc.SyncChatIRC;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class IRCCommands {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerIRCCommand(dispatcher);
        });
    }
    
    private static void registerIRCCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("irc")
            .requires(source -> source.hasPermissionLevel(2)) // Require OP level
            .then(literal("status")
                .executes(IRCCommands::getStatus))
            .then(literal("connect")
                .executes(IRCCommands::connect))
            .then(literal("disconnect")
                .executes(IRCCommands::disconnect))
            .then(literal("send")
                .then(argument("message", StringArgumentType.greedyString())
                    .executes(IRCCommands::sendMessage)))
            .then(literal("reload")
                .executes(IRCCommands::reloadConfig))
        );
    }
    
    private static int getStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (SyncChatIRC.getIrcClient() == null) {
            source.sendFeedback(() -> Text.literal("IRC client is not initialized"), false);
            return 0;
        }
        
        boolean connected = SyncChatIRC.getIrcClient().isConnected();
        String status = connected ? "Connected" : "Disconnected";
        String server = SyncChatIRC.getConfig().server + ":" + SyncChatIRC.getConfig().port;
        String channel = SyncChatIRC.getConfig().channel;
        
        source.sendFeedback(() -> Text.literal(String.format(
            "IRC Status: %s\nServer: %s\nChannel: %s\nEnabled: %s", 
            status, server, channel, SyncChatIRC.getConfig().enabled
        )), false);
        
        return 1;
    }
    
    private static int connect(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (SyncChatIRC.getIrcClient() == null) {
            source.sendFeedback(() -> Text.literal("IRC client is not initialized"), false);
            return 0;
        }
        
        if (SyncChatIRC.getIrcClient().isConnected()) {
            source.sendFeedback(() -> Text.literal("IRC client is already connected"), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("Attempting to connect to IRC..."), false);
        SyncChatIRC.getIrcClient().connect().thenRun(() -> {
            source.sendFeedback(() -> Text.literal("IRC connection attempt completed"), false);
        });
        
        return 1;
    }
    
    private static int disconnect(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (SyncChatIRC.getIrcClient() == null) {
            source.sendFeedback(() -> Text.literal("IRC client is not initialized"), false);
            return 0;
        }
        
        if (!SyncChatIRC.getIrcClient().isConnected()) {
            source.sendFeedback(() -> Text.literal("IRC client is not connected"), false);
            return 0;
        }
        
        SyncChatIRC.getIrcClient().disconnect();
        source.sendFeedback(() -> Text.literal("Disconnected from IRC"), false);
        
        return 1;
    }
    
    private static int sendMessage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String message = StringArgumentType.getString(context, "message");
        
        if (SyncChatIRC.getIrcClient() == null) {
            source.sendFeedback(() -> Text.literal("IRC client is not initialized"), false);
            return 0;
        }
        
        if (!SyncChatIRC.getIrcClient().isConnected()) {
            source.sendFeedback(() -> Text.literal("IRC client is not connected"), false);
            return 0;
        }
        
        SyncChatIRC.getIrcClient().sendMessage(message);
        source.sendFeedback(() -> Text.literal("Message sent to IRC: " + message), false);
        
        return 1;
    }
    
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // This would require modifying the main class to support reloading
            source.sendFeedback(() -> Text.literal("Config reload not yet implemented"), false);
            return 0;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Failed to reload config: " + e.getMessage()), false);
            return 0;
        }
    }
}
