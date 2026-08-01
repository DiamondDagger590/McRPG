package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.expansion.ContentExpansion;

/**
 * Content pack for registering {@link CombatCondition} implementations via the
 * {@link ContentExpansion} system.
 */
public class CombatConditionContentPack extends McRPGContentPack<CombatCondition> {

    /**
     * Constructs a new {@link CombatConditionContentPack}.
     *
     * @param contentExpansion The {@link ContentExpansion} providing this content.
     */
    public CombatConditionContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
