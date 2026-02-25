package us.eunoians.mcrpg.listener.lands;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import me.angeschossen.lands.api.events.LandDeleteEvent;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.impl.scope.impl.LandQuestScope;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LandDeleteListenerTest extends McRPGBaseTest {

    private QuestBoardManager mockBoardManager;
    private LandDeleteListener listener;

    @BeforeEach
    void setUp() {
        mockBoardManager = mock(QuestBoardManager.class);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockBoardManager);
        listener = new LandDeleteListener();
    }

    @DisplayName("LandDeleteEvent triggers handleScopeEntityRemoval with LAND_SCOPE_KEY and land name")
    @Test
    void onLandDelete_callsHandleScopeEntityRemoval_withCorrectArgs() {
        Land mockLand = mock(Land.class);
        when(mockLand.getName()).thenReturn("test_kingdom");

        LandDeleteEvent event = mock(LandDeleteEvent.class);
        when(event.getLand()).thenReturn(mockLand);

        listener.onLandDelete(event);

        verify(mockBoardManager).handleScopeEntityRemoval(
                eq(LandQuestScope.LAND_SCOPE_KEY),
                eq("test_kingdom"));
    }

    @DisplayName("LandDeleteEvent extracts correct land name from event")
    @Test
    void onLandDelete_extractsLandNameFromEvent() {
        Land mockLand = mock(Land.class);
        when(mockLand.getName()).thenReturn("unique_land_name_12345");

        LandDeleteEvent event = mock(LandDeleteEvent.class);
        when(event.getLand()).thenReturn(mockLand);

        listener.onLandDelete(event);

        verify(mockBoardManager).handleScopeEntityRemoval(
                eq(LandQuestScope.LAND_SCOPE_KEY),
                eq("unique_land_name_12345"));
    }

    @DisplayName("LAND_SCOPE_KEY constant has expected namespace and key")
    @Test
    void landScopeKey_hasExpectedValue() {
        NamespacedKey key = LandQuestScope.LAND_SCOPE_KEY;
        assertNotNull(key);
        assertEquals("mcrpg", key.getNamespace());
        assertEquals("land_scope", key.getKey());
    }

    @DisplayName("Multiple land deletions each trigger separate handleScopeEntityRemoval calls")
    @Test
    void multipleDeletions_eachTriggersHandleRemoval() {
        Land land1 = mock(Land.class);
        when(land1.getName()).thenReturn("land_one");
        Land land2 = mock(Land.class);
        when(land2.getName()).thenReturn("land_two");

        LandDeleteEvent event1 = mock(LandDeleteEvent.class);
        when(event1.getLand()).thenReturn(land1);
        LandDeleteEvent event2 = mock(LandDeleteEvent.class);
        when(event2.getLand()).thenReturn(land2);

        listener.onLandDelete(event1);
        listener.onLandDelete(event2);

        verify(mockBoardManager).handleScopeEntityRemoval(
                eq(LandQuestScope.LAND_SCOPE_KEY), eq("land_one"));
        verify(mockBoardManager).handleScopeEntityRemoval(
                eq(LandQuestScope.LAND_SCOPE_KEY), eq("land_two"));
    }
}
