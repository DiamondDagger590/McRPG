package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.expansion.ContentExpansion;

/**
 * A content pack that provides {@link Ability Abilities} for a given {@link ContentExpansion}.
 */
public final class AbilityContentPack extends McRPGContentPack<Ability> {

    public AbilityContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
