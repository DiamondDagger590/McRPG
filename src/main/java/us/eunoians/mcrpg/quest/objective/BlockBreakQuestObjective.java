package us.eunoians.mcrpg.quest.objective;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.quest.Quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This objective is progressed and completed by a player mining blocks.
 * <p>
 * This objective provides both an allow and ban list for {@link Material} to configure
 * what blocks will give progression to the quest. If the allow and ban list are empty, then any block
 * break will progress the objective. In the case the allow list and ban list for some reason contain content,
 * only the allow list will be looked at.
 */
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

    /**
     * Adds the provided {@link Material}s to the allow list.
     *
     * @param material The {@link Material}s to add to the allow list
     */
    public void addAllowedBlocks(@NotNull Material... material) {
        allowedBlocks.addAll(List.of(material));
    }

    /**
     * Adds the provided {@link Material}s to the ban list.
     *
     * @param material The {@link Material}s to add to the ban list.
     */
    public void addBannedBlocks(@NotNull Material... material) {
        bannedBlocks.addAll(List.of(material));
    }

    /**
     * Gets a copy of the {@link Set} of all allowed blocks.
     *
     * @return A copy of the {@link Set} of all allowed blocks.
     */
    @NotNull
    public Set<Material> getAllowedBlocks() {
        return ImmutableSet.copyOf(allowedBlocks);
    }

    /**
     * Gets a copy of the {@link Set} of all banned blocks.
     *
     * @return A copy of the {@link Set} of all banned blocks.
     */
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
                questHolder.getUUID().equals(blockBreakEvent.getPlayer().getUniqueId()) &&
                questHolder.isQuestActive(getQuest()) &&
                allowMode.isMaterialAllowed(this, blockBreakEvent.getBlock().getType()) &&
                blockBreakEvent.getPlayer().getGameMode() == GameMode.SURVIVAL;
    }

    @Override
    public void processEvent(@NotNull QuestHolder questHolder, @NotNull Event event) {
        // Since only one block can be broken per event, increment progression by 1
        progressObjective(1);
    }

    @Override
    public void startListeningForProgression() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void stopListeningForProgression() {
        BlockBreakEvent.getHandlerList().unregister(this);
    }

    @Override
    public Component getObjectiveTitle() {
        return McRPG.getInstance().getMiniMessage().deserialize("<gold>Block Break Objective</gold>");
    }

    @Override
    public List<Component> getObjectiveInfoText() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        List<Component> info = new ArrayList<>();
        AllowMode allowMode = AllowMode.getAllowMode(this);
        switch (allowMode) {
            case ALLOWED ->
                    info.add(miniMessage.deserialize("<gray>Break these blocks to progress this objective</gray>"));
            case BANNED ->
                    info.add(miniMessage.deserialize("<gray>Break any block besides these to progress this objective</gray>"));
            case ALL -> info.add(miniMessage.deserialize("<gray>Break any block to progress this objective</gray>"));
        }
        if (allowMode == AllowMode.ALLOWED) {
            for (Material material : allowedBlocks) {
                info.add(miniMessage.deserialize("  <gray>- <gold>" + material + "</gold></gray>"));
            }
        } else if (allowMode == AllowMode.BANNED) {
            for (Material material : bannedBlocks) {
                info.add(miniMessage.deserialize("  <gray>- <gold>" + material + "</gold></gray>"));
            }
        }
        return info;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        var questHolderOptional = McRPG.getInstance().getEntityManager().getQuestHolder(player.getUniqueId());
        if (questHolderOptional.isPresent()) {
            var questHolder = questHolderOptional.get();
            if (questHolder.isQuestActive(getQuest()) && canProcessEvent(questHolder, blockBreakEvent)) {
                processEvent(questHolder, blockBreakEvent);
            }
        }
    }

    /**
     * An enum that represents the mode for validating what {@link Material}s can progress the objective.
     */
    private enum AllowMode {
        ALL,
        ALLOWED,
        BANNED;

        /**
         * Checks to see if the provided {@link Material} can progress the objective based on the allow or ban settings
         *
         * @param blockBreakQuestObjective The {@link BlockBreakQuestObjective} to check the lists of
         * @param material                 The {@link Material} to check
         * @return {@code true} if the provided {@link Material} is valid for this allow mode.
         */
        public boolean isMaterialAllowed(@NotNull BlockBreakQuestObjective blockBreakQuestObjective, @NotNull Material material) {
            return switch (this) {
                case ALLOWED -> blockBreakQuestObjective.getAllowedBlocks().contains(material);
                case BANNED -> !blockBreakQuestObjective.getBannedBlocks().contains(material);
                case ALL -> true;
            };
        }

        /**
         * Gets the allow mode for the given {@link BlockBreakQuestObjective} based on the state of the allow/ban list.
         *
         * @param blockBreakQuestObjective The {@link BlockBreakQuestObjective} to get the allow mode for.
         * @return If both {@link BlockBreakQuestObjective#getAllowedBlocks()} and {@link BlockBreakQuestObjective#getBannedBlocks()}
         * return an empty {@link Set}, then {@link AllowMode#ALL} is returned. If both the allow and ban list are non-empty, then {@link AllowMode#ALLOWED}
         * is returned. Otherwise, it will return the allow mode corresponding to the list that has contents.
         */
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
