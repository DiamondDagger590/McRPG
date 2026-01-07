package us.eunoians.mcrpg.ability;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponent;
import us.eunoians.mcrpg.ability.impl.MockAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class BaseAbilityTest extends McRPGBaseTest {

    private MockAbility mockAbility;

    @BeforeEach
    public void setup() {
        mockAbility = new MockAbility(mcRPG);
    }

    @Test
    @DisplayName("Given no cancelling components, when checking if component cancels, then returns empty Optional")
    public void checkIfComponentCancels_returnsEmpty_whenNoCancellingComponents(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        BlockBreakEvent event = mock(BlockBreakEvent.class);

        Optional<EventCancellingComponent> result = mockAbility.checkIfComponentCancels(abilityHolder, event);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given a cancelling component that returns true, when checking if component cancels, then returns the component")
    public void checkIfComponentCancels_returnsComponent_whenShouldCancelReturnsTrue(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        BlockBreakEvent event = mock(BlockBreakEvent.class);

        EventCancellingComponent cancellingComponent = mock(EventCancellingComponent.class);
        when(cancellingComponent.shouldCancel(any(), any())).thenReturn(true);
        mockAbility.addCancellingComponent(cancellingComponent, BlockBreakEvent.class, 1);

        Optional<EventCancellingComponent> result = mockAbility.checkIfComponentCancels(abilityHolder, event);

        assertTrue(result.isPresent());
        assertEquals(cancellingComponent, result.get());
    }

    @Test
    @DisplayName("Given a cancelling component that returns false, when checking if component cancels, then returns empty Optional")
    public void checkIfComponentCancels_returnsEmpty_whenShouldCancelReturnsFalse(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        BlockBreakEvent event = mock(BlockBreakEvent.class);

        EventCancellingComponent cancellingComponent = mock(EventCancellingComponent.class);
        when(cancellingComponent.shouldCancel(any(), any())).thenReturn(false);
        mockAbility.addCancellingComponent(cancellingComponent, BlockBreakEvent.class, 1);

        Optional<EventCancellingComponent> result = mockAbility.checkIfComponentCancels(abilityHolder, event);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given a cancelling component for a different event class, when checking if component cancels, then returns empty Optional")
    public void checkIfComponentCancels_returnsEmpty_whenEventClassDoesNotMatch(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        BlockBreakEvent event = mock(BlockBreakEvent.class);

        EventCancellingComponent cancellingComponent = mock(EventCancellingComponent.class);
        when(cancellingComponent.shouldCancel(any(), any())).thenReturn(true);
        // Register for PlayerInteractEvent, not BlockBreakEvent
        mockAbility.addCancellingComponent(cancellingComponent, PlayerInteractEvent.class, 1);

        Optional<EventCancellingComponent> result = mockAbility.checkIfComponentCancels(abilityHolder, event);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given multiple cancelling components with different priorities, when checking if component cancels, then returns first matching component by priority")
    public void checkIfComponentCancels_returnsFirstMatchingByPriority(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        BlockBreakEvent event = mock(BlockBreakEvent.class);

        EventCancellingComponent lowPriorityComponent = mock(EventCancellingComponent.class);
        when(lowPriorityComponent.shouldCancel(any(), any())).thenReturn(true);

        EventCancellingComponent highPriorityComponent = mock(EventCancellingComponent.class);
        when(highPriorityComponent.shouldCancel(any(), any())).thenReturn(true);

        // Add high priority (10) first, then low priority (1)
        mockAbility.addCancellingComponent(highPriorityComponent, BlockBreakEvent.class, 10);
        mockAbility.addCancellingComponent(lowPriorityComponent, BlockBreakEvent.class, 1);

        Optional<EventCancellingComponent> result = mockAbility.checkIfComponentCancels(abilityHolder, event);

        assertTrue(result.isPresent());
        // Low priority (1) should be processed first
        assertEquals(lowPriorityComponent, result.get());
    }

    @Test
    @DisplayName("Given first component returns false and second returns true, when checking if component cancels, then returns second component")
    public void checkIfComponentCancels_returnsSecondComponent_whenFirstReturnsFalse(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        BlockBreakEvent event = mock(BlockBreakEvent.class);

        EventCancellingComponent firstComponent = mock(EventCancellingComponent.class);
        when(firstComponent.shouldCancel(any(), any())).thenReturn(false);

        EventCancellingComponent secondComponent = mock(EventCancellingComponent.class);
        when(secondComponent.shouldCancel(any(), any())).thenReturn(true);

        mockAbility.addCancellingComponent(firstComponent, BlockBreakEvent.class, 1);
        mockAbility.addCancellingComponent(secondComponent, BlockBreakEvent.class, 2);

        Optional<EventCancellingComponent> result = mockAbility.checkIfComponentCancels(abilityHolder, event);

        assertTrue(result.isPresent());
        assertEquals(secondComponent, result.get());
    }
}
