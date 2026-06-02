package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.chain.CascadeOrchestrator;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.FirstJoinChainAutoStartTrigger;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.ManualChainAutoStartTrigger;
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the bypass-permission check added to {@link QuestChainFirstJoinListener}.
 * Verifies that {@code mcrpg.tutorial.bypass} prevents tutorial chains from
 * auto-starting on first join, while non-tutorial chains are unaffected.
 */
public class QuestChainFirstJoinListenerBypassTest extends McRPGBaseTest {

    private static final String BYPASS_PERMISSION = "mcrpg.tutorial.bypass";

    private CascadeOrchestrator mockCascadeOrchestrator;
    private QuestChainRegistry chainRegistry;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        mockCascadeOrchestrator = mock(CascadeOrchestrator.class);
        chainRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);
        server.getPluginManager().registerEvents(
                new QuestChainFirstJoinListener(mockCascadeOrchestrator), mcRPG);
    }

    @AfterEach
    public void tearDown() {
        chainRegistry.clear();
        HandlerList.unregisterAll(mcRPG);
    }

    @Test
    @DisplayName("Given player has bypass permission and tutorial-sourced chain, when PlayerLoadEvent fires, then tryStartChain is not called")
    public void onPlayerLoad_bypassPermission_preventsTutorialChainStart() {
        var chainKey = TutorialQuestSource.TUTORIAL_CHAIN_KEY;
        QuestChainDefinition tutorialChain = new QuestChainDefinition.Builder(
                chainKey,
                TutorialQuestSource.KEY,
                FirstJoinChainAutoStartTrigger.KEY,
                List.of(QuestChainStep.simple(new org.bukkit.NamespacedKey("test", "step1")))
        ).build();
        chainRegistry.register(tutorialChain);

        PlayerMock playerMock = server.addPlayer();
        playerMock.addAttachment(mcRPG, BYPASS_PERMISSION, true);
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockCascadeOrchestrator, never()).tryStartChain(eq(playerMock), eq(chainKey));
    }

    @Test
    @DisplayName("Given player has bypass permission and non-tutorial chain, when PlayerLoadEvent fires, then tryStartChain IS called")
    public void onPlayerLoad_bypassPermission_doesNotAffectNonTutorialChain() {
        var chainKey = new org.bukkit.NamespacedKey("test", "non_tutorial_chain");
        var nonTutorialSourceKey = new org.bukkit.NamespacedKey("mcrpg", "manual");
        QuestChainDefinition nonTutorialChain = new QuestChainDefinition.Builder(
                chainKey,
                nonTutorialSourceKey,
                FirstJoinChainAutoStartTrigger.KEY,
                List.of(QuestChainStep.simple(new org.bukkit.NamespacedKey("test", "step1")))
        ).build();
        chainRegistry.register(nonTutorialChain);

        PlayerMock playerMock = server.addPlayer();
        playerMock.addAttachment(mcRPG, BYPASS_PERMISSION, true);
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockCascadeOrchestrator).tryStartChain(eq(playerMock), eq(chainKey));
    }

    @Test
    @DisplayName("Given player has no bypass permission and tutorial-sourced chain, when PlayerLoadEvent fires, then tryStartChain IS called")
    public void onPlayerLoad_noBypassPermission_tutorialChainStarts() {
        var chainKey = TutorialQuestSource.TUTORIAL_CHAIN_KEY;
        QuestChainDefinition tutorialChain = new QuestChainDefinition.Builder(
                chainKey,
                TutorialQuestSource.KEY,
                FirstJoinChainAutoStartTrigger.KEY,
                List.of(QuestChainStep.simple(new org.bukkit.NamespacedKey("test", "step1")))
        ).build();
        chainRegistry.register(tutorialChain);

        PlayerMock playerMock = server.addPlayer();
        // No bypass permission added
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockCascadeOrchestrator).tryStartChain(eq(playerMock), eq(chainKey));
    }
}
