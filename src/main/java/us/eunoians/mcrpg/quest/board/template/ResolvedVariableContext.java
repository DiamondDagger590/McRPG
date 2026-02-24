package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Snapshot of all resolved template variable values after pool selection and range scaling.
 *
 * @param resolvedValues   ordered map of variable name to resolved value. Pool variables map to
 *                         {@code List<String>} (merged values); range variables map to {@code Long}
 *                         (rounded result); the built-in {@code "difficulty"} maps to {@code Double}.
 * @param poolDifficulty   the average difficulty across all selected pools (1.0 if no pools)
 * @param rarityDifficulty the effective rarity difficulty multiplier (from template override or global)
 * @param difficulty       the combined difficulty ({@code poolDifficulty * rarityDifficulty})
 */
public record ResolvedVariableContext(
        @NotNull Map<String, Object> resolvedValues,
        double poolDifficulty,
        double rarityDifficulty,
        double difficulty
) {}
