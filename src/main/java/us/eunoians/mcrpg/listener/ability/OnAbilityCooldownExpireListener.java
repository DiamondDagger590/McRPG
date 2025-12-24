package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityCooldownExpireEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

/**
 * This listener handles notifying the player whenever their cooldown
 * is expired.
 */
public class OnAbilityCooldownExpireListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleExpire(AbilityCooldownExpireEvent abilityCooldownExpireEvent) {
        var playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(abilityCooldownExpireEvent.getAbilityHolder().getUUID());
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            Audience audience = mcRPGPlayer.getAsBukkitPlayer().get();
            audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer,
                    LocalizationKey.ABILITY_NO_LONGER_ON_COOLDOWN,
                    Map.of(AbilityItemPlaceholderKeys.ABILITY.getKey(), abilityCooldownExpireEvent.getAbility().getName(mcRPGPlayer))));
        }
    }
}
