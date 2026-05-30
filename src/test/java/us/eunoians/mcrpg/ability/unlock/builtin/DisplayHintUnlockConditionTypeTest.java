package us.eunoians.mcrpg.ability.unlock.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.UnlockConditionParseException;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplayHintUnlockConditionTypeTest extends McRPGBaseTest {

    @DisplayName("Given any holder, when isMet, then it always returns false")
    @Test
    public void isMet_alwaysReturnsFalse() {
        DisplayHintUnlockConditionType configured =
                new DisplayHintUnlockConditionType("<body>Buy from <primary>crates");
        assertFalse(configured.isMet(mock(SkillHolder.class)));
    }

    @DisplayName("Given the type, when calling isDisplayOnly, then it returns true")
    @Test
    public void isDisplayOnly_returnsTrue() {
        assertTrue(new DisplayHintUnlockConditionType().isDisplayOnly());
    }

    @DisplayName("Given a section with both locale-key and text, when parsing, then it throws")
    @Test
    public void parseConfig_throwsWhenBothFormsSet() {
        Section section = mock(Section.class);
        when(section.contains("locale-key")).thenReturn(true);
        when(section.contains("text")).thenReturn(true);
        assertThrows(UnlockConditionParseException.class,
                () -> new DisplayHintUnlockConditionType().parseConfig(section));
    }

    @DisplayName("Given a section with neither locale-key nor text, when parsing, then it throws")
    @Test
    public void parseConfig_throwsWhenNeitherFormSet() {
        Section section = mock(Section.class);
        when(section.contains("locale-key")).thenReturn(false);
        when(section.contains("text")).thenReturn(false);
        assertThrows(UnlockConditionParseException.class,
                () -> new DisplayHintUnlockConditionType().parseConfig(section));
    }

    @DisplayName("Given a section with only locale-key, when parsing, then a locale-key instance is returned")
    @Test
    public void parseConfig_parsesLocaleKey() {
        Section section = mock(Section.class);
        when(section.contains("locale-key")).thenReturn(true);
        when(section.contains("text")).thenReturn(false);
        when(section.getString("locale-key")).thenReturn("ability.unlock-condition.source.riptide-guardian");
        DisplayHintUnlockConditionType parsed = (DisplayHintUnlockConditionType)
                new DisplayHintUnlockConditionType().parseConfig(section);
        assertEquals(Route.fromString("ability.unlock-condition.source.riptide-guardian"), parsed.getLocaleKey());
    }

    @DisplayName("Given a section with only text, when parsing, then an inline instance is returned")
    @Test
    public void parseConfig_parsesInlineText() {
        Section section = mock(Section.class);
        when(section.contains("locale-key")).thenReturn(false);
        when(section.contains("text")).thenReturn(true);
        when(section.getString("text")).thenReturn("<body>Buy me");
        DisplayHintUnlockConditionType parsed = (DisplayHintUnlockConditionType)
                new DisplayHintUnlockConditionType().parseConfig(section);
        assertEquals("<body>Buy me", parsed.getInlineText());
    }

    @DisplayName("Given a section with blank text, when parsing, then it throws")
    @Test
    public void parseConfig_throwsOnBlankText() {
        Section section = mock(Section.class);
        when(section.contains("locale-key")).thenReturn(false);
        when(section.contains("text")).thenReturn(true);
        when(section.getString("text")).thenReturn("   ");
        assertThrows(UnlockConditionParseException.class,
                () -> new DisplayHintUnlockConditionType().parseConfig(section));
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        DisplayHintUnlockConditionType type = new DisplayHintUnlockConditionType();
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
