package tar.syncchatirc.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tar.syncchatirc.SyncChatIRC;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    
    @Shadow
    private ServerPlayerEntity owner;
    
    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void onAdvancementGranted(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        // Only proceed if the method returned true (advancement was actually granted)
        if (cir.getReturnValue() && SyncChatIRC.getIrcClient() != null && SyncChatIRC.getConfig().relayAdvancements) {
            // Check if this advancement should be announced
            AdvancementDisplay display = advancement.value().display().orElse(null);
            if (display != null && display.shouldAnnounceToChat()) {
                String playerName = owner.getDisplayName().getString();
                String advancementTitle = display.getTitle().getString();
                
                String formattedMessage = String.format(SyncChatIRC.getConfig().advancementFormat, playerName, advancementTitle);
                SyncChatIRC.getIrcClient().sendMessage(formattedMessage);
                SyncChatIRC.LOGGER.debug("MC -> IRC: {} earned advancement: {}", playerName, advancementTitle);
            }
        }
    }
}
