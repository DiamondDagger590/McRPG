package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages the {@link AbilityUpgradeQuestAttribute} in response to quest lifecycle events.
 * <p>
 * When an upgrade quest is cancelled, this listener clears the attribute so that the next
 * sanity check trigger (login, level-up, GUI open) can re-start the quest if the player
 * is still eligible.
 * <p>
 * When an upgrade quest completes, this listener clears the attribute. The actual tier
 * upgrade and cascading next-tier check are handled by
 * {@link us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType}.
 */
public class AbilityUpgradeQuestListener implements Listener {

    /**
     * Clears the {@link AbilityUpgradeQuestAttribute} for all in-scope players when
     * an upgrade quest is cancelled by an admin or the system.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestCancel(@NotNull QuestCancelEvent event) {
        clearUpgradeQuestAttribute(event.getQuestInstance());
    }

    /**
     * Clears the {@link AbilityUpgradeQuestAttribute} when an upgrade quest completes.
     * The reward type handles the actual tier upgrade and next-tier cascade separately.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestComplete(@NotNull QuestCompleteEvent event) {
        clearUpgradeQuestAttribute(event.getQuestInstance());
    }

    /**
     * Iterates all in-scope players and clears any {@link AbilityUpgradeQuestAttribute}
     * that references the given quest instance UUID.
     */
    private void clearUpgradeQuestAttribute(@NotNull QuestInstance questInstance) {
        UUID questUUID = questInstance.getQuestUUID();

        questInstance.getQuestScope().ifPresent(scope -> {
            McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
            AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.ABILITY);

            for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(playerUUID);
                if (mcRPGPlayerOpt.isEmpty()) {
                    continue;
                }

                AbilityHolder abilityHolder = mcRPGPlayerOpt.get().asSkillHolder();
                for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
                    Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
                    if (!(ability instanceof TierableAbility)) {
                        continue;
                    }

                    Optional<AbilityData> abilityDataOpt = abilityHolder.getAbilityData(ability);
                    if (abilityDataOpt.isEmpty()) {
                        continue;
                    }

                    AbilityData abilityData = abilityDataOpt.get();
                    abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE)
                            .ifPresent(attr -> {
                                if (attr instanceof AbilityUpgradeQuestAttribute questAttr
                                        && questAttr.getContent().equals(questUUID)) {
                                    abilityData.addAttribute(new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID()));
                                }
                            });
                }
            }
        });
    }
}
