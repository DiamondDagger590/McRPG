package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.database.Database;
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
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Built-in reward type that upgrades a {@link TierableAbility} to a specific tier
 * when granted. After upgrading, this reward clears the player's
 * {@link AbilityUpgradeQuestAttribute} and cascades to check whether the next tier's
 * upgrade quest should be started.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:ability_upgrade
 * ability: mcrpg:enhanced_bleed
 * tier: 2
 * </pre>
 */
public class AbilityUpgradeRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_upgrade");

    private final NamespacedKey abilityKey;
    private final int targetTier;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AbilityUpgradeRewardType() {
        this.abilityKey = null;
        this.targetTier = 0;
    }

    private AbilityUpgradeRewardType(@NotNull NamespacedKey abilityKey, int targetTier) {
        this.abilityKey = abilityKey;
        this.targetTier = targetTier;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public AbilityUpgradeRewardType parseConfig(@NotNull Section section) {
        String abilityKeyStr = section.getString("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        int tier = section.getInt("tier", 1);
        return new AbilityUpgradeRewardType(parsedKey, tier);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public AbilityUpgradeRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String abilityKeyStr = (String) config.get("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        int tier = config.containsKey("tier") ? ((Number) config.get("tier")).intValue() : 1;
        return new AbilityUpgradeRewardType(parsedKey, tier);
    }

    @Override
    public void grant(@NotNull Player player) {
        Logger logger = player.getServer().getLogger();
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(abilityKey)) {
            logger.warning("[AbilityUpgradeReward] Ability not found: " + abilityKey);
            return;
        }

        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        if (!(ability instanceof TierableAbility tierableAbility)) {
            logger.warning("[AbilityUpgradeReward] Ability " + abilityKey + " is not tierable");
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

        AbilityData abilityData = abilityDataOpt.get();
        abilityData.updateAttribute(new AbilityTierAttribute(targetTier), targetTier);
        abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE).ifPresent(attr -> {
            if (attr instanceof AbilityUpgradeQuestAttribute questAttr) {
                abilityData.addAttribute(new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID()));
            }
        });

        cascadeNextTierCheck(player, mcRPGPlayer, tierableAbility);
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("ability", abilityKey != null ? abilityKey.toString() : "");
        map.put("tier", targetTier);
        return map;
    }

    /**
     * After upgrading, checks if the player is eligible for the next tier's upgrade quest
     * and auto-starts it if possible. Verifies the player meets the skill level requirement
     * for the next tier before proceeding.
     */
    private void cascadeNextTierCheck(@NotNull Player player,
                                      @NotNull McRPGPlayer mcRPGPlayer,
                                      @NotNull TierableAbility tierableAbility) {
        int nextTier = targetTier + 1;
        if (nextTier > tierableAbility.getMaxTier()) {
            return;
        }

        if (tierableAbility instanceof SkillAbility skillAbility) {
            int requiredLevel = tierableAbility.getUnlockLevelForTier(nextTier);
            Optional<Integer> currentLevel = mcRPGPlayer.asSkillHolder()
                    .getSkillHolderData(skillAbility.getSkillKey())
                    .map(data -> data.getCurrentLevel());
            if (currentLevel.isEmpty() || currentLevel.get() < requiredLevel) {
                return;
            }
        }

        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        Optional<QuestDefinition> nextDefOpt = questManager.resolveUpgradeQuestDefinition(tierableAbility, nextTier);
        if (nextDefOpt.isEmpty()) {
            return;
        }

        QuestDefinition nextDef = nextDefOpt.get();
        Database database = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                if (!questManager.canPlayerStartQuest(connection, player.getUniqueId(), nextDef)) {
                    return;
                }

                player.getServer().getScheduler().runTask(
                        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST).plugin(),
                        () -> startUpgradeQuest(player, mcRPGPlayer, tierableAbility, nextTier, nextDef, questManager));
            } catch (SQLException e) {
                player.getServer().getLogger().log(Level.SEVERE,
                        "[AbilityUpgradeReward] Failed to check next tier eligibility for " + player.getName(), e);
            }
        });
    }

    /**
     * Starts an upgrade quest for the given ability and associates it with the player's
     * ability data via {@link AbilityUpgradeQuestAttribute}.
     */
    private void startUpgradeQuest(@NotNull Player player,
                                   @NotNull McRPGPlayer mcRPGPlayer,
                                   @NotNull TierableAbility tierableAbility,
                                   int targetTier,
                                   @NotNull QuestDefinition definition,
                                   @NotNull QuestManager questManager) {
        questManager.startQuest(definition, player.getUniqueId(), Map.of("tier", targetTier), new AbilityUpgradeQuestSource()).ifPresent(instance ->
                mcRPGPlayer.asSkillHolder().getAbilityData(tierableAbility).ifPresent(abilityData ->
                        abilityData.addAttribute(new AbilityUpgradeQuestAttribute(instance.getQuestUUID()))));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
