package us.eunoians.mcrpg.combat.condition;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatTrackerManager;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CombatConditionTask")
class CombatConditionTaskTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private CombatCondition condition;
    private NamespacedKey conditionKey;
    private CombatConditionTask task;

    @BeforeEach
    void setUp() {
        manager = mock(CombatTrackerManager.class);
        conditionKey = new NamespacedKey("mcrpg", "test_condition");
        condition = mock(CombatCondition.class);
        when(condition.getKey()).thenReturn(conditionKey);
        when(condition.getCheckIntervalSeconds()).thenReturn(1.0);
        task = new CombatConditionTask(mcRPG, manager, condition);
    }

    @Test
    @DisplayName("reports condition activity when in combat with no implied participants")
    void reportsConditionActivity_whenNoImpliedParticipants() {
        PlayerMock player = server.addPlayer();
        when(condition.isInCombat(player)).thenReturn(true);
        when(condition.getImpliedParticipants(player)).thenReturn(Set.of());

        task.onIntervalComplete();

        verify(manager).reportConditionActivity(player.getUniqueId(), conditionKey);
        verify(manager, never()).reportCombatActivity(any(), any());
    }

    @Test
    @DisplayName("reports combat activity for each implied participant when in combat")
    void reportsCombatActivity_whenImpliedParticipants() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();
        when(condition.isInCombat(player)).thenReturn(true);
        when(condition.getImpliedParticipants(player)).thenReturn(Set.of(mobUUID));

        task.onIntervalComplete();

        verify(manager).reportCombatActivity(player.getUniqueId(), mobUUID);
        verify(manager, never()).reportConditionActivity(any(), any());
    }

    @Test
    @DisplayName("reports nothing when the player is not in combat")
    void reportsNothing_whenNotInCombat() {
        PlayerMock player = server.addPlayer();
        when(condition.isInCombat(player)).thenReturn(false);

        task.onIntervalComplete();

        verify(manager, never()).reportConditionActivity(any(), any());
        verify(manager, never()).reportCombatActivity(any(), any());
    }

    @Test
    @DisplayName("a throwing condition is caught and does not report or propagate")
    void doesNotThrowOrReport_whenConditionThrows() {
        server.addPlayer();
        when(condition.isInCombat(any(LivingEntity.class))).thenThrow(new RuntimeException("condition failure"));

        assertDoesNotThrow(() -> task.onIntervalComplete());

        verify(manager, never()).reportConditionActivity(any(), eq(conditionKey));
        verify(manager, never()).reportCombatActivity(any(), any());
    }
}
