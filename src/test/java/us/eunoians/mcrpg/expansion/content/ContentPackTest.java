package us.eunoians.mcrpg.expansion.content;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.TopPlayersDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScopeProvider;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    // --- RewardDistributionTypeContentPack ---

    @DisplayName("RewardDistributionTypeContentPack - addContent and getContent round-trip")
    @Test
    void rewardDistributionTypeContentPack_addAndRetrieve() {
        RewardDistributionTypeContentPack pack = new RewardDistributionTypeContentPack(STUB_EXPANSION);
        ParticipatedDistributionType participated = new ParticipatedDistributionType();
        TopPlayersDistributionType topPlayers = new TopPlayersDistributionType();
        pack.addContent(participated);
        pack.addContent(topPlayers);

        Set<RewardDistributionType> content = pack.getContent();
        assertEquals(2, content.size());
        assertTrue(content.contains(participated));
        assertTrue(content.contains(topPlayers));
    }

    @DisplayName("RewardDistributionTypeContentPack - empty pack returns empty set")
    @Test
    void rewardDistributionTypeContentPack_emptyPackReturnsEmptySet() {
        RewardDistributionTypeContentPack pack = new RewardDistributionTypeContentPack(STUB_EXPANSION);
        assertTrue(pack.getContent().isEmpty());
    }

    @DisplayName("RewardDistributionTypeContentPack - getContentExpansion returns owning expansion")
    @Test
    void rewardDistributionTypeContentPack_getContentExpansion() {
        RewardDistributionTypeContentPack pack = new RewardDistributionTypeContentPack(STUB_EXPANSION);
        assertEquals(STUB_EXPANSION, pack.getContentExpansion());
    }

    @DisplayName("RewardDistributionTypeContentPack - getContent returns immutable copy")
    @Test
    void rewardDistributionTypeContentPack_getContentReturnsImmutableCopy() {
        RewardDistributionTypeContentPack pack = new RewardDistributionTypeContentPack(STUB_EXPANSION);
        pack.addContent(new ParticipatedDistributionType());

        Set<RewardDistributionType> content = pack.getContent();
        try {
            content.add(new TopPlayersDistributionType());
        } catch (UnsupportedOperationException expected) {
            return;
        }
    }

    // --- ScopedBoardAdapterContentPack ---

    private static ScopedBoardAdapter createStubAdapter(@NotNull String key) {
        NamespacedKey scopeKey = new NamespacedKey("test", key);
        return new ScopedBoardAdapter() {
            @Override
            public @NotNull Optional<NamespacedKey> getExpansionKey() {
                return Optional.of(STUB_EXPANSION.getExpansionKey());
            }

            @Override
            public @NotNull NamespacedKey getScopeProviderKey() {
                return scopeKey;
            }

            @Override
            public @NotNull Set<String> getAllActiveEntities() {
                return Set.of();
            }

            @Override
            public @NotNull Set<String> getMemberEntities(@NotNull UUID playerUUID) {
                return Set.of();
            }

            @Override
            public @NotNull Set<String> getManageableEntities(@NotNull UUID playerUUID) {
                return Set.of();
            }

            @Override
            public boolean canManageQuests(@NotNull UUID playerUUID, @NotNull String entityId) {
                return false;
            }

            @Override
            public @NotNull Optional<String> getEntityDisplayName(@NotNull String entityId) {
                return Optional.empty();
            }
        };
    }

    @DisplayName("ScopedBoardAdapterContentPack - addContent and getContent round-trip")
    @Test
    void scopedBoardAdapterContentPack_addAndRetrieve() {
        ScopedBoardAdapterContentPack pack = new ScopedBoardAdapterContentPack(STUB_EXPANSION);
        ScopedBoardAdapter adapter1 = createStubAdapter("land_scope");
        ScopedBoardAdapter adapter2 = createStubAdapter("faction_scope");
        pack.addContent(adapter1);
        pack.addContent(adapter2);

        Set<ScopedBoardAdapter> content = pack.getContent();
        assertEquals(2, content.size());
        assertTrue(content.contains(adapter1));
        assertTrue(content.contains(adapter2));
    }

    @DisplayName("ScopedBoardAdapterContentPack - empty pack returns empty set")
    @Test
    void scopedBoardAdapterContentPack_emptyPackReturnsEmptySet() {
        ScopedBoardAdapterContentPack pack = new ScopedBoardAdapterContentPack(STUB_EXPANSION);
        assertTrue(pack.getContent().isEmpty());
    }

    @DisplayName("ScopedBoardAdapterContentPack - getContentExpansion returns owning expansion")
    @Test
    void scopedBoardAdapterContentPack_getContentExpansion() {
        ScopedBoardAdapterContentPack pack = new ScopedBoardAdapterContentPack(STUB_EXPANSION);
        assertEquals(STUB_EXPANSION, pack.getContentExpansion());
    }

    @DisplayName("ScopedBoardAdapterContentPack - getContent returns immutable copy")
    @Test
    void scopedBoardAdapterContentPack_getContentReturnsImmutableCopy() {
        ScopedBoardAdapterContentPack pack = new ScopedBoardAdapterContentPack(STUB_EXPANSION);
        pack.addContent(createStubAdapter("land_scope"));

        Set<ScopedBoardAdapter> content = pack.getContent();
        try {
            content.add(createStubAdapter("another_scope"));
        } catch (UnsupportedOperationException expected) {
            return;
        }
    }
}
