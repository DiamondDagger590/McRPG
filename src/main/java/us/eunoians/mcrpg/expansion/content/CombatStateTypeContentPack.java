package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.expansion.ContentExpansion;

/**
 * Content pack for registering {@link CombatStateType} implementations via the
 * {@link ContentExpansion} system. Follows the same pattern as {@link CombatConditionContentPack}.
 */
public class CombatStateTypeContentPack extends McRPGContentPack<CombatStateType<?>> {

    /**
     * Constructs a new {@link CombatStateTypeContentPack}.
     *
     * @param contentExpansion The {@link ContentExpansion} providing this content.
     */
    public CombatStateTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
