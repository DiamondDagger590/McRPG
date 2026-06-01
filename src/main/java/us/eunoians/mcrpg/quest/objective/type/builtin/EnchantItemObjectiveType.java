package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking item enchantment progress.
 * <p>
 * Supports dual filtering by both item type and enchantment type. Config entries under
 * {@code items} can be vanilla material names or custom item identifiers via McCore's
 * {@link CustomItemWrapper}. Config entries under {@code enchantments} are enchantment
 * key names (e.g. {@code sharpness}, {@code efficiency}).
 * <p>
 * If both filters are specified, both must match for the enchantment to count. If only
 * one filter is specified, only that filter is applied. If neither is specified, any
 * enchantment counts toward progress.
 */
public class EnchantItemObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "enchant_item");

    private final Set<CustomItemWrapper> validItems;
    private final Set<String> validEnchantments;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public EnchantItemObjectiveType() {
        this.validItems = Set.of();
        this.validEnchantments = Set.of();
    }

    /**
     * Creates a configured instance with the specified valid items and enchantments.
     *
     * @param validItems        the set of items that count toward progress
     * @param validEnchantments the set of enchantment key names that count toward progress
     */
    private EnchantItemObjectiveType(@NotNull Set<CustomItemWrapper> validItems,
                                     @NotNull Set<String> validEnchantments) {
        this.validItems = validItems;
        this.validEnchantments = validEnchantments;
    }

    /**
     * {@inheritDoc}
     *
     * @return the namespaced key {@code mcrpg:enchant_item}
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
     * {@link CustomItemWrapper}, and the {@code enchantments} list as enchantment
     * key name strings.
     *
     * @param section the BoostedYaml section containing type-specific data
     * @return a new configured instance with the parsed item and enchantment sets
     */
    @NotNull
    @Override
    public EnchantItemObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomItemWrapper> items = Set.of();
        if (section.contains("items")) {
            items = section.getStringList("items").stream()
                    .map(CustomItemWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        Set<String> enchantments = Set.of();
        if (section.contains("enchantments")) {
            enchantments = new HashSet<>(section.getStringList("enchantments"));
        }
        return new EnchantItemObjectiveType(items, enchantments);
    }

    /**
     * {@inheritDoc}
     *
     * @param context the progress context from an event
     * @return {@code true} if the context is an {@link EnchantItemQuestContext}
     */
    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof EnchantItemQuestContext;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks both item and enchantment filters. If valid items are configured, the
     * enchanted item must match. If valid enchantments are configured, at least one
     * of the applied enchantments must match. Both filters must pass when both are
     * specified.
     *
     * @param instance the objective instance to potentially progress
     * @param context  the progress context from an enchant item event
     * @return 1 if the enchantment matches, 0 otherwise
     */
    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof EnchantItemQuestContext enchantContext)) {
            return 0;
        }

        var event = enchantContext.getEnchantItemEvent();

        if (!validItems.isEmpty()) {
            CustomItemWrapper enchantedItem = new CustomItemWrapper(event.getItem());
            if (!validItems.contains(enchantedItem)) {
                return 0;
            }
        }

        if (!validEnchantments.isEmpty()) {
            boolean hasMatchingEnchantment = event.getEnchantsToAdd().keySet().stream()
                    .anyMatch(enchantment -> validEnchantments.contains(enchantment.getKey().getKey()));
            if (!hasMatchingEnchantment) {
                return 0;
            }
        }

        return 1;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain. The description
     * varies based on which filters are active:
     * <ul>
     *     <li>No filters: generic "enchant any item" description</li>
     *     <li>Items only: standard any/single/multi item pattern</li>
     *     <li>Enchantments only: enchantment-specific description</li>
     *     <li>Both: combined description using the item filter as primary</li>
     * </ul>
     *
     * @param player           the player whose locale chain determines the language
     * @param requiredProgress the amount of progress needed to complete the objective
     * @return a descriptive string for enchant item objectives
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);

        boolean hasItems = !validItems.isEmpty();
        boolean hasEnchantments = !validEnchantments.isEmpty();

        if (!hasItems && !hasEnchantments) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_ANY,
                    Map.of("count", count));
        }

        if (hasItems && !hasEnchantments) {
            return describeItemsOnly(player, count);
        }

        if (!hasItems) {
            return describeEnchantmentsOnly(player, count);
        }

        return describeBoth(player, count);
    }

    /**
     * Produces a description when only item filters are configured.
     *
     * @param player the player whose locale chain determines the language
     * @param count  the required progress as a string
     * @return the localized description
     */
    @NotNull
    private String describeItemsOnly(@NotNull McRPGPlayer player, @NotNull String count) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        if (validItems.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_SINGLE_ITEM,
                    Map.of("count", count, "item", validItems.iterator().next().itemName()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_MULTI_HEADER, Map.of("count", count)));
        for (CustomItemWrapper item : validItems) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_MULTI_ITEM, Map.of("item", item.itemName())));
        }
        return sb.toString();
    }

    /**
     * Produces a description when only enchantment filters are configured.
     *
     * @param player the player whose locale chain determines the language
     * @param count  the required progress as a string
     * @return the localized description
     */
    @NotNull
    private String describeEnchantmentsOnly(@NotNull McRPGPlayer player, @NotNull String count) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        if (validEnchantments.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_SINGLE_ENCHANTMENT,
                    Map.of("count", count, "enchantment", validEnchantments.iterator().next()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_MULTI_HEADER, Map.of("count", count)));
        for (String enchantment : validEnchantments) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_MULTI_ENCHANTMENT, Map.of("enchantment", enchantment)));
        }
        return sb.toString();
    }

    /**
     * Produces a description when both item and enchantment filters are configured.
     * Uses the item as the primary descriptor since it is the more visible filter.
     *
     * @param player the player whose locale chain determines the language
     * @param count  the required progress as a string
     * @return the localized description
     */
    @NotNull
    private String describeBoth(@NotNull McRPGPlayer player, @NotNull String count) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        if (validItems.size() == 1 && validEnchantments.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_SINGLE_BOTH,
                    Map.of("count", count,
                            "item", validItems.iterator().next().itemName(),
                            "enchantment", validEnchantments.iterator().next()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_MULTI_HEADER, Map.of("count", count)));
        for (CustomItemWrapper item : validItems) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ENCHANT_ITEM_MULTI_ITEM, Map.of("item", item.itemName())));
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
