package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Built-in reward type that upgrades a {@link TierableAbility} to its next tier when granted.
 * Intended for use with repeatable, generic upgrade quests (one per ability).
 *
 * <pre>
 * type: mcrpg:ability_upgrade_next_tier
 * ability: mcrpg:enhanced_bleed
 * </pre>
 */
public class AbilityUpgradeNextTierRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_upgrade_next_tier");

    private final NamespacedKey abilityKey;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AbilityUpgradeNextTierRewardType() {
        this.abilityKey = null;
    }

    private AbilityUpgradeNextTierRewardType(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public AbilityUpgradeNextTierRewardType parseConfig(@NotNull Section section) {
        String abilityKeyStr = section.getString("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        return new AbilityUpgradeNextTierRewardType(parsedKey);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public AbilityUpgradeNextTierRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String abilityKeyStr = (String) config.get("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        return new AbilityUpgradeNextTierRewardType(parsedKey);
    }

    @Override
    public void grant(@NotNull Player player) {
        Logger logger = player.getServer().getLogger();
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        if (abilityKey == null || !abilityRegistry.registered(abilityKey)) {
            logger.warning("[AbilityUpgradeNextTierReward] Ability not found: " + abilityKey);
            return;
        }

        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        if (!(ability instanceof TierableAbility tierableAbility)) {
            logger.warning("[AbilityUpgradeNextTierReward] Ability " + abilityKey + " is not tierable");
            return;
        }

        McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }

        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        Optional<AbilityData> abilityDataOpt = mcRPGPlayer.asSkillHolder().getAbilityData(ability);
        if (abilityDataOpt.isEmpty()) {
            return;
        }

        int currentTier = tierableAbility.getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        int targetTier = currentTier + 1;
        if (targetTier > tierableAbility.getMaxTier()) {
            return;
        }

        if (tierableAbility instanceof SkillAbility skillAbility) {
            int requiredLevel = tierableAbility.getUnlockLevelForTier(targetTier);
            Optional<Integer> currentLevel = mcRPGPlayer.asSkillHolder()
                    .getSkillHolderData(skillAbility.getSkillKey())
                    .map(data -> data.getCurrentLevel());
            if (currentLevel.isEmpty() || currentLevel.get() < requiredLevel) {
                return;
            }
        }

        AbilityData abilityData = abilityDataOpt.get();
        abilityData.updateAttribute(new AbilityTierAttribute(targetTier), targetTier);
        abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE).ifPresent(attr -> {
            if (attr instanceof AbilityUpgradeQuestAttribute) {
                abilityData.addAttribute(new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID()));
            }
        });

        // After upgrading, reuse the centralized sanity check logic to start the next tier quest if eligible.
        // This keeps the behavior consistent with AbilityUpgradeRewardType without duplicating its async start logic.
        var questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        questManager.sanityCheckUpgradeQuests(mcRPGPlayer);
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("ability", abilityKey != null ? abilityKey.toString() : "");
        return map;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}

