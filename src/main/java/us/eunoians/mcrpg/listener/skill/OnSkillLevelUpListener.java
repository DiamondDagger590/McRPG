package us.eunoians.mcrpg.listener.skill;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.skill.PostSkillGainExpEvent;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleLevelUp(SkillGainLevelEvent skillGainLevelEvent) {
        SkillHolder skillHolder = skillGainLevelEvent.getSkillHolder();
        UUID uuid = skillHolder.getUUID();
        int levels = skillGainLevelEvent.getLevels();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(skillGainLevelEvent.getSkillKey());
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            player.sendMessage(miniMessage.deserialize(String.format("<green>You have gone up <gold>%d levels<green> in <gold>%s<green>.", levels, skill.getDisplayName())));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePostExperienceGain(PostSkillGainExpEvent skillGainExpEvent) {
        SkillHolder skillHolder = skillGainExpEvent.getSkillHolder();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(skillGainExpEvent.getSkillKey());

        if(Bukkit.getEntity(skillHolder.getUUID()) instanceof Player player && player.isOnline()) {
            McRPG.getInstance().getDisplayManager().sendExperienceUpdate(skillHolder, skill.getSkillKey());
        }
    }
}
