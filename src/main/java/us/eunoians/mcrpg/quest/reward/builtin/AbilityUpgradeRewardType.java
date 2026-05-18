package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.McRPG;
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
 * <p>
 * Display label resolution order:
 * <ol>
 *     <li>Auto-derived quest-scoped / template-scoped localization route</li>
 *     <li>Inline {@code display.rewards.<label>} from the quest's {@code display:} block,
 *         resolved with {@code <ability>} and {@code <tier>} placeholders</li>
 *     <li>{@link LocalizationKey#QUEST_REWARD_ABILITY_UPGRADE_FORMAT} type-level format
 *         template with the same placeholders (configurable globally in {@code en_quest.yml})</li>
 * </ol>
 */
public class AbilityUpgradeRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_upgrade");

    private final NamespacedKey abilityKey;
    private final int targetTier;
    @Nullable
    private final Route localizationRoute;
    @NotNull
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AbilityUpgradeRewardType() {
        this.abilityKey = null;
        this.targetTier = 0;
        this.localizationRoute = null;
        this.displayLabel = "";
    }

    private AbilityUpgradeRewardType(@Nullable NamespacedKey abilityKey, int targetTier,
                                     @Nullable Route localizationRoute, @NotNull String displayLabel) {
        this.abilityKey = abilityKey;
        this.targetTier = targetTier;
        this.localizationRoute = localizationRoute;
        this.displayLabel = displayLabel;
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
        return new AbilityUpgradeRewardType(parsedKey, tier, null, "");
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public AbilityUpgradeRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String abilityKeyStr = (String) config.get("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        int tier = config.containsKey("tier") ? ((Number) config.get("tier")).intValue() : 1;
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        String label = config.getOrDefault("display", "").toString();
        return new AbilityUpgradeRewardType(parsedKey, tier, route, label);
    }

    @Override
    public void grant(@NotNull Player player) {
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(abilityKey)) {
            McRPG.getInstance().getLogger().warning("[AbilityUpgradeReward] Ability not found: " + abilityKey);
            return;
        }

        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        if (!(ability instanceof TierableAbility tierableAbility)) {
            McRPG.getInstance().getLogger().warning("[AbilityUpgradeReward] Ability " + abilityKey + " is not tierable");
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
    public String describeForDisplay() {
        String abilityName = abilityKey != null ? formatAbilityName(abilityKey.getKey()) : "Unknown";
        return "Upgrade: " + abilityName + " (Tier " + targetTier + ")";
    }

    @NotNull
    @Override
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Map<String, String> vars = rewardVars(player);
        String label;

        if (localizationRoute != null) {
            try {
                label = localization.getLocalizedMessage(player, localizationRoute);
                return prependDefaultColor(localization, player, label);
            } catch (Exception ignored) {
                // Fall through to inline display label
            }
        }
        if (!displayLabel.isEmpty()) {
            label = localization.getLocalizedMessage(displayLabel, vars);
            return prependDefaultColor(localization, player, label);
        }
        try {
            label = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_ABILITY_UPGRADE_FORMAT, vars);
            return prependDefaultColor(localization, player, label);
        } catch (Exception ignored) {
            return describeForDisplay();
        }
    }

    @NotNull
    @Override
    public AbilityUpgradeRewardType withLocalizationRoute(@NotNull Route route) {
        return new AbilityUpgradeRewardType(abilityKey, targetTier, route, displayLabel);
    }

    @NotNull
    @Override
    public AbilityUpgradeRewardType withInlineDisplayLabel(@NotNull String label) {
        return new AbilityUpgradeRewardType(abilityKey, targetTier, localizationRoute, label);
    }

    /**
     * Builds the placeholder variable map for MiniMessage resolution without a player context.
     * Falls back to title-casing the registry key when no player is available.
     * Keys match the placeholders documented in {@code en_quest.yml}:
     * {@code <ability>} and {@code <tier>}.
     */
    @NotNull
    private Map<String, String> rewardVars() {
        String abilityName = abilityKey != null ? formatAbilityName(abilityKey.getKey()) : "Unknown";
        return Map.of("ability", abilityName, "tier", String.valueOf(targetTier));
    }

    /**
     * Builds the placeholder variable map for MiniMessage resolution with a player context.
     * Looks up the {@link Ability} from the registry and calls {@link Ability#getColoredName(McRPGPlayer)}
     * so the ability name carries its type-color and is self-closing.
     * Falls back to title-casing the registry key if the ability is not registered.
     *
     * @param player The player whose locale chain is used for name resolution.
     * @return A map containing {@code ability} (colored name) and {@code tier}.
     */
    @NotNull
    private Map<String, String> rewardVars(@NotNull McRPGPlayer player) {
        String abilityName;
        if (abilityKey != null) {
            AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
            if (abilityRegistry.registered(abilityKey)) {
                abilityName = abilityRegistry.getRegisteredAbility(abilityKey).getColoredName(player);
            } else {
                abilityName = formatAbilityName(abilityKey.getKey());
            }
        } else {
            abilityName = "Unknown";
        }
        return Map.of("ability", abilityName, "tier", String.valueOf(targetTier));
    }

    private String formatAbilityName(@NotNull String raw) {
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("ability", abilityKey != null ? abilityKey.toString() : "");
        map.put("tier", targetTier);
        if (!displayLabel.isEmpty()) {
            map.put("display", displayLabel);
        }
        if (localizationRoute != null) {
            map.put("localization-route", localizationRoute.join('.'));
        }
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
                McRPG.getInstance().getLogger().log(Level.SEVERE,
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

