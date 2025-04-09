package us.eunoians.mcrpg.listener.ability;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKeys;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityCooldownExpireEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

import java.util.Map;

/**
 * This listener handles notifying the player whenever their cooldown
 * is expired.
 */
public class OnAbilityCooldownExpireListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleExpire(AbilityCooldownExpireEvent abilityCooldownExpireEvent) {
        var playerOptional = McRPG.getInstance().getPlayerManager().getPlayer(abilityCooldownExpireEvent.getAbilityHolder().getUUID());
        if (playerOptional.isPresent() && playerOptional.get() instanceof McRPGPlayer mcRPGPlayer) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            McRPGLocalizationManager localizationManager = McRPG.getInstance().getLocalizationManager();
            Audience audience = McRPG.getInstance().getAdventure().player(mcRPGPlayer.getUUID());
            audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer,
                    LocalizationKeys.ABILITY_NO_LONGER_ON_COOLDOWN,
                    Map.of(AbilityItemPlaceholderKeys.ABILITY.getKey(), abilityCooldownExpireEvent.getAbility().getDisplayName(mcRPGPlayer))));
        }
    }
}
