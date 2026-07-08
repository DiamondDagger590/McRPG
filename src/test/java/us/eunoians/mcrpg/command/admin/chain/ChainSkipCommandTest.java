package us.eunoians.mcrpg.command.admin.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.quest.chain.QuestChainRepeatMode;
import us.eunoians.mcrpg.quest.chain.QuestChainState;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ChainSkipCommand#skipChain} static method for error sentinel
 * handling. Integration-level skip behavior (reward granting, chain completion)
 * is verified by manual server testing.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class ChainSkipCommandTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey STEP_QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("mcrpg", "manual");
    private static final NamespacedKey TRIGGER_KEY = new NamespacedKey("mcrpg", "first_join");

    @Test
    @DisplayName("Given a player with no chain state, when skipChain is called, then SKIP_ERROR_NO_STATE is returned")
    public void skipChain_returnsErrorNoState_whenPlayerHasNoChainState(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestChainDefinition chain = buildChain();

        int result = ChainSkipCommand.skipChain(mcRPGPlayer, chain);

        assertEquals(ChainSkipCommand.SKIP_ERROR_NO_STATE, result,
                "Expected SKIP_ERROR_NO_STATE when player has no chain state");
    }

    @Test
    @DisplayName("Given a player with a COMPLETED chain state, when skipChain is called, then SKIP_ERROR_TERMINAL is returned")
    public void skipChain_returnsErrorTerminal_whenChainIsCompleted(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestChainDefinition chain = buildChain();
        setChainState(mcRPGPlayer, QuestChainState.COMPLETED);

        int result = ChainSkipCommand.skipChain(mcRPGPlayer, chain);

        assertEquals(ChainSkipCommand.SKIP_ERROR_TERMINAL, result,
                "Expected SKIP_ERROR_TERMINAL when chain is in COMPLETED state");
    }

    @Test
    @DisplayName("Given a player with a FAILED chain state, when skipChain is called, then SKIP_ERROR_TERMINAL is returned")
    public void skipChain_returnsErrorTerminal_whenChainIsFailed(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestChainDefinition chain = buildChain();
        setChainState(mcRPGPlayer, QuestChainState.FAILED);

        int result = ChainSkipCommand.skipChain(mcRPGPlayer, chain);

        assertEquals(ChainSkipCommand.SKIP_ERROR_TERMINAL, result,
                "Expected SKIP_ERROR_TERMINAL when chain is in FAILED state");
    }

    @Test
    @DisplayName("Given a player with an ABANDONED chain state, when skipChain is called, then SKIP_ERROR_TERMINAL is returned")
    public void skipChain_returnsErrorTerminal_whenChainIsAbandoned(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestChainDefinition chain = buildChain();
        setChainState(mcRPGPlayer, QuestChainState.ABANDONED);

        int result = ChainSkipCommand.skipChain(mcRPGPlayer, chain);

        assertEquals(ChainSkipCommand.SKIP_ERROR_TERMINAL, result,
                "Expected SKIP_ERROR_TERMINAL when chain is in ABANDONED state");
    }

    @Test
    @DisplayName("Given all terminal states, when isTerminal is checked, then only ACTIVE returns false")
    public void questChainState_isTerminal_returnsFalseOnlyForActive() {
        // Validate the isTerminal contract used by skipChain's guard
        assertEquals(false, QuestChainState.ACTIVE.isTerminal());
        assertEquals(true, QuestChainState.COMPLETED.isTerminal());
        assertEquals(true, QuestChainState.FAILED.isTerminal());
        assertEquals(true, QuestChainState.ABANDONED.isTerminal());
        assertEquals(true, QuestChainState.EXPIRED.isTerminal());
    }

    /**
     * Builds a minimal chain definition with a single step for testing.
     *
     * @return test chain definition
     */
    @NotNull
    private QuestChainDefinition buildChain() {
        return new QuestChainDefinition.Builder(
                CHAIN_KEY,
                SOURCE_KEY,
                TRIGGER_KEY,
                List.of(QuestChainStep.simple(STEP_QUEST_KEY))
        ).build();
    }

    /**
     * Injects a {@link QuestChainPlayerState} with the given state into the player's chain
     * data, simulating prior chain progression without requiring full chain infrastructure.
     *
     * @param mcRPGPlayer the player
     * @param state       the state to set on the chain
     */
    private void setChainState(@NotNull McRPGPlayer mcRPGPlayer, @NotNull QuestChainState state) {
        var chainState = new QuestChainPlayerState(CHAIN_KEY, null, state, 0, null);
        mcRPGPlayer.getChainData().putChainState(chainState);
    }
}
