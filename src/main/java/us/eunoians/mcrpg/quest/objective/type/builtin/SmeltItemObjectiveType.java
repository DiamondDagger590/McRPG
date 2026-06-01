package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
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

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking item smelting progress.
 * <p>
 * Tracks items extracted from furnaces via {@link org.bukkit.event.inventory.FurnaceExtractEvent}.
 * Config entries under {@code items} are vanilla material names (e.g. {@code IRON_INGOT}).
 * If the list is empty, any smelted item counts toward progress.
 */
public class SmeltItemObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "smelt_item");

    private final Set<Material> validMaterials;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public SmeltItemObjectiveType() {
        this.validMaterials = Set.of();
    }

    /**
     * Creates a configured instance with the specified valid materials.
     *
     * @param validMaterials the set of materials that count toward progress
     */
    private SmeltItemObjectiveType(@NotNull Set<Material> validMaterials) {
        this.validMaterials = validMaterials;
    }

    /**
     * {@inheritDoc}
     *
     * @return the namespaced key {@code mcrpg:smelt_item}
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
     * {@link Material} via {@link Material#matchMaterial(String)}.
     *
     * @param section the BoostedYaml section containing type-specific data
     * @return a new configured instance with the parsed material set
     */
    @NotNull
    @Override
    public SmeltItemObjectiveType parseConfig(@NotNull Section section) {
        Set<Material> materials = Set.of();
        if (section.contains("items")) {
            materials = section.getStringList("items").stream()
                    .map(Material::matchMaterial)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new SmeltItemObjectiveType(materials);
    }

    /**
     * {@inheritDoc}
     *
     * @param context the progress context from an event
     * @return {@code true} if the context is a {@link SmeltItemQuestContext}
     */
    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof SmeltItemQuestContext;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The delta is the number of items extracted from the furnace, as reported by
     * {@link org.bukkit.event.inventory.FurnaceExtractEvent#getItemAmount()}.
     *
     * @param instance the objective instance to potentially progress
     * @param context  the progress context from a furnace extract event
     * @return the number of items smelted (0 if the material doesn't match)
     */
    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof SmeltItemQuestContext smeltContext)) {
            return 0;
        }

        var event = smeltContext.getFurnaceExtractEvent();

        if (validMaterials.isEmpty()) {
            return event.getItemAmount();
        }

        return validMaterials.contains(event.getItemType()) ? event.getItemAmount() : 0;
    }

    /**
     * Formats a material name for display by converting to lowercase and replacing
     * underscores with spaces.
     *
     * @param material the material to format
     * @return the formatted display name
     */
    @NotNull
    private String formatMaterialName(@NotNull Material material) {
        return material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves a localized description via the player's locale chain.
     *
     * @param player           the player whose locale chain determines the language
     * @param requiredProgress the amount of progress needed to complete the objective
     * @return a descriptive string for smelting objectives
     */
    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (validMaterials.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_SMELT_ITEM_ANY,
                    Map.of("count", count));
        }
        if (validMaterials.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_SMELT_ITEM_SINGLE,
                    Map.of("count", count, "item", formatMaterialName(validMaterials.iterator().next())));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_SMELT_ITEM_MULTI_HEADER, Map.of("count", count)));
        for (Material material : validMaterials) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_SMELT_ITEM_MULTI_ITEM, Map.of("item", formatMaterialName(material))));
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
