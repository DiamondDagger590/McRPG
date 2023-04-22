package us.eunoians.mcrpg.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.api.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.util.UUID;

public class OnPlayerLevelUpListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleLevelUp(SkillGainLevelEvent skillGainLevelEvent) {

        SkillHolder skillHolder = skillGainLevelEvent.getSkillHolder();
        int levels = skillGainLevelEvent.getLevels();
        UUID uuid = skillHolder.getUUID();
        Entity entity = Bukkit.getEntity(uuid);

        if(entity instanceof Player player && player.isOnline()) {
            player.sendMessage(String.format(ChatColor.GREEN + "Gained %s levels in " + skillGainLevelEvent.getSkillKey().getKey(), levels));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleExpGain(SkillGainExpEvent skillGainExpEvent) {

        SkillHolder skillHolder = skillGainExpEvent.getSkillHolder();
        int experience = skillGainExpEvent.getExperience();
        UUID uuid = skillHolder.getUUID();
        Entity entity = Bukkit.getEntity(uuid);

        if(entity instanceof Player player && player.isOnline()) {
            player.sendMessage(String.format(ChatColor.GREEN + "Gained %s experience in " + skillGainExpEvent.getSkillKey().getKey(), experience));
        }
    }
}
