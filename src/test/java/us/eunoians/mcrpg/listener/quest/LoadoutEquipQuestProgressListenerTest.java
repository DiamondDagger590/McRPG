package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent.ChangeReason;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadoutEquipQuestProgressListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;
    private McRPGPlayerManager mockPlayerManager;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getActiveQuestsForPlayer(any(UUID.class))).thenReturn(List.of());
        mockPlayerManager = mock(McRPGPlayerManager.class);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        server.getPluginManager().registerEvents(new LoadoutEquipQuestProgressListener(mockQuestManager), mcRPG);
    }

    @Test
    @DisplayName("Given a LoadoutAbilityChangeEvent with EQUIP reason on the active loadout, when fired, then progressQuests queries active quests for the player")
    public void onLoadoutChange_progressesQuests_whenReasonIsEquipOnActiveLoadout() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
        stubPlayerWithActiveSlot(playerUUID, 1);

        var event = new LoadoutAbilityChangeEvent(playerUUID, ChangeReason.EQUIP, null, abilityKey, 1);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager).getActiveQuestsForPlayer(playerUUID);
    }

    @Test
    @DisplayName("Given a LoadoutAbilityChangeEvent with UNEQUIP reason, when fired, then progressQuests is not invoked")
    public void onLoadoutChange_doesNotProgress_whenReasonIsUnequip() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");

        var event = new LoadoutAbilityChangeEvent(playerUUID, ChangeReason.UNEQUIP, abilityKey, null, 0);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager, never()).getActiveQuestsForPlayer(any(UUID.class));
    }

    @Test
    @DisplayName("Given a LoadoutAbilityChangeEvent with SWAP reason on the active loadout, when fired, then progressQuests queries active quests for the player")
    public void onLoadoutChange_progressesQuests_whenReasonIsSwapOnActiveLoadout() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey oldKey = new NamespacedKey("mcrpg", "bleed");
        NamespacedKey newKey = new NamespacedKey("mcrpg", "deeper_wound");
        stubPlayerWithActiveSlot(playerUUID, 2);

        var event = new LoadoutAbilityChangeEvent(playerUUID, ChangeReason.SWAP, oldKey, newKey, 2);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager).getActiveQuestsForPlayer(playerUUID);
    }

    @Test
    @DisplayName("Given a LoadoutAbilityChangeEvent with EQUIP reason on a non-active loadout, when fired, then progressQuests is not invoked")
    public void onLoadoutChange_doesNotProgress_whenEquipOnNonActiveLoadout() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
        stubPlayerWithActiveSlot(playerUUID, 1);

        var event = new LoadoutAbilityChangeEvent(playerUUID, ChangeReason.EQUIP, null, abilityKey, 3);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager, never()).getActiveQuestsForPlayer(any(UUID.class));
    }

    @Test
    @DisplayName("Given a LoadoutAbilityChangeEvent for an unloaded player, when fired, then progressQuests is not invoked")
    public void onLoadoutChange_doesNotProgress_whenPlayerNotLoaded() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.empty());

        var event = new LoadoutAbilityChangeEvent(playerUUID, ChangeReason.EQUIP, null, abilityKey, 1);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager, never()).getActiveQuestsForPlayer(any(UUID.class));
    }

    /**
     * Stubs the player manager to return a mock McRPGPlayer whose active loadout slot
     * matches the given value.
     *
     * @param playerUUID the UUID to register the player under
     * @param activeSlot the loadout slot to report as currently active
     */
    private void stubPlayerWithActiveSlot(UUID playerUUID, int activeSlot) {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        SkillHolder mockSkillHolder = mock(SkillHolder.class);
        when(mockPlayer.asSkillHolder()).thenReturn(mockSkillHolder);
        when(mockSkillHolder.getCurrentLoadoutSlot()).thenReturn(activeSlot);
        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.of(mockPlayer));
    }
}
