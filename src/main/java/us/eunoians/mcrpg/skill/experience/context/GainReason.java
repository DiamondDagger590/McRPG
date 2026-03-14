package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Describes why a player gained skill experience. Implemented as an interface
 * rather than a closed enum so that third-party {@link us.eunoians.mcrpg.expansion.ContentExpansion}
 * plugins can define their own reasons.
 *
 * @see McRPGGainReason
 */
public interface GainReason {

    /**
     * Gets the unique {@link NamespacedKey} identifying this gain reason.
     *
     * @return The unique key for this reason.
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Gets a human-readable display name for this reason, useful for
     * logging and admin UIs.
     *
     * @return The display name.
     */
    @NotNull
    String getDisplayName();
}
