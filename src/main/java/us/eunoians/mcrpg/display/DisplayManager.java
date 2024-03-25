package us.eunoians.mcrpg.display;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DisplayManager {

    private Map<UUID, BossBar> activeDisplays;

    public DisplayManager() {
        this.activeDisplays = new HashMap<>();
    }

    public boolean hasActiveDisplay(@NotNull UUID uuid) {
        return activeDisplays.containsKey(uuid);
    }

    public void sendExperienceUpdate(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey) {
        SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        Optional<SkillHolder.SkillHolderData> dataOptional = skillHolder.getSkillHolderData(skillKey);
        UUID uuid = skillHolder.getUUID();
        Player player = Bukkit.getPlayer(uuid);
        if (dataOptional.isPresent() && player != null) {

            Audience audience = McRPG.getInstance().getAdventure().player(player);
            removePreviousDisplay(uuid);

            SkillHolder.SkillHolderData skillHolderData = dataOptional.get();
            int currentLevel = skillHolderData.getCurrentLevel();
            int currentExperience = skillHolderData.getCurrentExperience();
            int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
            Component component = miniMessage.deserialize("<gray>Lv.<gold>" + currentLevel + " <gray>- " + skill.getDisplayName() + ": <gold>" + (experienceForNextLevel - currentExperience));
            BossBar bossBar = BossBar.bossBar(component, (((float) currentExperience)/((float) experienceForNextLevel)), BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_10);
            audience.showBossBar(bossBar);
            activeDisplays.put(uuid, bossBar);
            DelayableCoreTask delayableCoreTask = new DelayableCoreTask(McRPG.getInstance(), 10) {

                @Override
                public void run() {
                    audience.hideBossBar(bossBar);
                }
            };
            delayableCoreTask.runTask();
        }
    }

    private void removePreviousDisplay(@NotNull UUID uuid) {
        if (hasActiveDisplay(uuid)) {
            Audience audience = McRPG.getInstance().getAdventure().player(uuid);
            audience.hideBossBar(activeDisplays.remove(uuid));
        }
    }
}
