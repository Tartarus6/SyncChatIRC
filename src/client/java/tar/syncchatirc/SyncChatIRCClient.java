package tar.syncchatirc;

import net.fabricmc.api.ClientModInitializer;

public class SyncChatIRCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Client-side initialization
		SyncChatIRC.LOGGER.info("SyncChatIRC client initialized!");
	}
}