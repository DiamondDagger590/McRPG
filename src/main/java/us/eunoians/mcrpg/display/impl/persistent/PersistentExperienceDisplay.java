package us.eunoians.mcrpg.display.impl.persistent;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A persistent experience display will display information for a specific
 * {@link us.eunoians.mcrpg.skill.Skill} either until cancelled or until a specified
 * duration has elapsed.
 * <p>
 * All other experience updates are ignored in favor of this one, allowing players to track a specific
 * skill.
 */
public interface PersistentExperienceDisplay {

    /**
     * Gets the {@link NamespacedKey} representing the {@link us.eunoians.mcrpg.skill.Skill} being displayed.
     *
     * @return The {@link NamespacedKey} representing the {@link us.eunoians.mcrpg.skill.Skill} being displayed.
     */
    @NotNull
    NamespacedKey getSkillKey();

    /**
     * Gets an {@link Optional} containing the end time of this display in millis.
     * <p>
     * An empty optional indicates that this display will continue to exist until some action clears it.
     *
     * @return An {@link Optional} containing the end time of this display in millis, or empty
     * if there is no end time.
     */
    @NotNull
    Optional<Long> getExpireTime();

    /**
     * Checks to see if this experience display has expired.
     *
     * @return {@code true} if this experience display both has an expiry time and that time has expired
     */
    default boolean hasExpired() {
        return getExpireTime().isPresent() && getExpireTime().get() < System.currentTimeMillis();
    }
}
