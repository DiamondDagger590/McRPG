package us.eunoians.mcrpg.display.impl.persistent;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.display.impl.BossBarExperienceDisplay;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.time.Duration;
import java.util.Optional;

/**
 * This is a type of boss bar experience display that also is persistent, meaning it doesn't auto decay and will
 * take priority until it expires or is manually removed.
 */
// TODO https://github.com/DiamondDagger590/McRPG/issues/156
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
        this.expireTime = getMcRPGPlayer().getPlugin().getTimeProvider().now().toEpochMilli() + duration.toMillis();
        DelayableCoreTask delayableCoreTask = new DelayableCoreTask(getMcRPGPlayer().getPlugin(), (int) duration.toSeconds()) {

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
            SkillHolder skillHolder = getMcRPGPlayer().asSkillHolder();
            skillHolder.getSkillHolderData(skillKey).ifPresent(skillHolderData -> displayUpdate(skillKey, getMcRPGPlayer(), skillHolderData));
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
