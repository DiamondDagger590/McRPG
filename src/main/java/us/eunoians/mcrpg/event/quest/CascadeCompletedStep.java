package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable snapshot of a single auto-completed step within a cascade. Carried by
 * {@link CascadeFinalizeEvent} so external plugins can inspect which steps were
 * auto-completed without depending on internal orchestration types.
 *
 * @param questKey    the quest definition key of the completed step
 * @param displayName the resolved display name at the time of completion
 */
public record CascadeCompletedStep(
        @NotNull NamespacedKey questKey,
        @NotNull String displayName
) {}
