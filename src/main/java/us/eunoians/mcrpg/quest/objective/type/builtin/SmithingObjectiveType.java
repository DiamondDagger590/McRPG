package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
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
 * Built-in objective type for tracking smithing table usage progress.
 * <p>
 * Supports both vanilla items and custom items from plugins integrated via McCore's
 * {@link CustomItemWrapper}. Config entries under {@code items} can be vanilla material
 * names (e.g. {@code NETHERITE_SWORD}) or custom item identifiers from supported plugins.
 * If the list is empty, any smithed item counts toward progress.
 */
public class SmithingObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "smithing");

    private final Set<CustomItemWrapper> validItems;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public SmithingObjectiveType() {
        this.validItems = Set.of();
    }

    /**
     * Creates a configured instance with the specified valid items.
     *
     * @param validItems the set of items that count toward progress
     */
    private SmithingObjectiveType(@NotNull Set<CustomItemWrapper> validItems) {
        this.validItems = validItems;
    }

    /**
     * {@inheritDoc}
     *
     * @return the namespaced key {@code mcrpg:smithing}
     */
    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reads the {@code items} list from the section, parsing each entry as a
     * {@link CustomItemWrapper}.
     *
     * @param section the BoostedYaml section containing type-specific data
     * @return a new configured instance with the parsed item set
     */
    @NotNull
    @Override
    public SmithingObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomItemWrapper> items = Set.of();
        if (section.contains("items")) {
            items = section.getStringList("items").stream()
                    .map(CustomItemWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new SmithingObjectiveType(items);
    }

    /**
     * {@inheritDoc}
     *
     * @param context the progress context from an event
     * @return {@code true} if the context is a {@link SmithingQuestContext}
     */
    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof SmithingQuestContext;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Each smithing operation counts as 1 progress. The result item from the
     * {@link org.bukkit.event.inventory.SmithItemEvent} is matched against the
     * configured valid items set.
     *
     * @param instance the objective instance to potentially progress
     * @param context  the progress context from a smithing event
     * @return 1 if the result item matches, 0 otherwise
     */
    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof SmithingQuestContext smithContext)) {
            return 0;
        }

        if (validItems.isEmpty()) {
            return 1;
        }

        var event = smithContext.getSmithItemEvent();
        if (event.getCurrentItem() == null) {
            return 0;
        }

        CustomItemWrapper resultItem = new CustomItemWrapper(event.getCurrentItem());
        return validItems.contains(resultItem) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain.
     *
     * @param player           the player whose locale chain determines the language
     * @param requiredProgress the amount of progress needed to complete the objective
     * @return a descriptive string for smithing objectives
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validItems.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_SMITHING_ANY,
                    Map.of("count", count));
        }
        if (validItems.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_SMITHING_SINGLE,
                    Map.of("count", count, "item", validItems.iterator().next().itemName()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_SMITHING_MULTI_HEADER, Map.of("count", count)));
        for (CustomItemWrapper item : validItems) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_SMITHING_MULTI_ITEM, Map.of("item", item.itemName())));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @return the McRPG expansion key
     */
    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
