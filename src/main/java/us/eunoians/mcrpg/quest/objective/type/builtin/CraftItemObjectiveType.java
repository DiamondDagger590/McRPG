package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
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
 * Built-in objective type for tracking item crafting progress.
 * <p>
 * Supports both vanilla items and custom items from plugins integrated via McCore's
 * {@link CustomItemWrapper}. Config entries under {@code items} can be vanilla material
 * names (e.g. {@code DIAMOND_SWORD}) or custom item identifiers from supported plugins.
 * If the list is empty, any crafted item counts toward progress.
 * <p>
 * Handles shift-click crafting by computing the maximum number of items that can be
 * crafted from the available ingredients in the crafting matrix.
 */
public class CraftItemObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "craft_item");

    private final Set<CustomItemWrapper> validItems;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public CraftItemObjectiveType() {
        this.validItems = Set.of();
    }

    /**
     * Creates a configured instance with the specified valid items.
     *
     * @param validItems the set of items that count toward progress
     */
    private CraftItemObjectiveType(@NotNull Set<CustomItemWrapper> validItems) {
        this.validItems = validItems;
    }

    /**
     * {@inheritDoc}
     *
     * @return the namespaced key {@code mcrpg:craft_item}
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
    public CraftItemObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomItemWrapper> items = Set.of();
        if (section.contains("items")) {
            items = section.getStringList("items").stream()
                    .map(CustomItemWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new CraftItemObjectiveType(items);
    }

    /**
     * {@inheritDoc}
     *
     * @param context the progress context from an event
     * @return {@code true} if the context is a {@link CraftItemQuestContext}
     */
    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof CraftItemQuestContext;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles shift-click crafting by computing the maximum number of crafts from the
     * crafting matrix, multiplied by the recipe result amount. For normal clicks, the
     * delta is the recipe result amount.
     *
     * @param instance the objective instance to potentially progress
     * @param context  the progress context from a craft event
     * @return the number of items crafted (0 if the item doesn't match)
     */
    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof CraftItemQuestContext craftContext)) {
            return 0;
        }

        var event = craftContext.getCraftItemEvent();
        CustomItemWrapper craftedItem = new CustomItemWrapper(event.getRecipe().getResult());

        if (!validItems.isEmpty() && !validItems.contains(craftedItem)) {
            return 0;
        }

        int resultAmount = event.getRecipe().getResult().getAmount();

        if (event.isShiftClick()) {
            int maxCrafts = Integer.MAX_VALUE;
            ItemStack[] matrix = event.getInventory().getMatrix();
            for (ItemStack slot : matrix) {
                if (slot != null && !slot.getType().isAir()) {
                    maxCrafts = Math.min(maxCrafts, slot.getAmount());
                }
            }
            if (maxCrafts == Integer.MAX_VALUE) {
                maxCrafts = 1;
            }
            return (long) maxCrafts * resultAmount;
        }

        return resultAmount;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain.
     *
     * @param player           the player whose locale chain determines the language
     * @param requiredProgress the amount of progress needed to complete the objective
     * @return a descriptive string for crafting objectives
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validItems.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_CRAFT_ITEM_ANY,
                    Map.of("count", count));
        }
        if (validItems.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_CRAFT_ITEM_SINGLE,
                    Map.of("count", count, "item", validItems.iterator().next().itemName()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_CRAFT_ITEM_MULTI_HEADER, Map.of("count", count)));
        for (CustomItemWrapper item : validItems) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_CRAFT_ITEM_MULTI_ITEM, Map.of("item", item.itemName())));
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
