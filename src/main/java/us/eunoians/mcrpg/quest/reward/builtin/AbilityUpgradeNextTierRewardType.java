package us.eunoians.mcrpg.quest.reward.builtin;

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
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Built-in reward type that upgrades a {@link TierableAbility} to its next tier when granted.
 * Intended for use with repeatable, generic upgrade quests (one per ability).
 *
 * <pre>
 * type: mcrpg:ability_upgrade_next_tier
 * ability: mcrpg:enhanced_bleed
 * </pre>
 * <p>
 * Display label resolution order:
 * <ol>
 *     <li>Auto-derived quest-scoped / template-scoped localization route</li>
 *     <li>Inline {@code display.rewards.<label>} from the quest's {@code display:} block,
 *         resolved with the {@code <ability>} placeholder</li>
 *     <li>{@link LocalizationKey#QUEST_REWARD_ABILITY_UPGRADE_NEXT_TIER_FORMAT} type-level
 *         format template (configurable globally in {@code en_quest.yml})</li>
 * </ol>
 */
public class AbilityUpgradeNextTierRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_upgrade_next_tier");

    private final NamespacedKey abilityKey;
    @Nullable
    private final Route localizationRoute;
    @NotNull
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AbilityUpgradeNextTierRewardType() {
        this.abilityKey = null;
        this.localizationRoute = null;
        this.displayLabel = "";
    }

    private AbilityUpgradeNextTierRewardType(@Nullable NamespacedKey abilityKey,
                                             @Nullable Route localizationRoute,
                                             @NotNull String displayLabel) {
        this.abilityKey = abilityKey;
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
    public AbilityUpgradeNextTierRewardType parseConfig(@NotNull Section section) {
        String abilityKeyStr = section.getString("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        return new AbilityUpgradeNextTierRewardType(parsedKey, null, "");
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public AbilityUpgradeNextTierRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String abilityKeyStr = (String) config.get("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyStr);
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        String label = config.getOrDefault("display", "").toString();
        return new AbilityUpgradeNextTierRewardType(parsedKey, route, label);
    }

    @Override
    public void grant(@NotNull Player player) {
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        if (abilityKey == null || !abilityRegistry.registered(abilityKey)) {
            McRPG.getInstance().getLogger().warning("[AbilityUpgradeNextTierReward] Ability not found: " + abilityKey);
            return;
        }

        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        if (!(ability instanceof TierableAbility tierableAbility)) {
            McRPG.getInstance().getLogger().warning("[AbilityUpgradeNextTierReward] Ability " + abilityKey + " is not tierable");
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
    public String describeForDisplay() {
        String abilityName = abilityKey != null ? formatAbilityName(abilityKey.getKey()) : "Unknown";
        return "Upgrade: " + abilityName + " (Next Tier)";
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
            label = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_ABILITY_UPGRADE_NEXT_TIER_FORMAT, vars);
            return prependDefaultColor(localization, player, label);
        } catch (Exception ignored) {
            return describeForDisplay();
        }
    }

    @NotNull
    @Override
    public AbilityUpgradeNextTierRewardType withLocalizationRoute(@NotNull Route route) {
        return new AbilityUpgradeNextTierRewardType(abilityKey, route, displayLabel);
    }

    @NotNull
    @Override
    public AbilityUpgradeNextTierRewardType withInlineDisplayLabel(@NotNull String label) {
        return new AbilityUpgradeNextTierRewardType(abilityKey, localizationRoute, label);
    }

    /**
     * Builds the placeholder variable map for MiniMessage resolution without a player context.
     * Falls back to title-casing the registry key when no player is available.
     * Keys match the placeholders documented in {@code en_quest.yml}: {@code <ability>}.
     */
    @NotNull
    private Map<String, String> rewardVars() {
        String abilityName = abilityKey != null ? formatAbilityName(abilityKey.getKey()) : "Unknown";
        return Map.of("ability", abilityName);
    }

    /**
     * Builds the placeholder variable map for MiniMessage resolution with a player context.
     * Looks up the {@link us.eunoians.mcrpg.ability.Ability} from the registry and calls
     * {@link us.eunoians.mcrpg.ability.Ability#getColoredName(McRPGPlayer)} so the ability name
     * carries its type-color and is self-closing.
     * Falls back to title-casing the registry key if the ability is not registered.
     *
     * @param player The player whose locale chain is used for name resolution.
     * @return A map containing {@code ability} (colored name).
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
        return Map.of("ability", abilityName);
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
        if (!displayLabel.isEmpty()) {
            map.put("display", displayLabel);
        }
        if (localizationRoute != null) {
            map.put("localization-route", localizationRoute.join('.'));
        }
        return map;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}


