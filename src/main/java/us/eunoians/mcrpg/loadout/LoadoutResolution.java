package us.eunoians.mcrpg.loadout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the result of resolving a player's string input to a {@link Loadout}.
 * <p>
 * Resolution is attempted in priority order: slot index → exact name match → substring match.
 */
public sealed interface LoadoutResolution {

    /**
     * A single {@link Loadout} was unambiguously resolved from the input.
     *
     * @param loadout The resolved {@link Loadout}.
     */
    record Found(@NotNull Loadout loadout) implements LoadoutResolution {}

    /**
     * Multiple {@link Loadout}s matched the input (ambiguous substring or exact match collision).
     * The caller should prompt the player to be more specific.
     *
     * @param matches The list of {@link Loadout}s that all matched the input.
     */
    record Ambiguous(@NotNull List<Loadout> matches) implements LoadoutResolution {}

    /**
     * No {@link Loadout} matched the input at any resolution step.
     */
    record NotFound() implements LoadoutResolution {}
}
