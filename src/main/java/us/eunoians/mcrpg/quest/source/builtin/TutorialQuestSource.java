package us.eunoians.mcrpg.quest.source.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Quest source for tutorial quests managed by the tutorial quest chain.
 * Non-abandonable — players must use the {@link us.eunoians.mcrpg.setting.impl.DisableTutorialSetting}
 * to opt out, which triggers chain abandonment through
 * {@link us.eunoians.mcrpg.quest.chain.QuestChainManager#abandonChain}.
 */
public final class TutorialQuestSource extends QuestSource {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "tutorial");

    /**
     * The chain key for the built-in tutorial quest chain. Referenced by the
     * tutorial disable confirmation slot and the pre-quest-start listener
     * for chain abandonment. Defined here as the single source of truth for
     * all tutorial-identity constants.
     */
    public static final NamespacedKey TUTORIAL_CHAIN_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "tutorial_chain");

    /**
     * Creates a new tutorial quest source.
     */
    public TutorialQuestSource() {
        super(KEY);
    }

    /**
     * Tutorial quests are non-abandonable. Players must use the tutorial disable
     * setting to opt out — which abandons the chain as a whole.
     *
     * @return {@code false} always
     */
    @Override
    public boolean isAbandonable() {
        return false;
    }

    /**
     * Returns the expansion key for the McRPG built-in expansion.
     *
     * @return the McRPG expansion key
     */
    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
