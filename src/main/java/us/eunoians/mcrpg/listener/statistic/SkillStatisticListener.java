package us.eunoians.mcrpg.listener.statistic;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.PlayerStatisticData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.skill.PostSkillGainLevelEvent;
import us.eunoians.mcrpg.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.context.McRPGGainReason;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.Optional;

/**
 * Listens to skill experience and level events to increment statistics.
 * <p>
 * Uses {@link EventPriority#MONITOR} to read final values after all other
 * listeners have modified or cancelled the event.
 */
public class SkillStatisticListener implements Listener {

    /**
     * Tracks per-skill and total XP earned. Fires on the pre-event
     * so the XP amount is available (including overflow past max level).
     *
     * @param event The skill experience gain event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillGainExp(@NotNull SkillGainExpEvent event) {
        Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(event.getSkillHolder().getUUID());
        if (playerOptional.isEmpty()) {
            return;
        }

        PlayerStatisticData stats = playerOptional.get().getStatisticData();
        int experience = event.getExperience();

        // Per-skill XP
        stats.incrementLong(McRPGStatistic.getSkillExperienceKey(event.getSkillKey()), experience);

        // Total XP across all skills
        stats.incrementLong(McRPGStatistic.TOTAL_SKILL_EXPERIENCE.getStatisticKey(), experience);

        // Block-based statistics — only count when XP came from actually breaking a block
        if (event.getGainReason() == McRPGGainReason.BLOCK_BREAK) {
            stats.incrementLong(McRPGStatistic.BLOCKS_MINED.getStatisticKey(), 1);

            if (event.getSkillKey().equals(Mining.MINING_KEY)) {
                stats.incrementLong(McRPGStatistic.ORES_MINED.getStatisticKey(), 1);
            } else if (event.getSkillKey().equals(WoodCutting.WOODCUTTING_KEY)) {
                stats.incrementLong(McRPGStatistic.TREES_CHOPPED.getStatisticKey(), 1);
            } else if (event.getSkillKey().equals(Herbalism.HERBALISM_KEY)) {
                stats.incrementLong(McRPGStatistic.CROPS_HARVESTED.getStatisticKey(), 1);
            }
        }
    }

    /**
     * Tracks per-skill max level and total levels gained.
     *
     * @param event The post-level-up event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillLevelUp(@NotNull PostSkillGainLevelEvent event) {
        Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(event.getSkillHolder().getUUID());
        if (playerOptional.isEmpty()) {
            return;
        }

        PlayerStatisticData stats = playerOptional.get().getStatisticData();

        // Update per-skill max level (only increases, never decreases)
        stats.setMaxInt(McRPGStatistic.getSkillMaxLevelKey(event.getSkillKey()), event.getAfterLevel());

        // Increment total levels gained
        int levelsGained = event.getAfterLevel() - event.getBeforeLevel();
        stats.incrementLong(McRPGStatistic.TOTAL_SKILL_LEVELS_GAINED.getStatisticKey(), levelsGained);
    }
}
