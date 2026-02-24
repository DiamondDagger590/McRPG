package us.eunoians.mcrpg.quest.source.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Quest source for land-scoped board quests.
 */
public final class BoardLandQuestSource extends QuestSource {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "board_land");

    public BoardLandQuestSource() {
        super(KEY);
    }

    @Override
    public boolean isAbandonable() {
        return true;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
