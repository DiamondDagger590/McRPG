package us.eunoians.mcrpg.quest.reward;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestRewardGrantEvent;
import us.eunoians.mcrpg.event.quest.QuestRewardGrantedEvent;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link QuestRewardGranter} fires the interception events and honors listener cancellation
 * and reward-list mutation across the shared grant path used by all three grant sites.
 */
public class QuestRewardGranterTest extends McRPGBaseTest {

    private NamespacedKey key(@NotNull String name) {
        return new NamespacedKey(McRPGMethods.getMcRPGNamespace(), name);
    }

    /**
     * The MockBukkit server is shared across tests, so registered listeners persist on the events'
     * static handler lists. Clear this suite's two event types after each test to prevent a listener
     * from one test (e.g. the cancelling one) leaking into the next.
     */
    @AfterEach
    void clearRewardEventListeners() {
        QuestRewardGrantEvent.getHandlerList().unregisterAll();
        QuestRewardGrantedEvent.getHandlerList().unregisterAll();
    }

    @Test
    @DisplayName("grants all rewards and fires granted event when uncancelled")
    void grantToOnlinePlayer_grantsAll_whenNotCancelled() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        RecordingReward reward = new RecordingReward(key("good"));
        CapturingListener listener = new CapturingListener();
        server.getPluginManager().registerEvents(listener, McRPG.getInstance());

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, List.of(reward), key("quest"),
                null, RewardGrantContext.INLINE);

        assertTrue(reward.wasGranted(), "reward should be granted");
        assertEquals(1, granted.size(), "granted list should contain the one reward");
        assertNotNull(listener.grantedEvent, "post-grant event should fire");
        assertEquals(1, listener.grantedEvent.getGrantedRewards().size());
    }

    @Test
    @DisplayName("cancelled event grants nothing and returns empty")
    void grantToOnlinePlayer_grantsNothing_whenCancelled() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        RecordingReward reward = new RecordingReward(key("good"));
        server.getPluginManager().registerEvents(new CancellingListener(), McRPG.getInstance());

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, List.of(reward), key("quest"),
                null, RewardGrantContext.INLINE);

        assertFalse(reward.wasGranted(), "cancelled batch must not grant");
        assertTrue(granted.isEmpty(), "cancelled batch returns empty granted list");
    }

    @Test
    @DisplayName("listener mutation of the reward list is honored")
    void grantToOnlinePlayer_honorsMutation() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        RecordingReward original = new RecordingReward(key("original"));
        RecordingReward added = new RecordingReward(key("added"));
        server.getPluginManager().registerEvents(new MutatingListener(added), McRPG.getInstance());

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, List.of(original), key("quest"),
                null, RewardGrantContext.INLINE);

        assertFalse(original.wasGranted(), "listener removed the original reward");
        assertTrue(added.wasGranted(), "listener-added reward should be granted");
        assertEquals(1, granted.size());
    }

    @Test
    @DisplayName("a null reward inserted by a listener is skipped; real rewards still grant")
    void grantToOnlinePlayer_skipsNullReward() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        RecordingReward good = new RecordingReward(key("good"));
        server.getPluginManager().registerEvents(new NullInsertingListener(), McRPG.getInstance());

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, List.of(good), key("quest"),
                null, RewardGrantContext.INLINE);

        assertTrue(good.wasGranted(), "the real reward must still grant despite a null in the batch");
        assertEquals(List.of(good), granted, "the null must not appear in the granted list");
    }

    @Test
    @DisplayName("a throwing reward is isolated; other rewards still grant")
    void grantToOnlinePlayer_isolatesThrowingReward() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        ThrowingReward bad = new ThrowingReward(key("bad"));
        RecordingReward good = new RecordingReward(key("good"));

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, List.of(bad, good), key("quest"),
                null, RewardGrantContext.INLINE);

        assertTrue(good.wasGranted(), "the reward after the throwing one must still grant");
        assertEquals(List.of(good), granted, "the thrown reward must be absent from the granted list");
    }

    @Test
    @DisplayName("empty reward batch returns empty and fires no events")
    void grantToOnlinePlayer_emptyBatch_firesNoEvents() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        CapturingListener listener = new CapturingListener();
        server.getPluginManager().registerEvents(listener, McRPG.getInstance());

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, List.of(), key("quest"),
                null, RewardGrantContext.INLINE);

        assertTrue(granted.isEmpty());
        assertFalse(listener.grantSeen, "no pre-grant event should fire for an empty batch");
        assertNull(listener.grantedEvent, "no post-grant event should fire for an empty batch");
    }

    @Test
    @DisplayName("batch where every reward throws fires the pre-event but no granted event")
    void grantToOnlinePlayer_allThrow_firesNoGrantedEvent() {
        Player player = server.addPlayer();
        QuestRewardGranter granter = new QuestRewardGranter(McRPG.getInstance());
        CapturingListener listener = new CapturingListener();
        server.getPluginManager().registerEvents(listener, McRPG.getInstance());

        List<QuestRewardType> granted = granter.grantToOnlinePlayer(player,
                List.of(new ThrowingReward(key("bad1")), new ThrowingReward(key("bad2"))),
                key("quest"), null, RewardGrantContext.INLINE);

        assertTrue(granted.isEmpty());
        assertTrue(listener.grantSeen, "the cancellable pre-grant event still fires for a non-empty batch");
        assertNull(listener.grantedEvent, "no granted event fires when nothing was granted");
    }

    /** Captures both the pre- and post-grant events. */
    private static class CapturingListener implements Listener {
        private boolean grantSeen;
        private QuestRewardGrantedEvent grantedEvent;

        @EventHandler
        public void onGrant(@NotNull QuestRewardGrantEvent event) {
            this.grantSeen = true;
        }

        @EventHandler
        public void onGranted(@NotNull QuestRewardGrantedEvent event) {
            this.grantedEvent = event;
        }
    }

    /** Reward whose grant always throws. */
    private static class ThrowingReward extends RecordingReward {
        ThrowingReward(@NotNull NamespacedKey key) {
            super(key);
        }

        @Override
        public void grant(@NotNull Player player) {
            throw new IllegalStateException("boom");
        }
    }

    /** Cancels every grant event. */
    private static class CancellingListener implements Listener {
        @EventHandler
        public void onGrant(@NotNull QuestRewardGrantEvent event) {
            event.setCancelled(true);
        }
    }

    /** Inserts a null into the reward batch to simulate a misbehaving listener. */
    private static class NullInsertingListener implements Listener {
        @EventHandler
        public void onGrant(@NotNull QuestRewardGrantEvent event) {
            event.getRewards().add(null);
        }
    }

    /** Replaces the reward batch with a single injected reward. */
    private static class MutatingListener implements Listener {
        private final QuestRewardType replacement;

        MutatingListener(@NotNull QuestRewardType replacement) {
            this.replacement = replacement;
        }

        @EventHandler
        public void onGrant(@NotNull QuestRewardGrantEvent event) {
            List<QuestRewardType> rewards = event.getRewards();
            rewards.clear();
            rewards.add(replacement);
        }
    }

    /** Reward that records whether its grant ran. */
    private static class RecordingReward implements QuestRewardType {
        private final NamespacedKey key;
        private boolean granted;

        RecordingReward(@NotNull NamespacedKey key) {
            this.key = key;
        }

        boolean wasGranted() {
            return granted;
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }

        @Override
        public @NotNull QuestRewardType parseConfig(@NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@NotNull Player player) {
            granted = true;
        }

        @Override
        public @NotNull Map<String, Object> serializeConfig() {
            return Map.of();
        }

        @Override
        public @NotNull QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
