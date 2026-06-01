package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
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
 * Built-in objective type for tracking crop harvest progress.
 * <p>
 * Supports both vanilla materials and custom blocks from plugins integrated via McCore's
 * {@link CustomBlockWrapper}. Config entries under {@code blocks} can be vanilla material
 * names (e.g. {@code WHEAT}) or custom block identifiers from supported plugins.
 * If the list is empty, any crop harvest counts toward progress.
 */
public class HarvestCropObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "harvest_crop");

    private final Set<CustomBlockWrapper> validBlocks;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public HarvestCropObjectiveType() {
        this.validBlocks = Set.of();
    }

    private HarvestCropObjectiveType(@NotNull Set<CustomBlockWrapper> validBlocks) {
        this.validBlocks = validBlocks;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public HarvestCropObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomBlockWrapper> blocks = Set.of();
        if (section.contains("blocks")) {
            blocks = section.getStringList("blocks").stream()
                    .map(CustomBlockWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new HarvestCropObjectiveType(blocks);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof HarvestCropQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof HarvestCropQuestContext harvestContext)) {
            return 0;
        }

        if (validBlocks.isEmpty()) {
            return 1;
        }

        CustomBlockWrapper harvestedBlock = new CustomBlockWrapper(harvestContext.getHarvestEvent().getHarvestedBlock());
        return validBlocks.contains(harvestedBlock) ? 1 : 0;
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
        if (validBlocks.isEmpty()) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_HARVEST_CROP_ANY,
                    Map.of("count", count));
        }
        if (validBlocks.size() == 1) {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_HARVEST_CROP_SINGLE,
                    Map.of("count", count, "block", validBlocks.iterator().next().blockName()));
        }
        StringBuilder sb = new StringBuilder(localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_HARVEST_CROP_MULTI_HEADER, Map.of("count", count)));
        for (CustomBlockWrapper block : validBlocks) {
            sb.append("\n").append(localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_HARVEST_CROP_MULTI_ITEM, Map.of("block", block.blockName())));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
