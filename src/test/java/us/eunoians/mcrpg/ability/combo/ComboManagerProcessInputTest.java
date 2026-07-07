package us.eunoians.mcrpg.ability.combo;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link ComboManager#processInput(org.bukkit.entity.Player, ComboInput)},
 * {@link ComboManager#resetState(java.util.UUID)}, and timeout scheduling behavior.
 */
@ExtendWith(McRPGPlayerExtension.class)
class ComboManagerProcessInputTest extends McRPGBaseTest {

    private ComboManager comboManager;
    private DisplayManager displayManager;
    private McRPGPlayer mcRPGPlayer;
    private PlayerMock playerMock;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.playerMock = addPlayerToServer(mcRPGPlayer);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getStringList(MainConfigFile.COMBO_ALLOWED_ITEMS)).thenReturn(List.of("DIAMOND_SWORD"));

        displayManager = mock(DisplayManager.class);
        ActionBarHudDisplay hudDisplay = mock(ActionBarHudDisplay.class);
        lenient().when(displayManager.getOrCreateActionBarHud(any(McRPGPlayer.class))).thenReturn(hudDisplay);
        lenient().when(displayManager.getDisplay(any(McRPGPlayer.class), eq(ActionBarHudDisplay.class)))
                .thenReturn(Optional.of(hudDisplay));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(displayManager);

        comboManager = new ComboManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(comboManager);
    }

    @Nested
    @DisplayName("processInput")
    class ProcessInput {

        @Test
        @DisplayName("LEFT input with empty combo is a no-op")
        void processInput_leftWhenEmpty_noOp() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.LEFT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty());
        }

        @Test
        @DisplayName("RIGHT input starts a combo sequence")
        void processInput_rightInput_startsCombo() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertEquals(1, mcRPGPlayer.getComboState().getSequenceLength());
            assertEquals(List.of(ComboInput.RIGHT), mcRPGPlayer.getComboState().getCurrentSequence());
        }

        @Test
        @DisplayName("Non-allowed item skips processing")
        void processInput_nonAllowedItem_noOp() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.STONE));

            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty());
        }

        @Test
        @DisplayName("Empty hand (AIR) is always allowed")
        void processInput_emptyHand_allowed() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertEquals(1, mcRPGPlayer.getComboState().getSequenceLength());
        }

        @Test
        @DisplayName("RRR completes SLOT_1 and fires ComboCompleteEvent")
        void processInput_rrrPattern_firesComboCompleteEvent() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            AtomicInteger firedSlot = new AtomicInteger(-1);
            server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @org.bukkit.event.EventHandler
                public void onCombo(ComboCompleteEvent event) {
                    firedSlot.set(event.getSlotIndex());
                }
            }, mcRPG);

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty(),
                    "State should be cleared after combo completes");
            assertThat(server.getPluginManager(), hasFiredEventInstance(ComboCompleteEvent.class));
            assertEquals(1, firedSlot.get(), "RRR should complete SLOT_1 (index 1)");
        }

        @Test
        @DisplayName("RRL completes SLOT_2 and resets state")
        void processInput_rrlPattern_completesSlot2() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            AtomicInteger firedSlot = new AtomicInteger(-1);
            server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @org.bukkit.event.EventHandler
                public void onCombo(ComboCompleteEvent event) {
                    firedSlot.set(event.getSlotIndex());
                }
            }, mcRPG);

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.LEFT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty(),
                    "State should be cleared after combo completes");
            assertThat(server.getPluginManager(), hasFiredEventInstance(ComboCompleteEvent.class));
            assertEquals(2, firedSlot.get(), "RRL should complete SLOT_2 (index 2)");
        }

        @Test
        @DisplayName("RLR completes SLOT_3 and resets state")
        void processInput_rlrPattern_completesSlot3() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            AtomicInteger firedSlot = new AtomicInteger(-1);
            server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @org.bukkit.event.EventHandler
                public void onCombo(ComboCompleteEvent event) {
                    firedSlot.set(event.getSlotIndex());
                }
            }, mcRPG);

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.LEFT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty(),
                    "State should be cleared after combo completes");
            assertThat(server.getPluginManager(), hasFiredEventInstance(ComboCompleteEvent.class));
            assertEquals(3, firedSlot.get(), "RLR should complete SLOT_3 (index 3)");
        }

        @Test
        @DisplayName("Dead-end sequence resets state")
        void processInput_deadEnd_resetsState() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.LEFT);
            comboManager.processInput(playerMock, ComboInput.LEFT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty(),
                    "Dead-end sequence (RLL) should reset state");
        }

        @Test
        @DisplayName("Unregistered player is a no-op")
        void processInput_unregisteredPlayer_noOp() {
            PlayerMock unregisteredPlayer = server.addPlayer();
            unregisteredPlayer.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(unregisteredPlayer, ComboInput.RIGHT);

            // No exception thrown; system silently ignores unregistered players
        }

        @Test
        @DisplayName("Second RIGHT after initial RIGHT continues combo at length 2")
        void processInput_twoRightInputs_sequenceLengthTwo() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertEquals(2, mcRPGPlayer.getComboState().getSequenceLength());
        }

        @Test
        @DisplayName("LEFT input after RIGHT continues the combo")
        void processInput_leftAfterRight_continuesCombo() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.LEFT);

            assertEquals(2, mcRPGPlayer.getComboState().getSequenceLength());
            assertEquals(List.of(ComboInput.RIGHT, ComboInput.LEFT),
                    mcRPGPlayer.getComboState().getCurrentSequence());
        }
    }

    @Nested
    @DisplayName("resetState")
    class ResetState {

        @Test
        @DisplayName("resetState(UUID) clears combo sequence")
        void resetState_byUuid_clearsSequence() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            assertEquals(1, mcRPGPlayer.getComboState().getSequenceLength());

            comboManager.resetState(mcRPGPlayer.getUUID());

            assertTrue(mcRPGPlayer.getComboState().isEmpty());
        }

        @Test
        @DisplayName("resetState(McRPGPlayer) clears combo sequence")
        void resetState_byPlayer_clearsSequence() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);

            comboManager.resetState(mcRPGPlayer);

            assertTrue(mcRPGPlayer.getComboState().isEmpty());
        }

        @Test
        @DisplayName("resetState on empty state is no-op")
        void resetState_emptyState_noOp() {
            comboManager.resetState(mcRPGPlayer);

            assertTrue(mcRPGPlayer.getComboState().isEmpty());
            assertEquals(-1, mcRPGPlayer.getComboState().getTimeoutTaskId());
        }

        @Test
        @DisplayName("resetState resets timeout task ID to -1")
        void resetState_resetsTimeoutTaskId() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            assertFalse(mcRPGPlayer.getComboState().getTimeoutTaskId() == -1,
                    "Timeout should be scheduled after input");

            comboManager.resetState(mcRPGPlayer);

            assertEquals(-1, mcRPGPlayer.getComboState().getTimeoutTaskId());
        }
    }

    @Nested
    @DisplayName("timeout")
    class Timeout {

        @Test
        @DisplayName("Timeout is scheduled after first input")
        void processInput_firstInput_schedulesTimeout() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertTrue(mcRPGPlayer.getComboState().getTimeoutTaskId() != -1,
                    "Timeout task should be scheduled");
        }

        @Test
        @DisplayName("Each input refreshes the timeout task ID")
        void processInput_multipleInputs_refreshesTimeout() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            int firstTaskId = mcRPGPlayer.getComboState().getTimeoutTaskId();

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            int secondTaskId = mcRPGPlayer.getComboState().getTimeoutTaskId();

            assertTrue(firstTaskId != -1);
            assertTrue(secondTaskId != -1);
            assertFalse(firstTaskId == secondTaskId,
                    "Timeout should be refreshed with a new task on each input");
        }

        @Test
        @DisplayName("Timeout expiration resets combo state")
        void timeout_afterExpiry_resetsState() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            assertEquals(1, mcRPGPlayer.getComboState().getSequenceLength());

            server.getScheduler().performTicks(15L);

            assertTrue(mcRPGPlayer.getComboState().isEmpty(),
                    "Combo state should be reset after timeout expires");
        }

        @Test
        @DisplayName("Combo completed before timeout does not cause double-reset")
        void timeout_completedBeforeExpiry_noDoubleReset() {
            playerMock.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);
            comboManager.processInput(playerMock, ComboInput.RIGHT);

            assertTrue(mcRPGPlayer.getComboState().isEmpty());

            server.getScheduler().performTicks(15L);

            assertTrue(mcRPGPlayer.getComboState().isEmpty(),
                    "Already-reset state should remain empty after timeout fires");
        }
    }
}
