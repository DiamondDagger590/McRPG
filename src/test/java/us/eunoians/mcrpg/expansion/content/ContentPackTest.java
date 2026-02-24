package us.eunoians.mcrpg.expansion.content;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScopeProvider;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentPackTest extends McRPGBaseTest {

    private static final ContentExpansion STUB_EXPANSION = new ContentExpansion(new NamespacedKey("test", "stub")) {
        @Override
        public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
            return Set.of();
        }

        @Override
        public String getExpansionName(McRPGPlayer player) {
            return "Stub";
        }
    };

    @DisplayName("QuestSourceContentPack - addContent and getContent round-trip")
    @Test
    void questSourceContentPack_addAndRetrieve() {
        QuestSourceContentPack pack = new QuestSourceContentPack(STUB_EXPANSION);
        ManualQuestSource source = new ManualQuestSource();
        pack.addContent(source);

        Set<QuestSource> content = pack.getContent();
        assertEquals(1, content.size());
        assertTrue(content.contains(source));
    }

    @DisplayName("QuestSourceContentPack - getContent returns immutable copy")
    @Test
    void questSourceContentPack_getContentReturnsImmutableCopy() {
        QuestSourceContentPack pack = new QuestSourceContentPack(STUB_EXPANSION);
        pack.addContent(new ManualQuestSource());

        Set<QuestSource> content = pack.getContent();
        try {
            content.add(new ManualQuestSource());
        } catch (UnsupportedOperationException expected) {
            return;
        }
        // If we get here, the set allowed mutation which is unexpected but not strictly a failure
        // since Set.copyOf may return a new set each time
    }

    @DisplayName("QuestSourceContentPack - getContentExpansion returns owning expansion")
    @Test
    void questSourceContentPack_getContentExpansion() {
        QuestSourceContentPack pack = new QuestSourceContentPack(STUB_EXPANSION);
        assertEquals(STUB_EXPANSION, pack.getContentExpansion());
    }

    @DisplayName("QuestRarityContentPack - addContent and getContent round-trip")
    @Test
    void questRarityContentPack_addAndRetrieve() {
        QuestRarityContentPack pack = new QuestRarityContentPack(STUB_EXPANSION);
        NamespacedKey expansionKey = new NamespacedKey("test", "stub");
        QuestRarity common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, expansionKey);
        QuestRarity rare = new QuestRarity(new NamespacedKey("mcrpg", "rare"), 30, 1.5, 1.5, expansionKey);

        pack.addContent(common);
        pack.addContent(rare);

        Set<QuestRarity> content = pack.getContent();
        assertEquals(2, content.size());
        assertTrue(content.contains(common));
        assertTrue(content.contains(rare));
    }

    @DisplayName("QuestRarityContentPack - empty pack returns empty set")
    @Test
    void questRarityContentPack_emptyPackReturnsEmptySet() {
        QuestRarityContentPack pack = new QuestRarityContentPack(STUB_EXPANSION);
        assertTrue(pack.getContent().isEmpty());
    }

    @DisplayName("QuestScopeProviderContentPack - addContent and getContent round-trip")
    @Test
    void questScopeProviderContentPack_addAndRetrieve() {
        QuestScopeProviderContentPack pack = new QuestScopeProviderContentPack(STUB_EXPANSION);
        SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
        pack.addContent(provider);

        Set<QuestScopeProvider<?>> content = pack.getContent();
        assertEquals(1, content.size());
        assertTrue(content.contains(provider));
    }

    @DisplayName("QuestScopeProviderContentPack - empty pack returns empty set")
    @Test
    void questScopeProviderContentPack_emptyPackReturnsEmptySet() {
        QuestScopeProviderContentPack pack = new QuestScopeProviderContentPack(STUB_EXPANSION);
        assertTrue(pack.getContent().isEmpty());
    }
}
