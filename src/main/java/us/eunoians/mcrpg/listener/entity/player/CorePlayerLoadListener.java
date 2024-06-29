package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityCooldownAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.CooldownableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * A listener that handles things after the player has been loaded.
 */
public class CorePlayerLoadListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleLoad(PlayerLoadEvent event){
        CorePlayer corePlayer = event.getCorePlayer();
        if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            // Go through all abilities and restart the notification timer if the ability needs it
            for (NamespacedKey abilityKey : skillHolder.getAvailableAbilities()) {
                Ability ability = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(abilityKey);
                var abilityDataOptional = skillHolder.getAbilityData(abilityKey);
                if (abilityDataOptional.isPresent() && ability instanceof CooldownableAbility cooldownableAbility) {
                    AbilityData abilityData = abilityDataOptional.get();
                    var cooldownAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
                    if (cooldownAttributeOptional.isPresent()) {
                        AbilityCooldownAttribute attribute = (AbilityCooldownAttribute) cooldownAttributeOptional.get();
                        int diff = (int) (attribute.getContent() - System.currentTimeMillis());
                        if (attribute.shouldContentBeSaved() && diff > 0) {
                            skillHolder.startCooldownExpireNotificationTimer(abilityKey, diff);
                        }
                    }
                }
            }
        }
    }
}
