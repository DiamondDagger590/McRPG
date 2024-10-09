package us.eunoians.mcrpg.display.impl.persistent;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.display.impl.BossBarExperienceDisplay;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * This is a type of boss bar experience display that also is persistent, meaning it doesn't auto decay and will
 * take priority until it expires or is manually removed.
 */
public class PersistentBossBarExperienceDisplay extends BossBarExperienceDisplay implements PersistentExperienceDisplay {

    private final Long expireTime;
    private final NamespacedKey skillKey;

    public PersistentBossBarExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer, @NotNull NamespacedKey skillKey) {
        super(mcRPGPlayer);
        this.skillKey = skillKey;
        this.expireTime = null;
    }

    public PersistentBossBarExperienceDisplay(@NotNull McRPGPlayer player, @NotNull NamespacedKey skillKey, @NotNull Duration duration) {
        super(player);
        this.skillKey = skillKey;
        this.expireTime = System.currentTimeMillis() + duration.toMillis();
        DelayableCoreTask delayableCoreTask = new DelayableCoreTask(getMcRPGPlayer().getMcRPGInstance(), (int) duration.toSeconds()) {

            @Override
            public void run() {
                if (getActiveDisplay().isPresent() && getActiveDisplay().get() == bossBar) {
                    cleanDisplay();
                }
            }
        };
        delayableCoreTask.runTask();
    }

    @Override
    public void sendExperienceUpdate(@NotNull NamespacedKey skillKey) {
        // Only display updates for a single skill
        if (skillKey.equals(this.skillKey)) {
            McRPG mcRPG = getMcRPGPlayer().getMcRPGInstance();
            SkillHolder skillHolder = getMcRPGPlayer().asSkillHolder();
            SkillRegistry skillRegistry = mcRPG.getSkillRegistry();
            MiniMessage miniMessage = mcRPG.getMiniMessage();
            Skill skill = skillRegistry.getRegisteredSkill(skillKey);
            var dataOptional = skillHolder.getSkillHolderData(skillKey);
            UUID uuid = skillHolder.getUUID();
            Player player = Bukkit.getPlayer(uuid);
            if (dataOptional.isPresent() && player != null) {
                displayUpdate(skillKey, player, dataOptional.get());
            }
        }
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    @NotNull
    @Override
    public Optional<Long> getExpireTime() {
        return Optional.ofNullable(expireTime);
    }
}
