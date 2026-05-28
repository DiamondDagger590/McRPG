package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Built-in objective type for tracking ability unlock events.
 * <p>
 * Supports three filter modes (applied in priority order):
 * <ol>
 *   <li>Specific ability — {@code ability} config key with a namespaced key value</li>
 *   <li>Ability type — {@code ability-type} config key with {@code PASSIVE} or {@code ACTIVE}</li>
 *   <li>No filter — any unlock counts</li>
 * </ol>
 * Supports auto-complete for players who already have the matching ability unlocked when the quest starts.
 */
public class AbilityUnlockObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_unlock");

    @NotNull
    private final AbilityObjectiveFilter filter;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AbilityUnlockObjectiveType() {
        this.filter = AbilityObjectiveFilter.EMPTY;
    }

    private AbilityUnlockObjectiveType(@NotNull AbilityObjectiveFilter filter) {
        this.filter = filter;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public AbilityUnlockObjectiveType parseConfig(@NotNull Section section) {
        AbilityType typeFilter = null;
        if (section.contains("ability-type")) {
            String rawType = section.getString("ability-type");
            typeFilter = AbilityType.fromString(rawType).orElse(null);
            if (typeFilter == null) {
                McRPG.getInstance().getLogger().warning(
                        "Invalid ability-type '" + rawType + "' in " + KEY + " objective config — objective will never match");
                return new AbilityUnlockObjectiveType(AbilityObjectiveFilter.NEVER_MATCH);
            }
        }
        NamespacedKey specificFilter = null;
        if (section.contains("ability")) {
            String rawAbility = section.getString("ability");
            specificFilter = NamespacedKey.fromString(rawAbility);
            if (specificFilter == null) {
                McRPG.getInstance().getLogger().warning(
                        "Invalid ability key '" + rawAbility + "' in " + KEY + " objective config — objective will never match");
                return new AbilityUnlockObjectiveType(AbilityObjectiveFilter.NEVER_MATCH);
            }
        }
        return new AbilityUnlockObjectiveType(new AbilityObjectiveFilter(specificFilter, typeFilter));
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof AbilityUnlockQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof AbilityUnlockQuestContext unlockContext)) {
            return 0;
        }
        return filter.matchesAbility(unlockContext.getAbility()) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks whether the player already has any matching ability unlocked when the quest starts.
     */
    @NotNull
    @Override
    public OptionalLong checkAutoComplete(@NotNull UUID playerUUID) {
        var playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);
        Optional<McRPGPlayer> playerOpt = playerManager.getPlayer(playerUUID);
        if (playerOpt.isEmpty()) {
            return OptionalLong.empty();
        }
        var abilityHolder = playerOpt.get().asSkillHolder();
        // Short-circuit: when filtering for a specific ability, only check that one
        if (filter.getAbilityFilter().isPresent()) {
            NamespacedKey targetKey = filter.getAbilityFilter().get();
            boolean unlocked = abilityHolder.getAbilityData(targetKey)
                    .flatMap(data -> data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                    .filter(attr -> attr instanceof AbilityUnlockedAttribute)
                    .map(attr -> (AbilityUnlockedAttribute) attr)
                    .map(AbilityUnlockedAttribute::getContent)
                    .orElse(false);
            return unlocked ? OptionalLong.of(1) : OptionalLong.empty();
        }
        var abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        for (NamespacedKey abilityKey : abilityHolder.getAvailableAbilities()) {
            var ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (!(ability instanceof UnlockableAbility unlockable)) {
                continue;
            }
            if (!filter.matchesAbility(unlockable)) {
                continue;
            }
            boolean unlocked = abilityHolder.getAbilityData(abilityKey)
                    .flatMap(data -> data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                    .filter(attr -> attr instanceof AbilityUnlockedAttribute)
                    .map(attr -> (AbilityUnlockedAttribute) attr)
                    .map(AbilityUnlockedAttribute::getContent)
                    .orElse(false);
            if (unlocked) {
                return OptionalLong.of(1);
            }
        }
        return OptionalLong.empty();
    }

    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        if (filter.getAbilityFilter().isPresent()) {
            String abilityName = filter.resolveAbilityName(filter.getAbilityFilter().get());
            return localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ABILITY_UNLOCK_SPECIFIC,
                    Map.of("ability", abilityName));
        }
        if (filter.getAbilityTypeFilter().isPresent()) {
            AbilityType type = filter.getAbilityTypeFilter().get();
            switch (type) {
                case PASSIVE -> {
                    return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ABILITY_UNLOCK_PASSIVE);
                }
                case ACTIVE -> {
                    return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ABILITY_UNLOCK_ACTIVE);
                }
                case INNATE -> {
                    return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ABILITY_UNLOCK_INNATE);
                }
                default -> McRPG.getInstance().getLogger().warning(
                        "Unhandled AbilityType '" + type + "' in " + KEY + " describeObjective — falling through to 'any' description");
            }
        }
        return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ABILITY_UNLOCK_ANY);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
