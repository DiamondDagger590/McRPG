package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
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
 * Built-in objective type for tracking item pickup progress.
 * <p>
 * Supports both vanilla items and custom items from plugins integrated via McCore's
 * {@link CustomItemWrapper}. Config entries under {@code items} can be vanilla material
 * names (e.g. {@code DIAMOND}) or custom item identifiers from supported plugins.
 * If the list is empty, any picked-up item counts toward progress.
 * <p>
 * The progress delta is the stack amount of the picked-up item, not a flat 1.
 */
public class ItemPickupObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "item_pickup");

    private final Set<CustomItemWrapper> validItems;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ItemPickupObjectiveType() {
        this.validItems = Set.of();
    }

    /**
     * Creates a configured instance with the specified valid items.
     *
     * @param validItems the set of items that count toward progress
     */
    private ItemPickupObjectiveType(@NotNull Set<CustomItemWrapper> validItems) {
        this.validItems = validItems;
    }

    /**
     * {@inheritDoc}
     *
     * @return the namespaced key {@code mcrpg:item_pickup}
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
    public ItemPickupObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomItemWrapper> items = Set.of();
        if (section.contains("items")) {
            items = section.getStringList("items").stream()
                    .map(CustomItemWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new ItemPickupObjectiveType(items);
    }

    /**
     * {@inheritDoc}
     *
     * @param context the progress context from an event
     * @return {@code true} if the context is a {@link ItemPickupQuestContext}
     */
    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof ItemPickupQuestContext;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The delta is the stack amount of the picked-up item, allowing bulk pickups
     * to count fully toward progress.
     *
     * @param instance the objective instance to potentially progress
     * @param context  the progress context from an item pickup event
     * @return the stack amount of the picked-up item (0 if the item doesn't match)
     */
    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof ItemPickupQuestContext pickupContext)) {
            return 0;
        }

        var event = pickupContext.getPickupEvent();
        int amount = event.getItem().getItemStack().getAmount();

        if (validItems.isEmpty()) {
            return amount;
        }

        CustomItemWrapper pickedUpItem = new CustomItemWrapper(event.getItem().getItemStack());
        return validItems.contains(pickedUpItem) ? amount : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain.
     *
     * @param player           the player whose locale chain determines the language
     * @param requiredProgress the amount of progress needed to complete the objective
     * @return a descriptive string for item pickup objectives
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validItems.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ITEM_PICKUP_ANY,
                    Map.of("count", count));
        }
        if (validItems.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_ITEM_PICKUP_SINGLE,
                    Map.of("count", count, "item", resolveItemDisplayName(validItems.iterator().next())));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_ITEM_PICKUP_MULTI_HEADER, Map.of("count", count)));
        for (CustomItemWrapper item : validItems) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_ITEM_PICKUP_MULTI_ITEM, Map.of("item", resolveItemDisplayName(item))));
        }
        return sb.toString();
    }

    /**
     * Resolves a human-readable display name from a {@link CustomItemWrapper}.
     *
     * @param wrapper the item wrapper to resolve
     * @return the custom item identifier or a formatted material name
     */
    @NotNull
    private String resolveItemDisplayName(@NotNull CustomItemWrapper wrapper) {
        return wrapper.customItem()
                .orElseGet(() -> wrapper.material()
                        .map(Material::name)
                        .map(name -> name.toLowerCase().replace('_', ' '))
                        .orElse("unknown"));
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
