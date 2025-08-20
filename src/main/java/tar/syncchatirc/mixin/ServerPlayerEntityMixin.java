package tar.syncchatirc.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tar.syncchatirc.SyncChatIRC;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	
	@Inject(at = @At("HEAD"), method = "onDeath")
	private void onPlayerDeath(DamageSource damageSource, CallbackInfo info) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		Text deathMessage = damageSource.getDeathMessage(player);
		
		if (SyncChatIRC.getIrcClient() != null && SyncChatIRC.getConfig().relayDeathMessages) {
			String formattedMessage = String.format(SyncChatIRC.getConfig().deathFormat, deathMessage.getString());
			SyncChatIRC.getIrcClient().sendMessage(formattedMessage);
			SyncChatIRC.LOGGER.debug("MC -> IRC: Death message: {}", deathMessage.getString());
		}
	}
}