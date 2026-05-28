package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;

/**
 * Built-in objective type for tracking ability activation events.
 * <p>
 * Supports three filter modes (applied in priority order):
 * <ol>
 *   <li>Specific ability — {@code ability} config key with a namespaced key value</li>
 *   <li>Ability type — {@code ability-type} config key with {@code ACTIVE}, {@code PASSIVE},
 *       or {@code INNATE}</li>
 *   <li>No filter — any activation counts</li>
 * </ol>
 * Because activation is a real-time event, this type does not support auto-complete.
 */
public class AbilityActivateObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_activate");

    @NotNull
    private final AbilityObjectiveFilter filter;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public AbilityActivateObjectiveType() {
        this.filter = AbilityObjectiveFilter.EMPTY;
    }

    private AbilityActivateObjectiveType(@NotNull AbilityObjectiveFilter filter) {
        this.filter = filter;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public AbilityActivateObjectiveType parseConfig(@NotNull Section section) {
        AbilityType typeFilter = null;
        if (section.contains("ability-type")) {
            String rawType = section.getString("ability-type");
            typeFilter = AbilityType.fromString(rawType).orElse(null);
            if (typeFilter == null) {
                McRPG.getInstance().getLogger().warning(
                        "Invalid ability-type '" + rawType + "' in " + KEY + " objective config — objective will never match");
                return new AbilityActivateObjectiveType(AbilityObjectiveFilter.NEVER_MATCH);
            }
        }
        NamespacedKey specificFilter = null;
        if (section.contains("ability")) {
            String rawAbility = section.getString("ability");
            specificFilter = NamespacedKey.fromString(rawAbility);
            if (specificFilter == null) {
                McRPG.getInstance().getLogger().warning(
                        "Invalid ability key '" + rawAbility + "' in " + KEY + " objective config — objective will never match");
                return new AbilityActivateObjectiveType(AbilityObjectiveFilter.NEVER_MATCH);
            }
        }
        return new AbilityActivateObjectiveType(new AbilityObjectiveFilter(specificFilter, typeFilter));
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof AbilityActivateQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof AbilityActivateQuestContext activateContext)) {
            return 0;
        }
        return filter.matchesAbility(activateContext.getAbility()) ? 1 : 0;
    }

    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (filter.getAbilityFilter().isPresent()) {
            String abilityName = filter.resolveAbilityName(filter.getAbilityFilter().get());
            return localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ABILITY_ACTIVATE_SPECIFIC,
                    Map.of("count", count, "ability", abilityName));
        }
        if (filter.getAbilityTypeFilter().isPresent()) {
            AbilityType type = filter.getAbilityTypeFilter().get();
            switch (type) {
                case PASSIVE -> {
                    return localization.getLocalizedMessage(player,
                            LocalizationKey.QUEST_OBJECTIVE_ABILITY_ACTIVATE_PASSIVE,
                            Map.of("count", count));
                }
                case ACTIVE -> {
                    return localization.getLocalizedMessage(player,
                            LocalizationKey.QUEST_OBJECTIVE_ABILITY_ACTIVATE_ACTIVE,
                            Map.of("count", count));
                }
                case INNATE -> {
                    return localization.getLocalizedMessage(player,
                            LocalizationKey.QUEST_OBJECTIVE_ABILITY_ACTIVATE_INNATE,
                            Map.of("count", count));
                }
                default -> McRPG.getInstance().getLogger().warning(
                        "Unhandled AbilityType '" + type + "' in " + KEY + " describeObjective — falling through to 'any' description");
            }
        }
        return localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_ABILITY_ACTIVATE_ANY,
                Map.of("count", count));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
