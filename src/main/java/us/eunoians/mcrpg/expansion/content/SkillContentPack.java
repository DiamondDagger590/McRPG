package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.skill.Skill;

/**
 * A content pack that provides {@link Skill}s for a given {@link ContentExpansion}.
 */
public final class SkillContentPack extends McRPGContentPack<Skill> {

    public SkillContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
