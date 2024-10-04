package us.eunoians.mcrpg.expansion.handler;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;

/**
 * A content pack processor is in charge of processing a given {@link McRPGContentPack}
 * and doing whatever is required to load the content such that it works with McRPG.
 * <p>
 * An example would be registering the {@link us.eunoians.mcrpg.skill.Skill}s provided by a {@link us.eunoians.mcrpg.expansion.content.SkillContentPack}
 * to McRPG.
 */
public interface ContentPackProcessor {

    /**
     * Attempts to process the provided {@link McRPGContentPack} to set it up to be used by McRPG.
     *
     * @param mcRPG        The plugin instance.
     * @param mcRPGContent The {@link McRPGContent} to process.
     * @return {@code true} if the provided {@link McRPGContentPack} was processed.
     */
    boolean processContentPack(@NotNull McRPG mcRPG, @NotNull McRPGContentPack<? extends McRPGContent> mcRPGContent);
}
