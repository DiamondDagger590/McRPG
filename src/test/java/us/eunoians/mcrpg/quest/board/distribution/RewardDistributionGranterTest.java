package us.eunoians.mcrpg.quest.board.distribution;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RewardDistributionGranterTest extends McRPGBaseTest {

    private static final NamespacedKey QUEST_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_quest");

    private RewardDistributionGranter granter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        TrackingRewardType.GRANTED_TO.clear();
        granter = new RewardDistributionGranter(mcRPG);
    }

    static class TrackingRewardType implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "tracking_reward");
        static final List<UUID> GRANTED_TO = new ArrayList<>();

        private final long amount;

        TrackingRewardType(long amount) {
            this.amount = amount;
        }

        @Override
        public @org.jetbrains.annotations.NotNull NamespacedKey getKey() {
            return KEY;
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType parseConfig(@org.jetbrains.annotations.NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@org.jetbrains.annotations.NotNull Player player) {
            GRANTED_TO.add(player.getUniqueId());
        }

        @Override
        public @org.jetbrains.annotations.NotNull Map<String, Object> serializeConfig() {
            return Map.of("amount", amount);
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType fromSerializedConfig(@org.jetbrains.annotations.NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @org.jetbrains.annotations.NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withAmountMultiplier(double multiplier) {
            return new TrackingRewardType(Math.max(1, (long) (amount * multiplier)));
        }
    }

    @Nested
    @DisplayName("Online player grants")
    class OnlinePlayerGrants {

        @DisplayName("Online player receives reward immediately via grant()")
        @Test
        void onlinePlayer_rewardGrantedImmediately() {
            PlayerMock player = server.addPlayer();
            UUID playerUUID = player.getUniqueId();

            var reward = new TrackingRewardType(100);
            Map<UUID, List<QuestRewardType>> resolved = Map.of(playerUUID, List.of(reward));

            granter.grant(resolved, QUEST_KEY);

            assertEquals(1, TrackingRewardType.GRANTED_TO.size());
            assertEquals(playerUUID, TrackingRewardType.GRANTED_TO.get(0));
        }

        @DisplayName("Multiple rewards for one player are all granted")
        @Test
        void multipleRewards_allGranted() {
            PlayerMock player = server.addPlayer();
            UUID playerUUID = player.getUniqueId();

            var r1 = new TrackingRewardType(100);
            var r2 = new TrackingRewardType(200);
            var r3 = new TrackingRewardType(300);
            Map<UUID, List<QuestRewardType>> resolved = Map.of(playerUUID, List.of(r1, r2, r3));

            granter.grant(resolved, QUEST_KEY);

            assertEquals(3, TrackingRewardType.GRANTED_TO.size());
            assertTrue(TrackingRewardType.GRANTED_TO.stream().allMatch(uuid -> uuid.equals(playerUUID)));
        }

        @DisplayName("Multiple online players each receive their own rewards")
        @Test
        void multipleOnlinePlayers_eachReceivesOwn() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();

            var reward = new TrackingRewardType(500);
            Map<UUID, List<QuestRewardType>> resolved = Map.of(
                    player1.getUniqueId(), List.of(reward),
                    player2.getUniqueId(), List.of(reward)
            );

            granter.grant(resolved, QUEST_KEY);

            assertEquals(2, TrackingRewardType.GRANTED_TO.size());
            assertTrue(TrackingRewardType.GRANTED_TO.contains(player1.getUniqueId()));
            assertTrue(TrackingRewardType.GRANTED_TO.contains(player2.getUniqueId()));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @DisplayName("Empty rewards map results in no-op")
        @Test
        void emptyRewardsMap_noOp() {
            Map<UUID, List<QuestRewardType>> resolved = Map.of();

            granter.grant(resolved, QUEST_KEY);

            assertTrue(TrackingRewardType.GRANTED_TO.isEmpty());
        }

        @DisplayName("Player with empty reward list results in no grant call")
        @Test
        void playerWithEmptyRewardList_noGrant() {
            PlayerMock player = server.addPlayer();

            Map<UUID, List<QuestRewardType>> resolved = Map.of(player.getUniqueId(), List.of());

            granter.grant(resolved, QUEST_KEY);

            assertTrue(TrackingRewardType.GRANTED_TO.isEmpty());
        }
    }

    @Nested
    @DisplayName("Offline player path")
    class OfflinePlayerPath {

        private void registerMockDatabaseManager() {
            FileManager fileManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE);
            YamlDocument mainConfig = mock(YamlDocument.class);
            when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
            when(mainConfig.getInt(MainConfigFile.QUEST_PENDING_REWARDS_EXPIRY_DAYS, 30)).thenReturn(30);

            Database mockDatabase = mock(Database.class);
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);

            McRPGDatabaseManager mockDbManager = mock(McRPGDatabaseManager.class);
            when(mockDbManager.getDatabase()).thenReturn(mockDatabase);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);
        }

        @Test
        @DisplayName("Given an offline player UUID, when grant is called, then no immediate reward grant occurs")
        void offlinePlayer_noImmediateGrant() {
            registerMockDatabaseManager();
            UUID offlinePlayerUUID = UUID.randomUUID();

            var reward = new TrackingRewardType(100);
            Map<UUID, List<QuestRewardType>> resolved = Map.of(offlinePlayerUUID, List.of(reward));

            granter.grant(resolved, QUEST_KEY);

            assertTrue(TrackingRewardType.GRANTED_TO.isEmpty(),
                    "Offline player should not receive an immediate grant");
        }

        @Test
        @DisplayName("Given a mix of online and offline players, when grant is called, then only the online player receives immediate grants")
        void mixedOnlineOffline_onlyOnlineReceivesImmediateGrant() {
            registerMockDatabaseManager();
            PlayerMock onlinePlayer = server.addPlayer();
            UUID offlinePlayerUUID = UUID.randomUUID();

            var reward = new TrackingRewardType(100);
            Map<UUID, List<QuestRewardType>> resolved = Map.of(
                    onlinePlayer.getUniqueId(), List.of(reward),
                    offlinePlayerUUID, List.of(reward)
            );

            granter.grant(resolved, QUEST_KEY);

            assertEquals(1, TrackingRewardType.GRANTED_TO.size());
            assertEquals(onlinePlayer.getUniqueId(), TrackingRewardType.GRANTED_TO.get(0));
        }
    }
}
