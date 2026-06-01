package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking entity shearing progress.
 * <p>
 * Supports both vanilla entity types and custom entities from plugins integrated via McCore's
 * {@link CustomEntityWrapper}. Config entries under {@code entities} can be vanilla entity type
 * names (e.g. {@code SHEEP}) or custom entity identifiers from supported plugins. If the list
 * is empty, any entity shearing counts toward progress.
 */
public class ShearEntityObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "shear_entity");

    private final Set<CustomEntityWrapper> validEntities;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ShearEntityObjectiveType() {
        this.validEntities = Set.of();
    }

    private ShearEntityObjectiveType(@NotNull Set<CustomEntityWrapper> validEntities) {
        this.validEntities = validEntities;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public ShearEntityObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomEntityWrapper> entities = Set.of();
        if (section.contains("entities")) {
            entities = section.getStringList("entities").stream()
                    .map(CustomEntityWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new ShearEntityObjectiveType(entities);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof ShearEntityQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof ShearEntityQuestContext shearContext)) {
            return 0;
        }

        if (validEntities.isEmpty()) {
            return 1;
        }

        return validEntities.contains(shearContext.getEntityWrapper()) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain.
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validEntities.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_SHEAR_ENTITY_ANY,
                    Map.of("count", count));
        }
        if (validEntities.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_SHEAR_ENTITY_SINGLE,
                    Map.of("count", count, "entity", validEntities.iterator().next().entityName()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_SHEAR_ENTITY_MULTI_HEADER, Map.of("count", count)));
        for (CustomEntityWrapper entity : validEntities) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_SHEAR_ENTITY_MULTI_ITEM, Map.of("entity", entity.entityName())));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
