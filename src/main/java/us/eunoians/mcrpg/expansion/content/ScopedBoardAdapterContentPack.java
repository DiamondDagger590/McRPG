package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;

/**
 * A content pack that provides {@link ScopedBoardAdapter}s for a given {@link ContentExpansion}.
 */
public final class ScopedBoardAdapterContentPack extends McRPGContentPack<ScopedBoardAdapter> {

    public ScopedBoardAdapterContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
