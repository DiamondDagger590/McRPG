package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.expansion.ContentExpansion;

/**
 * A content pack that provides {@link UnlockConditionType}s for a given {@link ContentExpansion}.
 * <p>
 * McRPG's native built-in types ship through this same class via {@code McRPGExpansion} —
 * there is no internal back door. Third-party expansions follow the identical pattern.
 */
public final class UnlockConditionTypeContentPack extends McRPGContentPack<UnlockConditionType> {

    public UnlockConditionTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
