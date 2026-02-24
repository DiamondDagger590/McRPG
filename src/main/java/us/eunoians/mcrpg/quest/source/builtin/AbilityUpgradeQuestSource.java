package us.eunoians.mcrpg.quest.source.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Quest source for quests assigned as part of ability upgrade requirements.
 * These quests cannot be abandoned by the player.
 */
public final class AbilityUpgradeQuestSource extends QuestSource {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ability_upgrade");

    public AbilityUpgradeQuestSource() {
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
