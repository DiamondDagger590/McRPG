package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbilityActivateObjectiveTypeTest extends McRPGBaseTest {

    private AbilityActivateObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new AbilityActivateObjectiveType();
    }

    @DisplayName("getKey returns ability_activate key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(AbilityActivateObjectiveType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("canProcess returns true for AbilityActivateQuestContext")
    @Test
    public void canProcess_correctContextType_returnsTrue() {
        AbilityActivateQuestContext context = mock(AbilityActivateQuestContext.class);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("canProcess returns false for other context types")
    @Test
    public void canProcess_wrongContextType_returnsFalse() {
        BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("processProgress with wrong context type returns 0")
    @Test
    public void processProgress_wrongContext_returnsZero() {
        BlockBreakQuestContext wrongContext = mock(BlockBreakQuestContext.class);
        QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
        assertEquals(0, type.processProgress(instance, wrongContext));
    }

    @DisplayName("processProgress with matching context returns 1")
    @Test
    public void processProgress_matchingAbility_returnsOne() {
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
        AbilityActivateEvent event = mock(AbilityActivateEvent.class);
        when(event.getAbility()).thenReturn(mockAbility);
        AbilityActivateQuestContext context = new AbilityActivateQuestContext(event);
        QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
        assertEquals(1, type.processProgress(instance, context));
    }

    @DisplayName("parseConfig with no filters creates EMPTY filter")
    @Test
    public void parseConfig_noFilters_createsEmptyFilter() {
        Section section = mock(Section.class);
        when(section.contains("ability")).thenReturn(false);
        when(section.contains("ability-type")).thenReturn(false);
        AbilityActivateObjectiveType configured = type.parseConfig(section);
        assertNotNull(configured);
        // processProgress should work the same — EMPTY matches any ability
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "any_ability"));
        AbilityActivateEvent event = mock(AbilityActivateEvent.class);
        when(event.getAbility()).thenReturn(mockAbility);
        QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
        assertEquals(1, configured.processProgress(instance, new AbilityActivateQuestContext(event)));
    }

    @DisplayName("parseConfig with specific ability key creates correct filter")
    @Test
    public void parseConfig_withAbilityKey_createsSpecificFilter() {
        Section section = mock(Section.class);
        when(section.contains("ability")).thenReturn(true);
        when(section.getString("ability")).thenReturn("mcrpg:bleed");
        when(section.contains("ability-type")).thenReturn(false);
        AbilityActivateObjectiveType configured = type.parseConfig(section);
        // Matching ability should return 1
        Ability matchingAbility = mock(Ability.class);
        when(matchingAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
        AbilityActivateEvent matchEvent = mock(AbilityActivateEvent.class);
        when(matchEvent.getAbility()).thenReturn(matchingAbility);
        QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
        assertEquals(1, configured.processProgress(instance, new AbilityActivateQuestContext(matchEvent)));
        // Non-matching ability should return 0
        Ability otherAbility = mock(Ability.class);
        when(otherAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "deeper_wound"));
        AbilityActivateEvent otherEvent = mock(AbilityActivateEvent.class);
        when(otherEvent.getAbility()).thenReturn(otherAbility);
        assertEquals(0, configured.processProgress(instance, new AbilityActivateQuestContext(otherEvent)));
    }
}
