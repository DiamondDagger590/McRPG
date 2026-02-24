package us.eunoians.mcrpg.quest.source.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Quest source for manually assigned quests (e.g., via admin commands or API).
 * These quests cannot be abandoned by the player.
 */
public final class ManualQuestSource extends QuestSource {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "manual");

    public ManualQuestSource() {
        super(KEY);
    }

    @Override
    public boolean isAbandonable() {
        return false;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
