package us.eunoians.mcrpg.quest.objective;

import com.google.common.collect.ImmutableSet;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.quest.Quest;

import java.util.HashSet;
import java.util.Set;

public class BlockBreakQuestObjective extends QuestObjective {

    private final Set<Material> allowedBlocks;
    private final Set<Material> bannedBlocks;

    public BlockBreakQuestObjective(@NotNull Quest quest, int requiredProgression) {
        super(quest, requiredProgression);
        this.allowedBlocks = new HashSet<>();
        this.bannedBlocks = new HashSet<>();
    }

    public BlockBreakQuestObjective(@NotNull Quest quest, int requiredProgression, int currentProgression) {
        super(quest, requiredProgression, currentProgression);
        this.allowedBlocks = new HashSet<>();
        this.bannedBlocks = new HashSet<>();
    }

    @NotNull
    public Set<Material> getAllowedBlocks() {
        return ImmutableSet.copyOf(allowedBlocks);
    }

    @NotNull
    public Set<Material> getBannedBlocks() {
        return ImmutableSet.copyOf(bannedBlocks);
    }

    @Override
    public boolean canProcessEvent(@NotNull QuestHolder questHolder, @NotNull Event event) {
        AllowMode allowMode = AllowMode.getAllowMode(this);
        // TODO track natural block placement
        return event instanceof BlockBreakEvent blockBreakEvent &&
                !blockBreakEvent.isCancelled() &&
                !isObjectiveCompleted() &&
                allowMode.isMaterialAllowed(this, blockBreakEvent.getBlock().getType()) &&
                blockBreakEvent.getPlayer().getGameMode() == GameMode.SURVIVAL;
    }

    @Override
    public void processEvent(@NotNull QuestHolder questHolder, @NotNull Event event) {
        // Since only one block can be broken per event, increment progression by 1
        progressObjective(1);
    }

    private enum AllowMode {
        ALL,
        ALLOWED,
        BANNED;

        public boolean isMaterialAllowed(@NotNull BlockBreakQuestObjective blockBreakQuestObjective, @NotNull Material material) {
            return switch (this) {
                case ALLOWED -> blockBreakQuestObjective.getAllowedBlocks().contains(material);
                case BANNED -> blockBreakQuestObjective.getBannedBlocks().contains(material);
                case ALL -> true;
            };
        }

        public static AllowMode getAllowMode(@NotNull BlockBreakQuestObjective blockBreakQuestObjective) {
            if (blockBreakQuestObjective.getAllowedBlocks().isEmpty()) {
                if (blockBreakQuestObjective.getBannedBlocks().isEmpty()) {
                    return ALL;
                } else {
                    return BANNED;
                }
            } else {
                return AllowMode.ALLOWED;
            }
        }
    }
}
