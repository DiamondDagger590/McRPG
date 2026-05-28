package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
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
 * Built-in objective type for tracking when a player equips an ability to their active loadout.
 * <p>
 * Supports three filter modes (applied in priority order):
 * <ol>
 *   <li>Specific ability — {@code ability} config key with a namespaced key value</li>
 *   <li>Ability type — {@code ability-type} config key with {@code PASSIVE} or {@code ACTIVE}</li>
 *   <li>No filter — any loadout equip counts</li>
 * </ol>
 * Supports auto-complete for players who already have a matching ability in their active loadout
 * when the quest starts.
 */
public class LoadoutEquipObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "loadout_equip");

    @NotNull
    private final AbilityObjectiveFilter filter;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public LoadoutEquipObjectiveType() {
        this.filter = AbilityObjectiveFilter.EMPTY;
    }

    private LoadoutEquipObjectiveType(@NotNull AbilityObjectiveFilter filter) {
        this.filter = filter;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public LoadoutEquipObjectiveType parseConfig(@NotNull Section section) {
        AbilityType typeFilter = null;
        if (section.contains("ability-type")) {
            String rawType = section.getString("ability-type");
            typeFilter = AbilityType.fromString(rawType).orElse(null);
            if (typeFilter == null) {
                McRPG.getInstance().getLogger().warning(
                        "Invalid ability-type '" + rawType + "' in " + KEY + " objective config — objective will never match");
                return new LoadoutEquipObjectiveType(AbilityObjectiveFilter.NEVER_MATCH);
            }
        }
        NamespacedKey specificFilter = null;
        if (section.contains("ability")) {
            String rawAbility = section.getString("ability");
            specificFilter = NamespacedKey.fromString(rawAbility);
            if (specificFilter == null) {
                McRPG.getInstance().getLogger().warning(
                        "Invalid ability key '" + rawAbility + "' in " + KEY + " objective config — objective will never match");
                return new LoadoutEquipObjectiveType(AbilityObjectiveFilter.NEVER_MATCH);
            }
        }
        return new LoadoutEquipObjectiveType(new AbilityObjectiveFilter(specificFilter, typeFilter));
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof LoadoutEquipQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof LoadoutEquipQuestContext equipContext)) {
            return 0;
        }
        var abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        Ability ability = abilityRegistry.getRegisteredAbility(equipContext.getAbilityKey());
        return filter.matchesAbility(ability) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks whether the player already has a matching ability in their active loadout
     * when the quest starts.
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
        LoadoutHolder loadoutHolder = playerOpt.get().asSkillHolder();
        var abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        for (NamespacedKey abilityKey : loadoutHolder.getLoadout().getAbilities()) {
            Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (filter.matchesAbility(ability)) {
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
                    LocalizationKey.QUEST_OBJECTIVE_LOADOUT_EQUIP_SPECIFIC,
                    Map.of("ability", abilityName));
        }
        if (filter.getAbilityTypeFilter().isPresent()) {
            AbilityType type = filter.getAbilityTypeFilter().get();
            switch (type) {
                case PASSIVE -> {
                    return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_LOADOUT_EQUIP_PASSIVE);
                }
                case ACTIVE -> {
                    return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_LOADOUT_EQUIP_ACTIVE);
                }
                case INNATE -> {
                    return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_LOADOUT_EQUIP_INNATE);
                }
                default -> McRPG.getInstance().getLogger().warning(
                        "Unhandled AbilityType '" + type + "' in " + KEY + " describeObjective — falling through to 'any' description");
            }
        }
        return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_LOADOUT_EQUIP_ANY);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
