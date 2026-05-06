package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.stat.PlayerStat;

/**
 * A content pack that provides {@link PlayerStat} definitions for a given
 * {@link ContentExpansion}.
 * <p>
 * Stats included in this pack are registered to the {@link us.eunoians.mcrpg.stat.PlayerStatRegistry}
 * during content expansion processing. Third-party expansions can define custom stats
 * (e.g., Stamina, Focus) by adding {@link PlayerStat} entries to their pack.
 * <p>
 * The {@link us.eunoians.mcrpg.stat.PlayerStatRegistry} must already be registered in
 * {@link com.diamonddagger590.mccore.registry.RegistryAccess} before expansion processing
 * begins — the {@link us.eunoians.mcrpg.bootstrap.McRPGBootstrap} guarantees this ordering.
 */
public final class PlayerStatContentPack extends McRPGContentPack<PlayerStat> {

    /**
     * Creates a new {@link PlayerStatContentPack} for the given expansion.
     *
     * @param contentExpansion The {@link ContentExpansion} that owns this pack.
     */
    public PlayerStatContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
