package us.eunoians.mcrpg.listener.skill;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This listener is in charge of handling ability unlocks and ability point distributions
 */
public class OnSkillLevelUpListener implements Listener {

    private static final Map<UUID, Integer> LEVELS_GAINED = new HashMap<>();

    // TODO reintroduce display system and figure out how updates should be broadcasted
    @EventHandler(priority = EventPriority.MONITOR)
    public void handleLevelUp(SkillGainLevelEvent skillGainLevelEvent) {
        SkillHolder skillHolder = skillGainLevelEvent.getSkillHolder();
        UUID uuid = skillHolder.getUUID();
        int levels = skillGainLevelEvent.getLevels();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(skillGainLevelEvent.getSkillKey());
        Player player = Bukkit.getPlayer(uuid);
        // We only want to print things to players
        if (player != null) {
            // If there isn't a scheduled task, schedule one
            if (!LEVELS_GAINED.containsKey(skillHolder.getUUID())) {
                DelayableCoreTask delayableCoreTask = new DelayableCoreTask(McRPG.getInstance(), 1) {
                    @Override
                    public void run() {
                        if (LEVELS_GAINED.containsKey(uuid)) {
                            int levels = LEVELS_GAINED.remove(uuid);
                            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                            if (player.isOnline()) {
                                player.sendMessage(miniMessage.deserialize(String.format("<green>You have gone up <gold>%d levels<green> in <gold>%s<green>.", levels, skill.getDisplayName())));
                            }
                        }
                    }
                };
                delayableCoreTask.runTask();
            }
            // Add the level count to the tracked amount
            LEVELS_GAINED.put(uuid, LEVELS_GAINED.getOrDefault(uuid, 0) + levels);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleExpGain(SkillGainExpEvent skillGainExpEvent) {

        SkillHolder skillHolder = skillGainExpEvent.getSkillHolder();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(skillGainExpEvent.getSkillKey());
        int experience = skillGainExpEvent.getExperience();
        UUID uuid = skillHolder.getUUID();
        Entity entity = Bukkit.getEntity(uuid);

        if(entity instanceof Player player && player.isOnline()) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            player.sendMessage(miniMessage.deserialize(String.format("<green>You have gained <gold>%d experience<green> in <gold>%s<green>.", experience, skill.getDisplayName())));
        }
    }
}
