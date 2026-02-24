package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Built-in objective type for tracking block break progress.
 * <p>
 * Supports both vanilla materials and custom blocks from plugins integrated via McCore's
 * {@link CustomBlockWrapper}. Config entries under {@code blocks} can be vanilla material
 * names (e.g. {@code DIAMOND_ORE}) or custom block identifiers from supported plugins.
 * If the list is empty, any block counts toward progress.
 */
public class BlockBreakObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "block_break");

    private final Set<CustomBlockWrapper> validBlocks;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public BlockBreakObjectiveType() {
        this.validBlocks = Set.of();
    }

    private BlockBreakObjectiveType(@NotNull Set<CustomBlockWrapper> validBlocks) {
        this.validBlocks = validBlocks;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public BlockBreakObjectiveType parseConfig(@NotNull Section section) {
        Set<CustomBlockWrapper> blocks = Set.of();
        if (section.contains("blocks")) {
            blocks = section.getStringList("blocks").stream()
                    .map(CustomBlockWrapper::new)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new BlockBreakObjectiveType(blocks);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof BlockBreakQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof BlockBreakQuestContext blockContext)) {
            return 0;
        }

        if (validBlocks.isEmpty()) {
            return 1;
        }

        CustomBlockWrapper brokenBlock = new CustomBlockWrapper(blockContext.getBlockBreakEvent().getBlock());
        return validBlocks.contains(brokenBlock) ? 1 : 0;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
